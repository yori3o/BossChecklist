package com.yori3o.boss_checklist.common.client.gui;


import com.yori3o.boss_checklist.common.BossChecklistClient;
import com.yori3o.boss_checklist.common.client.ClientGlobalStatistics;
import com.yori3o.boss_checklist.common.client.boss.BossEntry;
import com.yori3o.boss_checklist.common.client.data.ClientDataSaver;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomButton;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomCheckbox;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomPageButton;
import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;
import com.yori3o.boss_checklist.common.event.ClientEvents;
import com.yori3o.boss_checklist.common.util.LoggerUtil;
import com.yori3o.boss_checklist.common.client.data.BossNameCache;
import com.yori3o.boss_checklist.common.client.data.BossService;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;



public class BossChecklistScreen extends Screen {

    
    private static final int BOOKMARK_SETTINGS_X = 387;
    private static final int BOOKMARK_SETTINGS_Y = 65;
    private static final int BOOKMARK_SETTINGS_WIDTH = 12;
    private static final int BOOKMARK_SETTINGS_HEIGHT = 22;


    private static final int ELEMENTS_PER_PAGE = 7;
    private static int currentSpread = 0;
    private int totalSpreads;
    

    private boolean statisticsTabEnabled = false;
    private boolean showStatisticsTab = false;

    
    public boolean invalidateNames = false;


    private EditBox searchBox;

    private List<CustomCheckbox> currentCheckboxes = new ArrayList<>();
    private CustomPageButton nextButton, prevButton;
    private CustomButton closeButton;
    private int leftPage, rightPage;

    private boolean skipNextRenderBackground = false;
    private boolean noBossesLoaded = false;
    private boolean noBossesFindedWhenSearch = false;
    private boolean namesMapAreLoaded = false;
    private final boolean FROM_PAUSE_MENU;

    private LinkedHashMap<String, Component> bosses = BossNameCache.CACHE_TRUNCATED; // read-only 
    private LinkedHashMap<String, Component> bossesFiltered = new LinkedHashMap<>();

    private int defeatedCount = 0;
    private int totalCount = 0;
    private float percent = 0;



    public BossChecklistScreen(boolean fromPauseMenu) {
        super(Component.literal("Boss checklist"));
        FROM_PAUSE_MENU = fromPauseMenu;
    }



    public void init() {
        super.init();

        statisticsTabEnabled = (ClientGlobalStatistics.top1 != null) && (DynamicConfigHandler.client().statisticsTabEnabled);
        
        reloadNamesMap();
        createButtons();
        updateSearch(null);
    }


    private void reloadNamesMap() {
        BossNameCache.rebuildIfNeeded();
        if (!namesMapAreLoaded) {
            bossesFiltered.putAll(bosses);
            namesMapAreLoaded = true;
        }
        if (invalidateNames) {
            BossNameCache.rebuild();
            bossesFiltered.clear();
            bossesFiltered.putAll(bosses);
        }
    }


    private void updateCounts() {
        if (DynamicConfigHandler.client().progressBarEnabled) {
            defeatedCount = ClientDataSaver.defeatedBossesCount();
            totalCount = bossesFiltered.size();
            percent = totalCount > 0 ? (float) defeatedCount / totalCount : 0f;
            percent = Math.min(percent, 1);
        }
    }


    private void createButtons() {
        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;

        double totalPages = Math.ceil((double) bossesFiltered.size() / ELEMENTS_PER_PAGE);
        totalSpreads = (int) Math.ceil(totalPages / 2.0);

        prevButton = new CustomPageButton(bookX + 138, bookY + 190, true, () -> {
            if (currentSpread > 0) {
                currentSpread--;
                updatePage();
            }
        });
        addRenderableWidget(prevButton);

        nextButton = new CustomPageButton(bookX + 350, bookY + 190, false, () -> {
            if (currentSpread < totalSpreads - 1) {
                currentSpread++;
                updatePage();
            }
        });
        addRenderableWidget(nextButton);

        if (statisticsTabEnabled) {
            CustomButton openStatsButton = new CustomButton(
                bookX + GuiConstants.STATS_TAB_X, bookY + GuiConstants.STATS_TAB_Y, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, 0, 0, 
                null, 
                GuiConstants.SMALL_BUTTON_TEXTURE, GuiConstants.SMALL_BUTTON_TEXTURE_hovered, GuiConstants.SMALL_BUTTON_TEXTURE_pressed, null, 
                () -> {
                    showStatisticsTab = !showStatisticsTab;
                }
            );
            addRenderableWidget(openStatsButton);
        }

        closeButton = new CustomButton(
            bookX + GuiConstants.CLOSE_BUTTON_X, bookY + GuiConstants.CLOSE_BUTTON_Y, GuiConstants.CLOSE_BUTTON_SIZE, GuiConstants.CLOSE_BUTTON_SIZE, 0, 0, 
            null, 
            GuiConstants.CLOSE_BUTTON_TEXTURE, GuiConstants.CLOSE_BUTTON_TEXTURE_hovered, GuiConstants.CLOSE_BUTTON_TEXTURE_hovered, null,
            () -> {
                onClose();
            }
        );
        addRenderableWidget(closeButton);
        
        if (DynamicConfigHandler.client().searchBarEnabled && this.height > 305) {
            this.searchBox = new EditBox(
                    this.font,
                    this.width / 2 - 100,
                    20,
                    200,
                    20,
                    Component.translatable("gui.boss_checklist.search")
            );

            this.searchBox.setHint(Component.translatable("gui.boss_checklist.search"));
            this.searchBox.setMaxLength(64);

            this.searchBox.setResponder(this::updateSearch);

            this.addRenderableWidget(this.searchBox);
        }
    }




    private void updatePage() {
        for (CustomCheckbox cb : currentCheckboxes) {
            removeWidget(cb);
        }
        currentCheckboxes.clear();
        
        if (bossesFiltered.isEmpty()) {
            if (BossNameCache.CACHE.isEmpty()) {
                LoggerUtil.warn("No bosses loaded at checklist! - Resources are empty or invalid.");
                noBossesLoaded = true;
            } else {
                noBossesFindedWhenSearch = true;
                updateCounts();
            }
            prevButton.visible = false;
            nextButton.visible = false;
            return;
        } else {
            noBossesFindedWhenSearch = false;
        }
        
        double totalPages = Math.ceil((double) bossesFiltered.size() / ELEMENTS_PER_PAGE);
        int totalSpreads = (int) Math.ceil(totalPages / 2.0);

        int fromIndex = currentSpread * (ELEMENTS_PER_PAGE * 2);
        int toIndex = Math.min(fromIndex + ELEMENTS_PER_PAGE * 2, bossesFiltered.size());

        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;

        int leftX = bookX + 134;
        int rightX = bookX + 266;
        int startY = bookY + 56;

        for (int i = fromIndex; i < toIndex; i++) {
            Entry<String, Component> entry = bossesFiltered.entrySet().stream().skip(i).findFirst().orElse(null);
            if (entry == null) break;

            Component name = entry.getValue();
            String bossId = entry.getKey();
            int localIndex = i - fromIndex;

            int x = (localIndex < ELEMENTS_PER_PAGE) ? leftX : rightX;
            int y = startY + (localIndex % ELEMENTS_PER_PAGE) * 18;

            BossEntry data = BossService.get(bossId);

            boolean isThisBossDefeated = data.progress().isMarkedAsDefeatedOnClient();

            CustomCheckbox cb = new CustomCheckbox(x, y, 300, name, isThisBossDefeated, true, data.progress().isFresh(),
                checked -> {
                    ClientDataSaver.setDefeated(bossId, checked, data.progress());
                    data.progress().clearFreshFlag();
                    updateCounts(); 
                },
                () -> {
                    Minecraft.getInstance().setScreen(new BossInfoScreen(this, bossId));
                    data.progress().clearFreshFlag();
                }
            );

            if (DynamicConfigHandler.client().animationsEnabled) {
                if (isThisBossDefeated && !data.progress().alreadyAnimated) {
                    data.progress().alreadyAnimated = true;
                    cb.isAnimating = true;
                    cb.animationPlayed = false;
                    cb.animationStartTime = System.currentTimeMillis();
                }
            }

            addRenderableWidget(cb);
            currentCheckboxes.add(cb);
        }
        
        prevButton.visible = currentSpread > 0;
        nextButton.visible = currentSpread < totalSpreads - 1;
        
        leftPage = currentSpread * 2 + 1;
        rightPage = currentSpread * 2 + 2;
        
        updateCounts();
    }





    private void updateSearch(String text) {
        bossesFiltered.clear();

        if (text == null) {
            bossesFiltered.putAll(bosses);
            updatePage();
            return;
        }

        String query = text.toLowerCase().trim();

        if (query.isEmpty()) {
            bossesFiltered.putAll(bosses);
            updatePage();
            return;
        }

        for (Entry<String, Component> entry : bosses.entrySet()) {
            String name = entry.getValue().getString().toLowerCase();

            if (name.contains(query) || entry.getKey().contains(query)) {
                bossesFiltered.put(entry.getKey(), entry.getValue());
            }
        }

        updatePage();
    }









    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        // FOR 1.20.1-
        //this.renderBackground(guiGraphics);

        this.skipNextRenderBackground = true;

        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;


        // FOR 1.21.4+ - add RenderType::guiTextured, as first argument and delete all RenderSystem.enableBlend();
        guiGraphics.blit(GuiConstants.BOOK, bookX, bookY, 0, 0, 512, 256, 512, 256);

        if (DynamicConfigHandler.client().progressBarEnabled) {
            guiGraphics.blit(GuiConstants.PROGRESS_BAR_BACKGROUND, (this.width / 2) - (GuiConstants.BAR_WIDTH / 2), bookY + 27, 0, 0, 
                GuiConstants.BAR_WIDTH, GuiConstants.BAR_HEIGHT, GuiConstants.BAR_WIDTH, GuiConstants.BAR_HEIGHT);
            guiGraphics.blit(GuiConstants.PROGRESS_BAR_FILL, (this.width / 2) - (GuiConstants.BAR_WIDTH / 2), bookY + 27, 0, 0, 
                (int) (percent * GuiConstants.BAR_WIDTH), GuiConstants.BAR_HEIGHT, GuiConstants.BAR_WIDTH, GuiConstants.BAR_HEIGHT);
            guiGraphics.drawCenteredString(this.font, defeatedCount + " / " + totalCount, (this.width / 2), bookY + 20, 0xFFFFFFFF);
        }

        if (DynamicConfigHandler.client().showConfigScreen) {
            guiGraphics.blit(GuiConstants.BOOKMARK_SETTINGS, bookX + BOOKMARK_SETTINGS_X, bookY + BOOKMARK_SETTINGS_Y, 0, 0, BOOKMARK_SETTINGS_WIDTH, BOOKMARK_SETTINGS_HEIGHT, BOOKMARK_SETTINGS_WIDTH, BOOKMARK_SETTINGS_HEIGHT);
        }

        // --- rendering statistics ---
        if (showStatisticsTab) {
            RenderSystem.enableBlend();
            guiGraphics.blit(GuiConstants.STATS_INFO_BACK, bookX + GuiConstants.STATS_TAB_X, bookY + GuiConstants.STATS_TAB_Y, 0, 0, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_HEIGHT, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_HEIGHT);
            guiGraphics.blit(GuiConstants.TOP_1, bookX + GuiConstants.STATS_TAB_X + 4, bookY + GuiConstants.STATS_TAB_Y + 24, 0, 0, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE);
            if (ClientGlobalStatistics.top2 != null) {
                guiGraphics.blit(GuiConstants.TOP_2, bookX + GuiConstants.STATS_TAB_X + 4, bookY + GuiConstants.STATS_TAB_Y + 38, 0, 0, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE);
                if (ClientGlobalStatistics.top3 != null) {
                    guiGraphics.blit(GuiConstants.TOP_3, bookX + GuiConstants.STATS_TAB_X + 4, bookY + GuiConstants.STATS_TAB_Y + 52, 0, 0, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE);
                }
            }
        }
        
        
        if (noBossesLoaded) {
            guiGraphics.drawWordWrap(font, Component.translatable("gui.boss_checklist.no_bosses_loaded"), bookX + 140, bookY + 53, 0xFF000000, GuiConstants.MAX_LABEL_WIDTH);
        } else if (noBossesFindedWhenSearch) {
            guiGraphics.drawString(font, Component.translatable("gui.boss_checklist.no_bosses_finded_when_search"), bookX + 140, bookY + 53, 0xFF000000, false);
        } else {
            guiGraphics.drawString(font, String.valueOf(leftPage), bookX + 187, bookY + 195, 0xFF45443F, false);
            guiGraphics.drawString(font, String.valueOf(rightPage), bookX + 320, bookY + 195, 0xFF45443F, false); 
        }

        
        renderTooltips(guiGraphics, mouseX, mouseY, partialTick, bookX, bookY);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // --- rendering the arrow icon in the statistics tab ---
        if (statisticsTabEnabled) {
            RenderSystem.enableBlend();
            if (showStatisticsTab) {
                guiGraphics.blit(GuiConstants.ARROW_UP, bookX + GuiConstants.STATS_TAB_X, bookY + GuiConstants.STATS_TAB_Y, 0, 0, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH);
            } else {
                guiGraphics.blit(GuiConstants.ARROW_DOWN, bookX + GuiConstants.STATS_TAB_X, bookY + GuiConstants.STATS_TAB_Y, 0, 0, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH);
            }
        }
    }





    private void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int bookX, int bookY) {
        if (DynamicConfigHandler.client().showConfigScreen) {
            if (mouseX >= bookX + BOOKMARK_SETTINGS_X && mouseX < bookX + BOOKMARK_SETTINGS_X + BOOKMARK_SETTINGS_WIDTH && mouseY >= bookY + BOOKMARK_SETTINGS_Y && mouseY < bookY + BOOKMARK_SETTINGS_Y + BOOKMARK_SETTINGS_HEIGHT) {
                guiGraphics.renderTooltip(
                    Minecraft.getInstance().font,
                    Component.literal(Component.translatable("gui.boss_checklist.settings").getString()),
                    mouseX, mouseY
                );
            }
        }
        if (showStatisticsTab) {
            if (mouseX >= bookX + GuiConstants.STATS_TAB_X + 3 && mouseX < bookX + GuiConstants.STATS_TAB_X + GuiConstants.STATS_TAB_WIDTH - 3 && mouseY >= bookY + GuiConstants.STATS_TAB_Y + 23 && mouseY < bookY + GuiConstants.STATS_TAB_Y + 34) {
                guiGraphics.renderTooltip(
                    Minecraft.getInstance().font,
                    Component.literal("1. " + ClientGlobalStatistics.top1 + " - §c" + ClientGlobalStatistics.damage1),
                    mouseX, mouseY
                );
            } else if (ClientGlobalStatistics.top2 != null) {
                if (mouseX >= bookX + GuiConstants.STATS_TAB_X + 3 && mouseX < bookX + GuiConstants.STATS_TAB_X + GuiConstants.STATS_TAB_WIDTH - 3 && mouseY >= bookY + GuiConstants.STATS_TAB_Y + 37 && mouseY < bookY + GuiConstants.STATS_TAB_Y + 48) {
                    guiGraphics.renderTooltip(
                        Minecraft.getInstance().font,
                        Component.literal("2. " + ClientGlobalStatistics.top2 + " - §c" + ClientGlobalStatistics.damage2),
                        mouseX, mouseY
                    );
                } else if (ClientGlobalStatistics.top3 != null) {
                    if (mouseX >= bookX + GuiConstants.STATS_TAB_X + 3 && mouseX < bookX + GuiConstants.STATS_TAB_X + GuiConstants.STATS_TAB_WIDTH - 3 && mouseY >= bookY + GuiConstants.STATS_TAB_Y + 53 && mouseY < bookY + GuiConstants.STATS_TAB_Y + 64) {
                        guiGraphics.renderTooltip(
                            Minecraft.getInstance().font,
                            Component.literal("3. " + ClientGlobalStatistics.top3 + " - §c" + ClientGlobalStatistics.damage3),
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
            this.skipNextRenderBackground = false;
            return;
        }
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
    }








    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;
        if (DynamicConfigHandler.client().showConfigScreen) {
            if (mouseX >= bookX + BOOKMARK_SETTINGS_X && mouseX <= bookX + BOOKMARK_SETTINGS_X + BOOKMARK_SETTINGS_WIDTH - 1 && mouseY >= bookY + BOOKMARK_SETTINGS_Y && mouseY <= bookY + BOOKMARK_SETTINGS_Y + BOOKMARK_SETTINGS_HEIGHT - 1) {
                minecraft.setScreen(new ConfigScreen(this));
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (BossChecklistClient.OPEN_CHECKLIST.matches(keyCode, scanCode)) {
            if (DynamicConfigHandler.client().searchBarEnabled) {
                if (!searchBox.canConsumeInput()) {
                    ClientEvents.openChecklistKeyWasDown = true;
                    onClose();
                    return true;
                }
            } else {
                ClientEvents.openChecklistKeyWasDown = true;
                onClose();
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (FROM_PAUSE_MENU) {
            this.minecraft.setScreen(new PauseScreen(true));
        } else {
            this.minecraft.setScreen(null);
        }
    }
}