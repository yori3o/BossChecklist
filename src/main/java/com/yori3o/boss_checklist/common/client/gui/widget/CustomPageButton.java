package com.yori3o.boss_checklist.common.client.gui.widget;


import com.yori3o.boss_checklist.common.client.gui.GuiConstants;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;



public class CustomPageButton extends Button {

    
    private final Identifier normalTex; 
    private final Identifier hoverTex; 

    private Identifier tex;


    public CustomPageButton(int x, int y, boolean left, Runnable onPressAction) {
        super(x, y, 23, 13, Component.empty(), b -> onPressAction.run(), Button.DEFAULT_NARRATION);

        if (left) {
            this.normalTex = GuiConstants.PAGE_BUTTON_TEXTURE_backward;
            this.hoverTex  = GuiConstants.PAGE_BUTTON_TEXTURE_backward_highlighted;
        } else {
            this.normalTex = GuiConstants.PAGE_BUTTON_TEXTURE_forward;
            this.hoverTex  = GuiConstants.PAGE_BUTTON_TEXTURE_forward_highlighted;
        }
    }

    @Override
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        tex = this.isHoveredOrFocused() ? hoverTex : normalTex;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, tex, this.getX(), this.getY(), 0, 0, 23, 13, 23, 13);
    }

    @Override
    public void playDownSound(SoundManager handler) {
        handler.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
    }
}