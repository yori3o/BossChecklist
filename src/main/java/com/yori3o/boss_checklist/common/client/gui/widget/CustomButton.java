package com.yori3o.boss_checklist.common.client.gui.widget;


 
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;



public class CustomButton extends Button {

    
    private final Identifier normalTex;
    private final Identifier hoverTex;
    private final Identifier pressedTex;
    private final Identifier overlayTex;

    private final int overlayWidth;
    private final int overlayHeight;

    private Identifier tex;
    private boolean pressedFlag = false;
    

    public CustomButton(
            int x, int y, int width, int height, int overlayWidth, int overlayHeight,
            Component label,
            Identifier normalTex,
            Identifier hoverTex,
            Identifier pressedTex,
            Identifier overlayTex,
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
    public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {

        if (pressedFlag) {
            tex = pressedTex;
        } else if (this.isHoveredOrFocused()) {
            tex = hoverTex;
        } else {
            tex = normalTex;
        }


        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, tex, this.getX(), this.getY(), 0, 0, this.width, this.height, this.width, this.height);
        
        if (overlayTex != null)
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, 
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

            guiGraphics.text(Minecraft.getInstance().font, text, textX, textY, 0xFFFFFFFF, false);
        }
    }

    @Override
    public void onPress(InputWithModifiers inputWithModifiers) {
        super.onPress(inputWithModifiers); 
    }

    @Override
    public void onClick(MouseButtonEvent mouseButtonEvent, boolean bool) {
        pressedFlag = true;
        this.onPress(null);
    }

    @Override
    public void onRelease(MouseButtonEvent mouseButtonEvent) {
        pressedFlag = false;
        this.setFocused(false);
    }

}