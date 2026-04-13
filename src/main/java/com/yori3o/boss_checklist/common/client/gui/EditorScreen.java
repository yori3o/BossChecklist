package com.yori3o.boss_checklist.common.client.gui;


import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2i;

import com.yori3o.boss_checklist.common.BossChecklistClient;
import com.yori3o.boss_checklist.common.client.boss.BossDefinition;
import com.yori3o.boss_checklist.common.client.boss.BossProgress;
import com.yori3o.boss_checklist.common.client.boss.CustomBossEntry;
import com.yori3o.boss_checklist.common.client.data.OverlapManager;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomButton;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomCheckbox;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomNumberEditBox;
import com.yori3o.boss_checklist.impl.PlatformUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;



public class EditorScreen extends Screen {
        

    private CustomButton closeButton;
    private final Screen parent;
    
    private CustomButton previewButton;
    private CustomButton saveButton;
    
    private EditBox idTextBox;
    private EditBox nameTextBox;
    private EditBox modTextBox;
    private EditBox spawnTextBox;
    private EditBox dropsTextBox;
    private CustomNumberEditBox scaleNumberBox;
    private CustomNumberEditBox yOffsetNumberBox;
    private CustomNumberEditBox positionNumberBox;
    private CustomCheckbox brokenModelCheckbox;
    private CustomCheckbox minibossCheckbox;

    private String id;
    private String name;
    private String mod;
    private String spawn;
    private String drops;
    private Integer scale;
    private Integer yOffset;
    private Float position;
    private boolean brokenModel = false;
    private boolean miniboss = false;
    
    private static final int CHECKBOX_X = 400;
    private static final int CB_BROKEN_MODEL_Y = 75;
    private static final int CB_MINIBOSS_Y = 95;
    
    private static final int TEXTBOX_X = 134;
    private static final int TB_ID_Y = 55;
    private static final int TB_NAME_Y = 85;
    private static final int TB_MOD_Y = 115;
    private static final int TB_SPAWN_Y = 145;
    private static final int TB_DROPS_Y = 175;
    private static final int TEXTBOX_X_RIGHT_PAGE = 270;

    private String errorText;

    private boolean reloadRequired = false;
    




    public EditorScreen(Screen parent) {
        super(Component.literal("Editor"));
        this.parent = parent;
    }


    public void init() {
        super.init();
        createButtons();
        errorText = null;
    }


    private void createButtons() {
        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;



        /// --- BUTTONS: PREVIEW, SAVE, CLOSE ----
        previewButton = new CustomButton(
            bookX + TEXTBOX_X_RIGHT_PAGE, bookY + GuiConstants.DROP_BUTTON_Y, GuiConstants.BIG_BUTTONS_WIDTH, GuiConstants.BIG_BUTTONS_HEIGHT, 
            GuiConstants.BIG_BUTTONS_OVERLAY_WIDTH, GuiConstants.BIG_BUTTONS_OVERLAY_HEIGHT, 
            Component.translatable("gui.boss_checklist.editor.preview"), 
            GuiConstants.BIG_BUTTON_TEXTURE, GuiConstants.BIG_BUTTON_TEXTURE_hovered, GuiConstants.BIG_BUTTON_TEXTURE_pressed, GuiConstants.BIG_BUTTON_TEXTURE_overlay, 
            () -> {
                openPreviewBossInfo();
            }
        );
        addRenderableWidget(previewButton);

        saveButton = new CustomButton(
            bookX + TEXTBOX_X_RIGHT_PAGE, bookY + GuiConstants.DROP_BUTTON_Y + 30, GuiConstants.BIG_BUTTONS_WIDTH, GuiConstants.BIG_BUTTONS_HEIGHT, 
            GuiConstants.BIG_BUTTONS_OVERLAY_WIDTH, GuiConstants.BIG_BUTTONS_OVERLAY_HEIGHT, 
            Component.translatable("gui.boss_checklist.editor.save"), 
            GuiConstants.BIG_BUTTON_TEXTURE, GuiConstants.BIG_BUTTON_TEXTURE_hovered, GuiConstants.BIG_BUTTON_TEXTURE_pressed, GuiConstants.BIG_BUTTON_TEXTURE_overlay, 
            () -> {
                save();
            }
        );
        addRenderableWidget(saveButton);

        closeButton = new CustomButton(
            bookX + GuiConstants.CLOSE_BUTTON_X, bookY + GuiConstants.CLOSE_BUTTON_Y, GuiConstants.CLOSE_BUTTON_SIZE, GuiConstants.CLOSE_BUTTON_SIZE, 0, 0, 
            Component.empty(), 
            GuiConstants.CLOSE_BUTTON_TEXTURE, GuiConstants.CLOSE_BUTTON_TEXTURE_hovered, GuiConstants.CLOSE_BUTTON_TEXTURE_hovered, null, 
            () -> {
                onClose();
            }
        );
        addRenderableWidget(closeButton);



        /// --- CHECKBOXES: BROKEN MODEL, MINIBOSS ---
        brokenModelCheckbox = new CustomCheckbox(bookX + CHECKBOX_X, bookY + CB_BROKEN_MODEL_Y, GuiConstants.MAX_LABEL_WIDTH, 
            Component.translatable("gui.boss_checklist.editor.broken_model"), brokenModel, 
            false, false,
            checked -> {brokenModel = checked;},
            null
        );
        addRenderableWidget(brokenModelCheckbox);

        minibossCheckbox = new CustomCheckbox(bookX + CHECKBOX_X, bookY + CB_MINIBOSS_Y, GuiConstants.MAX_LABEL_WIDTH, 
            Component.translatable("gui.boss_checklist.editor.miniboss"), miniboss, 
            false, false,
            checked -> {miniboss = checked;},
            null
        );
        addRenderableWidget(minibossCheckbox);

        

        /// --- EDITBOXES: ID, NAME, MOD, SPAWN INFO ---
        idTextBox = new EditBox(
            this.font,
            bookX + TEXTBOX_X,
            bookY + TB_ID_Y,
            105,
            20,
            Component.translatable("gui.boss_checklist.editor.boss_id")
        );
        idTextBox.setHint(Component.translatable("gui.boss_checklist.editor.boss_id"));
        idTextBox.setMaxLength(100);
        idTextBox.setResponder(this::updateIdText);
        if (id != null) idTextBox.setValue(id);
        addRenderableWidget(idTextBox);

        nameTextBox = new EditBox(
            this.font,
            bookX + TEXTBOX_X,
            bookY + TB_NAME_Y,
            105,
            20,
            Component.translatable("gui.boss_checklist.editor.boss_name")
        );
        nameTextBox.setHint(Component.translatable("gui.boss_checklist.editor.boss_name"));
        nameTextBox.setMaxLength(100);
        nameTextBox.setResponder(this::updateNameText);
        if (name != null) nameTextBox.setValue(name);
        addRenderableWidget(nameTextBox);

        modTextBox = new EditBox(
            this.font,
            bookX + TEXTBOX_X,
            bookY + TB_MOD_Y,
            105,
            20,
            Component.translatable("gui.boss_checklist.editor.boss_mod")
        );
        modTextBox.setHint(Component.translatable("gui.boss_checklist.editor.boss_mod"));
        modTextBox.setMaxLength(50);
        modTextBox.setResponder(this::updateModText);
        if (mod != null) modTextBox.setValue(mod);
        addRenderableWidget(modTextBox);

        spawnTextBox = new EditBox(
            this.font,
            bookX + TEXTBOX_X,
            bookY + TB_SPAWN_Y,
            105,
            20,
            Component.translatable("gui.boss_checklist.editor.boss_spawn")
        );
        spawnTextBox.setHint(Component.translatable("gui.boss_checklist.editor.boss_spawn"));
        spawnTextBox.setMaxLength(300);
        spawnTextBox.setResponder(this::updateSpawnText);
        if (spawn != null) spawnTextBox.setValue(spawn);
        addRenderableWidget(spawnTextBox);

        dropsTextBox = new EditBox(
            this.font,
            bookX + TEXTBOX_X,
            bookY + TB_DROPS_Y,
            105,
            20,
            Component.translatable("gui.boss_checklist.editor.boss_drops")
        );
        dropsTextBox.setHint(Component.translatable("gui.boss_checklist.editor.boss_drops"));
        dropsTextBox.setMaxLength(300);
        dropsTextBox.setResponder(this::updateDropsText);
        if (drops != null) dropsTextBox.setValue(drops);
        addRenderableWidget(dropsTextBox);



        /// --- NUMBERBOXES: SCALE, Y OFFSET ---
        scaleNumberBox = new CustomNumberEditBox(this.font, bookX + TEXTBOX_X_RIGHT_PAGE, bookY + 110, 80, 20,
            Component.translatable("gui.boss_checklist.editor.scale"), false, false
        );
        scaleNumberBox.setHint(Component.translatable("gui.boss_checklist.editor.scale"));
        scaleNumberBox.setMaxLength(2);
        scaleNumberBox.setResponder(this::updateScaleValue);
        if (scale != null) scaleNumberBox.setIntValue(scale);
        addRenderableWidget(scaleNumberBox);

        yOffsetNumberBox = new CustomNumberEditBox(this.font, bookX + TEXTBOX_X_RIGHT_PAGE, bookY + 140, 80, 20,
            Component.translatable("gui.boss_checklist.editor.y_offset"), true, false
        );
        yOffsetNumberBox.setHint(Component.translatable("gui.boss_checklist.editor.y_offset"));
        yOffsetNumberBox.setMaxLength(3);
        yOffsetNumberBox.setResponder(this::updateYOffsetValue);
        if (yOffset != null) yOffsetNumberBox.setIntValue(yOffset);
        addRenderableWidget(yOffsetNumberBox);

        positionNumberBox = new CustomNumberEditBox(this.font, bookX + TEXTBOX_X_RIGHT_PAGE, bookY + 170, 80, 20,
            Component.translatable("gui.boss_checklist.editor.position"), false, true
        );
        positionNumberBox.setHint(Component.translatable("gui.boss_checklist.editor.position"));
        positionNumberBox.setMaxLength(10);
        positionNumberBox.setResponder(this::updatePositionValue);
        if (position != null) positionNumberBox.setDoubleValue(position);
        addRenderableWidget(positionNumberBox);

    }




    private void updateIdText(String text) {
        id = text;
        suggetName();
        suggetModName();
    }
    private void updateNameText(String text) {
        name = text;
    }
    private void updateModText(String text) {
        mod = text;
    }
    private void updateSpawnText(String text) {
        spawn = text;
    }
    private void updateDropsText(String text) {
        drops = text;
    }

    private void updateScaleValue(String text) {
        scale = scaleNumberBox.getIntValue();
    }
    private void updateYOffsetValue(String text) {
        yOffset = yOffsetNumberBox.getIntValue();
    }
    private void updatePositionValue(String text) {
        position = (float)positionNumberBox.getDoubleValue();
    }



    private void openPreviewBossInfo() {
        if (!checkFields()) {
            return;
        }
        BossDefinition definition = new BossDefinition(id, position, scale, yOffset, brokenModel, miniboss, parseDrops(drops));
        BossProgress progress = new BossProgress();
        CustomBossEntry customBossEntry = new CustomBossEntry(definition, progress, name, mod, spawn);
        Minecraft.getInstance().setScreen(new BossInfoScreen(this, customBossEntry));
    }

    public static boolean isValidNamespacedId(String s) {
        if (s == null) return false;
        int colon = s.indexOf(':');
        if (colon == -1 || colon != s.lastIndexOf(':')) return false;
        if (colon == 0 || colon == s.length() - 1) return false;
        String[] splited = s.split(":");
        if (!Identifier.isValidNamespace(splited[0]) || !Identifier.isValidPath(splited[1])) return false;
        return true;
    }




    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.BOOKMARK_INFO, bookX + 199, bookY + 209, 0, 0, 16, 27, 16, 27);

        if (errorText != null) {
            guiGraphics.centeredText(font, errorText, centerX, centerY - 108, 0xFFB30202);
        }

        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);

        renderDesc(guiGraphics, mouseX, mouseY, bookX, bookY);
    }

    public void renderDesc(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, int bookX, int bookY) {

        if (mouseX >= bookX + 199 && mouseX < bookX + 199 + 16 && mouseY >= bookY + 210 && mouseY < bookY + 210 + 27) {
            renderTooltip(
                guiGraphics, Minecraft.getInstance().font,
                List.of(
                    Component.translatable("gui.boss_checklist.editor.guide_help"),
                    Component.translatable("gui.boss_checklist.editor.guide_id_format"),
                    Component.translatable("gui.boss_checklist.editor.guide_drop_format"),
                    Component.translatable("gui.boss_checklist.editor.guide_y_offset_inverted"),
                    Component.translatable("gui.boss_checklist.editor.guide_save_path")
                ),
                mouseX, mouseY
            );
        }
    }

    public static void renderTooltip(GuiGraphicsExtractor guiGraphics, Font font, List<MutableComponent> list, int mouseX, int mouseY) {
        List<ClientTooltipComponent> listTooltip = new ArrayList<>();
        for (MutableComponent component : list) {
            listTooltip.add(ClientTooltipComponent.create(
                component.getVisualOrderText()
            ));
        }
        guiGraphics.tooltip(
            font,
            listTooltip,
            mouseX, mouseY, (screenWidth, screenHeight, x, y, tooltipWidth, tooltipHeight) -> new Vector2i(x + 12, y - 2), null
        );
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
    public void onClose() {
        if (reloadRequired) {
            reloadRequired = false;
            Minecraft.getInstance().reloadResourcePacks();
        } else {
            Minecraft.getInstance().setScreen(parent);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        if (BossChecklistClient.OPEN_CHECKLIST.matches(keyEvent)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyEvent);
    }


    private List<String> parseDrops(String drops) {
        String[] dropsArray = drops.split(",");
        List<String> dropsList = new ArrayList<>();
        for (String drop : dropsArray) {
            drop = drop.replace("\"", "").replace(" ", "");
            dropsList.add(drop);
        }
        return dropsList;
    }

    private boolean checkFields() {
        if (position == null) position = 0f;
        if (scale == null) scale = 10;
        if (yOffset == null) yOffset = 0;
        if (name == null) name = id;
        if (mod == null) mod = "*mod name*";
        if (spawn == null) spawn = "*spawn info*";
        if (drops == null) drops = "";
        if (!isValidNamespacedId(id)) {
            errorText = Component.translatable("gui.boss_checklist.editor.error_invalid_id").getString();
            return false;
        }
        return true;
    }

    private void save() {
        if (!checkFields()) {
            return;
        }
        BossDefinition def = new BossDefinition(id, position, scale, yOffset, brokenModel, miniboss, parseDrops(drops));
        OverlapManager.saveOrAdd(def, name, mod, spawn);
        reloadRequired = true;
    }

    private void suggetName() {
        if (name != null) {
            if (!name.isEmpty()) return;
        }   
        if (!isValidNamespacedId(id)) return;
        String[] s = id.split(":");
        String translationKey = "entity." + s[0] + "." + s[1];
        if (Language.getInstance().has(translationKey)) {
            nameTextBox.setValue(Component.translatable(translationKey).getString());
        }  
    }

    private void suggetModName() {
        if (mod != null) {
            if (!mod.isEmpty()) return;
        }    
        String[] s = id.split(":", -1);
        String modName = PlatformUtil.getModName(s[0]);
        if (modName == null) return;
        modTextBox.setValue(modName);
    }
}