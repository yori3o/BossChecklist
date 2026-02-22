package com.yori3o.boss_checklist.common.client.gui.widget;


import com.yori3o.boss_checklist.common.client.gui.GuiConstants;
import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;
import com.yori3o.boss_checklist.common.sound.SoundRegistry;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Consumer;



public class CustomCheckbox extends AbstractWidget {

    
    private boolean selected;
    private final boolean isLabelClickDoingAnything;
    private final Consumer<Boolean> onValueChange;
    private final Runnable onLabelClick;

    private static final float SCALE = 0.5f;      // checkboxes scale
    private static final float TEXT_SCALE = 1.0f; 
    private static final int LOGICAL_BOX = 16;    
    private static final int PADDING = 4;         // between box and text

    private final int MAX_LENGTH;

    private boolean renderNotice = false;

    private final int labelLen;

    public boolean isAnimating = false;
    public boolean animationPlayed = true;
    public long animationStartTime = 0L;
    private static final int ANIMATION_DURATION_MS = 105;
    private static final int FRAME_COUNT = 6;


    public CustomCheckbox(int x, int y, int maxLength, Component label, boolean selected, boolean isLabelClickDoingAnything, boolean renderNotice,
                         Consumer<Boolean> onValueChange, Runnable onLabelClick) {

        super(x, y,
              (int) (LOGICAL_BOX * SCALE + Minecraft.getInstance().font.width(label) * TEXT_SCALE + PADDING * 2),
              (int) (LOGICAL_BOX * SCALE),
            label);

        this.selected = selected;
        this.isLabelClickDoingAnything = isLabelClickDoingAnything;
        this.onValueChange = onValueChange;
        this.onLabelClick = onLabelClick;
        this.renderNotice = renderNotice;
        this.MAX_LENGTH = maxLength;

        labelLen = (int) (Minecraft.getInstance().font.width(this.getMessage()) * TEXT_SCALE);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int sx = this.getX();
        int sy = this.getY();
        int boxScreen = (int) (LOGICAL_BOX * SCALE);

        boolean hoverBox = mouseX >= sx && mouseY >= sy && mouseX < sx + boxScreen && mouseY < sy + boxScreen;
        int labelStart = sx + boxScreen + PADDING;
        
        //int lines = (int) labelLen / MAX_LENGTH;
        boolean hoverLabel = (mouseX >= labelStart && mouseY >= sy /*(lines * 16)*/ && mouseX < labelStart + labelLen && mouseY < sy + boxScreen) || this.isFocused();

        // --- draw box ---
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(sx, sy, 0);
        guiGraphics.pose().scale(SCALE, SCALE, 1f);

        RenderSystem.enableBlend();

        if (renderNotice) {
            guiGraphics.blit(GuiConstants.NOTICE, -19, -19, 0, 0, 54, 54, 54, 54);
        }

        guiGraphics.blit(GuiConstants.CHECKBOX, 0, 0, 0, 0, LOGICAL_BOX, LOGICAL_BOX, LOGICAL_BOX, LOGICAL_BOX);

        int box = LOGICAL_BOX;
        int inner = hoverBox ? 0x54FFFFFF : 0x00000000;
        guiGraphics.fill(0, 0, box, box, inner);

        // checkmark
        if (this.selected) {
            if (DynamicConfigHandler.client().animationsEnabled) {
                RenderSystem.enableBlend();

                int frameIndex = FRAME_COUNT - 1; // last frame by default

                if (isAnimating && !animationPlayed) {
                    long elapsed = System.currentTimeMillis() - animationStartTime;
                    float progress = (float) elapsed / ANIMATION_DURATION_MS;

                    if (progress >= 1.0f) {
                        progress = 1.0f;
                        isAnimating = false;
                        animationPlayed = true;
                    }

                    frameIndex = Math.min((int) (progress * FRAME_COUNT), FRAME_COUNT - 1);
                }

                // We insert a frame: checkmark_0.png, checkmark_1.png, ...
                ResourceLocation frameTexture = ResourceLocation.fromNamespaceAndPath(
                    "boss_checklist",
                    "textures/gui/checkmark/checkmark_" + frameIndex + ".png"
                );

                guiGraphics.blit(frameTexture, -2, -2, 0, 0, 20, 20, 20, 20);
            } else {
                RenderSystem.enableBlend();
                guiGraphics.blit(ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/checkmark/checkmark_5.png"), -2, -2, 0, 0, 20, 20, 20, 20);
            }
        }


        guiGraphics.pose().popPose();

        // ==== draw text ====
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(labelStart, sy, 0);
        guiGraphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1f);

        int textColor;
        if (isLabelClickDoingAnything) {
            textColor = this.selected ? 0xFF209920 : 0xFFAA5555;

            if (hoverLabel) {
                textColor = this.selected ? 0xFF4DBD4D : 0xFFD69494;
            }
        } else {
            textColor = 0xFF323538;
        }

        guiGraphics.drawWordWrap(Minecraft.getInstance().font, this.getMessage(), 0, 0, MAX_LENGTH, textColor);

        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        if (!this.active && !this.visible) return false;

        int sx = this.getX();
        int sy = this.getY();
        int boxScreen = (int) (LOGICAL_BOX * SCALE);

        // click on box
        if (mouseX >= sx && mouseY >= sy && mouseX < sx + boxScreen && mouseY < sy + boxScreen) {
            this.selected = !this.selected;
            if (this.onValueChange != null) this.onValueChange.accept(this.selected);

            renderNotice = false;

            if (this.selected) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundRegistry.CHECKMARK_ADDED, 1.0F));
                if (DynamicConfigHandler.client().animationsEnabled) {
                    this.isAnimating = true;
                    this.animationPlayed = false;
                    this.animationStartTime = System.currentTimeMillis();
                }
            } else {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundRegistry.CHECKMARK_DELETED, 1.0F));
                if (DynamicConfigHandler.client().animationsEnabled) {
                    this.isAnimating = false;
                    this.animationPlayed = true;
                }
            }

            this.setFocused(false);

            return true;
        }

        // click on text
        int labelStart = sx + boxScreen + PADDING;
        int labelLen = (int) (Minecraft.getInstance().font.width(this.getMessage()) * TEXT_SCALE);
        if (mouseX >= labelStart && mouseY >= sy && mouseX < labelStart + labelLen && mouseY < sy + boxScreen) {
            if (this.onLabelClick != null) {
                onClick(mouseX, mouseY);
                this.setFocused(false);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        renderNotice = false;
        this.onLabelClick.run();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        if (InputConstants.KEY_RETURN == keyCode) {
            if (this.isFocused()) {
                if (this.onLabelClick != null) {
                    onClick(scanCode, modifiers);
                }
            }
        }
        return false;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean s) {
        this.selected = s;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}