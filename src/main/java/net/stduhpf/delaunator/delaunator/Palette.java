package net.stduhpf.delaunator.delaunator;

import java.util.ArrayList;
import java.lang.reflect.*;


import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.MapColor.Brightness;
import net.stduhpf.delaunator.delaunator.shapes.Point3D;
import net.stduhpf.delaunator.delaunator.shapes.TetrahedrizedColor;
import net.stduhpf.delaunator.delaunator.shapes.Tetrahedron3D;
import net.stduhpf.delaunator.delaunator.utils.DeColors;

public class Palette {
    public ArrayList<Block>[] blocksPerColor;
    public Tetrahedrization    data;
    public MapColor[]         COLORS      = new MapColor[63];
    private String[]           COLOR_NAMES = new String[63];
    private boolean[]          mask        = new boolean[63];
    private ArrayList<Point3D> vertices;
    
    public Palette() {
        Field[] blocks_fields =   Blocks.class.getFields();
        Field[] color_fields  = MapColor.class.getFields();

        @SuppressWarnings("unchecked")
        ArrayList<Block>[] _blocksPerColor = new ArrayList[63];
        blocksPerColor = _blocksPerColor;

        for (int index = 0; index < 63; ++index) {
            blocksPerColor[index] = new ArrayList<Block>();
            mask[index] = true;
        }
        for (Field f : blocks_fields) {
            if (f.getType().isAssignableFrom(Block.class)) {
                try {
                    Block currentBlock = (Block) f.get(null);
                    MapColor color = currentBlock.getDefaultMapColor();
                    if (color.id < 63)
                        blocksPerColor[color.id].add(currentBlock);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        
        Point3D.resetInstanceCount();
        for (Field f : color_fields) {
            if (f.getType().isAssignableFrom(MapColor.class)) {
                try {
                    MapColor color = (MapColor) f.get(null);
                    if (color.id >= 63)
                        continue;
                    COLOR_NAMES[color.id] = f.getName();
                    COLORS[color.id] = color;
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void build(boolean[] ignore, int useStaircase) {
        // TODO: Experiment with other (linear) color spaces. 
        // Maybe YCoCG isn't the best for perceptual uniformity 
        assert ignore.length >= 63;
        vertices = new ArrayList<>();
        for (int i = 1; i < 63; i++) {
            // System.out.println(COLOR_NAMES[i]);
            MapColor color = COLORS[i];
            if (color != null && !ignore[i]) {
                int[] colInt = DeColors.unpackColors(color.color);
                if (useStaircase == 1) {
                    double[] colYcc = DeColors.RGB2YCoCg
                            .mulv(DeColors.gammaInv(colInt, Brightness.HIGH.brightness));
                    Point3D point = new Point3D(colYcc[0], colYcc[1], colYcc[2], COLOR_NAMES[i] + "|U", color.id,
                            Brightness.HIGH);
                    vertices.add(point);
                    double[] col = DeColors.RGB2YCoCg
                            .mulv(DeColors.gammaInv(colInt, Brightness.NORMAL.brightness));
                    Point3D point1 = new Point3D(col[0], col[1], col[2], COLOR_NAMES[i] + "|N", color.id,
                            Brightness.NORMAL);
                    vertices.add(point1);
                    col = DeColors.RGB2YCoCg.mulv(DeColors.gammaInv(colInt, Brightness.LOW.brightness));
                    Point3D point2 = new Point3D(col[0], col[1], col[2], COLOR_NAMES[i] + "|D", color.id, Brightness.LOW);
                    vertices.add(point2);
                } else if (useStaircase == 2) {
                    double[] colYcc = DeColors.RGB2YCoCg
                            .mulv(DeColors.gammaInv(colInt, Brightness.HIGH.brightness));
                    Point3D point = new Point3D(colYcc[0], colYcc[1], colYcc[2], COLOR_NAMES[i] + "|U", color.id,
                            Brightness.HIGH);
                    vertices.add(point);
                    double[] col = DeColors.RGB2YCoCg
                            .mulv(DeColors.gammaInv(colInt, Brightness.NORMAL.brightness));
                    Point3D point1 = new Point3D(col[0], col[1], col[2], COLOR_NAMES[i] + "|N", color.id,
                            Brightness.NORMAL);
                    vertices.add(point1);
                    col = DeColors.RGB2YCoCg.mulv(DeColors.gammaInv(colInt, Brightness.LOW.brightness));
                    Point3D point2 = new Point3D(col[0], col[1], col[2], COLOR_NAMES[i] + "|D", color.id, Brightness.LOW);
                    vertices.add(point2);
                    col = DeColors.RGB2YCoCg
                            .mulv(DeColors.gammaInv(colInt, Brightness.LOWEST.brightness));
                    Point3D point3 = new Point3D(col[0], col[1], col[2], COLOR_NAMES[i] + "|I", color.id,
                            Brightness.LOWEST);
                    vertices.add(point3);

                    // throw new UnsupportedOperationException("Unimplemented");
                } else {
                    double[] colYcc = DeColors.RGB2YCoCg
                            .mulv(DeColors.gammaInv(colInt, Brightness.NORMAL.brightness));
                    Point3D point = new Point3D(colYcc[0], colYcc[1], colYcc[2], COLOR_NAMES[i] + "|N", color.id,
                            Brightness.NORMAL);
                    vertices.add(point);
                }
            }
        }
        
        data = new Tetrahedrization(vertices.size());
        data.addPoints(vertices);
    }

    public TetrahedrizedColor findBlockByColor(int color){
        double[] zero = {0,0,0};
        double[] unpackedColor = DeColors.RGB2YCoCg.mulv(DeColors.gammaInv(DeColors.unpackColorsGBR(color)));
        for (Point3D vertex : vertices) {
            if(vertex.values[0] == unpackedColor[0] && vertex.values[1] == unpackedColor[1] && vertex.values[2] == unpackedColor[2])
                return new TetrahedrizedColor(new Tetrahedron3D(vertex, vertex, vertex, vertex), zero);
        }
        return null;
    }

    public TetrahedrizedColor apply(int color){
        int[] colInt = DeColors.unpackColorsGBR(color);
        double[] colYcc = DeColors.RGB2YCoCg.mulv(DeColors.gammaInv(colInt));
        // colYcc[1]=-colYcc[1];
        // colYcc[2]=-colYcc[2];

        Tetrahedron3D tet = data.localTetra(colYcc);

        if(tet==null){
            return null;
        }

        double[] coords = tet.localCoords(colYcc);

        // for (int i = 0; i < 4; i++) {
        //     Point3D p = tet.vertices[i];
        //     if (p == data.bigT.vertices[0] || p == data.bigT.vertices[1] || p == data.bigT.vertices[2]
        //             || p == data.bigT.vertices[3]) {
        //         if (i != 3) {
        //             coords = Vector3.scale(coords, 2. / (2. - coords[i]));
        //             coords[i] = 0;
        //         } else {
        //             coords = Vector3.scale(coords, 1. / Vector3.dot(coords, new double[] { 1, 1, 1 }));
        //         }
        //     }
        // }

        // // if(dither<.01) System.out.println( tet.name + "\n" + coords[0] + " " + coords[1] + " " + coords[2] + " => "  + (1.- (coords[0]+coords[1]+coords[2])) + "\n");
        // int index = 0;
        // if (dither > coords[0]) {
        //     dither -= coords[0];
        //     if (dither > coords[1]) {
        //         dither -= coords[1];
        //         if (dither > coords[2]) {
        //             index = 3;
        //         } else {
        //             index = 2;
        //         }
        //     } else {
        //         index = 1;
        //     }
        // }

        // double[] paletteycc = tet.vertices[index].values; 
        // int[] paletteInt = DeColors.gamma(DeColors.YCoCg2RGB.mulv(paletteycc));

        // int finalColor = DeColors.packColors(paletteInt);
        // return finalColor;//MapColor.get(tet.vertices[index].colorId).color|0xFF<<24;
        return new TetrahedrizedColor(tet, coords, data.bigT);
    }
    
}
