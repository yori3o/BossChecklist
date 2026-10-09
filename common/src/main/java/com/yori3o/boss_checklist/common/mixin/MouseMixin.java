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
import com.yori3o.boss_checklist.common.util.LoggerUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;



@Mixin(MouseHandler.class)
public class MouseMixin
{
	@Inject(at = @At("RETURN"), method = "onScroll(JDD)V")
	private void onOnMouseScroll(long window, double horizontal,
		double vertical, CallbackInfo ci)
	{
        try {
            if (Minecraft.getInstance().screen instanceof EditorScreen screen) {
                if (screen.getFocused() instanceof CustomNumberEditBox box) {
                    box.onScroll(vertical);
                }
            }
            if (Screen.hasAltDown()) {
                if (Minecraft.getInstance().screen instanceof BossChecklistScreen screen) {
                    if (vertical == 0) return;
                    Minecraft mc = Minecraft.getInstance();
                    var window2 = mc.getWindow();
                    double mouseX = mc.mouseHandler.xpos() * window2.getGuiScaledWidth() / window2.getScreenWidth();
                    double mouseY = mc.mouseHandler.ypos() * window2.getGuiScaledHeight() / window2.getScreenHeight();
                    Optional<GuiEventListener> o = screen.getChildAt(
                        mouseX,
                        mouseY
                    );
                    if (o.isPresent() && o.get() instanceof CustomCheckbox box) {
                        box.moveBossPosition(vertical > 0, true, window);
                    }
                }
            }
        } catch (RuntimeException e) {
            LoggerUtil.errorWithException("Failed to handle mouse scroll in Boss Checklist: ", e);
        }
	}
}