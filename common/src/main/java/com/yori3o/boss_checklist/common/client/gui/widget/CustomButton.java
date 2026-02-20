package com.yori3o.boss_checklist.common.client.gui.widget;


import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;



public class CustomButton extends Button {

    
    private final ResourceLocation normalTex;
    private final ResourceLocation hoverTex;
    private final ResourceLocation pressedTex;
    private final ResourceLocation overlayTex;

    private final int overlayWidth;
    private final int overlayHeight;

    ResourceLocation tex;
    private boolean pressedFlag = false;
    

    public CustomButton(
            int x, int y, int width, int height, int overlayWidth, int overlayHeight,
            Component label,
            ResourceLocation normalTex,
            ResourceLocation hoverTex,
            ResourceLocation pressedTex,
            ResourceLocation overlayTex,
            Runnable onPressAction
    ) {
        super(x, y, width, height, label, b -> onPressAction.run(), Button.DEFAULT_NARRATION);
        this.normalTex = normalTex;
        this.hoverTex = hoverTex;
        this.pressedTex = pressedTex;
        this.overlayTex = overlayTex;
        this.overlayWidth = overlayWidth;
        this.overlayHeight = overlayHeight;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {

        if (pressedFlag) {
            tex = pressedTex;
        } else if (this.isHovered) {
            tex = hoverTex;
        } else {
            tex = normalTex;
        }

        RenderSystem.enableBlend();

        guiGraphics.blit(tex, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
        
        if (overlayTex != null)
            guiGraphics.blit(
                overlayTex, 
                this.getX() - (int) (0.5 * (overlayWidth - width)), 
                this.getY() - (int) (0.5 * (overlayHeight - height)), 
                0, 0, 
                overlayWidth, overlayHeight, overlayWidth, overlayHeight
            );

        Component text = this.getMessage();
        if (text != null) {
            int textWidth = Minecraft.getInstance().font.width(text);
            int textX = this.getX() + (this.width - textWidth) / 2;
            int textY = this.getY() + (this.height - 8) / 2;

            guiGraphics.drawString(Minecraft.getInstance().font, text, textX, textY, 0xFFFFFFFF, false);
        }
    }

    @Override
    public void onPress() {
        pressedFlag = true;
        super.onPress(); 
    }

    // on 1.20 it run only if cursor on button
    @Override
    public void onRelease(double mouseX, double mouseY) {
        pressedFlag = false;
    }

}