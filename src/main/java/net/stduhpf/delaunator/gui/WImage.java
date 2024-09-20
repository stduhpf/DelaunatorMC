package net.stduhpf.delaunator.gui;

import org.joml.Matrix4f;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.cottonmc.cotton.gui.widget.WWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;

public class WImage extends WWidget {

    private NativeImageBackedTexture image_texture;

    public NativeImageBackedTexture getImage_texture() {
        return image_texture;
    }

    private int imageWidth, imageHeight;
    private int max_w, max_h;
    private int offsetX = 0, offsetY = 0;

    public int getImageHeight() {
        return imageHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public WImage(NativeImageBackedTexture texture, int max_w, int max_h) {
        this.max_h = max_h;
        this.max_w = max_w;
        this.updateTexture(texture);
    }

    public WImage(NativeImageBackedTexture texture) {
        this(texture, 128, 128);
    }

    public void updateTexture(NativeImageBackedTexture texture) {
        this.image_texture = texture;
        
        this.x -= offsetX;
        this.y -= offsetY;
        this.offsetX = 0;
        this.offsetY = 0;
        if(image_texture==null)
            return;
        this.imageWidth = image_texture.getImage().getWidth();
        this.imageHeight = image_texture.getImage().getHeight();

        float w_scale = (float) max_w / (float) this.imageWidth;
        float h_scale = (float) max_h / (float) this.imageHeight;
        float scale = Math.min(w_scale, h_scale);

        this.imageWidth = (int) (scale * (float) this.imageWidth);
        this.imageHeight = (int) (scale * (float) this.imageHeight);

        this.offsetX = 1 + (max_w - this.imageWidth) / 2;
        this.offsetY = 1 + (max_h - this.imageHeight) / 2;

        System.out.println("Offset: " +offsetX + ", " + offsetY );

        this.x += offsetX;
        this.y += offsetY;
    }

    private static void blit(MatrixStack matrices, int x, int y, int width, int height, float u1, float v1, float u2,
            float v2) {
        Tessellator tessellator = Tessellator.getInstance();
        Matrix4f model = matrices.peek().getPositionMatrix();
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(model, x, y + height, 0).texture(u1, v2);
        buffer.vertex(model, x + width, y + height, 0).texture(u2, v2);
        buffer.vertex(model, x + width, y, 0).texture(u2, v1);
        buffer.vertex(model, x, y, 0).texture(u1, v1);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void paint(DrawContext ctx, int x, int y, int mouseX, int mouseY) {
        if(image_texture==null)
            return;
        RenderSystem.setShaderTexture(0, image_texture.getGlId());
        RenderSystem.enableBlend();
        blit(ctx.getMatrices(), x, y, (int) (imageWidth), (int) (imageHeight), 0, 0, 1, 1);
        RenderSystem.disableBlend();
    }

}
