package com.netralabs.itextpdf;

import com.itextpdf.kernel.geom.Matrix;

public final class MatrixUtils {
  private MatrixUtils() {}

  public static Matrix identity() { return new Matrix(1, 0, 0, 1, 0, 0); }

  public static Matrix translate(float tx, float ty) { return new Matrix(1, 0, 0, 1, tx, ty); }

  public static Matrix multiply(Matrix a, Matrix b) {
    return new Matrix(
        a.get(0) * b.get(0) + a.get(2) * b.get(1),
        a.get(1) * b.get(0) + a.get(3) * b.get(1),
        a.get(0) * b.get(2) + a.get(2) * b.get(3),
        a.get(1) * b.get(2) + a.get(3) * b.get(3),
        a.get(0) * b.get(4) + a.get(2) * b.get(5) + a.get(4),
        a.get(1) * b.get(4) + a.get(3) * b.get(5) + a.get(5)
    );
  }

}
