package net.stduhpf.delaunator.delaunator.shapes;

public class Sphere {
    public double[] center;
    public double radius;

    public Sphere(double[] center,double radius){
        assert center.length == 3;
        this.center = center;
        this.radius = radius;
    }
}
