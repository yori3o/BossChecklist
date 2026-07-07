package com.yori3o.boss_checklist.common.client.gui;


import com.yori3o.boss_checklist.common.BossChecklistClient;
import com.yori3o.boss_checklist.common.client.data.BossNameCache;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomButton;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomCheckbox;
import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;

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

    private static final int CHECKBOXES_PADDING = 8;





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

        int y = bookY + 70;

        if (IS_LOCAL_SERVER) {
            CustomCheckbox saveKillerName = new CustomCheckbox(bookX + cbs_server_x, y, 
                Component.translatable("gui.boss_checklist.settings.save_boss_killer"),
                GuiConstants.MAX_LABEL_WIDTH, 
                DynamicConfigHandler.server().saveBossKillerName,
                checked -> {DynamicConfigHandler.server().saveBossKillerName = checked;}
            );
            saveKillerName.setTooltipOnBox(Component.translatable("gui.boss_checklist.settings.save_boss_killer_desc"));
            y += saveKillerName.getLabelHeight() + CHECKBOXES_PADDING;

            CustomCheckbox enableStatistics = new CustomCheckbox(bookX + cbs_server_x, y, 
                Component.translatable("gui.boss_checklist.settings.enable_statistics"),
                GuiConstants.MAX_LABEL_WIDTH, 
                DynamicConfigHandler.server().statisticsEnabled, 
                checked -> {DynamicConfigHandler.server().statisticsEnabled = checked;}
            );
            enableStatistics.setTooltipOnBox(Component.translatable("gui.boss_checklist.settings.enable_statistics_desc"));
            y += enableStatistics.getLabelHeight() + CHECKBOXES_PADDING;

            CustomCheckbox enableAsync = new CustomCheckbox(bookX + cbs_server_x, y, 
                Component.translatable("gui.boss_checklist.settings.enable_async"),
                GuiConstants.MAX_LABEL_WIDTH, 
                DynamicConfigHandler.server().asyncLogic,
                checked -> {DynamicConfigHandler.server().asyncLogic = checked;}
            );
            enableAsync.setTooltipOnBox(Component.translatable("gui.boss_checklist.settings.enable_async_desc"));

            addRenderableWidget(saveKillerName);
            addRenderableWidget(enableStatistics);
            addRenderableWidget(enableAsync);
        }

        y = bookY + 70;

        CustomCheckbox progressionMode = new CustomCheckbox(bookX + cbs_client_x, y, 
            Component.translatable("gui.boss_checklist.settings.progression_mode"),
            GuiConstants.MAX_LABEL_WIDTH, 
            DynamicConfigHandler.client().progressionMode, 
            checked -> {
                DynamicConfigHandler.client().progressionMode = checked; 
                BossNameCache.invalidate();
                if (parent instanceof BossChecklistScreen screen) {
                    screen.invalidateNames = true;
                }
            }
        );
        progressionMode.setTooltipOnBox(Component.translatable("gui.boss_checklist.settings.progression_mode_desc"));
        y += progressionMode.getLabelHeight() + CHECKBOXES_PADDING;

        CustomCheckbox progressionModePlus = new CustomCheckbox(bookX + cbs_client_x, y, 
            Component.translatable("gui.boss_checklist.settings.progression_mode_plus"), 
            GuiConstants.MAX_LABEL_WIDTH, 
            DynamicConfigHandler.client().progressionModePlus,
            checked -> {DynamicConfigHandler.client().progressionModePlus = checked;}
        );
        progressionModePlus.setTooltipOnBox(Component.translatable("gui.boss_checklist.settings.progression_mode_plus_desc"));
        y += progressionModePlus.getLabelHeight() + CHECKBOXES_PADDING;

        CustomCheckbox searchBar = new CustomCheckbox(bookX + cbs_client_x, y,
            Component.translatable("gui.boss_checklist.settings.search_bar_enabled"),
            GuiConstants.MAX_LABEL_WIDTH, 
            DynamicConfigHandler.client().searchBarEnabled, 
            checked -> {DynamicConfigHandler.client().searchBarEnabled = checked;}
        );
        searchBar.setTooltipOnBox(Component.translatable("gui.boss_checklist.settings.search_bar_enabled_desc"));
        y += searchBar.getLabelHeight() + CHECKBOXES_PADDING;

        CustomCheckbox button = new CustomCheckbox(bookX + cbs_client_x, y, 
            Component.translatable("gui.boss_checklist.settings.button_enabled"), 
            GuiConstants.MAX_LABEL_WIDTH, 
            DynamicConfigHandler.client().openButtonEnabled, 
            checked -> {DynamicConfigHandler.client().openButtonEnabled = checked;}
        );
        button.setTooltipOnBox(Component.translatable("gui.boss_checklist.settings.button_enabled_desc"));
        y += button.getLabelHeight() + CHECKBOXES_PADDING;

        CustomCheckbox showEditorButton = new CustomCheckbox(bookX + cbs_client_x, y, 
            Component.translatable("gui.boss_checklist.settings.show_editor_button"), 
            GuiConstants.MAX_LABEL_WIDTH, 
            DynamicConfigHandler.client().showEditorButton, 
            checked -> {DynamicConfigHandler.client().showEditorButton = checked;}
        );
        showEditorButton.setTooltipOnBox(Component.translatable("gui.boss_checklist.settings.show_editor_button_desc"));

        addRenderableWidget(progressionMode);
        addRenderableWidget(progressionModePlus);
        addRenderableWidget(searchBar);
        addRenderableWidget(button);
        addRenderableWidget(showEditorButton);


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


        // FOR 1.21.4+ - add RenderType::guiTextured, as first argument and delete all RenderSystem.enableBlend();
        guiGraphics.blit(GuiConstants.BOOK, bookX, bookY, 0, 0, 512, 256, 512, 256);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.drawString(font,  "§l" + Component.translatable("gui.boss_checklist.settings").getString(), bookX + 137, bookY + 53, 0xFF000000, false);

        
    }









    @Override
    public void onClose() {
        DynamicConfigHandler.cc.save();
        
        if (IS_LOCAL_SERVER) {
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