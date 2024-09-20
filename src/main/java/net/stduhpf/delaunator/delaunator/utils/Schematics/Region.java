package net.stduhpf.delaunator.delaunator.utils.Schematics;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLongArray;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;


public class Region {
    int X;
    int Y;
    int Z;
    int width;
    int height;
    int length;
    List<BlockState> palette;
    int[][][] blocks;

    private int min_h;
    private int max_h;

    public Region(int x, int y, int z, int width, int height, int length) {
        this.X = x;
        this.Y = y;
        this.Z = z;
        this.width = width;
        this.height = height;
        this.length = length;

        this.min_h = height;
        this.max_h = 0;

        this.palette = new ArrayList<BlockState>();
        palette.add(Blocks.AIR.getDefaultState());
        this.blocks = new int[width][height][length];
    }


    public NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        NbtCompound pos = new NbtCompound();
        pos.putInt("x", X);
        pos.putInt("y", Y);
        pos.putInt("z", Z);
        nbt.put("Position", pos);

        NbtCompound size = new NbtCompound();
        size.putInt("x", width);
        size.putInt("y", (max_h-min_h+1));
        size.putInt("z", length);
        nbt.put("Size", size);

        NbtList plt = new NbtList();
        for (BlockState block : palette) {
            NbtCompound b = new NbtCompound();

            Identifier id = block.getBlock().getLootTableKey().getValue();
            String id_namespace = id.getNamespace();
            String[] split_path = id.getPath().split("/");
            String id_path = split_path[split_path.length - 1];         
            b.putString("Name", id_namespace + ":" + id_path);

            // NbtCompound props = new NbtCompound();
            // for (Property<?> p :block.getProperties()) {
            //     //TODO: props from named block properties
            //     props.putString(p.getName(), p.getValues().toString());
            // }
            // b.put("Properties", props);

            plt.add(b);
        }
        nbt.put("BlockStatePalette", plt);
        nbt.put("Entities", new NbtList());
        nbt.put("TileEntities", new NbtList());
        nbt.put("PendingBlockTicks", new NbtList());
        nbt.put("PendingFluidTicks", new NbtList());

        //bit-packing
        int bitsPerEntry = 1;
        int v = this.palette.size();
        while ((v >>= 1)!=0) {
            bitsPerEntry++;
        }

        System.out.println("palette " + palette.size() + "(" + bitsPerEntry + " bits)");


        long[] flat = new long[1+(width*height*length*bitsPerEntry>>6)];
        int ind = 0;
        int rem_bits = 0;
        System.out.println("range: " + this.min_h + " -> " + this.max_h);
        System.out.println("h: " + this.height);
        for (int y = this.min_h; y <= this.max_h; y++) {
            for (int z = 0; z < length; z++) {
                for (int x = 0; x < width; x++) {
                    long block = (long)this.blocks[x][y][z];
                    // if(z==0)System.out.println(block + ">>" + StringUtils.leftPad(Long.toBinaryString(block),6,"0"));
                    flat[ind] |= block<<rem_bits;
                    rem_bits+=bitsPerEntry;
                    if(rem_bits>=64){
                        // if(z==0)System.out.println( ind + " " +  StringUtils.leftPad( Long.toBinaryString( flat[ind] ), 64, "0" ) + " ->");
                        rem_bits = rem_bits % 64;
                        flat[++ind] = block>>(bitsPerEntry-rem_bits);
                    }
                    // if(z==0)System.out.println( ind + " " +  StringUtils.leftPad( Long.toBinaryString( flat[ind] ), 64, "0" ) );
                }
            }
        }
        System.out.println("last index: " + ind);
        System.out.println("exp last index: " + ((width*height*length*bitsPerEntry>>6)));

        System.out.println("fist val: " + flat[0]);
        
        NbtLongArray bl = new NbtLongArray(flat);
        nbt.put("BlockStates", bl);

        return nbt;
    }


    public void addBlock(int x, int y, int z, BlockState block) {
        if(y<this.min_h)
            this.min_h = y;
        if(y>this.max_h)
            this.max_h = y;
        
        
        int index = -1;
        // System.out.println("x:" + x + " y:" + y + " z:" + z);
        for (int i = 0; i < palette.size(); i++) {
            if (this.palette.get(i).equals(block)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            index = palette.size();
            this.palette.add(block);
        }
        this.blocks[x][y][z] = index;
    }

}
