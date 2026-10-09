package com.yori3o.boss_checklist.common.util;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;


/**
 * Tooltip rendering utility.
 */
public final class TooltipUtil {


    // main method
    private static final void renderTooltipMain(GuiGraphics guiGraphics, List<Component> list, int mouseX, int mouseY) {
        guiGraphics.renderComponentTooltip(
            Minecraft.getInstance().font,
            list,
            mouseX, mouseY
        );
    }

    /**
     * supports String and any Component
     */
    @SuppressWarnings("unchecked")
    public static void renderTooltip(GuiGraphics guiGraphics, List<? extends Object> list, int mouseX, int mouseY) {
        if (list.isEmpty()) return;
        if (list.get(0) instanceof ClientTooltipComponent) {
            renderTooltipMain(guiGraphics, (List<Component>)list, mouseX, mouseY);
        } else if (list.get(0) instanceof String) {
            List<Component> componentList = new ArrayList<>();
            for (String string : (List<String>)list) {
                componentList.add(Component.literal(string));
            }
            renderTooltipMain(guiGraphics, componentList, mouseX, mouseY);
        } else if (list.get(0) instanceof Component) {
            renderTooltipMain(guiGraphics, (List<Component>)list, mouseX, mouseY);
        }
    }

    public static void renderTooltip(GuiGraphics guiGraphics, ClientTooltipComponent tooltip, int mouseX, int mouseY) {
        if (tooltip == null) return;
        renderTooltipMain(guiGraphics, List.of((Component)tooltip), mouseX, mouseY);
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