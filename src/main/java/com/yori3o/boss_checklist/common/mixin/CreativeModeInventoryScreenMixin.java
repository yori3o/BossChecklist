package com.yori3o.boss_checklist.common.mixin;


import com.yori3o.boss_checklist.common.client.gui.BossChecklistScreen;
import com.yori3o.boss_checklist.common.client.gui.GuiConstants;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomButton;
import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * This mixin adds an open button to the pause menu.
 */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends Screen {


    protected CreativeModeInventoryScreenMixin() {
        super(Component.literal("Creative inventory screen"));
    }

    @Inject(
        method = "init()V", 
        at = @At(value = "TAIL")
    )
    private void BossChecklist$addBossChecklistButton(CallbackInfo ci) {
        if (DynamicConfigHandler.client().inventoryOpenButtonEnabled) {
            int x;
            int y;
            if (DynamicConfigHandler.client().alignInventoryButtonToCenter) {
                x = this.width / 2 + DynamicConfigHandler.client().inventoryOpenButtonXOffset;
                y = this.height / 2 + DynamicConfigHandler.client().inventoryOpenButtonYOffset;
            } else {
                x = DynamicConfigHandler.client().inventoryOpenButtonXOffset;
                y = DynamicConfigHandler.client().inventoryOpenButtonYOffset;
            }

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
                    Minecraft.getInstance().gui.setScreen(new BossChecklistScreen(this));
                }
            );

            this.addRenderableWidget(openButton);
        }
    }
}

