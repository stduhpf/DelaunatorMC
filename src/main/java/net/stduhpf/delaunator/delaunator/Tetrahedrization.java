package net.stduhpf.delaunator.delaunator;

import java.util.ArrayList;

import net.stduhpf.delaunator.delaunator.shapes.Point3D;
import net.stduhpf.delaunator.delaunator.shapes.Tetrahedron3D;
import net.stduhpf.delaunator.delaunator.shapes.Triangle3D;

public class Tetrahedrization {
    Tetrahedron3D bigT;
    ArrayList<Tetrahedron3D> tets;


    public Tetrahedrization(int count) {
        Triangle3D.setVertexCount(count + 4);
        tets = new ArrayList<>();
        double s = 1000.;
        bigT = new Tetrahedron3D(new Point3D( s, 0.0, 0.0, "0"),
                                 new Point3D(-s / 3.0, -s * Math.sqrt(2) / 1.5, 0.0, "1"),
                                 new Point3D(-s / 3.0,  s * Math.sqrt(2) / 3.0,  s * Math.sqrt(2.0 / 3.0), "2"),
                                 new Point3D(-s / 3.0,  s * Math.sqrt(2) / 3.0, -s * Math.sqrt(2.0 / 3.0), "3"));
        tets.add(bigT);
    }
    /**
     * Assumes Triangle3D.setVertexCount() has already been called
     */
    public Tetrahedrization() {
        tets = new ArrayList<>();
        double s = 100000.;
        bigT = new Tetrahedron3D(new Point3D( s, 0.0, 0.0, "0"),
                                 new Point3D(-s / 3.0, -s * Math.sqrt(2) / 1.5, 0.0, "1"),
                                 new Point3D(-s / 3.0,  s * Math.sqrt(2) / 3.0,  s * Math.sqrt(2.0 / 3.0), "2"),
                                 new Point3D(-s / 3.0,  s * Math.sqrt(2) / 3.0, -s * Math.sqrt(2.0 / 3.0), "3"));
        tets.add(bigT);
    }

    private void addPoint(Point3D vertex){

        // find cells that are affected by the new point (the point in contained in their bounding spheres)
        ArrayList<Tetrahedron3D> containers = new ArrayList<>();
        for (Tetrahedron3D t : tets) {
            if(vertex.isInSphere(t.boundingSphere)){
                containers.add(t);
                t.isContainer = true;
            }
        }
        // find all the faces of these cells that are not completely surrounded by affected cells
        ArrayList<Triangle3D> exteriorFaces = new ArrayList<>();
        for (Tetrahedron3D tet : containers)
            for (int i = 0; i < 4; ++i) {
                Triangle3D face = tet.triangles[i];
                if (!face.checked) { // no need to check a face twice
                    face.checked = true;
                    boolean keep = true;
                    for (Tetrahedron3D t : face.inTetra)
                        if (t != tet && t.isContainer) { // if both sides of the face are to be removed, we don't keep the face
                            keep = false;
                            // Triangle3D.destroy(face);
                            break;
                        }
                    if (keep)
                        exteriorFaces.add(face);
                }
            }
    
        for(Tetrahedron3D tet : containers){
            for(int i =0;i<4;++i){
                tet.triangles[i].checked = false;
                tet.triangles[i].inTetra.remove(tet);
                tet.vertices[i].inTetra.remove(tet);
            }
            if(!tets.remove(tet)){
                System.err.println("whaat");
            }
        }
        
        for(Triangle3D face : exteriorFaces){
            Tetrahedron3D t = new Tetrahedron3D(vertex, face);
            tets.add(t);
        }

    }   

    public void addPoints(ArrayList<Point3D> points){
        for (Point3D point : points) {
            this.addPoint(point);
        }
        System.out.println("Tcount: " + tets.size());
    }

    // private static boolean validCoords(double[] c){
    //     double margin = 0.;
    //     boolean allPositive = c[0] + margin >= 0. && c[1] + margin >= 0. && c[2] + margin >= 0.;
    //     boolean correctSum = c[0] + c[1] + c[2] <= 1. + 3. * margin;
    //     return allPositive && correctSum;
    // }
    private static double validCoords(double[] c){
        double d0 = -Math.min(0., c[0]),
               d1 = -Math.min(0., c[1]),
               d2 = -Math.min(0., c[2]),
               d3 = -Math.min(0., 1. - (c[0] + c[1] + c[2]));
        return d0 + d1 + d2 + d3;
    }

    public Tetrahedron3D localTetra(double[] point){
        // Tetrahedron3D tet = null;
        int i = 0 , j=-1;
        double d = Double.POSITIVE_INFINITY;
        for(Tetrahedron3D t : tets){
            double[] c = t.localCoords(point);
            double error = validCoords(c);
            if( error == 0.){
                return t;
            }
            if(error<d){
                d = error;
                j = i;
            }
            ++i;
        }
        // System.out.println("Missed tetrahedrons for color: Ycbcr(" + point[0] + point[1] + point[1] + "), picking the closest one...");
        return j>=0.?tets.get(j):null;
    }

}
