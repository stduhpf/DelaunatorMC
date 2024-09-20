package net.stduhpf.delaunator.delaunator.shapes;

import net.stduhpf.delaunator.delaunator.utils.Vector3;

public class TetrahedrizedColor {

    public Tetrahedron3D cell;

    public double[] coords;

    public TetrahedrizedColor(Tetrahedron3D cell, double[] coords) {
        assert coords.length == 3;
        this.cell = cell;
        this.coords = coords;
    }

    public TetrahedrizedColor(Tetrahedron3D cell, double[] coords, Tetrahedron3D bigT) {
        this(cell,coords);
        for (int i = 0; i < 4; i++) {
            Point3D p = cell.vertices[i];
            if (p == bigT.vertices[0] || p == bigT.vertices[1] || p == bigT.vertices[2]
                    || p == bigT.vertices[3]) {
                if (i != 3) {
                    this.coords = Vector3.scale(this.coords, 2. / (2. - this.coords[i]));
                    this.coords[i] = 0;
                } else {
                    this.coords = Vector3.scale(this.coords, 1. / Vector3.dot(this.coords, new double[] { 1, 1, 1 }));
                }
            }
        }
    }
    
    public Point3D applyDither(double dither){
        return applyDither(dither, 0.);
    }

    public Point3D applyDither(double dither, double centering){
        double[] coords = this.coords.clone();
        if (centering >= 0.5){
            int index = 3;
            double max = 1.-coords[0]-coords[1]-coords[2];
            for(int i=0;i<3;i++){
                if(coords[i]>max){
                    max = coords[i];
                    index = i;
                }
            }
            return cell.vertices[index];
        }
        if(centering>0.){
            double remaining = 1.-coords[0]-coords[1]-coords[2];
            double m  =0;
            boolean[] toClean = {false,false,false}; 
            while ((m = Math.min(Math.min(coords[0], coords[1]), Math.min(coords[2], remaining))) < centering) {
                double d = 1.-m;
                if (m==coords[0]){
                    remaining += coords[0]*remaining/d;
                    coords[1] += coords[0]*coords[1]/d;
                    coords[2] += coords[0]*coords[2]/d;

                    coords[0] = 1;
                    toClean[0] = true;
                }else if (m == coords[1]){
                    remaining += coords[1]*remaining/d;
                    coords[0] += coords[1]*coords[0]/d;
                    coords[2] += coords[1]*coords[2]/d;
                    
                    coords[1] = 1;
                    toClean[1] = true;
                }else if (m == coords[2]){
                    remaining += coords[2]*remaining/d;
                    coords[0] += coords[2]*coords[0]/d;
                    coords[1] += coords[2]*coords[1]/d;
                    
                    coords[2] = 1;
                    toClean[2] = true;
                }else{
                    coords[0] += remaining*coords[0]/d;
                    coords[1] += remaining*coords[1]/d;
                    coords[2] += remaining*coords[2]/d;
                    
                    remaining = 1;
                    // no need to clean remaining
                }
            }
            for(int i=0;i<3;++i){
                if(toClean[i])coords[i]=0;
            }
        }
        int index = 0;
        if (dither > coords[0]) {
            dither -= coords[0];
            if (dither > coords[1]) {
                dither -= coords[1];
                if (dither > coords[2]) {
                    index = 3;
                } else {
                    index = 2;
                }
            } else {
                index = 1;
            }
        }
        return cell.vertices[index];
    }    
    
}
