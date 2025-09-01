package com.netralabs;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public class VeraPdfClient {

  private final HttpClient http = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(10))
      .build();
  private final String baseUrl;

  public VeraPdfClient(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String validateUa2(Path pdf) throws Exception {
    String boundary = "----verapdf" + System.currentTimeMillis();

    byte[] head = (
        "--" + boundary + "\r\n" +
            "Content-Disposition: form-data; name=\"file\"; filename=\"" + pdf.getFileName() + "\"\r\n" +
            "Content-Type: application/pdf\r\n\r\n"
    ).getBytes(StandardCharsets.UTF_8);

    byte[] body = Files.readAllBytes(pdf);
    byte[] tail = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);

    HttpRequest req = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/api/validate/ua2"))
        .timeout(Duration.ofMinutes(2))
        .header("Accept", "application/json")
        .header("Content-Type", "multipart/form-data; boundary=" + boundary)
        .POST(HttpRequest.BodyPublishers.ofByteArrays(List.of(head, body, tail)))
        .build();

    HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
    if (resp.statusCode() / 100 != 2) {
      throw new RuntimeException("veraPDF HTTP " + resp.statusCode() + ": " + resp.body());
    }
    return resp.body(); // JSON string with full assertions
  }


}
