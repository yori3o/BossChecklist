package com.yori3o.boss_checklist.common.client.gui;


import com.yori3o.boss_checklist.common.config.DynamicConfigHandler;
import com.yori3o.boss_checklist.common.util.LoggerUtil;
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
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.joml.Matrix3x2fStack;
import org.joml.Quaternionf;
import org.joml.Vector2i;
import org.joml.Vector3f;



public class BossInfoScreen extends Screen {

    private static final int DROP_SPACING = 22; // length from icons items
    private static final int PER_ROW = 5; // maximum in row

    private static final float MOUSE_SENSITIVITY = 0.7f;

    // other various variables
    private static Identifier BOSS_IMG;
    private boolean brokenBossModel = false;
    private int bossNameLines;
    private Component summonText;
    private int bossYCorrection;
    private boolean dropsAreLoaded = false;
    private LivingEntity entity;
    private int bossScale;
    private boolean isBossDefeatedInWorld = false;
    private boolean isLocalServerOrAnyoneBossKilled = false;
    private int bossHealth;
    private double bossArmor;
    private boolean additionalInfo = false;
    private boolean showInfo = true;
    private boolean wikiLinkEnabled = false;
    private String wikiLink = "";
    private boolean showAdditionalInfo = true;
    private List<String> drops = new ArrayList<>();

    private final Screen parent;
    private final String bossId;
    private final BossEntry boss;
    private String bossName;
    private String modName;

    private CustomButton dropButton, spawnButton, openAttemptButton, closeButton;

    private enum InfoTab { NONE, DROP, SPAWN }
    private InfoTab currentTab = InfoTab.NONE;

    private String itemId;
    private String dropChance; // only if item in bosses.json have #*chance* suffix
    private boolean hasChance; // and this too

    // for model rendering and animation
    private long lastTime;
    private float rotationY = 0f;
    private float rotationX = 0f;
    private boolean dragging = false;
    private boolean allowRotation = true;

    private ClientBossAttempt lastAttempt;
    private boolean showLastAttempt = false;
    private boolean showLastAttemptInfo = false;
    private String[] top3names;
    private String[] top3damages;

    private List<Component> tooltip_health = new ArrayList<>();
    private List<Component> tooltip_notDefeated = new ArrayList<>();
    private List<Component> tooltip_defeated = new ArrayList<>();
    private List<Component> tooltip_attemptTime = new ArrayList<>();
    private List<Component> tooltip_attemptTop3 = new ArrayList<>();
    private String tooltip_duration = "";

    private boolean ignoreProgressionMode;


    
    

    public BossInfoScreen(Screen parent, String bossId) {
        super(Component.literal("Boss Info"));
        lastTime = System.nanoTime();
        this.parent = parent;
        this.bossId = bossId;
        this.boss = BossService.get(bossId);

        if (bossId.equals("minecraft:wither")) {
            if (PlatformUtil.isModLoaded("witherreincarnated")) {
                modName = Component.translatable("boss_checklist.mod.witherreincarnated").getString();
            } else {
                modName = Component.translatable("boss_checklist.mod." + boss.definition().modId()).getString();
            }
        } if (bossId.equals("minecraft:ender_dragon")) {
            if (PlatformUtil.isModLoaded("endertrigon")) {
                modName = Component.translatable("boss_checklist.mod.endertrigon").getString();
            } else {
                modName = Component.translatable("boss_checklist.mod." + boss.definition().modId()).getString();
            }
        } else {
            modName = Component.translatable("boss_checklist.mod." + boss.definition().modId()).getString();
        }
        bossName = Component.translatable("boss_checklist.boss." + bossId.replace(":", "_")).getString();
        summonText = Component.translatable("boss_checklist.summon." + bossId.replace(":", "_"));
    }

    public BossInfoScreen(Screen parent, CustomBossEntry boss) {
        super(Component.literal("Boss Info"));
        lastTime = System.nanoTime();
        this.parent = parent;
        this.bossId = boss.id();
        this.boss = boss;
        bossName = boss.name();
        modName = boss.modName();
        summonText = Component.literal(boss.spawnInfo());
        ignoreProgressionMode = true;
    }





    public void init() {
        super.init();

        if (boss.definition().brokenModel()) {
            BOSS_IMG = Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/bosses/" + bossId.replace(":", "_") + ".png");
            brokenBossModel = true;
        } else {
            rotationY += boss.definition().rotateY() + 180;
            bossScale = boss.definition().scale();
            bossYCorrection = boss.definition().yOffset();
        }

        // for entity rendering and info
        entity = createEntityFromId(bossId);
        if (entity == null) return;
        if (boss.definition().health() == -1) {
            bossHealth = (int) entity.getAttributeValue(Attributes.MAX_HEALTH);
        } else {
            bossHealth = (int) boss.definition().health();
        }
        if (boss.definition().armor() == -1) {
            bossArmor = (int) entity.getAttributeValue(Attributes.ARMOR);
        } else {
            bossArmor = (int) boss.definition().armor();
        }

        isBossDefeatedInWorld = boss.progress().isDefeated();
        isLocalServerOrAnyoneBossKilled = BossDefeatedHandler.anyoneBossKilledOnce || Minecraft.getInstance().isLocalServer();

        if (isBossDefeatedInWorld) {
            String killerName = boss.progress().killerName();

            // tooltip for green info bookmark
            tooltip_defeated.clear();
            if (killerName.equals("")) { // if null add only yes/no
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
            tooltip_notDefeated.clear();
            if (boss.definition().type() == 2) {
                    tooltip_notDefeated.add(Component.literal(Component.translatable("gui.boss_checklist.miniboss_not_defeated").getString()));
                } else {
                    tooltip_notDefeated.add(Component.literal(Component.translatable("gui.boss_checklist.not_defeated").getString()));
                }
            tooltip_notDefeated.add(Component.literal(Component.translatable("gui.boss_checklist.server_warning").getString()));
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
        
        bossNameLines = font.split(Component.literal("§l" + bossName), 110).size();

        if (!dropsAreLoaded) {
            for (String dropId : boss.definition().drops()) {
                // if mod not loaded item doesnt added to to list
                if (PlatformUtil.isModLoaded(dropId.split(":")[0])) {
                    drops.add(dropId);
                }
            }

            if (PlatformUtil.isModLoaded("endrem_additions") && bossId.equals("bosses_of_mass_destruction:void_blossom")) { // it for my other mod
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
        if (showInfo) {
            tooltip_health.add(Component.literal(Component.translatable("gui.boss_checklist.health").getString() + bossHealth));
            tooltip_health.add(Component.literal(Component.translatable("gui.boss_checklist.armor").getString() + bossArmor));
        } else {
            tooltip_health.add(Component.literal(Component.translatable("gui.boss_checklist.health").getString() + "?"));
            tooltip_health.add(Component.literal(Component.translatable("gui.boss_checklist.armor").getString() + "?"));
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


        createButtons();
    }











    public LivingEntity createEntityFromId(String id) {
        Identifier rl = Identifier.parse(id);

        // FOR 1.21.4+ - add .get().value()
        Optional<Reference<EntityType<?>>> a = BuiltInRegistries.ENTITY_TYPE.get(rl);
        EntityType<?> type = null;
        if (a.isPresent()) {
            type = a.get().value();
        } else {
            LoggerUtil.error("There is no entity with this ID: " + id);
            return null;
        }
        
        
        // FOR 1.21.4+ - add , EntitySpawnReason.LOAD
        Entity entity = type.create(Minecraft.getInstance().level, EntitySpawnReason.LOAD);
        
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity;
        } else {
            this.onClose();
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
                //currentTab = InfoTab.DROP;
            });
        }

        if (showAdditionalInfo) {
            spawnButton = new CustomButton(bookX + GuiConstants.SPAWN_BUTTON_X, bookY + GuiConstants.SPAWN_BUTTON_Y, GuiConstants.MEDIUM_BUTTONS_WIDTH, GuiConstants.MEDIUM_BUTTONS_HEIGHT, GuiConstants.MEDIUM_BUTTONS_OVERLAY_WIDTH, GuiConstants.MEDIUM_BUTTONS_OVERLAY_HEIGHT, Component.translatable("gui.boss_checklist.spawn_info"), GuiConstants.BUTTON_TEXTURE, GuiConstants.BUTTON_TEXTURE_hovered, GuiConstants.BUTTON_TEXTURE_pressed, GuiConstants.BUTTON_TEXTURE_overlay, () -> {
                currentTab = InfoTab.SPAWN;
            });
        } else {
            spawnButton = new CustomButton(bookX + GuiConstants.SPAWN_BUTTON_X, bookY + GuiConstants.SPAWN_BUTTON_Y, GuiConstants.MEDIUM_BUTTONS_WIDTH, GuiConstants.MEDIUM_BUTTONS_HEIGHT, GuiConstants.MEDIUM_BUTTONS_OVERLAY_WIDTH, GuiConstants.MEDIUM_BUTTONS_OVERLAY_HEIGHT, Component.translatable("gui.boss_checklist.spawn_info"), GuiConstants.BUTTON_TEXTURE, GuiConstants.BUTTON_TEXTURE, GuiConstants.BUTTON_TEXTURE, GuiConstants.BUTTON_TEXTURE_overlay, () -> {
                //currentTab = InfoTab.SPAWN;
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
        long now = System.nanoTime();
        float delta = (now - lastTime) / 1_000_000_000.0f; // seconds
        lastTime = now;

        if (allowRotation) {
            rotationY += 7.5f * delta;
        }


        // --- pictures ---
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.BOOKMARK, bookX + 145, bookY + 210, 0, 0, 16, 27, 16, 27);
        if (isBossDefeatedInWorld) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.BOOKMARK_DEFEATED, bookX + 172, bookY + 209, 0, 0, 16, 27, 16, 27);
        } else {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.BOOKMARK_NOT_DEFEATED, bookX + 172, bookY + 209, 0, 0, 16, 27, 16, 27);
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


        // FOR 1.21.4+ - add , false
        guiGraphics.textWithWordWrap(font, Component.literal("§l" + bossName), bookX + 137, bookY + 53, 110, 0xFF000000, false);

        int secondY = bookY + 53 + (bossNameLines * font.lineHeight) + 2;

        guiGraphics.textWithWordWrap(font, Component.literal(modName), bookX + 137, secondY, 110, 0xFF616161, false);


        if (showInfo) {
            if (brokenBossModel) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BOSS_IMG, bookX + 137, bookY + 100, 0, 0, 100, 100, 100, 100);
            } else {
                renderEntityInGui(guiGraphics, bookX + 132 + 56, bookY + 90 + 85, bossScale, partialTick);
            }
        }


        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        renderTooltips(guiGraphics, mouseX, mouseY, bookX, bookY);

        if (currentTab == InfoTab.DROP) {
            renderDrops(guiGraphics, mouseX, mouseY, bookX + 265, bookY + 80);
        } else if (currentTab == InfoTab.SPAWN) { // FOR 1.21.4+ - add , false
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
            guiGraphics.textWithWordWrap(this.font, Component.translatable("gui.boss_checklist.no_entity"), bookX + GuiConstants.DROP_BUTTON_X, bookY + GuiConstants.DROP_BUTTON_Y, 115, 0xFF616161, false);
        }

        // makes buttons gray if they are disabled
        if (!showInfo) {
            guiGraphics.fill(bookX + GuiConstants.DROP_BUTTON_X, bookY + GuiConstants.DROP_BUTTON_Y, bookX + GuiConstants.DROP_BUTTON_X + GuiConstants.MEDIUM_BUTTONS_WIDTH, bookY + GuiConstants.DROP_BUTTON_Y + GuiConstants.MEDIUM_BUTTONS_HEIGHT, 0x88AAAAAA);
            if (!showAdditionalInfo) {
                guiGraphics.fill(bookX + GuiConstants.SPAWN_BUTTON_X, bookY + GuiConstants.SPAWN_BUTTON_Y, bookX + GuiConstants.SPAWN_BUTTON_X + GuiConstants.MEDIUM_BUTTONS_WIDTH, bookY + GuiConstants.SPAWN_BUTTON_Y + GuiConstants.MEDIUM_BUTTONS_HEIGHT, 0x88AAAAAA);
            }
        }
    }






    // fuck
    public void renderTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, int bookX, int bookY) {
        if (entity == null) return;
        
        // health and armor info
        if (mouseX >= bookX + 145 && mouseX < bookX + 145 + 16 && mouseY >= bookY + 209 && mouseY < bookY + 209 + 27) {
            List<ClientTooltipComponent> tooltip = new ArrayList<>(Arrays.asList(
                    ClientTooltipComponent.create(tooltip_health.get(0).getVisualOrderText()),
                    ClientTooltipComponent.create(tooltip_health.get(1).getVisualOrderText())
                ));
            if (tooltip_health.size() > 2) {
                    tooltip.add(ClientTooltipComponent.create(tooltip_health.get(2).getVisualOrderText()));
                    tooltip.add(ClientTooltipComponent.create(tooltip_health.get(3).getVisualOrderText()));
            }
            guiGraphics.tooltip(
                Minecraft.getInstance().font,
                tooltip,
                mouseX, mouseY, (screenWidth, screenHeight, x, y, tooltipWidth, tooltipHeight) -> new Vector2i(x + 12, y - 2), null
            );
        } else {
            // Defeated or not info
            if (mouseX >= bookX + 172 && mouseX < bookX + 172 + 16 && mouseY >= bookY + 210 && mouseY < bookY + 210 + 27) {
                if (isBossDefeatedInWorld) { // yes
                    List<ClientTooltipComponent> tooltip = new ArrayList<>(Arrays.asList(
                            ClientTooltipComponent.create( tooltip_defeated.get(0).getVisualOrderText())
                        ));
                    if (tooltip_defeated.size() > 1) {
                            tooltip.add(ClientTooltipComponent.create(tooltip_defeated.get(1).getVisualOrderText()));
                    }
                    guiGraphics.tooltip(
                        Minecraft.getInstance().font,
                        tooltip,
                        mouseX, mouseY, (screenWidth, screenHeight, x, y, tooltipWidth, tooltipHeight) -> new Vector2i(x + 12, y - 2), null
                    );
                } else {
                        if (isLocalServerOrAnyoneBossKilled) { // no
                            guiGraphics.tooltip(
                                Minecraft.getInstance().font,
                                List.of(
                                    ClientTooltipComponent.create( tooltip_notDefeated.get(0).getVisualOrderText())
                                ),
                                mouseX, mouseY, (screenWidth, screenHeight, x, y, tooltipWidth, tooltipHeight) -> new Vector2i(x + 12, y - 2), null
                            );
                    } else { // no + info about server requires
                        guiGraphics.tooltip(
                            Minecraft.getInstance().font,
                            List.of(
                                ClientTooltipComponent.create( tooltip_notDefeated.get(0).getVisualOrderText()),
                                ClientTooltipComponent.create( tooltip_notDefeated.get(1).getVisualOrderText())
                            ),
                            mouseX, mouseY, (screenWidth, screenHeight, x, y, tooltipWidth, tooltipHeight) -> new Vector2i(x + 12, y - 2), null
                        );
                    }
                }
            } else { // Here are tooltips with checks to see if they are needed.
                if (wikiLinkEnabled && showAdditionalInfo) {
                    if (mouseX >= bookX + 350 && mouseX < bookX + 350 + 16 && mouseY >= bookY + 210 && mouseY < bookY + 210 + 27) {
                        guiGraphics.tooltip(
                            Minecraft.getInstance().font,
                            List.of(
                                ClientTooltipComponent.create( Component.translatable("gui.boss_checklist.wiki_link_info").getVisualOrderText())
                            ),
                            mouseX, mouseY, (screenWidth, screenHeight, x, y, tooltipWidth, tooltipHeight) -> new Vector2i(x + 12, y - 2), null
                        );
                    }
                }
                if (additionalInfo && showInfo) {
                    if (mouseX >= bookX + 199 && mouseX < bookX + 199 + 16 && mouseY >= bookY + 210 && mouseY < bookY + 210 + 27) {
                        guiGraphics.tooltip(
                            Minecraft.getInstance().font,
                            List.of(
                                ClientTooltipComponent.create( Component.translatable("gui.boss_checklist.info_" + bossId.replace(":", "_")).getVisualOrderText())
                            ),
                            mouseX, mouseY, (screenWidth, screenHeight, x, y, tooltipWidth, tooltipHeight) -> new Vector2i(x + 12, y - 2), null
                        );
                    }
                }
                if (showLastAttempt) {
                    if (showLastAttemptInfo) {
                        if (mouseX >= bookX + GuiConstants.STATS_TAB_X + 3 && mouseX < bookX + GuiConstants.STATS_TAB_X + GuiConstants.STATS_TAB_WIDTH - 3 && mouseY >= bookY + GuiConstants.STATS_TAB_Y + 23 && mouseY < bookY + GuiConstants.STATS_TAB_Y + 34) {
                            guiGraphics.tooltip(
                                Minecraft.getInstance().font,
                                List.of(
                                    ClientTooltipComponent.create( tooltip_attemptTime.get(0).getVisualOrderText()),
                                    ClientTooltipComponent.create( tooltip_attemptTime.get(1).getVisualOrderText())
                                ),
                                mouseX, mouseY, (screenWidth, screenHeight, x, y, tooltipWidth, tooltipHeight) -> new Vector2i(x + 12, y - 2), null
                            );
                        } else {
                            if (mouseX >= bookX + GuiConstants.STATS_TAB_X + 3 && mouseX < bookX + GuiConstants.STATS_TAB_X + GuiConstants.STATS_TAB_WIDTH - 3 && mouseY >= bookY + GuiConstants.STATS_TAB_Y + 37 && mouseY < bookY + GuiConstants.STATS_TAB_Y + 48) {
                                guiGraphics.tooltip(
                                    Minecraft.getInstance().font,
                                    List.of(
                                        ClientTooltipComponent.create(Component.literal(tooltip_duration).getVisualOrderText())
                                    ),
                                    mouseX, mouseY, (screenWidth, screenHeight, x, y, tooltipWidth, tooltipHeight) -> new Vector2i(x + 12, y - 2), null
                                );
                            } else if (!lastAttempt.damageMap_top3.isEmpty()) {
                                if (mouseX >= bookX + GuiConstants.STATS_TAB_X + 3 && mouseX < bookX + GuiConstants.STATS_TAB_X + GuiConstants.STATS_TAB_WIDTH - 3 && mouseY >= bookY + GuiConstants.STATS_TAB_Y + 53 && mouseY < bookY + GuiConstants.STATS_TAB_Y + 64) {
                                    List<ClientTooltipComponent> tooltip = new ArrayList<>(Arrays.asList(
                                            ClientTooltipComponent.create( tooltip_attemptTop3.get(0).getVisualOrderText()),
                                            ClientTooltipComponent.create( tooltip_attemptTop3.get(1).getVisualOrderText())
                                    ));
                                    if (tooltip_attemptTop3.size() > 2) {
                                        tooltip.add(ClientTooltipComponent.create( tooltip_attemptTop3.get(2).getVisualOrderText()));
                                        if (tooltip_attemptTop3.size() > 3) {
                                            tooltip.add(ClientTooltipComponent.create( tooltip_attemptTop3.get(3).getVisualOrderText()));
                                        }
                                    }

                                    guiGraphics.tooltip(
                                        Minecraft.getInstance().font,
                                        tooltip,
                                        mouseX, mouseY, (screenWidth, screenHeight, x, y, tooltipWidth, tooltipHeight) -> new Vector2i(x + 12, y - 2), null
                                    );
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
            
            int row = i / PER_ROW;
            int col = i % PER_ROW;

            int xPos = x + col * DROP_SPACING;
            int yPos = y + row * DROP_SPACING;

            String[] a = id.split("#");

            // if suffix #*number* there is render drop chance
            if (a.length > 1) {
                itemId = a[0];
                dropChance = a[1];
                hasChance = true;
            } else {
                itemId = a[0];
                hasChance = false;
            }

            // FOR 1.21.4+ - add .get().value()
            Optional<Reference<Item>> s = BuiltInRegistries.ITEM.get(Identifier.parse(itemId));
            if (!s.isPresent()) {
                LoggerUtil.warn("There is no item with this ID: " + itemId);
                return;
            }
            ItemStack stack = new ItemStack(s.get().value());
            
            guiGraphics.item(stack, xPos, yPos);

            if (hasChance) {
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
            EntityRenderState state = extractRenderState(entity);

            Quaternionf mainRotation = new Quaternionf()
                .rotateZ((float)Math.PI)
                .rotateX(rotationX * ((float)Math.PI / 180F))
                .rotateY(rotationY * ((float)Math.PI / 180F));

            if (state instanceof LivingEntityRenderState livingState) {
                livingState.yRot = 0; 
                livingState.xRot = 0;
            }
            
            float entityOffset = -state.boundingBoxHeight / 2.0F;
            Vector3f translation = new Vector3f(0.0F, ((entityOffset / (float) scale) + (bossYCorrection / (float) scale) - 1.1f), 0.0F);

            graphics.entity(state, (float)scale, translation, mainRotation, null, 
                x - 85, y - 85, x + 85, y + 85);

        } catch (Exception e) {
            LoggerUtil.errorWithException("Error when rendering boss:", e);
        }
    }


    private static EntityRenderState extractRenderState(final LivingEntity entity) {
        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        EntityRenderer<? super LivingEntity, ?> renderer = entityRenderDispatcher.getRenderer(entity);
        EntityRenderState renderState = renderer.createRenderState(entity, 1.0F);
        renderState.shadowPieces.clear();
        renderState.outlineColor = 0;
        return renderState;
    }




    // ONLY FOR 1.21.1+
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
            // drag started and rotation paused
            dragging = true;
            allowRotation = false;
            return true; // stop method
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