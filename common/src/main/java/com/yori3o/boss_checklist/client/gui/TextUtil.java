package com.yori3o.boss_checklist.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class TextUtil {

    public static Component truncateText(String text, int maxWidth) {
        String str = text;
        Minecraft mc = Minecraft.getInstance();
        if (mc.font.width(str) <= maxWidth) return Component.literal(text);

        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            sb.append(c);
            if (mc.font.width(sb.toString() + "…") > maxWidth) {
                sb.deleteCharAt(sb.length() - 1);
                break;
            }
        }
        sb.append("…");
        return Component.literal(sb.toString());
    }
}