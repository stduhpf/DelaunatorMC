package net.stduhpf.delaunator.delaunator.shapes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Triangle3D {
    private static Triangle3D[] triBuffer;

    private static int vertexCount =-1;

    public static void setVertexCount(int vertexCount) {
        Triangle3D.vertexCount = vertexCount;
        triBuffer = new Triangle3D[vertexCount*vertexCount*vertexCount];
    }

    static int indexFromPoints(Point3D A, Point3D B, Point3D C){
        List<Point3D> sortedList = Arrays.asList(A,B,C);
        int index = sortedList.get(0).getInstanceId() + sortedList.get(1).getInstanceId()*vertexCount + sortedList.get(2).getInstanceId()*vertexCount*vertexCount;
        return index;
    }
    
    public static void destroy(Triangle3D tri){
        int index = indexFromPoints(tri.vertices[0], tri.vertices[1], tri.vertices[2]);
        triBuffer[index]=null;
    }

    static Triangle3D Get(Point3D A, Point3D B, Point3D C){
        int index = indexFromPoints(A, B, C);
        if(triBuffer[index]==null){
            triBuffer[index] = new Triangle3D(A,B,C);
        }
        return  triBuffer[index];
    }

    public ArrayList<Tetrahedron3D> inTetra;
    public Point3D[] vertices = {null,null,null};
    public boolean checked = false;

    private Triangle3D(Point3D A, Point3D B, Point3D C) {
        inTetra = new ArrayList<Tetrahedron3D>();
        vertices[0]=A;
        vertices[1]=B;
        vertices[2]=C;
        checked = false;
    }
}
