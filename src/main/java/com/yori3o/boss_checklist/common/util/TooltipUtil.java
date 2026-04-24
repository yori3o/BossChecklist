package com.yori3o.boss_checklist.common.util;


import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2i;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;


/**
 * Tooltip rendering utility.
 */
public final class TooltipUtil {


    // main method
    private static final void renderTooltipMain(GuiGraphicsExtractor guiGraphics, List<ClientTooltipComponent> list, int mouseX, int mouseY) {
        guiGraphics.tooltip(
            Minecraft.getInstance().font,
            list,
            mouseX, mouseY, (screenWidth, screenHeight, x, y, tooltipWidth, tooltipHeight) -> new Vector2i(x + 12, y - 4), null
        );
    }

    /**
     * supports String and any Component
     */
    @SuppressWarnings("unchecked")
    public static void renderTooltip(GuiGraphicsExtractor guiGraphics, List<? extends Object> list, int mouseX, int mouseY) {
        if (list.isEmpty()) return;
        if (list.get(0) instanceof ClientTooltipComponent) {
            renderTooltipMain(guiGraphics, (List<ClientTooltipComponent>)list, mouseX, mouseY);
        } else if (list.get(0) instanceof String) {
            List<ClientTooltipComponent> componentList = new ArrayList<>();
            for (String string : (List<String>)list) {
                componentList.add(ClientTooltipComponent.create(Component.literal(string).getVisualOrderText()));
            }
            renderTooltipMain(guiGraphics, componentList, mouseX, mouseY);
        } else if (list.get(0) instanceof Component) {
            List<ClientTooltipComponent> componentList = new ArrayList<>();
            for (Component component : (List<Component>)list) {
                componentList.add(ClientTooltipComponent.create(component.getVisualOrderText()));
            }
            renderTooltipMain(guiGraphics, componentList, mouseX, mouseY);
        }
    }

    public static void renderTooltip(GuiGraphicsExtractor guiGraphics, ClientTooltipComponent tooltip, int mouseX, int mouseY) {
        if (tooltip == null) return;
        renderTooltipMain(guiGraphics, List.of(tooltip), mouseX, mouseY);
    }

    public static void renderTooltip(GuiGraphicsExtractor guiGraphics, Component tooltip, int mouseX, int mouseY) {
        if (tooltip == null || tooltip.getString().isEmpty()) return;
        renderTooltipMain(guiGraphics, List.of(ClientTooltipComponent.create(tooltip.getVisualOrderText())), mouseX, mouseY);
    }


    public static void renderTooltip(GuiGraphicsExtractor guiGraphics, String tooltip, int mouseX, int mouseY) {
        if (tooltip == null || tooltip.isEmpty()) return;
        renderTooltipMain(guiGraphics, List.of(ClientTooltipComponent.create(Component.literal(tooltip).getVisualOrderText())), mouseX, mouseY);
    }


}