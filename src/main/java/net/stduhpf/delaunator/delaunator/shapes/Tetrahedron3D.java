package net.stduhpf.delaunator.delaunator.shapes;

import net.stduhpf.delaunator.delaunator.utils.Matrix3x3;
import net.stduhpf.delaunator.delaunator.utils.Vector3;

public class Tetrahedron3D {

    public boolean isContainer = false;
    public Triangle3D[] triangles = { null, null, null, null };
    public Point3D[] vertices = { null, null, null, null };

    public String name;

    public Matrix3x3 mat;
    public double[] orig;

    public Sphere boundingSphere;

    public boolean isPlanar = false;

    public Tetrahedron3D(Point3D A, Triangle3D BCD){
        isContainer = false;
        Point3D B = BCD.vertices[0];
        Point3D C = BCD.vertices[1];
        Point3D D = BCD.vertices[2];
        
        vertices[0] = A;
        triangles[0] = BCD;
        vertices[1] = B;
        triangles[1] = Triangle3D.Get(A, C, D);
        vertices[2] = C;
        triangles[2] = Triangle3D.Get(A, B, D);
        vertices[3] = D;
        triangles[3] = Triangle3D.Get(A, B, C);

        for (int i = 0; i < 4; ++i) {
            triangles[i].inTetra.add(this);
             vertices[i].inTetra.add(this);
            //  System.out.println(Vector3.str(vertices[i].values));
        }

        name = "(" + A.name + " " + B.name + " " + C.name + " " + D.name + ")";

        // init linalg stuff
        orig = D.values;
        double[][] desc = { Vector3.diff(A.values, orig), 
                            Vector3.diff(B.values, orig),
                            Vector3.diff(C.values, orig) };
        mat = new Matrix3x3(desc);
        isPlanar = Math.abs(mat.det())<=1e-10;
        // if(isPlanar)System.out.println("Coplanar " + name);

        boundingSphere = getBoundingSphere();
        // System.out.println(name);
        // System.out.println(boundingSphere.radius);
        // System.out.println(Vector3.length(Vector3.diff(A.values, boundingSphere.center)));
        // System.out.println(Vector3.length(Vector3.diff(B.values, boundingSphere.center)));
        // System.out.println(Vector3.length(Vector3.diff(C.values, boundingSphere.center)));
        // System.out.println(Vector3.str(boundingSphere.center));
        // System.out.println("");

    }

    public Tetrahedron3D(Point3D A, Point3D B, Point3D C, Point3D D) {
        this(A,Triangle3D.Get(B, C, D));
    }

    public Sphere getBoundingSphere(){
        double[] k = Vector3.scale(mat.inner(),0.5);
        double[] g = mat.inverse().mulv(k);
        return new Sphere(Vector3.add(g, orig), Vector3.length(g));   
    }

    public double[] localCoords(double[] point){
        assert point.length == 3;
        double[] loc = Vector3.diff(point, orig);
        double[] c = mat.inverse().vmul(loc);
        return c;
    }

}
