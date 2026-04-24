package com.yori3o.boss_checklist.common.util;


import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;


public final class MoveCursorUtil {

    public static void setCursorPos(int newX, int newY, long window) {
        GLFW.glfwSetCursorPos(window, newX, newY);
    }

    public static void moveCursor(int deltaX, int deltaY, long window) {
        double[] x = new double[1];
        double[] y = new double[1];

        GLFW.glfwGetCursorPos(window, x, y);

        double mouseX = x[0];
        double mouseY = y[0];
        GLFW.glfwSetCursorPos(window, mouseX + deltaX, mouseY + deltaY);
    }

    public static void moveCursorInChecklist(long window, int rowDelta, int pageDelta) {
        final int ROW_SPACING = Minecraft.getInstance().options.guiScale().get() * 18;
        final int PAGE_SPACING = Minecraft.getInstance().options.guiScale().get() * 132;

        moveCursor(pageDelta * PAGE_SPACING, rowDelta * ROW_SPACING, window);
    }
}