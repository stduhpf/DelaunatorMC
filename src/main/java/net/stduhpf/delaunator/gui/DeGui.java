package net.stduhpf.delaunator.gui;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

import javax.imageio.ImageIO;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WBox;
import io.github.cottonmc.cotton.gui.widget.WButton;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WSlider;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.WToggleButton;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.Color;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor.Brightness;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.stduhpf.delaunator.delaunator.Palette;
import net.stduhpf.delaunator.delaunator.shapes.Point3D;
import net.stduhpf.delaunator.delaunator.shapes.TetrahedrizedColor;
import net.stduhpf.delaunator.delaunator.utils.MapPrints.MapTileSet;
import net.stduhpf.delaunator.delaunator.utils.Schematics.Litematic;
import net.stduhpf.delaunator.delaunator.utils.Schematics.Region;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeGui extends LightweightGuiDescription {
    public static final Logger LOGGER = LoggerFactory.getLogger("modid");

    protected int DEFAULT_GRID = 18;

    private static String[] ditherNames = { "IGN", "Bayer" };
    private static String[] paletteTypes = { "Flat", "Staircase", "Full"};
    private int ditherMethod = 0;

    public DeScreen screen = null;

    protected WGridPanel root;

    private Palette palette = null;

    private int currentFile = 0;
    private String pathname = "";
    private WImage picture = null;
    private WImage q_picture = null;

    private WImage prev_picture = null;
    private WLabel label = null;

    private int paletteType = 0;

    private TetrahedrizedColor[][] raw = null;

    private Boolean suppressUpdates = false;
    
    private WSlider ditherMinSlider;
    private WButton pasteButton;
    private WButton schemButton;


    static NativeImageBackedTexture fileToTexture(File file) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(file);
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        int[] pixels = new int[width * height];
        bufferedImage.getRGB(0, 0, width, height, pixels, 0, width);

        NativeImage nativeImage = new NativeImage(width, height, false);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                int a = (pixel >> 24) & 0xFF;
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = (pixel) & 0xFF;

                int abgr = (a << 24) | (b << 16) | (g << 8) | (r);
                nativeImage.setColor(x, y, abgr);
            }
        }
        System.out.println("w: " + width + ", h: " + height);

        return new NativeImageBackedTexture(nativeImage);
    }

    private double dither_IGN(int x, int y, int __) {
        return (52.9829189 * (0.06711056 * (double) (x) + 0.00583715 * (double) (y)) % 1) % 1;
    }

    private double dither_Bayer(int x, int y, int n) {
        if (n <= 0)
            return 0.;
        if (n == 1)
            return ((double) y * (double) y * .75 + (double) x * .5) % 1;
        return dither_Bayer(x / 2, y / 2, n - 1) / 4 + dither_Bayer(x, y, 1);
    }

    public DeGui() {
        // TODO: helpful tooltips

        File localPath = new File(System.getProperty("user.dir") + File.separator + "images");
        if(!localPath.exists()) {
            localPath.mkdir();
        }
        String[] files = localPath.list();
        this.pathname = files.length > 0 ? files[0] : "No files found";

        // MinecraftClient client = MinecraftClient.getInstance();
        root = new WGridPanel();
        setRootPanel(root);
        // root.setSize(DEFAULT_GRID*25, DEFAULT_GRID*12);
        root.setInsets(Insets.ROOT_PANEL);

        // WSprite icon = new WSprite(Identifier.tryParse("minecraft",
        // "textures/item/redstone.png"));
        // root.add(icon, 0, 2, 1, 1);

        {   //Image viewer
            WPlainPanel display = new WPlainPanel();
            root.add(display, 7, 0, 7, 7);

            WButton bg0 = new WButton();
            bg0.setEnabled(false);
            display.add(bg0, 0, 0, display.getWidth(), display.getHeight());
            picture = new WImage(null, display.getWidth() - 2, display.getWidth() - 2);
            display.add(picture, 0, 0);
            q_picture = new WImage(null, display.getWidth() - 2, display.getWidth() - 2);
            display.add(q_picture, 0, 0);
        }


        WToggleButton unitSwitch = new WToggleButton(Text.literal("Maps"));
        //img size input
        WGridPanel sizeInputs = new WGridPanel();
        root.add(sizeInputs, 15,6);
        WLabel lblw = new WLabel(Text.literal("Width"));
        sizeInputs.add(lblw,0,0);
        lblw.setVerticalAlignment(VerticalAlignment.CENTER);
        WLabel lblh = new WLabel(Text.literal("Height"));
        sizeInputs.add(lblh,0,1);
        lblh.setVerticalAlignment(VerticalAlignment.CENTER);

        WTextField widthDisplay = new WTextField();
        sizeInputs.add(widthDisplay,2,0,2,1);
        WTextField heightDisplay = new WTextField();
        sizeInputs.add(heightDisplay,2,1,2,1);

        widthDisplay.setChangedListener(text->{
            if(suppressUpdates)return;
            try {
                float w = Float.parseFloat(text);
                float h = w * picture.getImageHeight()/picture.getImageWidth();                 
                suppressUpdates	= true;
                heightDisplay.setText("" + (float)h);
                suppressUpdates	= false;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        });

        heightDisplay.setChangedListener(text->{
            if(suppressUpdates)return;
            try {
                float h = Float.parseFloat(text);
                float w = h * picture.getImageWidth()/picture.getImageHeight();
                suppressUpdates	= true;
                widthDisplay.setText("" + (float)w);
                suppressUpdates	= false;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        });
        
        unitSwitch.setOnToggle(on -> {
            try {
                float w = Float.parseFloat(widthDisplay.getText());
                float h = Float.parseFloat(heightDisplay.getText());
                if(on){
                    suppressUpdates	= true;
                    widthDisplay.setText("" + (float)w/128f);
                    heightDisplay.setText("" + (float)h/128f);
                    suppressUpdates	= false;
                }else{
                    suppressUpdates	= true;
                    widthDisplay.setText("" + (int)(w*128));
                    heightDisplay.setText("" + (int)(h*128));
                    suppressUpdates	= false;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            
        });
        root.add(unitSwitch, 17, 5);
        WLabel blockLabel = new WLabel(Text.literal("Blocks"));
        blockLabel.setVerticalAlignment(VerticalAlignment.CENTER);
        root.add(blockLabel, 15, 5, 2, 1);


        WToggleButton toggleResize = new WToggleButton(Text.literal("Resize Image."));
        toggleResize.setOnToggle(on -> {
            widthDisplay.setEditable(on);
            heightDisplay.setEditable(on);
        });
        root.add(toggleResize, 15, 4);
          

        {   // Image explorer
            WGridPanel imgLoader = new WGridPanel();
            root.add(imgLoader, 0, 0);

            WButton bg = new WButton();
            bg.setEnabled(false);
            prev_picture = new WImage(null, 5 * DEFAULT_GRID - 2, 5 * DEFAULT_GRID - 2);

            imgLoader.add(bg, 0, 1, 5, 5);
            imgLoader.add(prev_picture, 0, 1, 5, 5);

            File prev_imageFile = new File(localPath.toString() + File.separator + pathname);
            if (prev_imageFile.exists()) {
                try {
                    NativeImageBackedTexture texture = fileToTexture(prev_imageFile);
                    prev_picture.updateTexture(texture);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("invalid image file: " + pathname);
                }
            }

            WButton openButton = new WButton(Text.literal("Chose"));
            imgLoader.add(openButton, 1, 7, 3, 1);
            openButton.setOnClick(() -> {
                File imageFile = new File(localPath.toString() + File.separator + pathname);
                if (imageFile.exists()) {
                    try {
                        NativeImageBackedTexture texture = fileToTexture(imageFile);
                        // int X = picture.getX(), Y = picture.getY();
                        picture.updateTexture(texture);
                        q_picture.updateTexture(null);
                        int w = picture.getImage_texture().getImage().getWidth();
                        int h = picture.getImage_texture().getImage().getHeight();
                        suppressUpdates	= true;
                        widthDisplay.setText("" + w);
                        heightDisplay.setText("" + h);
                        if(unitSwitch.getToggle()){
                            widthDisplay.setText("" + (float)w/128f);
                            heightDisplay.setText("" + (float)h/128f);
                        }
                        suppressUpdates	= false;
                        // picture.setLocation(X, Y);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println("invalid image file: " + pathname);
                    }
                }
            });

            WButton prevButton = new WButton(Text.literal("<"));
            imgLoader.add(prevButton, 0, 7, 1, 1);
            prevButton.setOnClick(() -> {
                currentFile = (currentFile - 1 + files.length) % files.length;
                pathname = files[currentFile];
                label.setText(Text.literal(pathname));

                File imageFile = new File(localPath.toString() + File.separator + pathname);
                if (imageFile.exists()) {
                    try {
                        NativeImageBackedTexture texture = fileToTexture(imageFile);
                        prev_picture.updateTexture(texture);

                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println("invalid image file: " + pathname);
                        try {
                            prev_picture.updateTexture(null);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }

                    }
                }

            });

            WButton nextButton = new WButton(Text.literal(">"));
            imgLoader.add(nextButton, 4, 7, 1, 1);
            nextButton.setOnClick(() -> {
                currentFile = (currentFile + 1) % files.length;
                pathname = files[currentFile];
                label.setText(Text.literal(pathname));

                File imageFile = new File(localPath.toString() + File.separator + pathname);
                if (imageFile.exists()) {
                    try {
                        NativeImageBackedTexture texture = fileToTexture(imageFile);
                        prev_picture.updateTexture(texture);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println("invalid image file: " + pathname);
                        try {
                            prev_picture.updateTexture(null);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                }
            });

            WButton ofdButton = new WButton(Text.literal("Open img folder"));
            imgLoader.add(ofdButton, 0, 0, 5, 1);
            ofdButton.setOnClick(() -> {
                Util.getOperatingSystem().open(localPath);
            });

            label = new WLabel(Text.literal(pathname), 0xFF888888);
            label.setVerticalAlignment(VerticalAlignment.BOTTOM);
            imgLoader.add(label, 0, 6, 3, 1);

        }

        {   //Action buttons
            WGridPanel actionPanel = new WGridPanel();
            root.add(actionPanel,15,0);
            
            // TODO: allw user to chose coordinates
            pasteButton = new WButton(Text.literal("Place into world"));
            actionPanel.add(pasteButton, 0, 0, 5, 1);
            pasteButton.setOnClick(() -> {
                this.pasteImage();
            });
            
            schemButton = new WButton(Text.literal("Save as schematic"));
            actionPanel.add(schemButton, 0, 1, 5, 1);
            schemButton.setOnClick(() -> {
                this.saveSchem();
            });
    
            WButton mapButton = new WButton(Text.literal("Print to map item"));
            actionPanel.add(mapButton, 0, 2, 5, 1);
            mapButton.setOnClick(() -> {
                this.saveMap();
            });
        }

        WButton paletteTypeButton = new WButton(Text.literal("Type: " + paletteTypes[paletteType]));
        root.add(paletteTypeButton, 0, 9, 5, 1);
        paletteTypeButton.setOnClick(() -> {
            paletteType++;
            paletteType = paletteType%paletteTypes.length;
            palette = null;
            if(paletteType == 2){
                // impossible to build
                pasteButton.setEnabled(false);
                schemButton.setEnabled(false);
            }else{
                pasteButton.setEnabled(true);
                schemButton.setEnabled(true);
            }
            paletteTypeButton.setLabel(Text.literal("Type: " + paletteTypes[paletteType]));
        });

        {
            WGridPanel ditherAssebly = new WGridPanel();
            root.add(ditherAssebly,7,9);

            WButton ditherButton = new WButton(Text.literal("Dither: " + ditherNames[ditherMethod]));
            ditherAssebly.add(ditherButton, 0, 0, 5, 1);
            ditherButton.setOnClick(() -> {
                ditherMethod++;
                if (ditherMethod >= ditherNames.length)
                    ditherMethod = 0;
                ditherButton.setLabel(Text.literal("Dither: " + ditherNames[ditherMethod]));
                this.previewImage();
            });

            ditherMinSlider = new WSlider(0,256,Axis.HORIZONTAL);
            ditherAssebly.add(ditherMinSlider, 0, 1,5,1);
        }

        // ditherMinSlider.setDraggingFinishedListener(val->{
        //     System.out.println(val);
        //     this.previewImage();
        // });

        ditherMinSlider.setValueChangeListener( val -> this.previewImage());





        WButton convertButton = new WButton(Text.literal("Convert"));
        root.add(convertButton, 8, 7, 5, 1);
        convertButton.setOnClick(() -> {
            if (picture == null || picture.getImage_texture()==null)
                return;
            if (palette == null) {
                palette = new Palette();
                palette.build(new boolean[63], paletteType);
                System.out.println("built palette");
            }
            NativeImage img = picture.getImage_texture().getImage();
            int targetWidth=img.getWidth(), targetHeight=img.getHeight();
            if(toggleResize.getToggle()){
                try {
                    float w = Float.parseFloat(widthDisplay.getText());
                    float h = Float.parseFloat(heightDisplay.getText());
                    if(unitSwitch.getToggle()){
                        w *= 128;
                        h *= 128;
                    }
                    targetWidth = (int)w;
                    targetHeight = (int)h;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }


            raw = new TetrahedrizedColor[targetWidth][targetHeight];
            for (int x = 0; x < targetWidth; ++x) {
                for (int y = 0; y < targetHeight; ++y) {
                    int color = img.getColor(x*img.getWidth()/targetWidth, y*img.getHeight()/targetHeight);
                    // double dither = new Random().nextDouble();

                    TetrahedrizedColor retT = palette.findBlockByColor(color);
                    if (retT == null)
                        retT = palette.apply(color);
                    if (retT == null) {
                        System.err.println(
                                "Invalid color: (0x" + Integer.toHexString(color) + " at coords " + x + ", " + y);
                        // img.setColor(x, y, 0xFF00FFFF);
                        continue;
                    }
                    raw[x][y] = retT;
                }
            }
            this.previewImage();
            System.out.println("done");
        });

        root.validate(this);
    }

    private void previewImage() {
        if (raw == null)
            return;        
        NativeImage dst = new NativeImage(raw.length,raw[0].length, false);
        for (int x = 0; x < dst.getWidth(); ++x) {
            int h = 192;
            for (int y = 0; y < dst.getHeight(); ++y) {
                TetrahedrizedColor retT = raw[x][y];

                if (retT == null) {
                    dst.setColor(x, y, 0xFF00FFFF);
                    continue;
                }

                double dither = 0.5;

                if (ditherMethod == 1) {
                    dither = dither_Bayer(x, y, 7);
                } else {
                    dither = dither_IGN(x, y, 7);
                }

                Point3D retP = retT.applyDither(dither, (float)(ditherMinSlider.getValue())/512.);
                int ret = retP.colorId;

                int i = 0;

                Block b = palette.blocksPerColor[ret].get(0);

                while (++i < palette.blocksPerColor[ret].size() && !b.getDefaultState().isFullCube(null, null)) {
                    b = palette.blocksPerColor[ret].get(i);
                }

                try {
                    if (retP.shade == Brightness.HIGH) {
                        if (h > 192)
                            h = 192;
                        else
                            h--;
                    }
                    if (retP.shade == Brightness.LOW) {
                        if (h < 192)
                            h = 192;
                        else
                            h++;
                    }
                    // System.out.println(b.getLootTableId().toShortTranslationKey().split("/")[1] +
                    // " " + b.getDefaultState().isFullCube(null, null));
                    dst.setColor(x, y, palette.COLORS[retP.colorId].getRenderColor(retP.shade));
                } catch (ArrayIndexOutOfBoundsException e) {
                }

            }
        }
        q_picture.updateTexture(new NativeImageBackedTexture(dst));
    }

    private Point3D ditheredVertexAt(int x, int y) {
        TetrahedrizedColor retT = raw[x][y];

        double dither = 0.5;

        if (ditherMethod == 1) {
            dither = dither_Bayer(x, y, 7);
        } else {
            dither = dither_IGN(x, y, 7);
        }

        return retT.applyDither(dither, (float)(ditherMinSlider.getValue())/512.);
    }

    // TODO: Allow centering offsets (optionnal?)
    // for both maps and world prints

    private void saveMap() {
        if (raw == null)
            return;

        NativeImage img = q_picture.getImage_texture().getImage();

        MapTileSet tileSet = new MapTileSet(raw.length, raw[0].length);

        for (int x = 0; x < raw.length; ++x) {

            for (int z = 0; z < raw[x].length; ++z) {

                Point3D retP = ditheredVertexAt(x, z);

                try {

                    tileSet.setColor(x, z, retP.colorId, retP.shade.id);

                } catch (ArrayIndexOutOfBoundsException e) {
                    // TODO investigate why it is needed
                }
            }
        }
        NbtCompound mapItem = tileSet.printToNbt(0, 0);
        try {
            // TODO increment map Ids until avail
            // Maybe some server side modding to accept the new map?
            MinecraftClient clientInstance = MinecraftClient.getInstance();
            ClientPlayerEntity player = clientInstance.player;
            IntegratedServer localServer = clientInstance.getServer();
            if(localServer!=null){
                String path = localServer.getSavePath(WorldSavePath.ROOT).getParent().toString() + File.separator + "data" + File.separator + "map_69.dat";
                NbtIo.write(mapItem, Paths.get(path));
                player.networkHandler.sendChatCommand("give @s minecraft:filled_map[minecraft:map_id=69]");
            }else{
                // multiplayer

                String savedMapsPath = System.getProperty("user.dir") + File.separator + "savedMaps";
                File f = new File(savedMapsPath);
                if(!f.exists()){
                    f.mkdir();
                }
                String path = savedMapsPath + File.separator + "map_69.dat";
                NbtIo.write(mapItem, Paths.get(path));
                player.sendMessage(Text.literal("WARNING: Can't directly add custom maps to server."), false);
                player.sendMessage(Text.literal("Saved as " + path), false);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Litematic schem = new Litematic(pathname, "Delaunator", "img", null);
        // schem.addRegion("Artwork", reg);
        // schem.save(Paths.get(System.getProperty("user.dir") + File.separator +
        // "schematics" + File.separator +"saved.litematic"));
        // System.out.println("Saved!");
        MinecraftClient clientInstance = MinecraftClient.getInstance();
        ClientPlayerEntity player = clientInstance.player;
    }

    private void saveSchem() {
        if (raw == null)
            return;

        int min_h = 62;
        int max_h = 319;
        int std_h = (max_h + min_h) / 2;

        int streaks_y[] = new int[raw[0].length];
        int streaks_o[][] = new int[raw.length][raw[0].length];

        for (int x = 0; x < raw.length; ++x) {
            int h = 0;
            int s = 0;
            Brightness lb = null;
            for (int z = 0; z < raw[x].length; ++z) {
                Point3D retP = ditheredVertexAt(x, z);
                if (lb != null && retP.shade != lb && retP.shade != Brightness.NORMAL) {
                    streaks_y[s++] = h;
                    h = 0;
                }
                if (retP.shade != Brightness.NORMAL) {
                    lb = retP.shade;
                    if (lb == Brightness.LOW)
                        h--;
                    if (lb == Brightness.HIGH)
                        h++;
                    if (lb == Brightness.LOWEST){
                        LOGGER.warn("Can't create schematic with full palette type.");
                        return;
                    }

                }

            }
            streaks_y[s++] = h;

            h = std_h;
            for (int z = 0; z < s; ++z) {
                h += streaks_y[z];
                streaks_o[x][z] = 0;
                if (h > max_h)
                    streaks_o[x][z] = max_h - h;
                if (h < min_h)
                    streaks_o[x][z] = min_h - h;
                h += streaks_o[x][z];
            }

        }

        int height = 384;
        Region reg = new Region(0, 0, 0, raw.length, height, raw[0].length);

        NativeImage img = q_picture.getImage_texture().getImage();

        for (int x = 0; x < raw.length; ++x) {
            Brightness lb = null;

            int h = std_h;

            int streak_index = 0;

            int offset = streaks_o[x][streak_index];

            h += offset + Integer.signum(offset);

            offset = 0;

            for (int z = 0; z < raw[x].length; ++z) {

                Point3D retP = ditheredVertexAt(x, z);
                int colorid = retP.colorId;

                int i = 0;

                if (retP.shade != lb && retP.shade != Brightness.NORMAL) {
                    ++streak_index;
                    offset = streaks_o[x][streak_index];
                }
                // if(offset!=0)
                // System.out.println("o: " + offset);

                Block b = palette.blocksPerColor[colorid].get(0);
                while (!b.getDefaultState().isFullCube(null, null) && ++i < palette.blocksPerColor[colorid].size()) {
                    // System.out.println(palette.blocksPerColor[colorid].toString());
                    b = palette.blocksPerColor[colorid].get(i);
                }
                boolean waterlogged = colorid == 12;
                if (waterlogged) {
                    b = Blocks.OAK_LEAVES;
                }

                // System.out.println(b.getLootTableId().toShortTranslationKey());
                try {

                    if (retP.shade == Brightness.LOW) {
                        h--;
                        h += offset;
                        offset = 0;
                    }
                    if (retP.shade == Brightness.HIGH) {
                        h++;
                        h += offset;
                        offset = 0;
                    }

                    // TODO: non-default state?
                    reg.addBlock(x, h + 64, z, b.getDefaultState());

                } catch (ArrayIndexOutOfBoundsException e) {
                    // TODO investigate why it is needed
                }
                if (retP.shade != Brightness.NORMAL)
                    lb = retP.shade;

            }
        }

        Litematic schem = new Litematic(pathname, "Delaunator", "img", null);
        schem.addRegion("Artwork", reg);
        schem.save(Paths.get(
                System.getProperty("user.dir") + File.separator + "schematics" + File.separator + "saved.litematic"));
        System.out.println("Saved!");
    }

    private void pasteImage() {
        if (raw == null)
            return;

        int min_h = 62;
        int max_h = 319;
        int std_h = (max_h + min_h) / 2;

        int streaks_y[] = new int[raw[0].length];
        int streaks_o[][] = new int[raw.length][raw[0].length];

        for (int x = 0; x < raw.length; ++x) {
            int h = 0;
            int s = 0;
            Brightness lb = null;
            for (int z = 0; z < raw[x].length; ++z) {
                Point3D retP = ditheredVertexAt(x, z);
                if (lb != null && retP.shade != lb && retP.shade != Brightness.NORMAL) {
                    streaks_y[s++] = h;
                    h = 0;
                }
                if (retP.shade != Brightness.NORMAL) {
                    lb = retP.shade;
                    if (lb == Brightness.LOW)
                        h--;
                    if (lb == Brightness.HIGH)
                        h++;
                    if (lb == Brightness.LOWEST){
                        LOGGER.warn("Can't place blocks with full palette type.");
                        return;
                    }
                }

            }
            streaks_y[s++] = h;

            h = std_h;
            for (int z = 0; z < s; ++z) {
                h += streaks_y[z];
                streaks_o[x][z] = 0;
                if (h > max_h)
                    streaks_o[x][z] = max_h - h;
                if (h < min_h)
                    streaks_o[x][z] = min_h - h;
                h += streaks_o[x][z];
            }

        }

        int map_x = 0;
        int map_y = 0;
        NativeImage img = q_picture.getImage_texture().getImage();
        MinecraftClient clientInstance = MinecraftClient.getInstance();
        ClientPlayerEntity player = clientInstance.player;

        for (int x = 0; x < raw.length; ++x) {
            Brightness lb = null;

            int h = std_h;

            int streak_index = 0;

            int offset = streaks_o[x][streak_index];

            h += offset + Integer.signum(offset);

            offset = 0;

            // TODO: edit map content directly if possible
            player.networkHandler.sendChatCommand("setblock " + (map_x * 128 + (x - 64)) + " " + h + " "
                    + (map_y * 128 + (-65)) + " minecraft:cobblestone");
            for (int z = 0; z < raw[x].length; ++z) {

                Point3D retP = ditheredVertexAt(x, z);
                int colorid = retP.colorId;

                int i = 0;

                if (retP.shade != lb && retP.shade != Brightness.NORMAL) {
                    ++streak_index;
                    offset = streaks_o[x][streak_index];
                }
                // if(offset!=0)
                // System.out.println("o: " + offset);

                Block b = palette.blocksPerColor[colorid].get(0);
                while (!b.getDefaultState().isFullCube(null, null) && ++i < palette.blocksPerColor[colorid].size()) {
                    // System.out.println(palette.blocksPerColor[colorid].toString());
                    b = palette.blocksPerColor[colorid].get(i);
                }
                boolean waterlogged = colorid == 12;
                if (waterlogged) {
                    b = Blocks.OAK_LEAVES;
                }

                // System.out.println(b.getLootTableId().toShortTranslationKey());
                try {

                    if (retP.shade == Brightness.LOW) {
                        h--;
                        h += offset;
                        offset = 0;
                    }
                    if (retP.shade == Brightness.HIGH) {
                        h++;
                        h += offset;
                        offset = 0;
                    }
                    // System.out.println("h: " + h);
                    // if(retP.shade != lb && retP.shade!=Brightness.NORMAL){
                    // endh_1 = h + streaks_y[x][y];
                    // endh_2 = endh_1;
                    // offset = streaks_z[x][y];
                    // if(offset<raw[x].length){
                    // endh_2 += streaks_y[x][offset];
                    // }
                    // if(endh_2<min_h)h += min_h-endh_2;
                    // if(endh_2>max_h)h += max_h-endh_2;
                    // }

                    // if( retP.shade!=Brightness.NORMAL){
                    // if(rem>max_h && retP.shade!=lb) h = max_h-streaks[x][0];
                    // if(rem<min_h && retP.shade!=lb) h = min_h-streaks[x][0];
                    // }

                    // h = (h-min_h)%(max_h-min_h+1) + min_h;

                    String persistant = (b.getLootTableKey().getValue().toString().contains("leaves")
                            ? "[persistent=true" + (waterlogged ? ",waterlogged=true" : "") + "]"
                            : ""); // leaves are disappearing
                    Identifier id = b.getLootTableKey().getValue();
                    String id_namespace = id.getNamespace();
                    String id_path = id.getPath().split("/")[1];
                    // System.out.println(id_namespace + ":" + id_path);
                    String command = "setblock " + (map_x * 128 + (x - 64)) + " " + h + " " + (map_y * 128 + (z - 64))
                            + " " + id_namespace + ":" + id_path + persistant;
                    player.networkHandler.sendChatCommand(command);
                    // player.sendMessage(Text.of(command));

                    // return;
                } catch (ArrayIndexOutOfBoundsException e) {
                    // TODO investigate why it is needed
                }
                if (retP.shade != Brightness.NORMAL)
                    lb = retP.shade;

            }
        }
    }
}
