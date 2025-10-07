package com.yori3o.boss_checklist.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CustomButton extends Button {

    private final ResourceLocation normalTex;
    private final ResourceLocation hoverTex;
    private final ResourceLocation pressedTex;

    ResourceLocation tex;
    private boolean pressedFlag = false;

    public CustomButton(
            int x, int y, int width, int height,
            Component label,
            ResourceLocation normalTex,
            ResourceLocation hoverTex,
            ResourceLocation pressedTex,
            Runnable onPressAction
    ) {
        super(x, y, width, height, label, b -> onPressAction.run(), Button.DEFAULT_NARRATION);
        this.normalTex = normalTex;
        this.hoverTex = hoverTex;
        this.pressedTex = pressedTex;
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

        guiGraphics.blit(tex, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);

        int textWidth = Minecraft.getInstance().font.width(this.getMessage());
        int textX = this.getX() + (this.width - textWidth) / 2;
        int textY = this.getY() + (this.height - 8) / 2; // 8 = высота строки шрифта

        guiGraphics.drawString(Minecraft.getInstance().font, this.getMessage(), textX, textY, 0xFFFFFF, false);
    }

    @Override
    public void onPress() {
        pressedFlag = true;
        super.onPress(); 
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        pressedFlag = false;
    }

    
    // can make a custom sound
    //@Override
    //public void playDownSound(SoundManager handler) {
    //    handler.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
    //}
}