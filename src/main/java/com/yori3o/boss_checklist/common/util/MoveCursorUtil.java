package com.yori3o.boss_checklist.common.util;


import net.minecraft.client.Minecraft;

import java.nio.FloatBuffer;

import org.lwjgl.sdl.SDLMouse;


public final class MoveCursorUtil {

    public static void setCursorPos(int newX, int newY, long window) {
        SDLMouse.SDL_WarpMouseInWindow(window, newX, newY);
    }

    public static void moveCursor(int deltaX, int deltaY, long window) {
        FloatBuffer x = FloatBuffer.allocate(1);
        FloatBuffer y = FloatBuffer.allocate(1);

        SDLMouse.SDL_GetMouseState(x, y);

        float mouseX = x.get(0);
        float mouseY = y.get(0);
        SDLMouse.SDL_WarpMouseInWindow(window, mouseX + deltaX, mouseY + deltaY);
    }

    public static void moveCursorInChecklist(long window, int rowDelta, int pageDelta) {
        final int ROW_SPACING = Minecraft.getInstance().options.guiScale().get() * 18;
        final int PAGE_SPACING = Minecraft.getInstance().options.guiScale().get() * 132;

        moveCursor(pageDelta * PAGE_SPACING, rowDelta * ROW_SPACING, window);
    }
}