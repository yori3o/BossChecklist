package com.yori3o.boss_checklist.common.client.gui;


import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.yori3o.boss_checklist.common.BossChecklistClient;
import com.yori3o.boss_checklist.common.client.ClientGlobalStatistics;
import com.yori3o.boss_checklist.common.client.boss.BossEntry;
import com.yori3o.boss_checklist.common.client.data.ClientDataSaver;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomButton;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomCheckbox;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomPageButton;
import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;
import com.yori3o.boss_checklist.common.util.LoggerUtil;
import com.yori3o.boss_checklist.common.client.data.BossNameCache;
import com.yori3o.boss_checklist.common.client.data.BossService;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
//import net.minecraft.client.renderer.RenderType; // FOR 1.21.4+

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;



public class BossChecklistScreen extends Screen {

    
    private static final int BOOKMARK_SETTINGS_x = 387;
    private static final int BOOKMARK_SETTINGS_y = 65;


    private static final int ELEMENTS_PER_PAGE = 7;
    private static int currentSpread = 0;
    private int totalSpreads;
    
    public static final int MAX_LABEL_WIDTH = 100;


    private static final int StatsInfoX = 86;
    private static final int StatsInfoY = 51;
    private static final int StatsInfoWidth = 17;
    private static final int StatsInfoHeight = 68;
    private static final int IconsSize = 10;

    private boolean showStats = false;
    private boolean showStatsInfo = false;

    private boolean showTop2 = false;
    private boolean showTop3 = false;

    
    public boolean invalidateNames = false;


    private EditBox searchBox;

    private List<CustomCheckbox> currentCheckboxes = new ArrayList<>();
    private CustomPageButton nextButton, prevButton;
    private CustomButton closeButton;
    private int leftPage;
    private int rightPage;

    private boolean skipNextRenderBackground = false;
    private boolean noBossesLoaded = false;
    private boolean noBossesFindedWhenSearch = false;
    private boolean checklistAreLoaded = false;
    private final boolean fromPauseMenu;

    private LinkedHashMap<String, Component> bosses = BossNameCache.CACHE; // read-only 
    private LinkedHashMap<String, Component> bossesFiltered = new LinkedHashMap<>();

    private int defeatedCount;
    private int totalCount;
    private float percent;



    public BossChecklistScreen(Boolean fromPauseMenu) {
        super(Component.literal("Checklist"));
        this.fromPauseMenu = fromPauseMenu;
    }



    public void init() {
        super.init();
        
        if (!checklistAreLoaded) {
            bossesFiltered.putAll(bosses);

            checklistAreLoaded = true;
        }
        if (invalidateNames) {
            BossNameCache.rebuild();
            bossesFiltered.clear();
            bossesFiltered.putAll(bosses);
        }

        if (!ClientGlobalStatistics.top1.equals("")) {
            showStats = true;
            if (!ClientGlobalStatistics.top2.equals("")) showTop2 = true;
            if (!ClientGlobalStatistics.top3.equals("")) showTop3 = true;

        }
        
        createButtons();
        updateSearch(null);
    }






    private void updateCounts() {
        defeatedCount = ClientDataSaver.defeatedBossesCount();
        totalCount = bossesFiltered.size();
        percent = totalCount > 0 ? (float) defeatedCount / totalCount : 0f;
        percent = Math.min(percent, 1);
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

        if (showStats) {
            CustomButton openStatsButton = new CustomButton(
                bookX + StatsInfoX, bookY + StatsInfoY, StatsInfoWidth, StatsInfoWidth, 0, 0, 
                Component.literal(""), 
                GuiConstants.SMALL_BUTTON_TEXTURE, GuiConstants.SMALL_BUTTON_TEXTURE_hovered, GuiConstants.SMALL_BUTTON_TEXTURE_pressed, null, 
                () -> {
                    showStatsInfo = !showStatsInfo;
                }
            );
            addRenderableWidget(openStatsButton);
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
        
        if (DynamicConfigHandler.searchBarEnabled_dynamic) {
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

            boolean isFresh = false;
            if (isThisBossDefeated && data.progress().isFresh())
                isFresh = true;

            CustomCheckbox cb = new CustomCheckbox(x, y, 300, name, isThisBossDefeated, true, isFresh,
                checked -> {
                        CompletableFuture.runAsync(() -> {
                            data.progress().markDefeatedClient(checked);
                            ClientDataSaver.setDefeated(bossId, checked);
                            updateCounts(); 
                        });
                },
                () -> {
                    Minecraft.getInstance().setScreen(new BossInfoScreen(this, bossId));
                    data.progress().clearFreshFlag();
                }
            );

            if (DynamicConfigHandler.animationsEnabled) {
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

        guiGraphics.blit(GuiConstants.PROGRESS_BAR_BACKGROUND, (this.width / 2) - (GuiConstants.BarWidth / 2), bookY + 27, 0, 0, 
            GuiConstants.BarWidth, GuiConstants.BarHeight, GuiConstants.BarWidth, GuiConstants.BarHeight);
        guiGraphics.blit(GuiConstants.PROGRESS_BAR_FILL, (this.width / 2) - (GuiConstants.BarWidth / 2), bookY + 27, 0, 0, 
            (int) (percent * GuiConstants.BarWidth), GuiConstants.BarHeight, GuiConstants.BarWidth, GuiConstants.BarHeight);

        if (DynamicConfigHandler.showConfigScreen) {
            guiGraphics.blit(GuiConstants.BOOKMARK_SETTINGS, bookX + BOOKMARK_SETTINGS_x, bookY + BOOKMARK_SETTINGS_y, 0, 0, 12, 22, 12, 22);
        }

        if (showStats) {
            RenderSystem.enableBlend();
            if (showStatsInfo) {
                guiGraphics.blit(GuiConstants.STATS_INFO_BACK, bookX + StatsInfoX, bookY + StatsInfoY, 0, 0, StatsInfoWidth, StatsInfoHeight, StatsInfoWidth, StatsInfoHeight);
                guiGraphics.blit(GuiConstants.TOP_1, bookX + StatsInfoX + 4, bookY + StatsInfoY + 24, 0, 0, IconsSize, IconsSize, IconsSize, IconsSize);
                if (showTop2) guiGraphics.blit(GuiConstants.TOP_2, bookX + StatsInfoX + 4, bookY + StatsInfoY + 38, 0, 0, IconsSize, IconsSize, IconsSize, IconsSize);
                if (showTop3) guiGraphics.blit(GuiConstants.TOP_3, bookX + StatsInfoX + 4, bookY + StatsInfoY + 52, 0, 0, IconsSize, IconsSize, IconsSize, IconsSize);
            }
        }

        guiGraphics.drawCenteredString(this.font, defeatedCount + " / " + totalCount, (this.width / 2), bookY + 20, 0xFFFFFFFF);
        
        if (noBossesLoaded) {
            guiGraphics.drawWordWrap(font, Component.translatable("gui.boss_checklist.no_bosses_loaded"), bookX + 140, bookY + 53, 0xFF000000, MAX_LABEL_WIDTH);
        } else if (noBossesFindedWhenSearch) {
            guiGraphics.drawString(font, Component.translatable("gui.boss_checklist.no_bosses_finded_when_search"), bookX + 140, bookY + 53, 0xFF000000, false);
        } else {
            guiGraphics.drawString(font, String.valueOf(leftPage), bookX + 187, bookY + 195, 0xFF45443F, false);
            guiGraphics.drawString(font, String.valueOf(rightPage), bookX + 320, bookY + 195, 0xFF45443F, false); 
        }

        
        renderTooltips(guiGraphics, mouseX, mouseY, partialTick, bookX, bookY);

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (showStats) {
            RenderSystem.enableBlend();
            if (showStatsInfo) {
                guiGraphics.blit(GuiConstants.ARROW_UP, bookX + StatsInfoX, bookY + StatsInfoY, 0, 0, StatsInfoWidth, StatsInfoWidth, StatsInfoWidth, StatsInfoWidth);
            } else {
                guiGraphics.blit(GuiConstants.ARROW_DOWN, bookX + StatsInfoX, bookY + StatsInfoY, 0, 0, StatsInfoWidth, StatsInfoWidth, StatsInfoWidth, StatsInfoWidth);
            }
        }
    }





    private void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, int bookX, int bookY) {
        if (DynamicConfigHandler.showConfigScreen) {
            if (mouseX >= bookX + BOOKMARK_SETTINGS_x && mouseX < bookX + BOOKMARK_SETTINGS_x + 12 && mouseY >= bookY + BOOKMARK_SETTINGS_y && mouseY < bookY + BOOKMARK_SETTINGS_y + 22) {
                guiGraphics.renderTooltip(
                    Minecraft.getInstance().font,
                    Component.literal(Component.translatable("gui.boss_checklist.settings").getString()),
                    mouseX, mouseY
                );
            }
        }
        if (showStats) {
            if (showStatsInfo) {
                if (mouseX >= bookX + StatsInfoX + 3 && mouseX < bookX + StatsInfoX + StatsInfoWidth - 3 && mouseY >= bookY + StatsInfoY + 23 && mouseY < bookY + StatsInfoY + 34) {
                    guiGraphics.renderTooltip(
                        Minecraft.getInstance().font,
                        Component.literal("1. " + ClientGlobalStatistics.top1 + " - §c" + ClientGlobalStatistics.damage1),
                        mouseX, mouseY
                    );
                } else if (showTop2) {
                    if (mouseX >= bookX + StatsInfoX + 3 && mouseX < bookX + StatsInfoX + StatsInfoWidth - 3 && mouseY >= bookY + StatsInfoY + 37 && mouseY < bookY + StatsInfoY + 48) {
                        guiGraphics.renderTooltip(
                            Minecraft.getInstance().font,
                            Component.literal("2. " + ClientGlobalStatistics.top2 + " - §c" + ClientGlobalStatistics.damage2),
                            mouseX, mouseY
                        );
                    } else if (showTop3) {
                        if (mouseX >= bookX + StatsInfoX + 3 && mouseX < bookX + StatsInfoX + StatsInfoWidth - 3 && mouseY >= bookY + StatsInfoY + 53 && mouseY < bookY + StatsInfoY + 64) {
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
        if (DynamicConfigHandler.showConfigScreen) {
            if (mouseX >= bookX + BOOKMARK_SETTINGS_x && mouseX <= bookX + BOOKMARK_SETTINGS_x + 11 && mouseY >= bookY + BOOKMARK_SETTINGS_y && mouseY <= bookY + BOOKMARK_SETTINGS_y + 22) {
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
            if (DynamicConfigHandler.searchBarEnabled_dynamic) {
                if (!searchBox.canConsumeInput()) {
                    onClose();
                    return true;
                }
            } else {
                onClose();
                return true;
            }
        } else if (keyCode == InputConstants.KEY_LEFT) {
            if (currentSpread > 0) {
                currentSpread--;
                updatePage();
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            }
            return true;
        } else if (keyCode == InputConstants.KEY_RIGHT) {
            if (currentSpread < totalSpreads - 1) {
                currentSpread++;
                updatePage();
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (fromPauseMenu) {
            this.minecraft.setScreen(new PauseScreen(true));
        } else {
            this.minecraft.setScreen(null);
        }
    }
}