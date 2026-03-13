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
    

    private CustomButton closeButton;

    private final boolean IS_LOCAL_SERVER;

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


    // --- server config variables ---
    public static boolean saveBossKillerName = DynamicConfigHandler.server().saveBossKillerName;
    public static boolean statisticsEnabled = DynamicConfigHandler.server().statisticsEnabled;
    public static boolean asyncLogic = DynamicConfigHandler.server().asyncLogic;

    // --- client config variables ---
    public static boolean progressionMode = DynamicConfigHandler.client().progressionMode;
    public static boolean progressionModePlus = DynamicConfigHandler.client().progressionModePlus;
    public static boolean searchBarEnabled = DynamicConfigHandler.client().searchBarEnabled;
    public static boolean openButtonEnabled = DynamicConfigHandler.client().openButtonEnabled;
    




    public ConfigScreen(Screen parent) {
        super(Component.literal("Config"));
        this.parent = parent;
        IS_LOCAL_SERVER = Minecraft.getInstance().isLocalServer();
    }


    public void init() {
        super.init();
        createButtons();
    }


    private void createButtons() {
        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;

        CustomCheckbox progression_mode = new CustomCheckbox(bookX + cbs_client_x, bookY + cb1_y, GuiConstants.MAX_LABEL_WIDTH, 
            Component.translatable("gui.boss_checklist.settings.progression_mode"), DynamicConfigHandler.client().progressionMode, 
            false, false,
            checked -> {
                progressionMode = checked; 
                BossNameCache.invalidate();
                if (parent instanceof BossChecklistScreen screen) {
                    screen.invalidateNames = true;
                }
            },
            null
        );
        CustomCheckbox progression_mode_plus = new CustomCheckbox(bookX + cbs_client_x, bookY + cb2_y, GuiConstants.MAX_LABEL_WIDTH, 
            Component.translatable("gui.boss_checklist.settings.progression_mode_plus"), DynamicConfigHandler.client().progressionModePlus, 
            false, false,
            checked -> {progressionModePlus = checked;},
            null
        );
        CustomCheckbox search_bar = new CustomCheckbox(bookX + cbs_client_x, bookY + cb3_y, GuiConstants.MAX_LABEL_WIDTH, 
            Component.translatable("gui.boss_checklist.settings.search_bar_enabled"), DynamicConfigHandler.client().searchBarEnabled, 
            false, false,
            checked -> {searchBarEnabled = checked;},
            null
        );
        CustomCheckbox button = new CustomCheckbox(bookX + cbs_client_x, bookY + cb4_y, GuiConstants.MAX_LABEL_WIDTH, 
            Component.translatable("gui.boss_checklist.settings.button_enabled"), DynamicConfigHandler.client().openButtonEnabled, 
            false, false,
            checked -> {openButtonEnabled = checked;},
            null
        );

        addRenderableWidget(progression_mode);
        addRenderableWidget(progression_mode_plus);
        addRenderableWidget(search_bar);
        addRenderableWidget(button);


        if (IS_LOCAL_SERVER) {
            CustomCheckbox killer_save = new CustomCheckbox(bookX + cbs_server_x, bookY + cb21_y, GuiConstants.MAX_LABEL_WIDTH, 
                Component.translatable("gui.boss_checklist.settings.save_boss_killer"), DynamicConfigHandler.server().saveBossKillerName, 
                false, false,
                checked -> {saveBossKillerName = checked;},
                null
            );
            CustomCheckbox enable_statistics = new CustomCheckbox(bookX + cbs_server_x, bookY + cb22_y, GuiConstants.MAX_LABEL_WIDTH, 
                Component.translatable("gui.boss_checklist.settings.enable_statistics"), DynamicConfigHandler.server().statisticsEnabled, 
                false, false,
                checked -> {statisticsEnabled = checked;},
                null
            );
            CustomCheckbox enable_async = new CustomCheckbox(bookX + cbs_server_x, bookY + cb23_y, GuiConstants.MAX_LABEL_WIDTH, 
                Component.translatable("gui.boss_checklist.settings.enable_async"), DynamicConfigHandler.server().asyncLogic, 
                false, false,
                checked -> {asyncLogic = checked;},
                null
            );
            addRenderableWidget(killer_save);
            addRenderableWidget(enable_statistics);
            addRenderableWidget(enable_async);
        }

        closeButton = new CustomButton(
            bookX + GuiConstants.CLOSE_BUTTON_X, bookY + GuiConstants.CLOSE_BUTTON_Y, GuiConstants.CLOSE_BUTTON_SIZE, GuiConstants.CLOSE_BUTTON_SIZE, 0, 0, 
            Component.empty(), 
            GuiConstants.CLOSE_BUTTON_TEXTURE, GuiConstants.CLOSE_BUTTON_TEXTURE_hovered, GuiConstants.CLOSE_BUTTON_TEXTURE_hovered, null, 
            () -> {
                onClose();
            }
        );

        addRenderableWidget(closeButton);

    }












    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        
        // FOR 1.20.1-
        this.renderBackground(guiGraphics);

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

        if (IS_LOCAL_SERVER) {
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













    @Override
    public void onClose() {

        ClientConfig.Values ccv = DynamicConfigHandler.client();
        ccv.progressionMode = progressionMode;
        ccv.progressionModePlus = progressionModePlus;
        ccv.searchBarEnabled = searchBarEnabled;
        ccv.openButtonEnabled = openButtonEnabled;
        
        DynamicConfigHandler.cc.save();
        
        if (IS_LOCAL_SERVER) {
            ServerConfig.Values scv = DynamicConfigHandler.server();
            scv.saveBossKillerName = saveBossKillerName;
            scv.statisticsEnabled = statisticsEnabled;
            scv.asyncLogic = asyncLogic;

            DynamicConfigHandler.sc.save();
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