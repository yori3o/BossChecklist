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
import com.yori3o.boss_checklist.common.BossChecklistClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.Holder.Reference;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector3f;



public class BossInfoScreen extends Screen {


    
    private Identifier BOSS_IMG;
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
            BOSS_IMG = Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/bosses/" + boss.id().replace(":", "_") + ".png");
        } else {
            rotationY = boss.definition().rotateY() + 180;
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
        Identifier i = Identifier.parse(id);

        Optional<Reference<EntityType<?>>> a = BuiltInRegistries.ENTITY_TYPE.get(i);
        EntityType<?> type = null;
        if (a.isPresent()) {
            type = a.get().value();
        } else {
            return null;
        }
        
        Entity entity = type.create(Minecraft.getInstance().level, EntitySpawnReason.LOAD);
        
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
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
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

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.BOOKMARK, bookX + 145, bookY + 210, 0, 0, 16, 27, 16, 27);
        if (isLocalServerOrAnyoneBossKilled) {
            if (isBossDefeatedInWorld) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.BOOKMARK_DEFEATED, bookX + 172, bookY + 209, 0, 0, 16, 27, 16, 27);
            } else {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.BOOKMARK_NOT_DEFEATED, bookX + 172, bookY + 209, 0, 0, 16, 27, 16, 27);
            }
        }
        if (additionalInfo && showInfo) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.BOOKMARK_INFO, bookX + 199, bookY + 209, 0, 0, 16, 27, 16, 27);
        }
        if (wikiLinkEnabled && showAdditionalInfo) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.BOOKMARK_WIKI, bookX + 350, bookY + 210, 0, 0, 16, 27, 16, 27);
        }
        if (showLastAttempt) {
            if (showLastAttemptInfo) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.STATS_INFO_BACK, bookX + GuiConstants.STATS_TAB_X, bookY + GuiConstants.STATS_TAB_Y, 0, 0, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_HEIGHT, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_HEIGHT);
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.BATTLE_ICON, bookX + GuiConstants.STATS_TAB_X + 4, bookY + GuiConstants.STATS_TAB_Y + 24, 0, 0, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE);
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.DURATION_ICON, bookX + GuiConstants.STATS_TAB_X + 4, bookY + GuiConstants.STATS_TAB_Y + 38, 0, 0, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE);
                if (!lastAttempt.damageMap_top3.isEmpty()) {
                    guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.TOP_ICON, bookX + GuiConstants.STATS_TAB_X + 4, bookY + GuiConstants.STATS_TAB_Y + 52, 0, 0, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE, GuiConstants.ICONS_SIZE);
                }
            }
        }

        guiGraphics.textWithWordWrap(font, Component.literal("§l" + bossName), bookX + 137, bookY + 53, 110, 0xFF000000, false);

        guiGraphics.textWithWordWrap(font, Component.literal(modName), bookX + 137, bookY + modNameYoffset, 110, 0xFF616161, false);


        if (showInfo) {
            if (boss.definition().brokenModel()) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BOSS_IMG, bookX + 137, bookY + 100, 0, 0, 100, 100, 100, 100);
            } else {
                renderEntityInGui(guiGraphics, bookX + 132 + 56, bookY + 90 + 85, boss.definition().scale(), partialTick);
            }
        }


        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        renderTooltips(guiGraphics, mouseX, mouseY, bookX, bookY);

        if (currentTab == InfoTab.DROP) {
            renderDrops(guiGraphics, mouseX, mouseY, bookX + 265, bookY + 80);
        } else if (currentTab == InfoTab.SPAWN) {
            guiGraphics.textWithWordWrap(font,  summonText,  bookX + 265,  bookY + 80, 117, 0xFF000000, false);
        }

        if (showLastAttempt) {
             
            if (showLastAttemptInfo) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.ARROW_UP, bookX + GuiConstants.STATS_TAB_X, bookY + GuiConstants.STATS_TAB_Y, 0, 0, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH);
            } else {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.ARROW_DOWN, bookX + GuiConstants.STATS_TAB_X, bookY + GuiConstants.STATS_TAB_Y, 0, 0, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH, GuiConstants.STATS_TAB_WIDTH);
            }
        }

        if (entity == null) {
            if (entityNotLivingError) {
                guiGraphics.textWithWordWrap(this.font, Component.translatable("gui.boss_checklist.entity_not_living"), bookX + GuiConstants.DROP_BUTTON_X, bookY + GuiConstants.DROP_BUTTON_Y + 8, 115, 0xFF616161, false);
            } else {
                guiGraphics.textWithWordWrap(this.font, Component.translatable("gui.boss_checklist.no_entity").append("\"" + boss.id() + "\""), bookX + GuiConstants.DROP_BUTTON_X, bookY + GuiConstants.DROP_BUTTON_Y + 8, 115, 0xFF616161, false);
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





    public void renderTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, int bookX, int bookY) {
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






    private void renderDrops(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, int x, int y) {
        boolean renderTooltip = false;
        List<ClientTooltipComponent> tooltip = new ArrayList<>();

        if (drops == null || drops.isEmpty()) {
            guiGraphics.text(this.font, Component.translatable("gui.boss_checklist.no_drop").getString(), x, y, 0xFF616161, false);
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

            Optional<Reference<Item>> s = BuiltInRegistries.ITEM.get(Identifier.parse(itemId));
            if (!s.isPresent()) {
                LoggerUtil.warn("There is no item with this ID: " + itemId);
                return;
            }
            ItemStack stack = new ItemStack(s.get().value());
            
            guiGraphics.item(stack, xPos, yPos);

            if (dropChance != null) {
                Matrix3x2fStack poseStack = guiGraphics.pose();
                poseStack.pushMatrix();
                guiGraphics.pose().scale(0.75f, 0.75f);
                guiGraphics.text(this.font, dropChance, (int) (xPos / 0.75 + 4), (int) (yPos / 0.75 + 22), 0xFF616161, false);
                poseStack.popMatrix();
            }

            // tooltip if mouse hovered
            if (mouseX >= xPos && mouseX <= xPos + 16 && mouseY >= yPos && mouseY <= yPos + 16) {
                renderTooltip = true;
                tooltip.clear();

                List<Component> b = InventoryScreen.getTooltipFromItem(Minecraft.getInstance(), stack);

                for (Component c : b) {
                    tooltip.add(ClientTooltipComponent.create(c.getVisualOrderText()));
                }
            }
            i++;
        }
        if (renderTooltip) {
            guiGraphics.tooltip(
                this.font,
                tooltip,
                mouseX, mouseY, (screenWidth, screenHeight, x2, y2, tooltipWidth, tooltipHeight) -> new Vector2i(x2 + 12, y2 - 2), null
            );
        }
    }



    public void renderEntityInGui(GuiGraphicsExtractor graphics, int x, int y, int scale, float partialTicks) {
        try {
            if (entity == null) return;
            EntityRenderState renderState = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity).createRenderState(entity, 1.0F);
            renderState.shadowPieces.clear();
            renderState.outlineColor = 0;

            Quaternionf mainRotation = new Quaternionf()
                .rotateZ((float)Math.PI)
                .rotateX(rotationX * ((float)Math.PI / 180F))
                .rotateY(rotationY * ((float)Math.PI / 180F));
            
            float entityOffset = -renderState.boundingBoxHeight / 2.0F;
            Vector3f translation = new Vector3f(0.0F, ((entityOffset / (float) scale) + (boss.definition().yOffset() / (float) scale) - 1.1f), 0.0F);

            graphics.entity(renderState, (float)scale, translation, mainRotation, null, 
                x - 71, y - 90, x + 71, y + 90);

        } catch (Exception e) {
            LoggerUtil.errorWithException("Error when rendering boss in gui: ", e);
            onClose();
        }
    }




    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);

        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.BOOK, bookX, bookY, 0, 0, 512, 256, 512, 256);
    }








    

    private boolean isMouseOverBoss(double mouseX, double mouseY) {
        return mouseX >= ((this.width - 512) / 2) + 135  && mouseX <= ((this.width - 512) / 2) + 135 + 110 &&
            mouseY >= ((this.height - 256) / 2) + 90 && mouseY <= ((this.height - 256) / 2) + 90 + 110;
    }


    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        int mouseX = (int) mouseButtonEvent.x();
        int mouseY = (int) mouseButtonEvent.y();
        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;

        if (mouseButtonEvent.button() == 0 && isMouseOverBoss(mouseX, mouseY)) {
            dragging = true;
            allowRotation = false;
            return true;
        }
        if (wikiLinkEnabled) {
            if (mouseX >= bookX + 350  && mouseX <= bookX + 350 + 16 && mouseY >= bookY + 210 && mouseY <= bookY + 210 + 27) {
                minecraft.setScreen(new ConfirmLinkScreen((confirmed) -> {
                    if (confirmed) {
                        net.minecraft.util.Util.getPlatform().openUri(wikiLink);
                    }
                    this.minecraft.setScreen(this);
                }, wikiLink, true));
                return true;
            }
        }

        return super.mouseClicked(mouseButtonEvent, bl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent mouseButtonEvent, double dragX, double dragY) {
        if (dragging && mouseButtonEvent.button() == 0) {
            final float MOUSE_SENSITIVITY = 0.7f;
            rotationY += (float)(dragX * MOUSE_SENSITIVITY);
            rotationX -= (float)(dragY * MOUSE_SENSITIVITY);
            rotationX = Mth.clamp(rotationX, -75f, 75f);
            return true;
        }
        
        return super.mouseDragged(mouseButtonEvent, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent mouseButtonEvent) {
        if (mouseButtonEvent.button() == 0 && dragging) {
            dragging = false;
            allowRotation = true; // rotation unpaused
            return true;
        }
        return super.mouseReleased(mouseButtonEvent);
    }


    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (BossChecklistClient.OPEN_CHECKLIST.matches(keyEvent)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyEvent);
    }

}