package com.yori3o.boss_checklist.common.client.gui;


import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;
import com.yori3o.boss_checklist.common.util.LoggerUtil;
import com.yori3o.boss_checklist.common.util.TooltipUtil;
import com.yori3o.boss_checklist.impl.PlatformUtil;
import com.yori3o.boss_checklist.common.client.boss.BossEntry;
import com.yori3o.boss_checklist.common.client.boss.CustomBossEntry;
import com.yori3o.boss_checklist.common.client.data.BossDefeatedHandler;
import com.yori3o.boss_checklist.common.client.data.BossService;
import com.yori3o.boss_checklist.common.client.data.ClientBossAttempt;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomButton;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.yori3o.boss_checklist.common.BossChecklistClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.ArrayList;
import java.util.List;



public class BossInfoScreen extends Screen {


    
    private ResourceLocation BOSS_IMG;
    private int modNameYoffset;
    private Component summonText;
    private boolean dropsAreLoaded;
    private LivingEntity entity;
    private boolean isBossDefeatedInWorld;
    private boolean isLocalServerOrAnyoneBossKilled;
    private int bossHealth;
    private int bossArmor;
    private boolean additionalInfo;
    private boolean showInfo = true;
    private boolean wikiLinkEnabled;
    private String wikiLink = "";
    private boolean showAdditionalInfo = true;
    private List<String> drops = new ArrayList<>();

    private final Screen parent;
    private final BossEntry boss;
    private String bossName;
    private String modName;

    private CustomButton dropButton, spawnButton, openAttemptButton, closeButton;

    private enum InfoTab { NONE, DROP, SPAWN }
    private InfoTab currentTab = InfoTab.NONE;

    private long lastTime;
    private float rotationY;
    private float rotationX;
    private boolean dragging = false;
    private boolean allowRotation = true;

    private ClientBossAttempt lastAttempt;
    private boolean showLastAttempt = false;
    private boolean showLastAttemptInfo = false;
    private String[] top3names;
    private String[] top3damages;

    private List<Component> tooltip_health = new ArrayList<>();
    private String tooltip_notDefeated;
    private List<Component> tooltip_defeated = new ArrayList<>();
    private List<Component> tooltip_attemptTime = new ArrayList<>();
    private List<Component> tooltip_attemptTop3 = new ArrayList<>();
    private String tooltip_duration = "";

    private Component additionalInfoComponent;

    private boolean ignoreProgressionMode;
    private boolean entityNotLivingError;


    
    

    public BossInfoScreen(Screen parent, String bossId) {
        super(Component.literal("Boss Info"));
        lastTime = System.nanoTime();
        this.parent = parent;
        this.boss = BossService.get(bossId);

        if (boss.id().equals("minecraft:wither")) {
            if (PlatformUtil.isModLoaded("witherreincarnated")) {
                modName = Component.translatable("boss_checklist.mod.witherreincarnated").getString();
            } else {
                modName = Component.translatable("boss_checklist.mod." + boss.definition().modId()).getString();
            }
        } if (boss.id().equals("minecraft:ender_dragon")) {
            if (PlatformUtil.isModLoaded("endertrigon")) {
                modName = Component.translatable("boss_checklist.mod.endertrigon").getString();
            } else {
                modName = Component.translatable("boss_checklist.mod." + boss.definition().modId()).getString();
            }
        } else {
            modName = Component.translatable("boss_checklist.mod." + boss.definition().modId()).getString();
        }
        bossName = Component.translatable("boss_checklist.boss." + boss.id().replace(":", "_")).getString();
        summonText = Component.translatable("boss_checklist.summon." + boss.id().replace(":", "_"));
        additionalInfoComponent = Component.translatable("boss_checklist.info." + boss.id().replace(":", "_"));
    }

    public BossInfoScreen(Screen parent, CustomBossEntry boss) { // for preview in editor
        super(Component.literal("Boss Info"));
        lastTime = System.nanoTime();
        this.parent = parent;
        this.boss = boss;
        bossName = boss.name();
        modName = boss.modName();
        summonText = Component.literal(boss.spawnInfo());
        additionalInfoComponent = Component.literal(boss.addtlInfo());
        ignoreProgressionMode = true;
    }





    public void init() {
        super.init();

        if (boss.definition().brokenModel()) {
            BOSS_IMG = ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/bosses/" + boss.id().replace(":", "_") + ".png");
        } else {
            rotationY = boss.definition().rotateY();
        }

        entity = createEntityFromId(boss.id());

        modNameYoffset = (font.split(Component.literal("§l" + bossName), 110).size() * font.lineHeight) + 55;

        if (entity != null) {
            if (boss.definition().health() == -1) {
                bossHealth = (int)entity.getAttributeValue(Attributes.MAX_HEALTH);
            } else {
                bossHealth = boss.definition().health();
            }
            if (boss.definition().armor() == -1) {
                bossArmor = (int)entity.getAttributeValue(Attributes.ARMOR);
            } else {
                bossArmor = boss.definition().armor();
            }

            isBossDefeatedInWorld = boss.progress().isDefeated();
            isLocalServerOrAnyoneBossKilled = BossDefeatedHandler.anyoneBossKilledOnce || Minecraft.getInstance().isLocalServer();

            if (isBossDefeatedInWorld) {
                String killerName = boss.progress().killerName();

                tooltip_defeated.clear();
                if (killerName.equals("")) {
                    if (boss.definition().type() == 2) {
                        tooltip_defeated.add(Component.literal(Component.translatable("gui.boss_checklist.miniboss_defeated").getString()));
                    } else {
                        tooltip_defeated.add(Component.literal(Component.translatable("gui.boss_checklist.defeated").getString()));
                    }
                } else { 
                    if (boss.definition().type() == 2) {
                        tooltip_defeated.add(Component.literal(Component.translatable("gui.boss_checklist.miniboss_defeated").getString()));
                    } else {
                        tooltip_defeated.add(Component.literal(Component.translatable("gui.boss_checklist.defeated").getString()));
                    }
                    tooltip_defeated.add(Component.literal(Component.translatable("gui.boss_checklist.killer_name").getString() + killerName));
                }  
            } else {
                if (boss.definition().type() == 2) {
                    tooltip_notDefeated = Component.translatable("gui.boss_checklist.miniboss_not_defeated").getString();
                } else {
                    tooltip_notDefeated = Component.translatable("gui.boss_checklist.not_defeated").getString();
                }
            }

            additionalInfo = boss.definition().additionalInfo();

            if (!ignoreProgressionMode) {
                if (DynamicConfigHandler.client().progressionMode && !isBossDefeatedInWorld) {
                    showInfo = false;
                    bossName = "???";
                    if (DynamicConfigHandler.client().progressionModePlus) {
                        showAdditionalInfo = false;
                        modName = "???";
                        wikiLink = "";
                        additionalInfo = false;
                    }
                }
            }
            
            if (!boss.definition().wikiLink().equals("")) {
                wikiLinkEnabled = true;
                wikiLink = boss.definition().wikiLink();
            }

            if (!dropsAreLoaded) {
                for (String dropId : boss.definition().drops()) {
                    if (PlatformUtil.isModLoaded(dropId.split(":")[0])) {
                        drops.add(dropId);
                    }
                }

                if (PlatformUtil.isModLoaded("endrem_additions") && boss.id().equals("bosses_of_mass_destruction:void_blossom")) { // it for my other mod
                    drops.add("endrem:blossom_eye");
                }
                
                dropsAreLoaded = true;
            }

            lastAttempt = boss.progress().lastAttempt();
            if (lastAttempt != null) {
                showLastAttempt = true;
                tooltip_duration = Component.translatable("gui.boss_checklist.duration").getString() + "§l" + lastAttempt.duration;
                top3names = lastAttempt.damageMap_top3.keySet().toArray(new String[lastAttempt.damageMap_top3.size()]);
                top3damages = lastAttempt.damageMap_top3.values().toArray(new String[lastAttempt.damageMap_top3.size()]);
                tooltip_attemptTime.clear();
                tooltip_attemptTop3.clear();
                tooltip_attemptTime.add(Component.literal(Component.translatable("gui.boss_checklist.last_battle").getString()));
                tooltip_attemptTime.add(Component.literal("§l" + lastAttempt.dateAndTime));
                if (lastAttempt.damageMap_top3.size() >= 1) {  
                    tooltip_attemptTop3.add(Component.literal(Component.translatable("gui.boss_checklist.top_players").getString()));
                    tooltip_attemptTop3.add(Component.literal("1. " + top3names[0] + " - §c" + top3damages[0]));
                    if (lastAttempt.damageMap_top3.size() >= 2) {
                        tooltip_attemptTop3.add(Component.literal("2. " + top3names[1] + " - §c" + top3damages[1]));
                        if (lastAttempt.damageMap_top3.size() >= 3) {
                            tooltip_attemptTop3.add(Component.literal("3. " + top3names[2] + " - §c" + top3damages[2]));
                        }
                    }
                }
            }
        
            tooltip_health.clear();
            if (DynamicConfigHandler.client().showHealthAndArmor) {
                if (showInfo) {
                    tooltip_health.add(Component.literal(Component.translatable("gui.boss_checklist.health").getString() + bossHealth));
                    tooltip_health.add(Component.literal(Component.translatable("gui.boss_checklist.armor").getString() + bossArmor));
                } else {
                    tooltip_health.add(Component.literal(Component.translatable("gui.boss_checklist.health").getString() + "?"));
                    tooltip_health.add(Component.literal(Component.translatable("gui.boss_checklist.armor").getString() + "?"));
                }
            }
            String bossVersion = boss.definition().modVersion();

            if (!bossVersion.equals("")) {
                String installed_version = PlatformUtil.getVerison(boss.definition().modId());

                if (bossVersion.equals(installed_version)) {
                    tooltip_health.add(Component.literal("§7" + Component.translatable("gui.boss_checklist.boss_version").getString() + bossVersion));
                    tooltip_health.add(Component.literal("§7" + Component.translatable("gui.boss_checklist.mod_version").getString() + installed_version));
                } else {
                    tooltip_health.add(Component.literal("§7" + Component.translatable("gui.boss_checklist.boss_version").getString() + "§4" + bossVersion));
                    tooltip_health.add(Component.literal("§7" + Component.translatable("gui.boss_checklist.mod_version").getString() + "§4" + installed_version));
                }
                
            }
        }
        createButtons();
    }











    private LivingEntity createEntityFromId(String id) {
        ResourceLocation i = ResourceLocation.parse(id);

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(i);
        if (type == null) return null;
        
        Entity entity = type.create(Minecraft.getInstance().level);
        
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity;
        } else {
            entityNotLivingError = true;
            return null;
        }
    }




    private void createButtons() {
        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;

        if (showInfo) {
            dropButton = new CustomButton(
                bookX + GuiConstants.DROP_BUTTON_X, bookY + GuiConstants.DROP_BUTTON_Y, GuiConstants.MEDIUM_BUTTONS_WIDTH, GuiConstants.MEDIUM_BUTTONS_HEIGHT, 
                GuiConstants.MEDIUM_BUTTONS_OVERLAY_WIDTH, GuiConstants.MEDIUM_BUTTONS_OVERLAY_HEIGHT, 
                Component.translatable("gui.boss_checklist.drop"), 
                GuiConstants.BUTTON_TEXTURE, GuiConstants.BUTTON_TEXTURE_hovered, GuiConstants.BUTTON_TEXTURE_pressed, GuiConstants.BUTTON_TEXTURE_overlay, 
                () -> {
                    currentTab = InfoTab.DROP;
                }
            );
        } else {
            dropButton = new CustomButton(
                bookX + GuiConstants.DROP_BUTTON_X, bookY + GuiConstants.DROP_BUTTON_Y, GuiConstants.MEDIUM_BUTTONS_WIDTH, GuiConstants.MEDIUM_BUTTONS_HEIGHT, 
                GuiConstants.MEDIUM_BUTTONS_OVERLAY_WIDTH, GuiConstants.MEDIUM_BUTTONS_OVERLAY_HEIGHT, 
                Component.translatable("gui.boss_checklist.drop"), GuiConstants.BUTTON_TEXTURE, GuiConstants.BUTTON_TEXTURE, GuiConstants.BUTTON_TEXTURE, GuiConstants.BUTTON_TEXTURE_overlay, () -> {
            });
        }

        if (showAdditionalInfo) {
            spawnButton = new CustomButton(bookX + GuiConstants.SPAWN_BUTTON_X, bookY + GuiConstants.SPAWN_BUTTON_Y, GuiConstants.MEDIUM_BUTTONS_WIDTH, GuiConstants.MEDIUM_BUTTONS_HEIGHT, GuiConstants.MEDIUM_BUTTONS_OVERLAY_WIDTH, GuiConstants.MEDIUM_BUTTONS_OVERLAY_HEIGHT, Component.translatable("gui.boss_checklist.spawn_info"), GuiConstants.BUTTON_TEXTURE, GuiConstants.BUTTON_TEXTURE_hovered, GuiConstants.BUTTON_TEXTURE_pressed, GuiConstants.BUTTON_TEXTURE_overlay, () -> {
                currentTab = InfoTab.SPAWN;
            });
        } else {
            spawnButton = new CustomButton(bookX + GuiConstants.SPAWN_BUTTON_X, bookY + GuiConstants.SPAWN_BUTTON_Y, GuiConstants.MEDIUM_BUTTONS_WIDTH, GuiConstants.MEDIUM_BUTTONS_HEIGHT, GuiConstants.MEDIUM_BUTTONS_OVERLAY_WIDTH, GuiConstants.MEDIUM_BUTTONS_OVERLAY_HEIGHT, Component.translatable("gui.boss_checklist.spawn_info"), GuiConstants.BUTTON_TEXTURE, GuiConstants.BUTTON_TEXTURE, GuiConstants.BUTTON_TEXTURE, GuiConstants.BUTTON_TEXTURE_overlay, () -> {
            });
        }

        if (showLastAttempt) {
            openAttemptButton = new CustomButton(bookX + GuiConstants.STATS_TAB_X, bookY + GuiConstants.STATS_TAB_Y, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, 0, 0, Component.empty(), GuiConstants.SMALL_BUTTON_TEXTURE, GuiConstants.SMALL_BUTTON_TEXTURE_hovered, GuiConstants.SMALL_BUTTON_TEXTURE_pressed, null, () -> {
                showLastAttemptInfo = !showLastAttemptInfo;
            });
            addRenderableWidget(openAttemptButton);
        }

        closeButton = new CustomButton(bookX + GuiConstants.CLOSE_BUTTON_X, bookY + GuiConstants.CLOSE_BUTTON_Y, GuiConstants.CLOSE_BUTTON_SIZE, GuiConstants.CLOSE_BUTTON_SIZE, 0, 0, Component.empty(), GuiConstants.CLOSE_BUTTON_TEXTURE, GuiConstants.CLOSE_BUTTON_TEXTURE_hovered, GuiConstants.CLOSE_BUTTON_TEXTURE_hovered, null, () -> {
            onClose();
        });

        addRenderableWidget(closeButton);
        if (entity == null) return;
        addRenderableWidget(dropButton);
        addRenderableWidget(spawnButton);
    }








    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;

        // --- for correct rotation of the model ---
        if (DynamicConfigHandler.client().animationsEnabled) {
            long now = System.nanoTime();
            float delta = (now - lastTime) / 1_000_000_000.0f; // seconds
            lastTime = now;

            if (allowRotation) {
                rotationY += 7.5f * delta;
            }
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);

        guiGraphics.blit(GuiConstants.BOOKMARK, bookX + 145, bookY + 210, 0, 0, 16, 27, 16, 27);
        if (isLocalServerOrAnyoneBossKilled) {
            if (isBossDefeatedInWorld) {
                guiGraphics.blit(GuiConstants.BOOKMARK_DEFEATED, bookX + 172, bookY + 209, 0, 0, 16, 27, 16, 27);
            } else {
                guiGraphics.blit(GuiConstants.BOOKMARK_NOT_DEFEATED, bookX + 172, bookY + 209, 0, 0, 16, 27, 16, 27);
            }
        }
        if (additionalInfo && showInfo) {
            guiGraphics.blit(GuiConstants.BOOKMARK_INFO, bookX + 199, bookY + 209, 0, 0, 16, 27, 16, 27);
        }
        if (wikiLinkEnabled && showAdditionalInfo) {
            guiGraphics.blit(GuiConstants.BOOKMARK_WIKI, bookX + 350, bookY + 210, 0, 0, 16, 27, 16, 27);
        }
        if (showLastAttempt) {
            if (showLastAttemptInfo) {
                RenderSystem.enableBlend();
                guiGraphics.blit(GuiConstants.STATS_INFO_BACK, bookX + GuiConstants.STATS_TAB_X, bookY + GuiConstants.STATS_TAB_Y, 0, 0, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_HEIGHT, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_HEIGHT);
                guiGraphics.blit(GuiConstants.BATTLE_ICON, bookX + GuiConstants.STATS_TAB_X + 4, bookY + GuiConstants.STATS_TAB_Y + 24, 0, 0, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE);
                guiGraphics.blit(GuiConstants.DURATION_ICON, bookX + GuiConstants.STATS_TAB_X + 4, bookY + GuiConstants.STATS_TAB_Y + 38, 0, 0, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE);
                if (!lastAttempt.damageMap_top3.isEmpty()) {
                    guiGraphics.blit(GuiConstants.TOP_ICON, bookX + GuiConstants.STATS_TAB_X + 4, bookY + GuiConstants.STATS_TAB_Y + 52, 0, 0, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE);
                }
                RenderSystem.disableBlend();
            }
        }

        guiGraphics.drawWordWrap(font, Component.literal("§l" + bossName), bookX + 137, bookY + 53, 110, 0xFF000000);

        guiGraphics.drawWordWrap(font, Component.literal(modName), bookX + 137, bookY + modNameYoffset, 110, 0xFF616161);


        if (showInfo) {
            if (boss.definition().brokenModel()) {
                RenderSystem.enableBlend();
                guiGraphics.blit(BOSS_IMG, bookX + 137, bookY + 100, 0, 0, 100, 100, 100, 100);
                RenderSystem.disableBlend();
            } else {
                renderEntityInGui(guiGraphics, bookX + 132 + 56, bookY + 90 + 85, boss.definition().scale(), partialTick);
            }
        }


        

        renderTooltips(guiGraphics, mouseX, mouseY, bookX, bookY);

        if (currentTab == InfoTab.DROP) {
            renderDrops(guiGraphics, mouseX, mouseY, bookX + 265, bookY + 80);
        } else if (currentTab == InfoTab.SPAWN) {
            guiGraphics.drawWordWrap(font,  summonText,  bookX + 265,  bookY + 80, 117, 0xFF000000);
        }

        if (showLastAttempt) {
            RenderSystem.enableBlend();
             
            if (showLastAttemptInfo) {
                guiGraphics.blit(GuiConstants.ARROW_UP, bookX + GuiConstants.STATS_TAB_X, bookY + GuiConstants.STATS_TAB_Y, 0, 0, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH);
            } else {
                guiGraphics.blit(GuiConstants.ARROW_DOWN, bookX + GuiConstants.STATS_TAB_X, bookY + GuiConstants.STATS_TAB_Y, 0, 0, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH);
            }
            RenderSystem.disableBlend();
        }

        if (entity == null) {
            if (entityNotLivingError) {
                guiGraphics.drawWordWrap(this.font, Component.translatable("gui.boss_checklist.entity_not_living"), bookX + GuiConstants.DROP_BUTTON_X, bookY + GuiConstants.DROP_BUTTON_Y + 8, 115, 0xFF616161);
            } else {
                guiGraphics.drawWordWrap(this.font, Component.translatable("gui.boss_checklist.no_entity").append("\"" + boss.id() + "\""), bookX + GuiConstants.DROP_BUTTON_X, bookY + GuiConstants.DROP_BUTTON_Y + 8, 115, 0xFF616161);
            }
        }

        // makes buttons gray if they are disabled
        if (!showInfo) {
            guiGraphics.fill(bookX + GuiConstants.DROP_BUTTON_X, bookY + GuiConstants.DROP_BUTTON_Y, bookX + GuiConstants.DROP_BUTTON_X + GuiConstants.MEDIUM_BUTTONS_WIDTH, bookY + GuiConstants.DROP_BUTTON_Y + GuiConstants.MEDIUM_BUTTONS_HEIGHT, 0x88AAAAAA);
            if (!showAdditionalInfo) {
                guiGraphics.fill(bookX + GuiConstants.SPAWN_BUTTON_X, bookY + GuiConstants.SPAWN_BUTTON_Y, bookX + GuiConstants.SPAWN_BUTTON_X + GuiConstants.MEDIUM_BUTTONS_WIDTH, bookY + GuiConstants.SPAWN_BUTTON_Y + GuiConstants.MEDIUM_BUTTONS_HEIGHT, 0x88AAAAAA);
            }
        }
    }





    public void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY, int bookX, int bookY) {
        if (entity == null) return;
        // health and armor info
        if (mouseX >= bookX + 145 && mouseX < bookX + 145 + 16 && mouseY >= bookY + 209 && mouseY < bookY + 209 + 27) {
            TooltipUtil.renderTooltip(guiGraphics, tooltip_health, mouseX, mouseY);
        } else {
            // Defeated or not info
            if (mouseX >= bookX + 172 && mouseX < bookX + 172 + 16 && mouseY >= bookY + 210 && mouseY < bookY + 210 + 27) {
                if (isBossDefeatedInWorld) { // yes
                    TooltipUtil.renderTooltip(guiGraphics, tooltip_defeated, mouseX, mouseY);
                } else {
                    if (isLocalServerOrAnyoneBossKilled) { // no
                        TooltipUtil.renderTooltip(guiGraphics, tooltip_notDefeated, mouseX, mouseY);
                    }
                }
            } else { // Here are tooltips with checks to see if they are needed.
                if (wikiLinkEnabled && showAdditionalInfo) {
                    if (mouseX >= bookX + 350 && mouseX < bookX + 350 + 16 && mouseY >= bookY + 210 && mouseY < bookY + 210 + 27) {
                        TooltipUtil.renderTooltip(guiGraphics, Component.translatable("gui.boss_checklist.wiki_link_info"), mouseX, mouseY);
                    }
                }
                if (additionalInfo && showInfo) {
                    if (mouseX >= bookX + 199 && mouseX < bookX + 199 + 16 && mouseY >= bookY + 210 && mouseY < bookY + 210 + 27) {
                        TooltipUtil.renderTooltip(guiGraphics, additionalInfoComponent, mouseX, mouseY);
                    }
                }
                if (showLastAttempt) {
                    if (showLastAttemptInfo) {
                        if (mouseX >= bookX + GuiConstants.STATS_TAB_X + 3 && mouseX < bookX + GuiConstants.STATS_TAB_X + GuiConstants.STATS_TAB_WIDTH - 3 && mouseY >= bookY + GuiConstants.STATS_TAB_Y + 23 && mouseY < bookY + GuiConstants.STATS_TAB_Y + 34) {
                            TooltipUtil.renderTooltip(guiGraphics, tooltip_attemptTime, mouseX, mouseY);
                        } else {
                            if (mouseX >= bookX + GuiConstants.STATS_TAB_X + 3 && mouseX < bookX + GuiConstants.STATS_TAB_X + GuiConstants.STATS_TAB_WIDTH - 3 && mouseY >= bookY + GuiConstants.STATS_TAB_Y + 37 && mouseY < bookY + GuiConstants.STATS_TAB_Y + 48) {
                                TooltipUtil.renderTooltip(guiGraphics, tooltip_duration, mouseX, mouseY);
                            } else if (!lastAttempt.damageMap_top3.isEmpty()) {
                                if (mouseX >= bookX + GuiConstants.STATS_TAB_X + 3 && mouseX < bookX + GuiConstants.STATS_TAB_X + GuiConstants.STATS_TAB_WIDTH - 3 && mouseY >= bookY + GuiConstants.STATS_TAB_Y + 53 && mouseY < bookY + GuiConstants.STATS_TAB_Y + 64) {
                                    TooltipUtil.renderTooltip(guiGraphics, tooltip_attemptTop3, mouseX, mouseY);
                                }
                            }
                        }
                    }
                }
            }
        }
    }






    private void renderDrops(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y) {
        boolean renderTooltip = false;
        List<ClientTooltipComponent> tooltip = new ArrayList<>();
        ItemStack stackForTooltip = null;

        if (drops == null || drops.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable("gui.boss_checklist.no_drop").getString(), x, y, 0xFF616161, false);
            return;
        }
        

        int i = 0;
        for (String id : drops) {
            final int PER_ROW = 5;
            final int DROP_SPACING = 22;
            
            int row = i / PER_ROW;
            int col = i % PER_ROW;

            int xPos = x + col * DROP_SPACING;
            int yPos = y + row * DROP_SPACING;

            String[] a = id.split("#");

            String itemId = a[0];
            String dropChance = null;
            if (a.length > 1) dropChance = a[1];

            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemId));
            if (item == null) {
                LoggerUtil.warn("There is no item with this ID: " + itemId);
                return;
            }
            ItemStack stack = new ItemStack(item);
            
            guiGraphics.renderItem(stack, xPos, yPos);

            if (dropChance != null) {
                PoseStack poseStack = guiGraphics.pose();
                poseStack.pushPose();
                guiGraphics.pose().scale(0.75f, 0.75f, 1f);
                guiGraphics.drawString(this.font, dropChance, (int) (xPos / 0.75 + 4), (int) (yPos / 0.75 + 22), 0xFF616161, false);
                poseStack.popPose();
            }

            // tooltip if mouse hovered
            if (mouseX >= xPos && mouseX <= xPos + 16 && mouseY >= yPos && mouseY <= yPos + 16) {
                renderTooltip = true;
                stackForTooltip = stack;
                tooltip.clear();

                List<Component> b = InventoryScreen.getTooltipFromItem(Minecraft.getInstance(), stack);

                for (Component c : b) {
                    tooltip.add(ClientTooltipComponent.create(c.getVisualOrderText()));
                }
            }
            i++;
        }
        if (renderTooltip) {
            guiGraphics.renderTooltip(this.font, stackForTooltip, mouseX, mouseY);
        }
    }



    public void renderEntityInGui(GuiGraphics graphics, int x, int y, int scale, float partialTicks) {
        try {
            if (entity == null) return;
        
            PoseStack poseStack = graphics.pose();
            poseStack.pushPose();

            //Lighting.setupForEntityInInventory(); // for 1.20.1

            poseStack.translate(x, y + boss.definition().yOffset(), 100); // for 1.20.1 or 1.21.4 change z to 100
            poseStack.scale((float) scale, (float) scale, (float) scale);
            poseStack.translate(0, -entity.getBbHeight() / 2.0F, 0); 
            
            poseStack.mulPose(Axis.XP.rotationDegrees(rotationX + 180));
            poseStack.mulPose(Axis.YP.rotationDegrees(-rotationY + 180));

            // FOR 1.21.4 - delete  0,
            Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0, 0, 0, 0, 0, poseStack, Minecraft.getInstance().renderBuffers().bufferSource(), 15728880);
            Minecraft.getInstance().renderBuffers().bufferSource().endBatch();

            poseStack.popPose();

            //Lighting.setupFor3DItems(); // for 1.20.1
        } catch (Exception e) {
            LoggerUtil.errorWithException("Error when rendering boss in gui: ", e);
            onClose();
        }
    }




    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);

        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;

        guiGraphics.blit(GuiConstants.BOOK, bookX, bookY, 0, 0, 512, 256, 512, 256);
    }








    

    private boolean isMouseOverBoss(double mouseX, double mouseY) {
        return mouseX >= ((this.width - 512) / 2) + 135  && mouseX <= ((this.width - 512) / 2) + 135 + 110 &&
            mouseY >= ((this.height - 256) / 2) + 90 && mouseY <= ((this.height - 256) / 2) + 90 + 110;
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;

        if (button == 0 && isMouseOverBoss(mouseX, mouseY)) {
            dragging = true;
            allowRotation = false;
            return true;
        }
        if (wikiLinkEnabled) {
            if (mouseX >= bookX + 350  && mouseX <= bookX + 350 + 16 && mouseY >= bookY + 210 && mouseY <= bookY + 210 + 27) {
                minecraft.setScreen(new ConfirmLinkScreen((confirmed) -> {
                    if (confirmed) {
                        net.minecraft.Util.getPlatform().openUri(wikiLink);
                    }
                    this.minecraft.setScreen(this);
                }, wikiLink, true));
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0) {
            final float MOUSE_SENSITIVITY = 0.7f;
            rotationY += (float)(dragX * MOUSE_SENSITIVITY);
            rotationX -= (float)(dragY * MOUSE_SENSITIVITY);
            rotationX = Mth.clamp(rotationX, -75f, 75f);
            return true;
        }
        
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            allowRotation = true; // rotation unpaused
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }


    @Override
    public void onClose() {
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