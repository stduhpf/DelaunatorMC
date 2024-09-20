package net.stduhpf.delaunator.delaunator.shapes;

import java.util.ArrayList;

import net.minecraft.block.MapColor.Brightness;
import net.stduhpf.delaunator.delaunator.utils.Vector3;

public class Point3D implements Comparable<Point3D> {
    private static int instance_count = 0;
    
    public static void resetInstanceCount(){
        instance_count = 0;
    }

    private int instanceId = 0;
    public int getInstanceId() {
        return instanceId;
    }

    public double[] values = { 0., 0., 0. };
    public ArrayList<Tetrahedron3D> inTetra;
    public String name;
    public int colorId = 0;  
    public Brightness shade = Brightness.NORMAL; 

    public Point3D(double x, double y, double z, String name,int id, Brightness brightness) {
        this(x,y,z,name);
        colorId = id;
        this.shade = brightness;
    }

    public Point3D(double x, double y, double z, String name) {
        values[0] = x;
        values[1] = y;
        values[2] = z;
        inTetra = new ArrayList<Tetrahedron3D>();
        instanceId = instance_count++;
        this.name = name;
    }

    public int compareTo(Point3D that) {
        if (this.values[0] == that.values[0]) {
            if (this.values[1] == that.values[1]) {
                return (int) Math.signum(this.values[2] - that.values[2]);
            }
            return (int) Math.signum(this.values[1] - that.values[1]);
        }
        return (int) Math.signum(this.values[0] - that.values[0]);
    }

    public boolean isInSphere(Sphere s){
        return Vector3.length(Vector3.diff(values,s.center)) <= s.radius;
    }

}
