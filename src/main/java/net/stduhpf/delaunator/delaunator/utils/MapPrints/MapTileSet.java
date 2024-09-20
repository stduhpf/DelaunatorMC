package net.stduhpf.delaunator.delaunator.utils.MapPrints;

import net.minecraft.item.map.MapState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public class MapTileSet {

    int content_width;
    int content_length;
    int tile_width;
    int tile_length;
    int num_tiles_x;
    int num_tiles_z;

    int number_of_tiles;

    byte[][][] tile_map;


    public MapTileSet(int content_width, int content_length){
        this.content_width = content_width;
        this.content_length = content_length;
        this.tile_width = 128;
        this.tile_length = 128;
        this.num_tiles_x = 1 + ((content_width - 1) / tile_width);
        this.num_tiles_z = 1 + ((content_length - 1) / tile_length);
        this.number_of_tiles = num_tiles_x * num_tiles_z;

        this.tile_map = new byte[num_tiles_x][num_tiles_z][tile_width * tile_length];
        System.out.println("Number of tiles: " + number_of_tiles);
    }

    public void setColor(int x, int z, int colorId, int shade){
        int X = x/tile_width;
        int Z = z/tile_length;
        tile_map[X][Z][x%tile_width + (z%tile_length)*tile_width] = (byte)(colorId*4 + shade);
    }

    public NbtCompound printToNbt(int tile_x, int tile_z){
        return printToNbt(tile_x, tile_z, "delaunator:images");
    }

    public NbtCompound printToNbt(int tile_x, int tile_z, String dimName){
        NbtCompound nbt = new NbtCompound();
        NbtCompound data = new NbtCompound();
        
        data.putString("dimension", dimName);
        
        data.putByte("scale", (byte)0);
        data.putByte("trackingPosition", (byte)0);
        data.putByte("unlimitedTracking", (byte)0);
        data.putByte("locked", (byte)1);

        data.putInt("xCenter", tile_x*tile_width);
        data.putInt("zCenter", tile_z*tile_length);

        data.put("banners", new NbtList());
        data.put("frames", new NbtList());

        byte[] colors = tile_map[tile_x][tile_z];

        data.putByteArray("colors", colors);

        nbt.put("data", data);
        nbt.putInt("DataVersion", 3955);
        return nbt;
    }
}
