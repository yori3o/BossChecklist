package com.yori3o.boss_checklist.client.gui;


import com.yori3o.boss_checklist.client.data.BossData;
import com.yori3o.boss_checklist.client.data.ClientDataSaver;
import com.yori3o.boss_checklist.client.data.BossRegistry;
import com.yori3o.boss_checklist.BossChecklistClient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
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
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.util.ArrayList;
import java.util.List;

import dev.architectury.platform.Platform;




public class BossInfoScreen extends Screen {
    public static final Logger LOGGER = LogManager.getLogger("boss_checklist");

    private static final ResourceLocation background_texture = 
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/boss_book.png");
    private static final ResourceLocation BUTTON_TEXTURE =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/button.png");
    private static final ResourceLocation BUTTON_TEXTURE_hovered =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/button_hovered.png");
    private static final ResourceLocation BUTTON_TEXTURE_pressed =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/button_pressed.png");
    private static final ResourceLocation BOOKMARK =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/bookmark.png");
    private static final ResourceLocation BOOKMARK_NOT_DEFEATED =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/bookmark_not_defeated.png");
    private static final ResourceLocation BOOKMARK_DEFEATED =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/bookmark_defeated.png");
    private static final ResourceLocation BOOKMARK_INFO =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/bookmark_info.png");
    private static final ResourceLocation BOOKMARK_WIKI =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/bookmark_wiki.png");

    // optimizations and bug fixes
    private static ResourceLocation BOSS;
    private boolean brokenBossModel = false; // for mods like BOMD and bosses rise
    private boolean bossNameIsBig = false;
    private boolean skipNextRenderBackground = false;
    private Component summonText;
    private int bossYCorrection;
    private boolean dropsAreLoaded = false;

    private final Screen parent;
    private final String bossId;
    private final BossData boss;
    private String bossName;
    private String modName;

    private CustomButton dropButton;
    private CustomButton spawnButton;

    private enum InfoTab { NONE, DROP, SPAWN }
    private InfoTab currentTab = InfoTab.NONE;

    private int dropSpacing = 22; // length from icons items
    private int perRow = 5;   // maximum in row

    private String itemid;
    private String dropChance; // only if item in bosses.json have #*chance* suffix
    private Boolean chance; // and this too

    private float rotationY = 0;
    private float rotationX = 0f;
    private boolean dragging = false;
    private static final float MOUSE_SENS = 0.7f;
    private boolean allowRotation = true;

    private LivingEntity entity;
    private int bossScale;
    private boolean isBossDefeated_InWorld = false;
    private boolean isLocalServerOrAnyoneBossKilled = false;
    private int boss_health;
    private double boss_armor;
    private boolean additionalInfo = false;
    private boolean showInfo = true;
    private boolean wiki_link_enabled = false;
    private String wiki_link = "";

    private EntityRenderDispatcher dispatcher;
    private MultiBufferSource.BufferSource buffer;

    private List<Component> tooltip = new ArrayList<>();
    private List<Component> tooltip2 = new ArrayList<>();
    private List<Component> tooltip3 = new ArrayList<>();

    private List<String> drops = new ArrayList<>();


    public static LivingEntity createEntityFromId(String id) {
        Minecraft mc = Minecraft.getInstance();

        ResourceLocation rl = ResourceLocation.parse(id);

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(rl);
        if (type == null) {
            throw new IllegalArgumentException("Not found EntityType by id: " + id);
        }
        
        Entity entity = type.create(mc.level);
        
        return (LivingEntity) entity; // crash if not Living entity
    }
    

    public BossInfoScreen(Screen parent, String bossId) {
        super(Component.literal("Boss Info"));
        this.parent = parent;
        this.bossId = bossId;
        this.boss = BossRegistry.get(bossId);
        bossName = Component.translatable("boss_checklist.boss." + bossId.replace(":", "_")).getString();
        modName = Component.translatable("boss_checklist.mod." + boss.getModId()).getString();
    }


    public void init() {
        super.init();

        bossScale = boss.getScale();
        bossYCorrection = boss.getYOffset();

        // for entity rendering and info
        entity = createEntityFromId(bossId);
        if (boss.getHealth() == -1) {
            boss_health = (int) entity.getAttributeValue(Attributes.MAX_HEALTH);
        } else {
            boss_health = (int) boss.getHealth();
        }
        if (boss.getArmor() == -1) {
            boss_armor = (int) entity.getAttributeValue(Attributes.ARMOR);
        } else {
            boss_armor = (int) boss.getArmor();
        }
        dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        buffer = Minecraft.getInstance().renderBuffers().bufferSource();

        isBossDefeated_InWorld = ClientDataSaver.defeatedBossesIds_InWorld.contains(bossId);

        isLocalServerOrAnyoneBossKilled = ClientDataSaver.defeatedBossesIds_InWorld.size() > 0 || Minecraft.getInstance().isLocalServer();

        if (isBossDefeated_InWorld) {
            String killerName = ClientDataSaver.whoKiller(bossId);

            // tooltip for green info bookmark
            tooltip3.clear();
            if (killerName.length() == 0) { // if null add only yes/no
                tooltip3.add(Component.literal(Component.translatable("gui.boss_checklist.defeated").getString()));
            } else { 
                tooltip3.add(Component.literal(Component.translatable("gui.boss_checklist.defeated").getString()));
                tooltip3.add(Component.literal(Component.translatable("gui.boss_checklist.killer_name").getString() + killerName));
            }
            
        }
        if (BossChecklistClient.isProgressionMode_dynamic && !isBossDefeated_InWorld) {
                showInfo = false;
                bossName = "???";
                modName = "???";
            }

        summonText = Component.translatable("boss_checklist.summon." + bossId.replace(":", "_"));
        
        if (boss.isBrokenModel()) {
            BOSS = ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/bosses/" + bossId.replace(":", "_") + ".png");
            brokenBossModel = true;
        }
        
        additionalInfo = boss.isAdditionalInfo();

        if (!boss.getWikiLink().equals("")) {
            wiki_link_enabled = true;
            wiki_link = boss.getWikiLink();
        }
        
        bossNameIsBig = Minecraft.getInstance().font.width(bossName) > 95;
        

        if (!dropsAreLoaded) {
            for (String dropId : boss.getDrops()) {
                // if mod not loaded item doesnt added to to list
                if (Platform.isModLoaded(dropId.split(":")[0])) {
                    drops.add(dropId);
                }
            }

            if (Platform.isModLoaded("endrem_additions") && bossId.equals("bosses_of_mass_destruction:void_blossom")) { // it my other mod
                drops.add("endrem:blossom_eye");
            }
            
            dropsAreLoaded = true;
        }
        

        tooltip.clear();
        if (showInfo) {
            tooltip.add(Component.literal(Component.translatable("gui.boss_checklist.health").getString() + boss_health));
            tooltip.add(Component.literal(Component.translatable("gui.boss_checklist.armor").getString() + boss_armor));
        } else {
            tooltip.add(Component.literal(Component.translatable("gui.boss_checklist.health").getString() + "?"));
            tooltip.add(Component.literal(Component.translatable("gui.boss_checklist.armor").getString() + "?"));
        }

        tooltip2.clear();
        tooltip2.add(Component.literal(Component.translatable("gui.boss_checklist.not_defeated").getString()));
        tooltip2.add(Component.literal(Component.translatable("gui.boss_checklist.server_warning").getString()));


        createButtons();
    }


    private void createButtons() {
        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;

        if (showInfo) {
            dropButton = new CustomButton(bookX + 265, bookY + 51, 50, 18, Component.translatable("gui.boss_checklist.drop"), BUTTON_TEXTURE, BUTTON_TEXTURE_hovered, BUTTON_TEXTURE_pressed, () -> {
                currentTab = InfoTab.DROP;
            });
        } else {
            dropButton = new CustomButton(bookX + 265, bookY + 51, 50, 18, Component.translatable("gui.boss_checklist.drop"), BUTTON_TEXTURE, BUTTON_TEXTURE, BUTTON_TEXTURE, () -> {
                //currentTab = InfoTab.DROP;
            });
        }

        spawnButton = new CustomButton(bookX + 328, bookY + 51, 50, 18, Component.translatable("gui.boss_checklist.spawn_info"), BUTTON_TEXTURE, BUTTON_TEXTURE_hovered, BUTTON_TEXTURE_pressed, () -> {
            currentTab = InfoTab.SPAWN;
        });

        addRenderableWidget(dropButton);
        addRenderableWidget(spawnButton);
    }



    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        // FOR 1.20.1-
        //this.renderBackground(guiGraphics);

        this.skipNextRenderBackground = true;

        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;

        if (allowRotation)
            rotationY += 0.5f * partialTick;


        guiGraphics.blit(background_texture, bookX, bookY, 0, 0, 512, 256, 512, 256);

        guiGraphics.blit(BOOKMARK, bookX + 145, bookY + 210, 0, 0, 16, 27, 16, 27);

        if (isBossDefeated_InWorld) {
            guiGraphics.blit(BOOKMARK_DEFEATED, bookX + 172, bookY + 209, 0, 0, 16, 27, 16, 27);
        } else {
            guiGraphics.blit(BOOKMARK_NOT_DEFEATED, bookX + 172, bookY + 209, 0, 0, 16, 27, 16, 27);
        }

        if (additionalInfo && showInfo) {
            guiGraphics.blit(BOOKMARK_INFO, bookX + 199, bookY + 209, 0, 0, 16, 27, 16, 27);
        }

        if (wiki_link_enabled) {
            guiGraphics.blit(BOOKMARK_WIKI, bookX + 350, bookY + 210, 0, 0, 16, 27, 16, 27);
        }


        if (bossNameIsBig) {
            guiGraphics.drawWordWrap(font,  Component.literal("§l" + bossName), bookX + 137, bookY + 53, 110, 0xFF000000);
            guiGraphics.drawWordWrap(font,  Component.literal(modName),  bookX + 137,  bookY + 73, 110, 0xFF616161);
        } else {
            guiGraphics.drawString(font,  "§l" + bossName, bookX + 137, bookY + 53, 0xFF000000, false);
            guiGraphics.drawWordWrap(font,  Component.literal(modName),  bookX + 137,  bookY + 65, 110, 0xFF616161);
        }

        if (currentTab == InfoTab.DROP) {
            renderDrops(guiGraphics, mouseX, mouseY, bookX + 265, bookY + 80);
        } else if (currentTab == InfoTab.SPAWN) {
            renderSummon(guiGraphics, bookX + 265, bookY + 80);
        }

        if (showInfo) {
            if (brokenBossModel) {
                guiGraphics.blit(BOSS, bookX + 137, bookY + 100, 0, 0, 100, 100, 100, 100);
            } else {
                renderEntityInGui(guiGraphics, bookX + 132 + 56, bookY + 90 + 85, bossScale, partialTick);
            }
        }

        
        // health and armor info
        if (mouseX >= bookX + 145 && mouseX < bookX + 145 + 16 && mouseY >= bookY + 209 && mouseY < bookY + 209 + 27) {
            guiGraphics.renderComponentTooltip(
                Minecraft.getInstance().font,
                tooltip,
                mouseX, mouseY
            );
        } else {
        // additional info only if required
        if (additionalInfo && showInfo) {
            if (mouseX >= bookX + 199 && mouseX < bookX + 199 + 16 && mouseY >= bookY + 210 && mouseY < bookY + 210 + 27) {
                guiGraphics.renderTooltip(
                    Minecraft.getInstance().font,
                    Component.literal(Component.translatable("gui.boss_checklist.info_" + bossId.replace(":", "_")).getString()),
                    mouseX, mouseY
                );
            }
        }
        // Defeated or not info
        if (mouseX >= bookX + 172 && mouseX < bookX + 172 + 16 && mouseY >= bookY + 210 && mouseY < bookY + 210 + 27) {
            if (isBossDefeated_InWorld) { // yes
                guiGraphics.renderComponentTooltip(
                    Minecraft.getInstance().font,
                    tooltip3,
                    mouseX, mouseY
                );
            } else {
                    if (isLocalServerOrAnyoneBossKilled) { // no
                        guiGraphics.renderTooltip(
                            Minecraft.getInstance().font,
                            Component.literal(Component.translatable("gui.boss_checklist.not_defeated").getString()),
                            mouseX, mouseY
                        );
                } else { // no + info about server requires
                    guiGraphics.renderComponentTooltip(
                        Minecraft.getInstance().font,
                        tooltip2,
                        mouseX, mouseY
                    );
                }
            }
        } else {
            if (mouseX >= bookX + 350 && mouseX < bookX + 350 + 16 && mouseY >= bookY + 210 && mouseY < bookY + 210 + 27) {
                guiGraphics.renderTooltip(
                    Minecraft.getInstance().font,
                    Component.literal(Component.translatable("gui.boss_checklist.wiki_link_info").getString()),
                    mouseX, mouseY
                );
            }
        } } 
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (!showInfo) {
            guiGraphics.fill(bookX + 265, bookY + 51, bookX + 265 + 50, bookY + 51 + 18, 0x88AAAAAA);
        }
    }


    private void renderDrops(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y) {

        if (drops == null || drops.isEmpty()) {
            guiGraphics.drawString(this.font, Component.translatable("gui.boss_checklist.no_drop").getString(), x, y, 0xFF616161, false);
            return;
        }
        

        int i = 0;
        for (String id : drops) {
            
            int row = i / perRow;
            int col = i % perRow;

            int xPos = x + col * dropSpacing;
            int yPos = y + row * dropSpacing;

            // if suffix #*number* there is render drop chance
            if (id.split("#").length > 1) {
                itemid = id.split("#")[0];
                dropChance = id.split("#")[1];
                chance = true;
            } else {
                itemid = id.split("#")[0];
                chance = false;
            }

            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemid));
            ItemStack stack = new ItemStack(item);
            
            guiGraphics.renderItem(stack, xPos, yPos);

            if (chance) {
                PoseStack poseStack = guiGraphics.pose();
                poseStack.pushPose();
                guiGraphics.pose().scale(0.75f, 0.75f, 1f);
                guiGraphics.drawString(this.font, dropChance + "%", (int) (xPos / 0.75 + 4), (int) (yPos / 0.75 + 22), 0xFF616161, false);
                guiGraphics.pose().popPose();
            }

            // tooltip if mouse hovered
            if (mouseX >= xPos && mouseX <= xPos + 16 && mouseY >= yPos && mouseY <= yPos + 16) {
                guiGraphics.renderTooltip(this.font, stack, mouseX, mouseY);
            }

            i++;
        }
    }

    private void renderSummon(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.drawWordWrap(font,  summonText,  x,  y, 117, 0x000000);
    }


    public void renderEntityInGui(GuiGraphics graphics, int x, int y, int scale, float partialTicks) {
        
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        //entity.tickCount = 0; // working only in singleplayer

        poseStack.translate(x, y + bossYCorrection, 0); // for 1.20.1- change z to 100
        
        poseStack.scale((float) scale, (float) scale, (float) scale);
        
        poseStack.translate(0, -entity.getBbHeight() / 2.0F, 0); 
        
        poseStack.mulPose(Axis.XP.rotationDegrees(rotationX + 180));
        
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotationY + 180));
        //entity.lerpHeadTo(-rotationY + 180, 0); // dont working

        

        dispatcher.render(entity, 0, 0, 0, 0, 0, poseStack, buffer, 15728880);
        buffer.endBatch();

        poseStack.popPose();
    }

    

    private boolean isMouseOverBoss(double mouseX, double mouseY) {
    return mouseX >= ((this.width - 512) / 2) + 135  && mouseX <= ((this.width - 512) / 2) + 135 + 110 &&
           mouseY >= ((this.height - 256) / 2) + 90 && mouseY <= ((this.height - 256) / 2) + 90 + 110;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isMouseOverBoss(mouseX, mouseY)) {
            // drag started and rotation paused
            dragging = true;
            allowRotation = false;
            return true; // stop method
        }
        if (wiki_link_enabled) {
            if (mouseX >= ((this.width - 512) / 2) + 350  && mouseX <= ((this.width - 512) / 2) + 350 + 16 && mouseY >= ((this.height - 256) / 2) + 210 && mouseY <= ((this.height - 256) / 2) + 210 + 27) {
                minecraft.setScreen(new ConfirmLinkScreen((confirmed) -> {
                    if (confirmed) {
                        net.minecraft.Util.getPlatform().openUri(wiki_link);
                    }
                    this.minecraft.setScreen(this);
                }, wiki_link, true));
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0) {
            rotationY += (float)(dragX * MOUSE_SENS);
            rotationX -= (float)(dragY * MOUSE_SENS);
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
        // if ESC set screen to checklist
        Minecraft.getInstance().setScreen(parent);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (BossChecklistClient.OPEN_CHECKLIST.matches(keyCode, scanCode)) {
            Minecraft.getInstance().setScreen(parent);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
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
}