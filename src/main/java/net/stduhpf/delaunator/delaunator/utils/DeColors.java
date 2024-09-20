package net.stduhpf.delaunator.delaunator.utils;

public class DeColors {
    private static double[][] _RGB2YCoCg = {{0.25, 0.5, 0.25},
                                            {0.5 , 0.0, -.5 },
                                            {-.25, 0.5, -.25}};
    public static Matrix3x3 RGB2YCoCg = new Matrix3x3(_RGB2YCoCg);
    private static double[][] _YCoCg2RGB = {{1., 1.,-1.},
                                            {1., 0., 1.},
                                            {1.,-1.,-1.}};
    public static Matrix3x3 YCoCg2RGB = new Matrix3x3(_YCoCg2RGB);
    static {
        RGB2YCoCg.forceSetInverse(YCoCg2RGB);
    }

    public static int[] unpackColors(int color) {
        int[] ret = { (color & 0xFF0000) >> 16, (color & 0xFF00) >> 8, (color & 0xFF) };
        return ret;
    }

    public static int[] unpackColorsGBR(int color) {
        int[] ret = { (color & 0xFF) , (color & 0xFF00) >> 8,(color & 0xFF0000) >> 16};
        return ret;
    }

    public static int packColors(int[] color) {
        assert color.length == 3;
        int ret = 0xFF << 24 | (color[0] & 0xFF) << 16 | (color[1] & 0xFF) << 8 | (color[2] & 0xFF);
        return ret;
    }

    public static double[] gammaInv(int[] color) {
        double p = 2.2;
        double ret[] = { Math.pow(color[0] / 255.0, p), Math.pow(color[1] / 255.0, p), Math.pow(color[2] / 255.0, p) };
        return ret;
    }
    public static double[] gammaInv(int[] color, int scale) {
        double p = 2.2;
        double ret[] = { Math.pow(((color[0] * scale) / 255) / 255.0, p),
                         Math.pow(((color[1] * scale) / 255) / 255.0, p),
                         Math.pow(((color[2] * scale) / 255) / 255.0, p) };
        return ret;
    }

    public static int[] gamma(double[] color) {
        double p = 1. / 2.2;
        int ret[] = { (int) (Math.pow(color[0], p) * 255.0), (int) (Math.pow(color[1], p) * 255.0),
                (int) (Math.pow(color[2], p) * 255.0) };
        return ret;
    }
}
