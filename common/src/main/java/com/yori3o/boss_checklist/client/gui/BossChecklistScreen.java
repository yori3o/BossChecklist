package com.yori3o.boss_checklist.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;

import com.yori3o.boss_checklist.data.BossData;
import com.yori3o.boss_checklist.data.BossDataClientSaver;
import com.yori3o.boss_checklist.data.BossRegistry;
import com.yori3o.boss_checklist.init.BossChecklistKeyMapping;

public class BossChecklistScreen extends Screen {

    private static final BossDataClientSaver DATA_SAVER = new BossDataClientSaver();

    public static final Logger LOGGER = LogManager.getLogger("boss_checklist");

    private static final ResourceLocation bar_background = 
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/sprites/boss_bar/white_background.png");
    private static final ResourceLocation bar_progress = 
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/sprites/boss_bar/white_progress.png");

    private static final ResourceLocation background_texture = ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/boss_book.png");
    private static final int ELEMENTS_PER_PAGE = 7;
    private int currentSpread = 0;
    
    private int maxTextWidth = 95;
    private List<BossData> bossDataList = new ArrayList<>();

    private List<CustomCheckbox> currentCheckboxes = new ArrayList<>();
    private CustomPageButton nextButton;
    private CustomPageButton prevButton;
    private int leftPage;
    private int rightPage;

    private boolean skipNextRenderBackground = false;
    private boolean noBossesLoaded = false;
    private boolean bossesListAreLoaded = false;

    private List<Component> bosses = new ArrayList<>();
    private List<String> bossIds = new ArrayList<>();

    private int defeatedCount;
    private int totalCount;
    private float percent;

    public BossChecklistScreen() {
        super(Component.literal("Checklist"));
    }

    public void init() {
        super.init();

        if (!bossesListAreLoaded) {
            bossesListAreLoaded = true;

            bossDataList = BossRegistry.all().stream().toList();

            // create list ID
            bossIds.addAll(BossRegistry.all().stream()
                .map(BossData::getId)
                .toList());
        
            // create truncated names list 
            for (BossData b : bossDataList) {
                //if (ClientConfig.hideUndefeatedBossInfo && !BossDataClientSaver.defeatedBossesIds_InWorld.contains(b.getId())) {
                //    bosses.add(Component.literal("???"));
                //} else {
                    // translated name
                    String translated = Component.translatable("boss_checklist.boss." + b.getId()).getString();
                    // truncate
                    String truncated = TextUtil.truncateText(translated, maxTextWidth).getString();

                    bosses.add(Component.literal(truncated));
                //}
            }
        }
        
        updateCounts();
        createPageButtons();
        updatePage();
    }

    private void updateCounts() {
        defeatedCount = DATA_SAVER.defeatedBossesCount();
        totalCount = bossIds.size();
        percent = totalCount > 0 ? (float) defeatedCount / totalCount : 0f;
    }

    private void createPageButtons() {
        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;

        double totalPages = Math.ceil((double) bosses.size() / ELEMENTS_PER_PAGE);
        int totalSpreads = (int) Math.ceil(totalPages / 2.0);

        prevButton = new CustomPageButton(bookX + 138, bookY + 190, true, () -> {
            if (currentSpread > 0) {
                currentSpread--;
                updatePage();
            }
        });

        nextButton = new CustomPageButton(bookX + 350, bookY + 190, false, () -> {
            if (currentSpread < totalSpreads - 1) {
                currentSpread++;
                updatePage();
            }
        });

        addRenderableWidget(prevButton);
        addRenderableWidget(nextButton);
    }

    private void updatePage() {
        if (bosses.isEmpty()) {
            LOGGER.warn("No bosses loaded at checklist!");
            prevButton.visible = false;
            nextButton.visible = false;
            noBossesLoaded = true;
            return;
        }
        for (CustomCheckbox cb : currentCheckboxes) {
            removeWidget(cb);
        }
        currentCheckboxes.clear();

        double totalPages = Math.ceil((double) bosses.size() / ELEMENTS_PER_PAGE);
        int totalSpreads = (int) Math.ceil(totalPages / 2.0);

        int fromIndex = currentSpread * (ELEMENTS_PER_PAGE * 2);
        int toIndex = Math.min(fromIndex + ELEMENTS_PER_PAGE * 2, bosses.size());

        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;

        int leftX = bookX + 134;
        int rightX = bookX + 266;
        int startY = bookY + 56;

        for (int i = fromIndex; i < toIndex; i++) {
            String boss = bosses.get(i).getString();
            String bossId = bossIds.get(i);
            int localIndex = i - fromIndex;

            int x = (localIndex < ELEMENTS_PER_PAGE) ? leftX : rightX;
            int y = startY + (localIndex % ELEMENTS_PER_PAGE) * 18;

            CustomCheckbox cb = new CustomCheckbox(x, y, Component.literal(boss), DATA_SAVER.isBossDefeated(bossId),
                checked -> {BossDataClientSaver.BossDefeated(bossId, checked); updateCounts();},
                () -> Minecraft.getInstance().setScreen(new BossInfoScreen(this, bossId))
            );

            addRenderableWidget(cb);
            currentCheckboxes.add(cb);
        }
        
        prevButton.visible = currentSpread > 0;
        nextButton.visible = currentSpread < totalSpreads - 1;
        
        leftPage = currentSpread * 2 + 1;
        rightPage = currentSpread * 2 + 2;
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

        guiGraphics.blit(bar_background, bookX + 165, bookY + 29, 0, 0, 182, 5, 182, 5);
        guiGraphics.blit(bar_progress, bookX + 165, bookY + 29, 0, 0, (int)(percent * 182), 5, 182, 5);

        
        guiGraphics.drawString(this.font, defeatedCount + " / " + totalCount, (this.width - Minecraft.getInstance().font.width(defeatedCount + " / " + totalCount)) / 2, bookY + 20, 0xFFFFFF, false);
        

        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        if (noBossesLoaded) {
            guiGraphics.drawString(font, "No bosses loaded!", bookX + 140, bookY + 53, 0x000000, false);
        }
        else {
            guiGraphics.drawString(font, String.valueOf(leftPage), bookX + 187, bookY + 195, 0x000000, false);
            guiGraphics.drawString(font, String.valueOf(rightPage), bookX + 320, bookY + 195, 0x000000, false); 
        }
    }

    

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (BossChecklistKeyMapping.OPEN_CHECKLIST.matches(keyCode, scanCode)) {
            this.minecraft.setScreen(null); // null = close screen
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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
}