package com.yori3o.boss_checklist.common.mixin;


import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.yori3o.boss_checklist.common.client.gui.EditorScreen;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomNumberEditBox;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;



@Mixin(MouseHandler.class)
public class MouseMixin
{
	@Inject(at = @At("RETURN"), method = "onScroll(JDD)V")
	private void onOnMouseScroll(long window, double horizontal,
		double vertical, CallbackInfo ci)
	{
		if (Minecraft.getInstance().screen instanceof EditorScreen screen) {
            if (screen.getFocused() instanceof CustomNumberEditBox box) {
                box.onScroll(vertical);
            }
        }
	}
	
	/*@WrapWithCondition(at = @At(value = "INVOKE",
		target = "Lnet/minecraft/world/entity/player/Inventory;setSelectedSlot(I)V"),
		method = "onScroll(JDD)V")
	private boolean wrapOnMouseScroll(Inventory inventory, int slot)
	{
		return !WiZoom.INSTANCE.getZoomKey().isDown();
	}*/
}