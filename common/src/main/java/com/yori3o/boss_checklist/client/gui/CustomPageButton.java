package com.yori3o.boss_checklist.client.gui;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

public class CustomPageButton extends Button {
    private static final ResourceLocation BUTTON_TEXTURE_backward =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/sprites/widget/page_backward.png");
    private static final ResourceLocation BUTTON_TEXTURE_backward_highlighted =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/sprites/widget/page_backward_highlighted.png");
    private static final ResourceLocation BUTTON_TEXTURE_forward =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/sprites/widget/page_forward.png");
    private static final ResourceLocation BUTTON_TEXTURE_forward_highlighted =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/sprites/widget/page_forward_highlighted.png");

    private final ResourceLocation normalTex; 
    private final ResourceLocation hoverTex; 
    @SuppressWarnings("unused")
    private final boolean left;

    ResourceLocation tex;

    public CustomPageButton(int x, int y, boolean left, Runnable onPressAction) {
        super(x, y, 23, 13, Component.empty(), b -> onPressAction.run(), Button.DEFAULT_NARRATION);
        this.left = left;

        if (left) {
            this.normalTex = BUTTON_TEXTURE_backward;
            this.hoverTex  = BUTTON_TEXTURE_backward_highlighted;
        } else {
            this.normalTex = BUTTON_TEXTURE_forward;
            this.hoverTex  = BUTTON_TEXTURE_forward_highlighted;
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        tex = this.isHovered() ? hoverTex : normalTex;
        guiGraphics.blit(tex, this.getX(), this.getY(), 0, 0, 23, 13, 23, 13);
    }

    @Override
    public void playDownSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
    }
}