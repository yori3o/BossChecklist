package com.yori3o.boss_checklist.common.client.gui;


import java.util.ArrayList;
import java.util.List;

import com.yori3o.boss_checklist.common.BossChecklistClient;
import com.yori3o.boss_checklist.common.client.boss.BossEntry;
import com.yori3o.boss_checklist.common.client.boss.BossDefinition;
import com.yori3o.boss_checklist.common.client.boss.BossProgress;
import com.yori3o.boss_checklist.common.client.boss.CustomBossEntry;
import com.yori3o.boss_checklist.common.client.data.OverrideManager;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomButton;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomCheckbox;
import com.yori3o.boss_checklist.common.client.gui.widget.CustomNumberEditBox;
import com.yori3o.boss_checklist.common.util.LoggerUtil;
import com.yori3o.boss_checklist.common.util.TooltipUtil;
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
        

    private final Screen parent;
    private CustomButton closeButton, previewButton, saveButton;
    
    private EditBox idTextBox, nameTextBox, modTextBox, spawnTextBox, dropsTextBox, versionTextBox, wikiTextBox, addInfoTextBox, descriptionTextBox;
    private CustomNumberEditBox scaleNumberBox, yOffsetNumberBox, positionNumberBox;
    private CustomCheckbox brokenModelCheckbox, minibossCheckbox, additionalInfoCheckbox;

    private String id, name, mod, spawn, drops, version, wikiLink, description, addtlInfo;
    private Integer scale, yOffset;
    private Float position;
    private boolean brokenModel, miniboss, additionalInfo;

    private final BossEntry bossToEdit;
    private final String modTranslationKey;
    
    private static final int TEXTBOX_X = 134;
    private static final int TEXTBOX_X_RIGHT_PAGE = 270;
    private static final int TB_ID_Y = 55;
    private static final int TB_NAME_Y = 85;
    private static final int TB_MOD_Y = 115;
    private static final int TB_SPAWN_Y = 145;
    private static final int TB_DROPS_Y = 175;

    private static final int ADDITIONAL_X = 0;
    private static final int TB_VERSION_Y = 55;
    private static final int CB_BROKEN_MODEL_Y = 82;
    private static final int CB_MINIBOSS_Y = 98;
    private static final int TB_WIKI_Y = 145;
    private static final int TB_ADDINFO_Y = 175;
    private static final int TB_DESCRIPTION_Y = TB_MOD_Y;


    private String errorText;

    private boolean reloadRequired = false;
    




    public EditorScreen(Screen parent) {
        this(parent, null, null);
    }

    EditorScreen(Screen parent, BossEntry bossToEdit, String modTranslationKey) {
        super(Component.literal("Editor"));
        this.parent = parent;
        this.bossToEdit = bossToEdit;
        this.modTranslationKey = modTranslationKey;

        if (bossToEdit != null) {
            initializeEditValues();
        }
    }

    private void initializeEditValues() {
        String bossTranslationId = bossToEdit.id().replace(":", "_");
        id = bossToEdit.id();
        name = Language.getInstance().getOrDefault("boss_checklist.boss." + bossTranslationId);
        mod = Language.getInstance().getOrDefault(modTranslationKey);
        spawn = Language.getInstance().getOrDefault("boss_checklist.summon." + bossTranslationId);
        String descriptionKey = "boss_checklist.desc." + bossTranslationId;
        description = Language.getInstance().has(descriptionKey)
            ? Language.getInstance().getOrDefault(descriptionKey)
            : "";
        String additionalInfoKey = "boss_checklist.info." + bossTranslationId;
        addtlInfo = Language.getInstance().has(additionalInfoKey)
            ? Language.getInstance().getOrDefault(additionalInfoKey)
            : "";
        BossDefinition definition = bossToEdit.definition();
        drops = String.join(", ", definition.drops());
        scale = definition.scale();
        yOffset = definition.yOffset();
        version = definition.modVersion();
        additionalInfo = definition.additionalInfo();
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
            Component.translatable(bossToEdit == null ? "gui.boss_checklist.editor.save" : "gui.boss_checklist.editor.update"),
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

        if (bossToEdit != null) {
            createEditWidgets(bookX, bookY);
            return;
        }

        /// --- CHECKBOXES: BROKEN MODEL, MINIBOSS ---
        brokenModelCheckbox = new CustomCheckbox(bookX + ADDITIONAL_X, bookY + CB_BROKEN_MODEL_Y,
            Component.translatable("gui.boss_checklist.editor.broken_model"), 
            GuiConstants.MAX_LABEL_WIDTH,
            brokenModel, 
            checked -> {brokenModel = checked;}
        );
        addRenderableWidget(brokenModelCheckbox);

        minibossCheckbox = new CustomCheckbox(bookX + ADDITIONAL_X, bookY + CB_MINIBOSS_Y,
            Component.translatable("gui.boss_checklist.editor.miniboss"), 
            GuiConstants.MAX_LABEL_WIDTH,
            miniboss, 
            checked -> {miniboss = checked;}
        );
        addRenderableWidget(minibossCheckbox);

        

        /// --- EDITBOXES: ID, NAME, MOD, SPAWN INFO, VERSION ---
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

        versionTextBox = new EditBox(
            this.font,
            bookX + ADDITIONAL_X,
            bookY + TB_VERSION_Y,
            105,
            20,
            Component.translatable("gui.boss_checklist.editor.boss_version")
        );
        versionTextBox.setHint(Component.translatable("gui.boss_checklist.editor.boss_version"));
        versionTextBox.setMaxLength(300);
        versionTextBox.setResponder(this::updateVersionText);
        if (version != null) versionTextBox.setValue(version);
        addRenderableWidget(versionTextBox);

        wikiTextBox = new EditBox(
            this.font,
            bookX + ADDITIONAL_X,
            bookY + TB_WIKI_Y,
            105,
            20,
            Component.translatable("gui.boss_checklist.editor.boss_wiki_link")
        );
        wikiTextBox.setHint(Component.translatable("gui.boss_checklist.editor.boss_wiki_link"));
        wikiTextBox.setMaxLength(300);
        wikiTextBox.setResponder(this::updateWikiText);
        if (wikiLink != null) wikiTextBox.setValue(wikiLink);
        addRenderableWidget(wikiTextBox);

        addInfoTextBox = new EditBox(
            this.font,
            bookX + ADDITIONAL_X,
            bookY + TB_ADDINFO_Y,
            105,
            20,
            Component.translatable("gui.boss_checklist.editor.boss_additional_info")
        );
        addInfoTextBox.setHint(Component.translatable("gui.boss_checklist.editor.boss_additional_info"));
        addInfoTextBox.setMaxLength(300);
        addInfoTextBox.setResponder(this::updateAddtlText);
        if (addtlInfo != null) addInfoTextBox.setValue(addtlInfo);
        addRenderableWidget(addInfoTextBox);

        descriptionTextBox = new EditBox(
            this.font,
            bookX + ADDITIONAL_X,
            bookY + TB_DESCRIPTION_Y,
            105,
            20,
            Component.translatable("gui.boss_checklist.editor.boss_description")
        );
        descriptionTextBox.setHint(Component.translatable("gui.boss_checklist.editor.boss_description"));
        descriptionTextBox.setMaxLength(300);
        descriptionTextBox.setResponder(this::updateDescriptionText);
        if (description != null) descriptionTextBox.setValue(description);
        addRenderableWidget(descriptionTextBox);




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

    private void createEditWidgets(int bookX, int bookY) {
        idTextBox = new EditBox(this.font, bookX + TEXTBOX_X, bookY + TB_ID_Y, 105, 20,
            Component.translatable("gui.boss_checklist.editor.boss_id"));
        idTextBox.setHint(Component.translatable("gui.boss_checklist.editor.boss_id"));
        idTextBox.setValue(id);
        idTextBox.setEditable(false);
        addRenderableWidget(idTextBox);

        nameTextBox = createEditTextBox(bookX + TEXTBOX_X, bookY + TB_NAME_Y, "gui.boss_checklist.editor.boss_name", 100, this::updateNameText, name);
        modTextBox = createEditTextBox(bookX + TEXTBOX_X, bookY + TB_MOD_Y, "gui.boss_checklist.editor.boss_mod", 50, this::updateModText, mod);
        spawnTextBox = createEditTextBox(bookX + TEXTBOX_X, bookY + TB_SPAWN_Y, "gui.boss_checklist.editor.boss_spawn", 300, this::updateSpawnText, spawn);
        descriptionTextBox = createEditTextBox(bookX + TEXTBOX_X, bookY + TB_DROPS_Y, "gui.boss_checklist.editor.boss_description", 300, this::updateDescriptionText, description);
        addInfoTextBox = createEditTextBox(bookX + TEXTBOX_X_RIGHT_PAGE, bookY + TB_ID_Y, "gui.boss_checklist.editor.boss_additional_info", 300, this::updateAddtlText, addtlInfo);
        dropsTextBox = createEditTextBox(bookX + ADDITIONAL_X, bookY + CB_BROKEN_MODEL_Y + 30 - 2, "gui.boss_checklist.editor.boss_drops", 300, this::updateDropsText, drops);
        versionTextBox = createEditTextBox(bookX + ADDITIONAL_X, bookY + TB_VERSION_Y + 30 - 5, "gui.boss_checklist.editor.boss_version", 300, this::updateVersionText, version);

        scaleNumberBox = new CustomNumberEditBox(this.font, bookX + ADDITIONAL_X, bookY + 140, 80, 20,
            Component.translatable("gui.boss_checklist.editor.scale"), false, false);
        scaleNumberBox.setHint(Component.translatable("gui.boss_checklist.editor.scale"));
        scaleNumberBox.setMaxLength(2);
        scaleNumberBox.setResponder(this::updateScaleValue);
        scaleNumberBox.setIntValue(scale);
        addRenderableWidget(scaleNumberBox);

        yOffsetNumberBox = new CustomNumberEditBox(this.font, bookX + ADDITIONAL_X, bookY + 170, 80, 20,
            Component.translatable("gui.boss_checklist.editor.y_offset"), true, false);
        yOffsetNumberBox.setHint(Component.translatable("gui.boss_checklist.editor.y_offset"));
        yOffsetNumberBox.setMaxLength(3);
        yOffsetNumberBox.setResponder(this::updateYOffsetValue);
        yOffsetNumberBox.setIntValue(yOffset);
        addRenderableWidget(yOffsetNumberBox);

        additionalInfoCheckbox = new CustomCheckbox(bookX + TEXTBOX_X_RIGHT_PAGE, bookY + TB_NAME_Y,
            Component.translatable("gui.boss_checklist.editor.additional_info_enabled"),
            GuiConstants.MAX_LABEL_WIDTH,
            additionalInfo,
            checked -> additionalInfo = checked
        );
        addRenderableWidget(additionalInfoCheckbox);

        previewButton.setY(bookY + 115);
        saveButton.setY(bookY + 145);
    }

    private EditBox createEditTextBox(int x, int y, String labelKey, int maxLength, java.util.function.Consumer<String> responder, String value) {
        EditBox textBox = new EditBox(this.font, x, y, 105, 20, Component.translatable(labelKey));
        textBox.setHint(Component.translatable(labelKey));
        textBox.setMaxLength(maxLength);
        textBox.setResponder(responder);
        textBox.setValue(value == null ? "" : value);
        addRenderableWidget(textBox);
        return textBox;
    }

    private void updateIdText(String text) {
        id = text;
        suggetName();
        suggetModName();
        suggetVersion();
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
    private void updateDescriptionText(String text) {
        description = text;
    }
    private void updateDropsText(String text) {
        drops = text;
    }
    private void updateVersionText(String text) {
        version = text;
    }
    private void updateWikiText(String text) {
        wikiLink = text;
    }
    private void updateAddtlText(String text) {
        addtlInfo = text;
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
        BossDefinition definition = bossToEdit == null
            ? new BossDefinition(id, position, scale, yOffset, brokenModel, miniboss, parseDrops(drops), version, wikiLink, additionalInfo)
            : bossToEdit.definition().copyForPreview(additionalInfo, parseDrops(drops), scale, yOffset, version);
        BossProgress progress = new BossProgress();
        CustomBossEntry customBossEntry = new CustomBossEntry(definition, progress, name, mod, spawn, description, addtlInfo);
        Minecraft.getInstance().gui.setScreen(new BossInfoScreen(this, customBossEntry));
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

        if (bossToEdit != null) {
            renderEditLabels(guiGraphics, bookX, bookY);
        }
        renderDesc(guiGraphics, mouseX, mouseY, bookX, bookY);
    }

    private void renderEditLabels(GuiGraphicsExtractor guiGraphics, int bookX, int bookY) {
        guiGraphics.text(font, Component.translatable("gui.boss_checklist.editor.boss_id"), bookX + TEXTBOX_X, bookY + 45 + 2, 0xFF000000, false);
        guiGraphics.text(font, Component.translatable("gui.boss_checklist.editor.boss_name"), bookX + TEXTBOX_X, bookY + 75 + 2, 0xFF000000, false);
        guiGraphics.text(font, Component.translatable("gui.boss_checklist.editor.boss_mod"), bookX + TEXTBOX_X, bookY + 105 + 2, 0xFF000000, false);
        guiGraphics.text(font, Component.translatable("gui.boss_checklist.editor.boss_spawn"), bookX + TEXTBOX_X, bookY + 135 + 2, 0xFF000000, false);
        guiGraphics.text(font, Component.translatable("gui.boss_checklist.editor.boss_description"), bookX + TEXTBOX_X, bookY + 165 + 2, 0xFF000000, false);
        guiGraphics.text(font, Component.translatable("gui.boss_checklist.editor.boss_additional_info"), bookX + TEXTBOX_X_RIGHT_PAGE, bookY + 45 + 2, 0xFF000000, false);
        guiGraphics.text(font, Component.translatable("gui.boss_checklist.editor.boss_version"), bookX + ADDITIONAL_X, bookY + TB_VERSION_Y - 8 + 30 - 5, 0xFF000000, false);
        guiGraphics.text(font, Component.translatable("gui.boss_checklist.editor.boss_drops"), bookX + ADDITIONAL_X, bookY + CB_BROKEN_MODEL_Y - 8 + 30 - 2, 0xFF000000, false);
        guiGraphics.text(font, Component.translatable("gui.boss_checklist.editor.scale"), bookX + ADDITIONAL_X, bookY + 102 + 30, 0xFF000000, false);
        guiGraphics.text(font, Component.translatable("gui.boss_checklist.editor.y_offset"), bookX + ADDITIONAL_X, bookY + 132 + 30, 0xFF000000, false);
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
        TooltipUtil.renderTooltip(guiGraphics, listTooltip, mouseX, mouseY);
    }





    // ONLY FOR 1.21.1+
    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);

        int bookX = (this.width - 512) / 2;
        int bookY = (this.height - 256) / 2;

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.BOOK, bookX, bookY, 0, 0, 512, 256, 512, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GuiConstants.LEFT_SHEET, bookX - 6, bookY + 45, 0, 0, 122, 167, 122, 167);
    }





    @Override
    public void onClose() {
        if (reloadRequired) {
            reloadRequired = false;
            Minecraft.getInstance().reloadResourcePacks();
        } else {
            Minecraft.getInstance().gui.setScreen(parent);
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
        List<String> dropsList = new ArrayList<>();
        for (String drop : drops.split(",")) {
            String normalizedDrop = drop.replace("\"", "").trim();
            if (!normalizedDrop.isEmpty()) {
                dropsList.add(normalizedDrop);
            }
        }
        return dropsList;
    }

    private boolean checkFields() {
        if (!isValidNamespacedId(id)) {
            errorText = Component.translatable("gui.boss_checklist.editor.error_invalid_id").getString();
            return false;
        }
        if (position == null) position = 0f;
        if (scale == null) scale = 10;
        if (yOffset == null) yOffset = 0;
        if (name == null) name = id;
        if (mod == null) mod = "*mod name*";
        if (spawn == null) spawn = "*spawn info*";
        if (drops == null) drops = "";
        if (description == null) description = "";
        if (addtlInfo == null) addtlInfo = "";
        if (bossToEdit == null) {
            additionalInfo = !addtlInfo.isEmpty();
        }
        return true;
    }

    private void save() {
        if (!checkFields()) {
            return;
        }
        if (bossToEdit == null) {
            BossDefinition definition = new BossDefinition(id, position, scale, yOffset, brokenModel, miniboss, parseDrops(drops), version, wikiLink, additionalInfo);
            OverrideManager.saveAndAdd(definition, name, mod, spawn, description, addtlInfo);
            reloadRequired = true;
            return;
        }

        BossDefinition override = bossToEdit.definition().copyForOverride(
            additionalInfo,
            parseDrops(drops),
            scale,
            yOffset,
            version
        );
        if (!OverrideManager.saveEditedBoss(override, name, modTranslationKey, mod, spawn, description, addtlInfo)) {
            errorText = Component.translatable("gui.boss_checklist.editor.error_save").getString();
            return;
        }
        reloadEditedBoss();
    }

    private void reloadEditedBoss() {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.reloadResourcePacks().whenComplete((unused, error) -> {
            if (error != null) {
                Exception reloadException = error instanceof Exception exception ? exception : new Exception(error);
                LoggerUtil.errorWithException("Failed to reload resources after editing a boss: ", reloadException);
                minecraft.execute(() -> errorText = Component.translatable("gui.boss_checklist.editor.error_reload").getString());
                return;
            }

            minecraft.execute(() -> {
                if (parent instanceof BossInfoScreen bossInfoScreen) {
                    minecraft.gui.setScreen(bossInfoScreen.createRefreshedScreen());
                } else {
                    minecraft.gui.setScreen(parent);
                }
            });
        });
    }

    private void suggetName() {
        if (name != null) {
            if (!name.isEmpty()) return;
        }   
        if (!isValidNamespacedId(id)) return;
        String translationKey = "entity." + id.replace(":", ".");
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

    private void suggetVersion() {
        if (version != null) {
            if (!version.isEmpty()) return;
        }    
        String[] s = id.split(":", -1);
        String version = PlatformUtil.getVerison(s[0]);
        if (version == null) return;
        versionTextBox.setValue(version);
    }
}