package com.yori3o.boss_checklist.common.mixin;


import com.yori3o.boss_checklist.common.client.gui.BossChecklistScreen;
import com.yori3o.boss_checklist.common.client.gui.GuiConstants;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomButton;
import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * This mixin adds an open button to the pause menu.
 */
@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {


    protected PauseScreenMixin() {
        super(Component.literal("Pause menu"));
    }

    @Inject(
        method = "init()V", 
        at = @At(value = "TAIL")
    )
    private void BossChecklist$addBossChecklistButton(CallbackInfo ci) {
        if (DynamicConfigHandler.client().openButtonEnabled) {
            if (((PauseScreen)(Object)this).showsPauseMenu()) {
            
                int x = this.width / 2 + 107;
                int y = (this.height / 4 + 26) + DynamicConfigHandler.client().openButtonYOffset;

                CustomButton openButton = new CustomButton(
                    x, 
                    y, 
                    20, 
                    20, 
                    0, 
                    0, 
                    Component.empty(), 
                    GuiConstants.OPEN_BUTTON_TEXTURE, 
                    GuiConstants.OPEN_BUTTON_TEXTURE_hovered, 
                    GuiConstants.OPEN_BUTTON_TEXTURE_hovered, 
                    null, 
                    () -> {
                        Minecraft.getInstance().setScreen(new BossChecklistScreen(true));
                    }
                );

                this.addRenderableWidget(openButton);
            }
        }
    }
}

