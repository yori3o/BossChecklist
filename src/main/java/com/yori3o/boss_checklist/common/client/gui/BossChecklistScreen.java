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
import com.yori3o.boss_checklist.common.util.TooltipUtil;
import com.yori3o.boss_checklist.common.client.data.BossNameCache;
import com.yori3o.boss_checklist.common.client.data.BossService;

 
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;



public class BossChecklistScreen extends Screen {

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


    public void reloadNamesMap() {
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
            flipPage(-1);
        });
        addRenderableWidget(prevButton);

        nextButton = new CustomPageButton(bookX + 350, bookY + 190, false, () -> {
            flipPage(1);
        });
        addRenderableWidget(nextButton);

        if (statisticsTabEnabled) {
            CustomButton openStatsButton = new CustomButton(
                bookX + GuiConstants.STATS_TAB_X, bookY + GuiConstants.STATS_TAB_Y, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, 0, 0, 
                Component.empty(), 
                GuiConstants.SMALL_BUTTON_TEXTURE, GuiConstants.SMALL_BUTTON_TEXTURE_hovered, GuiConstants.SMALL_BUTTON_TEXTURE_pressed, null, 
                () -> {
                    showStatisticsTab = !showStatisticsTab;
                }
            );
            addRenderableWidget(openStatsButton);
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

        
        if (DynamicConfigHandler.client().showEditorButton) {
            CustomButton openEditorButton = new CustomButton(
                bookX + GuiConstants.CLOSE_BUTTON_X, bookY + GuiConstants.CLOSE_BUTTON_Y + 160, GuiConstants.EDITOR_BUTTON_SIZE, GuiConstants.EDITOR_BUTTON_SIZE, 0, 0, 
                Component.empty(), 
                GuiConstants.EDITOR_BUTTON_TEXTURE, GuiConstants.EDITOR_BUTTON_TEXTURE_hovered, GuiConstants.EDITOR_BUTTON_TEXTURE_hovered, null,
                () -> {
                    this.minecraft.gui.setScreen(new EditorScreen(this));
                }
            );
            addRenderableWidget(openEditorButton);
        }

        if (DynamicConfigHandler.client().showConfigScreen) {
            CustomButton openConfigButton = new CustomButton(
                bookX + GuiConstants.CONFIG_BUTTON_X, bookY + GuiConstants.CONFIG_BUTTON_Y, GuiConstants.CONFIG_BUTTONS_WIDTH, GuiConstants.CONFIG_BUTTONS_HEIGHT, 0, 0, 
                Component.empty(), 
                GuiConstants.CONFIG_BUTTON_TEXTURE, GuiConstants.CONFIG_BUTTON_TEXTURE_highlighted, GuiConstants.CONFIG_BUTTON_TEXTURE_highlighted, null,
                () -> {
                    this.minecraft.gui.setScreen(new ConfigScreen(this));
                }
            );
            addRenderableWidget(openConfigButton);
        }
        
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


    public void flipPage(int delta) {
        if (delta == 0) return;
        if (delta > 0) {
            if (currentSpread < totalSpreads - 1) {
                currentSpread++;
                updatePage();
            }
        } else {
            if (currentSpread > 0) {
                currentSpread--;
                updatePage();
            }
        }
    }

    public void updatePage() {
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

            CustomCheckbox cb = new CustomCheckbox(x, y, name, 300, isThisBossDefeated,
                checked -> {
                    ClientDataSaver.setDefeated(bossId, checked, data.progress());
                    data.progress().clearFreshFlag();
                    updateCounts(); 
                }
            );
            cb.setOnLabelClick(
                () -> {
                    Minecraft.getInstance().gui.setScreen(new BossInfoScreen(this, bossId));
                    data.progress().clearFreshFlag();
                }
            );
            cb.setRenderNotice(data.progress().isFresh());
            cb.setBossToMoveWithMouseScroll(this, data);

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
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        

        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;


        // FOR 1.21.4+ - add RenderPipelines.GUI_TEXTURED, as first argument and delete all  
        //guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.BOOK, bookX, bookY, 0, 0, 512, 256, 512, 256);

        if (DynamicConfigHandler.client().progressBarEnabled) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.PROGRESS_BAR_BACKGROUND, (this.width / 2) - (GuiConstants.BAR_WIDTH / 2), bookY + 27, 0, 0, 
                GuiConstants.BAR_WIDTH, GuiConstants.BAR_HEIGHT, GuiConstants.BAR_WIDTH, GuiConstants.BAR_HEIGHT);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.PROGRESS_BAR_FILL, (this.width / 2) - (GuiConstants.BAR_WIDTH / 2), bookY + 27, 0, 0, 
                (int) (percent * GuiConstants.BAR_WIDTH), GuiConstants.BAR_HEIGHT, GuiConstants.BAR_WIDTH, GuiConstants.BAR_HEIGHT);
            guiGraphics.centeredText(this.font, defeatedCount + " / " + totalCount, (this.width / 2), bookY + 20, 0xFFFFFFFF);
        }

        // --- rendering statistics ---
        if (showStatisticsTab) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.STATS_INFO_BACK, bookX + GuiConstants.STATS_TAB_X, bookY + GuiConstants.STATS_TAB_Y, 0, 0, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_HEIGHT, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_HEIGHT);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.TOP_1, bookX + GuiConstants.STATS_TAB_X + 4, bookY + GuiConstants.STATS_TAB_Y + 24, 0, 0, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE);
            if (ClientGlobalStatistics.top2 != null) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.TOP_2, bookX + GuiConstants.STATS_TAB_X + 4, bookY + GuiConstants.STATS_TAB_Y + 38, 0, 0, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE);
                if (ClientGlobalStatistics.top3 != null) {
                    guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.TOP_3, bookX + GuiConstants.STATS_TAB_X + 4, bookY + GuiConstants.STATS_TAB_Y + 52, 0, 0, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE);
                }
            }
        }
        
        
        if (noBossesLoaded) {
            guiGraphics.textWithWordWrap(font, Component.translatable("gui.boss_checklist.no_bosses_loaded"), bookX + 137, bookY + 54, GuiConstants.MAX_LABEL_WIDTH, 0xFF000000, false);
        } else if (noBossesFindedWhenSearch) {
            guiGraphics.text(font, Component.translatable("gui.boss_checklist.no_bosses_finded_when_search"), bookX + 137, bookY + 54, 0xFF000000, false);
        } else {
            guiGraphics.text(font, String.valueOf(leftPage), bookX + 187, bookY + 195, 0xFF45443F, false);
            guiGraphics.text(font, String.valueOf(rightPage), bookX + 320, bookY + 195, 0xFF45443F, false); 
        }

    
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        renderTooltips(guiGraphics, mouseX, mouseY, partialTick, bookX, bookY);

        // --- rendering the arrow icon in the statistics tab ---
        if (statisticsTabEnabled) {
            if (showStatisticsTab) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.ARROW_UP, bookX + GuiConstants.STATS_TAB_X, bookY + GuiConstants.STATS_TAB_Y, 0, 0, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH);
            } else {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.ARROW_DOWN, bookX + GuiConstants.STATS_TAB_X, bookY + GuiConstants.STATS_TAB_Y, 0, 0, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH);
            }
        }
    }




    private void renderTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, int bookX, int bookY) {
        if (showStatisticsTab) {
            if (mouseX >= bookX + GuiConstants.STATS_TAB_X + 3 && mouseX < bookX + GuiConstants.STATS_TAB_X + GuiConstants.STATS_TAB_WIDTH - 3 && mouseY >= bookY + GuiConstants.STATS_TAB_Y + 23 && mouseY < bookY + GuiConstants.STATS_TAB_Y + 34) {
                TooltipUtil.renderTooltip(
                    guiGraphics,
                    "1. " + ClientGlobalStatistics.top1 + " - §c" + ClientGlobalStatistics.damage1,
                    mouseX, mouseY
                );
            } else if (ClientGlobalStatistics.top2 != null) {
                if (mouseX >= bookX + GuiConstants.STATS_TAB_X + 3 && mouseX < bookX + GuiConstants.STATS_TAB_X + GuiConstants.STATS_TAB_WIDTH - 3 && mouseY >= bookY + GuiConstants.STATS_TAB_Y + 37 && mouseY < bookY + GuiConstants.STATS_TAB_Y + 48) {
                    TooltipUtil.renderTooltip(
                        guiGraphics,
                        "2. " + ClientGlobalStatistics.top2 + " - §c" + ClientGlobalStatistics.damage2,
                        mouseX, mouseY
                    );
                } else if (ClientGlobalStatistics.top3 != null) {
                    if (mouseX >= bookX + GuiConstants.STATS_TAB_X + 3 && mouseX < bookX + GuiConstants.STATS_TAB_X + GuiConstants.STATS_TAB_WIDTH - 3 && mouseY >= bookY + GuiConstants.STATS_TAB_Y + 53 && mouseY < bookY + GuiConstants.STATS_TAB_Y + 64) {
                        TooltipUtil.renderTooltip(
                            guiGraphics,
                            "3. " + ClientGlobalStatistics.top3 + " - §c" + ClientGlobalStatistics.damage3,
                            mouseX, mouseY
                        );
                    }
                }
            }
        }
    }




    // ONLY FOR 1.21.1+
    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);

        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.BOOK, bookX, bookY, 0, 0, 512, 256, 512, 256);
    }







    
    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (BossChecklistClient.OPEN_CHECKLIST.matches(keyEvent)) {
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
        return super.keyPressed(keyEvent);
    }

    @Override
    public void onClose() {
        if (FROM_PAUSE_MENU) {
            this.minecraft.gui.setScreen(new PauseScreen(true));
        } else {
            this.minecraft.gui.setScreen(null);
        }
    }
}