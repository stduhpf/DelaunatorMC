package net.stduhpf.delaunator.gui;


import io.github.cottonmc.cotton.gui.client.CottonClientScreen;


// import net.minecraft.client.render.;

public class DeScreen extends CottonClientScreen {

    public DeScreen(DeGui description) {
        super(description);
        description.screen = this;
    }

}
