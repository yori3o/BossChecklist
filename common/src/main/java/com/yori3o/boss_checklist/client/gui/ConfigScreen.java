package com.yori3o.boss_checklist.client.gui;


import com.yori3o.boss_checklist.BossChecklist;
import com.yori3o.boss_checklist.BossChecklistClient;
import com.yori3o.boss_checklist.config.ClientConfig;
import com.yori3o.boss_checklist.config.ServerConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;




public class ConfigScreen extends Screen {

    private static final ResourceLocation background_texture = 
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/boss_book.png");
    private boolean skipNextRenderBackground = false;
    private final Screen parent;

    private boolean isLocalServer;

    private ClientConfig Config = new ClientConfig();
    private ServerConfig ServerConfig = new ServerConfig();
    

    public ConfigScreen(Screen parent) {
        super(Component.literal("Config"));
        this.parent = parent;
    }


    public void init() {
        super.init();

        isLocalServer = minecraft.isLocalServer();


        createButtons();
    }


    private void createButtons() {
        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;

        CustomCheckbox cb = new CustomCheckbox(bookX + 134, bookY + 70, Component.literal(Component.translatable("gui.boss_checklist.settings.progression_mode").getString()), BossChecklistClient.isProgressionMode_dynamic, false,
            checked -> {Config.setProgressionMode(checked); BossChecklistClient.isProgressionMode_dynamic = checked;},
            () -> {}
        );
        addRenderableWidget(cb);

        if (isLocalServer) {
            CustomCheckbox cb2 = new CustomCheckbox(bookX + 265, bookY + 70, Component.literal(""), BossChecklist.isSaveBossKiller_dynamic, false,
                checked -> {ServerConfig.setSaveBossKiller(checked); BossChecklist.isSaveBossKiller_dynamic = checked;},
                () -> {}
            );
            addRenderableWidget(cb2);
        }

        
    }



    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        // FOR 1.20.1-
        //this.renderBackground(guiGraphics);

        this.skipNextRenderBackground = true;

        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;


        guiGraphics.blit(background_texture, bookX, bookY, 0, 0, 512, 256, 512, 256);
        guiGraphics.drawString(font,  "§l" + Component.translatable("gui.boss_checklist.settings").getString(), bookX + 137, bookY + 53, 0xFF000000, false);

        if (isLocalServer) {
            guiGraphics.drawWordWrap(font,  Component.literal(Component.translatable("gui.boss_checklist.settings.save_boss_killer").getString()), bookX + 277, bookY + 70, 110, 0xFF323538);
        }

        if (mouseX >= bookX + 124 && mouseX < bookX + 134 + 8 && mouseY >= bookY + 70 && mouseY < bookY + 70 + 8) {
            guiGraphics.renderTooltip(
                Minecraft.getInstance().font,
                Component.literal(Component.translatable("gui.boss_checklist.settings.progression_mode_desc").getString()),
                mouseX, mouseY
            );
        } else { if (isLocalServer && (mouseX >= bookX + 265 && mouseX < bookX + 265 + 8 && mouseY >= bookY + 70 && mouseY < bookY + 70 + 8)) {
            guiGraphics.renderTooltip(
                Minecraft.getInstance().font,
                Component.literal(Component.translatable("gui.boss_checklist.settings.save_boss_killer_desc").getString()),
                mouseX, mouseY
            );
        } }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }



    @Override
    public void onClose() {
        // if ESC set screen to checklist
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (BossChecklistClient.OPEN_CHECKLIST.matches(keyCode, scanCode)) {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    // ONLY FOR 1.21.1+
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.skipNextRenderBackground) {
            // for normal render
            this.skipNextRenderBackground = false;
            return;
        }
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
    }
}