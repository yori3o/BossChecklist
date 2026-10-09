package com.yori3o.boss_checklist.common.util;


import net.minecraft.client.Minecraft;

import org.lwjgl.glfw.GLFW;


public final class MoveCursorUtil {

    public static void setCursorPos(int newX, int newY, long window) {
        GLFW.glfwSetCursorPos(window, newX, newY);
    }

    public static void moveCursor(int deltaX, int deltaY, long window) {
        double[] x = new double[1];
        double[] y = new double[1];

        GLFW.glfwGetCursorPos(window, x, y);
        GLFW.glfwSetCursorPos(window, x[0] + deltaX, y[0] + deltaY);
    }

    public static void moveCursorInChecklist(long window, int rowDelta, int pageDelta) {
        final int ROW_SPACING = Minecraft.getInstance().options.guiScale().get() * 18;
        final int PAGE_SPACING = Minecraft.getInstance().options.guiScale().get() * 132;

        moveCursor(pageDelta * PAGE_SPACING, rowDelta * ROW_SPACING, window);
    }
}