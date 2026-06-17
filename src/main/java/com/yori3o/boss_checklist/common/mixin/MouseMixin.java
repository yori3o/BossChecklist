package com.yori3o.boss_checklist.common.mixin;


import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.yori3o.boss_checklist.common.client.gui.BossChecklistScreen;
import com.yori3o.boss_checklist.common.client.gui.EditorScreen;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomCheckbox;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomNumberEditBox;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;



@Mixin(MouseHandler.class)
public class MouseMixin
{
	@Inject(at = @At("RETURN"), method = "onScroll(JDD)V")
	private void onOnMouseScroll(long window, double horizontal,
		double vertical, CallbackInfo ci)
	{
		if (Minecraft.getInstance().gui.screen() instanceof EditorScreen screen) {
            if (screen.getFocused() instanceof CustomNumberEditBox box) {
                box.onScroll(vertical);
            }
        }
        if (Minecraft.getInstance().hasAltDown()) {
            if (Minecraft.getInstance().gui.screen() instanceof BossChecklistScreen screen) {
                if (vertical == 0) return;
                Optional<GuiEventListener> o = screen.getChildAt(Minecraft.getInstance().mouseHandler.getScaledXPos(Minecraft.getInstance().getWindow()), Minecraft.getInstance().mouseHandler.getScaledYPos(Minecraft.getInstance().getWindow()));
                if (o.isPresent()) {
                    if (o.get() instanceof CustomCheckbox box) {
                        box.moveBossPosition(vertical > 0, true, window);
                    } 
                }
            }
        }
	}
}