package com.yori3o.boss_checklist.common.client.gui;


import com.yori3o.boss_checklist.common.BossChecklistClient;
import com.yori3o.boss_checklist.common.client.data.BossNameCache;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomButton;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomCheckbox;
import com.yori3o.boss_checklist.common.config.ClientConfig;
import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;
import com.yori3o.boss_checklist.common.config.ServerConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;



public class ConfigScreen extends Screen {
        
    private boolean skipNextRenderBackground = false;

    private CustomButton closeButton;

    private final boolean isLocalServer;

    private final Screen parent;

    private static final int cbs_client_x = 134;
    private static final int cbs_server_x = 265;

    private static final int cb1_y = 70;
    private static final int cb2_y = 90;
    private static final int cb3_y = 110;
    private static final int cb4_y = 130;

    private static final int cb21_y = 70;
    private static final int cb22_y = 100;
    private static final int cb23_y = 130;

    private static final int MAX_LABEL_WIDTH = 105;


    // --- server config variables ---
    public static boolean saveBossKiller = DynamicConfigHandler.saveBossKiller_dynamic;
    public static boolean statisticsEnabled = DynamicConfigHandler.statisticsEnabled_dynamic;
    public static boolean asyncLogic = DynamicConfigHandler.asyncLogic_dynamic;

    // --- client config variables ---
    public static boolean progressionMode = DynamicConfigHandler.progressionMode_dynamic;
    public static boolean progressionModePlus = DynamicConfigHandler.progressionModePlus_dynamic;
    public static boolean searchBarEnabled = DynamicConfigHandler.searchBarEnabled_dynamic;
    public static boolean openButtonEnabled = DynamicConfigHandler.openButtonEnabled_dynamic;
    




    public ConfigScreen(Screen parent) {
        super(Component.literal("Config"));
        this.parent = parent;
        isLocalServer = Minecraft.getInstance().isLocalServer();
    }


    public void init() {
        super.init();
        createButtons();
    }


    private void createButtons() {
        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;

        CustomCheckbox progression_mode = new CustomCheckbox(bookX + cbs_client_x, bookY + cb1_y, MAX_LABEL_WIDTH, 
            Component.translatable("gui.boss_checklist.settings.progression_mode"), DynamicConfigHandler.progressionMode_dynamic, 
            false, false,
            checked -> {
                progressionMode = checked; 
                BossNameCache.invalidate();
                if (parent instanceof BossChecklistScreen screen) {
                    screen.invalidateNames = true;
                }
            },
            () -> {}
        );
        CustomCheckbox progression_mode_plus = new CustomCheckbox(bookX + cbs_client_x, bookY + cb2_y, MAX_LABEL_WIDTH, 
            Component.translatable("gui.boss_checklist.settings.progression_mode_plus"), DynamicConfigHandler.progressionModePlus_dynamic, 
            false, false,
            checked -> {progressionModePlus = checked;},
            () -> {}
        );
        CustomCheckbox search_bar = new CustomCheckbox(bookX + cbs_client_x, bookY + cb3_y, MAX_LABEL_WIDTH, 
            Component.translatable("gui.boss_checklist.settings.search_bar_enabled"), DynamicConfigHandler.searchBarEnabled_dynamic, 
            false, false,
            checked -> {searchBarEnabled = checked;},
            () -> {}
        );
        CustomCheckbox button = new CustomCheckbox(bookX + cbs_client_x, bookY + cb4_y, MAX_LABEL_WIDTH, 
            Component.translatable("gui.boss_checklist.settings.button_enabled"), DynamicConfigHandler.openButtonEnabled_dynamic, 
            false, false,
            checked -> {openButtonEnabled = checked;},
            () -> {}
        );

        addRenderableWidget(progression_mode);
        addRenderableWidget(progression_mode_plus);
        addRenderableWidget(search_bar);
        addRenderableWidget(button);


        if (isLocalServer) {
            CustomCheckbox killer_save = new CustomCheckbox(bookX + cbs_server_x, bookY + cb21_y, MAX_LABEL_WIDTH, 
                Component.translatable("gui.boss_checklist.settings.save_boss_killer"), DynamicConfigHandler.saveBossKiller_dynamic, 
                false, false,
                checked -> {saveBossKiller = checked;},
                () -> {}
            );
            CustomCheckbox enable_statistics = new CustomCheckbox(bookX + cbs_server_x, bookY + cb22_y, MAX_LABEL_WIDTH, 
                Component.translatable("gui.boss_checklist.settings.enable_statistics"), DynamicConfigHandler.statisticsEnabled_dynamic, 
                false, false,
                checked -> {statisticsEnabled = checked;},
                () -> {}
            );
            CustomCheckbox enable_async = new CustomCheckbox(bookX + cbs_server_x, bookY + cb23_y, MAX_LABEL_WIDTH, 
                Component.translatable("gui.boss_checklist.settings.enable_async"), DynamicConfigHandler.asyncLogic_dynamic, 
                false, false,
                checked -> {asyncLogic = checked;},
                () -> {}
            );
            addRenderableWidget(killer_save);
            addRenderableWidget(enable_statistics);
            addRenderableWidget(enable_async);
        }

        closeButton = new CustomButton(
            bookX + GuiConstants.CloseButtonX, bookY + GuiConstants.CloseButtonY, GuiConstants.CloseButtonWidth, GuiConstants.CloseButtonHeight, 0, 0, 
            null, 
            GuiConstants.CLOSE_BUTTON_TEXTURE, GuiConstants.CLOSE_BUTTON_TEXTURE_hovered, GuiConstants.CLOSE_BUTTON_TEXTURE_hovered, null, 
            () -> {
                onClose();
            }
        );

        addRenderableWidget(closeButton);

    }












    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        // FOR 1.20.1-
        //this.renderBackground(guiGraphics);

        this.skipNextRenderBackground = true;

        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;


        guiGraphics.blit(GuiConstants.BOOK, bookX, bookY, 0, 0, 512, 256, 512, 256);
        guiGraphics.drawString(font,  "§l" + Component.translatable("gui.boss_checklist.settings").getString(), bookX + 137, bookY + 53, 0xFF000000, false);

        renderDesc(guiGraphics, mouseX, mouseY, bookX, bookY);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }




    public void renderDesc(GuiGraphics guiGraphics, int mouseX, int mouseY, int bookX, int bookY) {

        if (mouseX >= bookX + cbs_client_x && mouseX < bookX + cbs_client_x + 8 && mouseY >= bookY + cb1_y && mouseY < bookY + cb1_y + 8) {
            guiGraphics.renderTooltip(
                Minecraft.getInstance().font,
                Component.translatable("gui.boss_checklist.settings.progression_mode_desc"),
                mouseX, mouseY
            );
        } else { 
            if (mouseX >= bookX + cbs_client_x && mouseX < bookX + cbs_client_x + 8 && mouseY >= bookY + cb2_y && mouseY < bookY + cb2_y + 8) {
                guiGraphics.renderTooltip(
                    Minecraft.getInstance().font,
                    Component.translatable("gui.boss_checklist.settings.progression_mode_plus_desc"),
                    mouseX, mouseY
                );
            } else {
                if (mouseX >= bookX + cbs_client_x && mouseX < bookX + cbs_client_x + 8 && mouseY >= bookY + cb3_y && mouseY < bookY + cb3_y + 8) {
                    guiGraphics.renderTooltip(
                        Minecraft.getInstance().font,
                        Component.translatable("gui.boss_checklist.settings.search_bar_enabled_desc"),
                        mouseX, mouseY
                    );
                } else {
                    if (mouseX >= bookX + cbs_client_x && mouseX < bookX + cbs_client_x + 8 && mouseY >= bookY + cb4_y && mouseY < bookY + cb4_y + 8) {
                        guiGraphics.renderTooltip(
                            Minecraft.getInstance().font,
                            Component.translatable("gui.boss_checklist.settings.button_enabled_desc"),
                            mouseX, mouseY
                        );
                    } 
                }
            }
        }

        if (isLocalServer) {
            if (mouseX >= bookX + cbs_server_x && mouseX < bookX + cbs_server_x + 8 && mouseY >= bookY + cb21_y && mouseY < bookY + cb21_y + 8) {
                    guiGraphics.renderTooltip(
                        Minecraft.getInstance().font,
                        Component.translatable("gui.boss_checklist.settings.save_boss_killer_desc"),
                        mouseX, mouseY
                    );
            } else {
                if (mouseX >= bookX + cbs_server_x && mouseX < bookX + cbs_server_x + 8 && mouseY >= bookY + cb22_y && mouseY < bookY + cb22_y + 8) {
                    guiGraphics.renderTooltip(
                        Minecraft.getInstance().font,
                        Component.translatable("gui.boss_checklist.settings.enable_statistics_desc"),
                            mouseX, mouseY
                    );
                } else {
                    if (mouseX >= bookX + cbs_server_x && mouseX < bookX + cbs_server_x + 8 && mouseY >= bookY + cb23_y && mouseY < bookY + cb23_y + 8) {
                        guiGraphics.renderTooltip(
                            Minecraft.getInstance().font,
                            Component.translatable("gui.boss_checklist.settings.enable_async_desc"),
                                mouseX, mouseY
                        );
                    }
                }
            } 
        }
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









    @Override
    public void onClose() {

        ClientConfig cc = new ClientConfig();
        cc.get().progressionMode = progressionMode;
        cc.get().progressionModePlus = progressionModePlus;
        cc.get().openButtonEnabled = openButtonEnabled;
        cc.get().searchBarEnabled = searchBarEnabled;
        
        DynamicConfigHandler.ClientConfigUpdate(cc.get());
        cc.save();
        
        if (isLocalServer) {
            ServerConfig sc = new ServerConfig();
            sc.get().saveBossKiller = saveBossKiller;
            sc.get().statisticsEnabled = statisticsEnabled;
            sc.get().asyncLogic = asyncLogic;

            DynamicConfigHandler.ServerConfigUpdate(sc.get());
            sc.save();
        }

        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (BossChecklistClient.OPEN_CHECKLIST.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}