package com.yori3o.boss_checklist.common.util;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;


/**
 * Tooltip rendering utility.
 */
public final class TooltipUtil {


    // main method
    private static void renderTooltipMain(GuiGraphics guiGraphics, List<Component> list, int mouseX, int mouseY) {
        guiGraphics.renderComponentTooltip(Minecraft.getInstance().font, list, mouseX, mouseY);
    }

    /**
     * supports String and any Component
     */
    public static void renderTooltip(GuiGraphics guiGraphics, List<? extends Object> list, int mouseX, int mouseY) {
        if (list.isEmpty()) return;

        List<Component> components = new ArrayList<>(list.size());
        for (Object entry : list) {
            if (entry instanceof String string) {
                components.add(Component.literal(string));
            } else if (entry instanceof Component component) {
                components.add(component);
            } else {
                throw new IllegalArgumentException("Unsupported tooltip element: " + entry.getClass().getName());
            }
        }
        renderTooltipMain(guiGraphics, components, mouseX, mouseY);
    }

    public static void renderTooltip(GuiGraphics guiGraphics, Component tooltip, int mouseX, int mouseY) {
        if (tooltip == null || tooltip.getString().isEmpty()) return;
        renderTooltipMain(guiGraphics, List.of(tooltip), mouseX, mouseY);
    }


    public static void renderTooltip(GuiGraphics guiGraphics, String tooltip, int mouseX, int mouseY) {
        if (tooltip == null || tooltip.isEmpty()) return;
        renderTooltipMain(guiGraphics, List.of(Component.literal(tooltip)), mouseX, mouseY);
    }


}