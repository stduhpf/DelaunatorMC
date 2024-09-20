package net.stduhpf.delaunator.delaunator.utils;

public class Vector3 {
    
    public static double[] add(double[] a, double[] b) {
        assert a.length == 3 && b.length == 3;
        double[] ret = { a[0] + b[0], a[1] + b[1], a[2] + b[2] };
        return ret;
    }

    public static double[] diff(double[] a, double[] b) {
        assert a.length == 3 && b.length == 3;
        double[] ret = { a[0] - b[0], a[1] - b[1], a[2] - b[2] };
        return ret;
    }

    public static double[] scale(double[] a, double x) {
        assert a.length == 3;
        double[] ret = { a[0] * x, a[1] * x, a[2] * x };
        return ret;
    }

    public static double dot(double[] a, double[] b) {
        assert a.length == 3 && b.length == 3;
        return a[0] * b[0] + a[1] * b[1] + a[2] * b[2];
    }

    public static double length(double[] a) {
        assert a.length == 3;
        return Math.sqrt(Vector3.dot(a, a));
    }

    public static String str(double[] a){
        assert a.length == 3;
        return "[" + a[0] +", " + a[1] +", " + a[2] +"]";
    }
}
