package com.yori3o.boss_checklist.common.util;


import net.minecraft.client.Minecraft;

import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.system.MemoryStack;


public final class MoveCursorUtil {

    public static void setCursorPos(int newX, int newY, long window) {
        try {
            SDLMouse.SDL_WarpMouseInWindow(window, newX, newY);
        } catch (RuntimeException e) {
            LoggerUtil.errorWithException("Failed to warp mouse cursor to " + newX + ", " + newY + ": ", e);
        }
    }

    public static void moveCursor(int deltaX, int deltaY, long window) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var x = stack.mallocFloat(1);
            var y = stack.mallocFloat(1);

            SDLMouse.SDL_GetMouseState(x, y);

            float mouseX = x.get(0);
            float mouseY = y.get(0);
            SDLMouse.SDL_WarpMouseInWindow(window, mouseX + deltaX, mouseY + deltaY);
        } catch (RuntimeException e) {
            LoggerUtil.errorWithException(
                "Failed to move mouse cursor by " + deltaX + ", " + deltaY + ": ",
                e
            );
        }
    }

    public static void moveCursorInChecklist(long window, int rowDelta, int pageDelta) {
        final int ROW_SPACING = Minecraft.getInstance().options.guiScale().get() * 18;
        final int PAGE_SPACING = Minecraft.getInstance().options.guiScale().get() * 132;

        moveCursor(pageDelta * PAGE_SPACING, rowDelta * ROW_SPACING, window);
    }
}