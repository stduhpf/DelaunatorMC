package net.stduhpf.delaunator.delaunator.utils.Schematics;
import java.nio.file.Path;
import java.util.HashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
//TODO turn this into its own project?

public class Litematic {
    String name; 
    String author;
    String desc;
    HashMap<String, Region> region;
    long timestamp;
    int width;
    int height;
    int length;
    int volume;
    int block_count;


    public Litematic(String name, String author, String desc, HashMap<String, Region> region) {
        this.name = name;
        this.author = author;
        this.desc = desc;
        this.region = region;
        this.timestamp = System.currentTimeMillis();
        this.width = 0;
        this.height = 0;
        this.length = 0;

        if(this.region== null) {
            this.region = new HashMap<String, Region>();
        }
        
    }

    public void save(Path path){
        NbtCompound nbt = this.toNbt();
        try{
            NbtIo.write(nbt, path);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private NbtCompound toNbt() {
        NbtCompound nbt = new NbtCompound();
        nbt.putInt("Version", 6);
        nbt.putInt("SubVersion", 1);
        nbt.putInt("MinecraftDataVersion", 3955);
        NbtCompound meta = new NbtCompound();
        NbtCompound enclose = new NbtCompound();
        // Capitalized keys
        meta.putString("Name", name);
        meta.putString("Author", author);
        meta.putString("Description", desc);
        meta.putLong("TimeCreated", timestamp);
        meta.putLong("TimeModified", timestamp);
        
        enclose.putInt("x", width);
        enclose.putInt("y", height);
        enclose.putInt("z", length);
        meta.put("EnclosingSize", enclose);

        
        int volume = 0;
        int blockCount = 0;
        int counter = 0;
        NbtCompound regs = new NbtCompound();
        for(String key : region.keySet()) {
            Region reg = region.get(key);
            regs.put(key,reg.toNbt());
            System.out.println("Adding region: " + key);
            volume += reg.height*reg.length*reg.width;
            // TODO: this is a dirty hack taht works with maps only
            blockCount+= reg.length*reg.width;
            counter++;
        }
        meta.putInt("RegionCount", counter);
        meta.putInt("TotalVolume", volume);
        meta.putInt("TotalBlocks", blockCount);

        nbt.put("Metadata", meta);
        nbt.put("Regions", regs);

        return nbt;
    }

    public void addRegion(String name, Region region) {
        this.region.put(name, region);
        this.width = region.width;
        this.height = region.height;
        this.length = region.length;
    }

}
