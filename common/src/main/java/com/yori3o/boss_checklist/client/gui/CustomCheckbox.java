package com.yori3o.boss_checklist.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
//import com.yori3o.boss_checklist.BossChecklist;
import com.yori3o.boss_checklist.BossChecklistClient;
//import com.yori3o.boss_checklist.PlatformUtils;

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
    private final Consumer<Boolean> onValueChange;
    private final Runnable onLabelClick;

    private static final ResourceLocation CHECKMARK_TEXTURE = ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/checkmark.png");
    private static final ResourceLocation CHECKBOX_TEXTURE = ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/checkbox.png");

    private static final float SCALE = 0.5f;      // checkboxes scale
    private static final float TEXT_SCALE = 1.0f; 
    private static final int LOGICAL_BOX = 16;    
    private static final int PADDING = 4;         // between box and text

    public CustomCheckbox(int x, int y, Component label, boolean selected,
                         Consumer<Boolean> onValueChange, Runnable onLabelClick) {

        super(x, y,
              (int) (LOGICAL_BOX * SCALE + Minecraft.getInstance().font.width(label) * TEXT_SCALE + PADDING * 2),
              (int) (LOGICAL_BOX * SCALE),
            label);

        this.selected = selected;
        this.onValueChange = onValueChange;
        this.onLabelClick = onLabelClick;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int sx = this.getX();
        int sy = this.getY();
        int boxScreen = (int) (LOGICAL_BOX * SCALE);

        boolean hoverBox = mouseX >= sx && mouseY >= sy && mouseX < sx + boxScreen && mouseY < sy + boxScreen;
        int labelStart = sx + boxScreen + PADDING;
        int labelLen = (int) (Minecraft.getInstance().font.width(this.getMessage()) * TEXT_SCALE);
        boolean hoverLabel = mouseX >= labelStart && mouseY >= sy && mouseX < labelStart + labelLen && mouseY < sy + boxScreen;

        // ==== draw box ====
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(sx, sy, 0);
        guiGraphics.pose().scale(SCALE, SCALE, 1f);

        guiGraphics.blit(CHECKBOX_TEXTURE, 0, 0, 0, 0, LOGICAL_BOX, LOGICAL_BOX, LOGICAL_BOX, LOGICAL_BOX);

        int box = LOGICAL_BOX;
        int inner = hoverBox ? 0x54FFFFFF : 0x00000000;
        guiGraphics.fill(0, 0, box, box, inner);

        // checkmark
        if (this.selected) {
            RenderSystem.enableBlend();
            guiGraphics.blit(CHECKMARK_TEXTURE, -2, -2, 0, 0, 20, 20, 20, 20);
        }

        guiGraphics.pose().popPose();

        // ==== draw text ====
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(labelStart, sy, 0);
        guiGraphics.pose().scale(TEXT_SCALE, TEXT_SCALE, 1f);

        int textColor = this.selected ? 0xFF209920 : 0xFFAA5555;

        if (hoverLabel) {
            textColor = this.selected ? 0xFF4DBD4D : 0xFFD69494;
        }

        guiGraphics.drawString(Minecraft.getInstance().font, this.getMessage(), 0, 0, textColor, false);

        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;

        int sx = this.getX();
        int sy = this.getY();
        int boxScreen = (int) (LOGICAL_BOX * SCALE);

        // click on box
        if (mouseX >= sx && mouseY >= sy && mouseX < sx + boxScreen && mouseY < sy + boxScreen) {
            this.selected = !this.selected;
            if (this.onValueChange != null) this.onValueChange.accept(this.selected);

            // FOR 1.21.1+

            if (this.selected) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(BossChecklistClient.CHECKMARK_ADDED, 1.0F));
            } else {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(BossChecklistClient.CHECKMARK_DELETED, 1.0F));
            }

            // FOR 1.20.1-
            
            /*if (this.selected) {
                if (Platform.isForge()) {
                    Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI(PlatformUtils.getSound(new ResourceLocation("boss_checklist", "checkmark_added")), 1.0F));
                } else {
                   Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI(BossChecklistClient.CHECKMARK_ADDED, 1.0F));
                } 
            } else if (Platform.isForge()) {
                    Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI(PlatformUtils.getSound(new ResourceLocation("boss_checklist", "checkmark_deleted")), 1.0F));
                } else {
                    Minecraft.getInstance().getSoundManager().play((SoundInstance)SimpleSoundInstance.forUI(BossChecklistClient.CHECKMARK_DELETED, 1.0F));
            } */

            return true;
        }

        // click on text
        int labelStart = sx + boxScreen + PADDING;
        int labelLen = (int) (Minecraft.getInstance().font.width(this.getMessage()) * TEXT_SCALE);
        if (mouseX >= labelStart && mouseY >= sy && mouseX < labelStart + labelLen && mouseY < sy + boxScreen) {
            if (this.onLabelClick != null) {
                this.onLabelClick.run();
                return true;
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