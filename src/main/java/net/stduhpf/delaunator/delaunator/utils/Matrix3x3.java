package net.stduhpf.delaunator.delaunator.utils;

public class Matrix3x3 {
    public double[][] data = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
    private double det = 0.;
    private boolean detChecked = false;
    private Matrix3x3 adj = null;
    private Matrix3x3 inverse = null;
    private Matrix3x3 transpose = null;

    public Matrix3x3(double[][] data) {
        assert data.length == 3 && data[0].length == 3 && data[1].length == 3 && data[2].length == 3;
        this.data = data;
    }

    public Matrix3x3() {
    }

    public Matrix3x3 mult(Matrix3x3 that) {
        Matrix3x3 out = new Matrix3x3();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                for (int k = 0; k < 3; k++)
                    out.data[i][j] += this.data[i][k] * that.data[k][j];
        return out;
    }

    public double[] inner() { ///no idea how to call it, but it's basically diag(M*M.transpose())
        double[] out = {0.,0.,0.};
        for (int i = 0; i < 3; i++)
            out[i] = Vector3.dot(this.data[i],this.data[i]);
        return out;
    }


    public double[] vmul(double[] vec) {
        assert vec.length == 3;
        double[] out = {0.,0.,0.};
        for (int j = 0; j < 3; j++)
            for (int i = 0; i < 3; i++)
                out[j] += vec[i] * this.data[i][j];
        return out;
    }

    public double[] mulv(double[] vec) {
        assert vec.length == 3;
        double[] out = {0.,0.,0.};
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                out[i] += this.data[i][j] * vec[j];
        return out;
    }


    public double det() {
        if (!detChecked){
            if (adj == null) adj();
            det = data[0][0] * (adj.data[0][0])
                + data[0][1] * (adj.data[1][0])
                + data[0][2] * (adj.data[2][0]);
            detChecked = true;
        }
        return det;
    }

    // data = {{data[0][0], data[0][1], data[0][2]},
    //         {data[1][0], data[1][1], data[1][2]},
    //         {data[2][0], data[2][1], data[2][2]}}

    public Matrix3x3 adj() {
        if (adj == null) {
            double[][] adjData = { { data[1][1] * data[2][2] - data[1][2] * data[2][1],
                                     data[0][2] * data[2][1] - data[0][1] * data[2][2],
                                     data[0][1] * data[1][2] - data[1][1] * data[0][2] },
                                   { data[1][2] * data[2][0] - data[1][0] * data[2][2],
                                     data[0][0] * data[2][2] - data[0][2] * data[2][0],
                                     data[1][0] * data[0][2] - data[0][0] * data[1][2] },
                                   { data[1][0] * data[2][1] - data[2][0] * data[1][1],
                                     data[2][0] * data[0][1] - data[0][0] * data[2][1],
                                     data[0][0] * data[1][1] - data[1][0] * data[0][1] } };
            adj = new Matrix3x3(adjData);
        }
        return adj;
    }

    public Matrix3x3 scale(double s) {
        Matrix3x3 out = new Matrix3x3();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                out.data[i][j] = data[i][j] * s;
        return out;
    }
    public Matrix3x3 normalize(double s) {
        Matrix3x3 out = new Matrix3x3();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                out.data[i][j] = data[i][j] / s;
        return out;
    }


    public Matrix3x3 inverse() {
        if (inverse == null) {
            Matrix3x3 adj = this.adj();
            inverse = adj.normalize(this.det());
            inverse.inverse = this;
        }
        return inverse;
    }

    public Matrix3x3 transpose() {
        if (transpose == null) {
            transpose = new Matrix3x3();
            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++)
                    transpose.data[i][j] = data[j][i];
            transpose.transpose = this;
        }
        return transpose;
    }

    public double[] diag() {
        double[] ret = { data[0][0], data[1][1], data[2][2] };
        return ret;
    }

    public String str(){
        return "\n["+ Vector3.str(this.data[0])+ "\n " + Vector3.str(this.data[1])+ "\n " + Vector3.str(this.data[2]) + "]";
    }

    /**
     * Only use this method if you are sure the matrix you are providing is actually the inverse
     * @param inverse the already computed inverse matrix
     */
    public void forceSetInverse(Matrix3x3 inverse){
        this.inverse = inverse;
        inverse.inverse = this;
    }

}