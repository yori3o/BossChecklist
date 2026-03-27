package com.yori3o.boss_checklist.common.client.gui.widget;


import com.yori3o.boss_checklist.common.client.gui.GuiConstants;
import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;
import com.yori3o.boss_checklist.common.sound.SoundRegistry;
import com.mojang.blaze3d.platform.InputConstants;
 
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

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

    public boolean avoidFocusedLogic = false; // TODO: Bring order, in particular with the rendering logic
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
    public void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        int sx = this.getX();
        int sy = this.getY();
        int boxScreen = (int) (LOGICAL_BOX * SCALE);

        boolean hoverBox = (!avoidFocusedLogic && this.isFocused() && onLabelClick == null) || (mouseX >= sx && mouseY >= sy && mouseX < sx + boxScreen && mouseY < sy + boxScreen);
        int labelStart = sx + boxScreen + PADDING;
        
        boolean hoverLabel = (!avoidFocusedLogic && this.isFocused()) || (mouseX >= labelStart && mouseY >= sy && mouseX < labelStart + labelLen && mouseY < sy + boxScreen);

        // --- draw box ---
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(sx, sy);
        guiGraphics.pose().scale(SCALE);

         

        if (renderNotice) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.NOTICE, -19, -19, 0, 0, 54, 54, 54, 54);
        }

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.CHECKBOX, 0, 0, 0, 0, LOGICAL_BOX, LOGICAL_BOX, LOGICAL_BOX, LOGICAL_BOX);

        int box = LOGICAL_BOX;
        int inner = hoverBox ? 0x54FFFFFF : 0x00000000;
        guiGraphics.fill(0, 0, box, box, inner);

        // checkmark
        if (this.selected) {
            if (DynamicConfigHandler.client().animationsEnabled) {
                 

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
                Identifier frameTexture = Identifier.fromNamespaceAndPath(
                    "boss_checklist",
                    "textures/gui/checkmark/checkmark_" + frameIndex + ".png"
                );

                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, frameTexture, -2, -2, 0, 0, 20, 20, 20, 20);
            } else {
                 
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/checkmark/checkmark_5.png"), -2, -2, 0, 0, 20, 20, 20, 20);
            }
        }


        guiGraphics.pose().popMatrix();

        // ==== draw text ====
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(labelStart, sy);
        guiGraphics.pose().scale(TEXT_SCALE, TEXT_SCALE);

        int textColor;
        if (isLabelClickDoingAnything) {
            if (hoverLabel) {
                textColor = this.selected ? 0xFF4DBD4D : 0xFFD69494;
            } else {
                textColor = this.selected ? 0xFF209920 : 0xFFAA5555;
            }
        } else {
            textColor = 0xFF323538;
        }

        guiGraphics.textWithWordWrap(Minecraft.getInstance().font, this.getMessage(), 0, 0, MAX_LENGTH, textColor, false);

        guiGraphics.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        int button = mouseButtonEvent.button();
        int mouseX = (int) mouseButtonEvent.x();
        int mouseY = (int) mouseButtonEvent.y();
        if (button != 0) return false;
        if (!this.active && !this.visible) return false;

        int sx = this.getX();
        int sy = this.getY();
        int boxScreen = (int) (LOGICAL_BOX * SCALE);

        // click on box
        if (mouseX >= sx && mouseY >= sy && mouseX < sx + boxScreen && mouseY < sy + boxScreen) {
            invertValue();
            avoidFocusedLogic = true;
            return true;
        }

        // click on text
        int labelStart = sx + boxScreen + PADDING;
        int labelLen = (int) (Minecraft.getInstance().font.width(this.getMessage()) * TEXT_SCALE);
        if (mouseX >= labelStart && mouseY >= sy && mouseX < labelStart + labelLen && mouseY < sy + boxScreen) {
            if (this.onLabelClick != null) {
                onClick(null, true);
                avoidFocusedLogic = true;
                return true;
            }
        }

        return false;
    }

    @Override
    public void onClick(MouseButtonEvent mouseButtonEvent, boolean bool) {
        renderNotice = false;
        this.onLabelClick.run();
    }

    public void invertValue() {
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
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (InputConstants.KEY_RETURN == keyEvent.key()) {
            if (this.isFocused() && !avoidFocusedLogic) {
                if (this.onLabelClick != null) {
                    onClick(null, true);
                } else if (this.onValueChange != null) {
                    invertValue();
                }
            }
        }
        return super.keyPressed(keyEvent);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean s) {
        this.selected = s;
    }

    @Override
    public void onRelease(MouseButtonEvent mouseButtonEvent) {
        avoidFocusedLogic = false;
        this.setFocused(false);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}