package com.yori3o.boss_checklist.common.client.gui;


import net.minecraft.resources.Identifier;


/**
 * This class contains most of the textures and button positions.
 */
public class GuiConstants {
    

    public static final Identifier BOOK = 
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/book.png");


    public static final Identifier BUTTON_TEXTURE =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/button.png");
    public static final Identifier BUTTON_TEXTURE_hovered =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/button_hovered.png");
    public static final Identifier BUTTON_TEXTURE_pressed =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/button_pressed.png");
    public static final Identifier BUTTON_TEXTURE_overlay =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/button_overlay.png");
        
    public static final Identifier BIG_BUTTON_TEXTURE =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/big_button.png");
    public static final Identifier BIG_BUTTON_TEXTURE_hovered =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/big_button_hovered.png");
    public static final Identifier BIG_BUTTON_TEXTURE_pressed =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/big_button_pressed.png");
    public static final Identifier BIG_BUTTON_TEXTURE_overlay =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/big_button_overlay.png");

    public static final Identifier SMALL_BUTTON_TEXTURE_hovered =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/small_button_hovered.png");
    public static final Identifier SMALL_BUTTON_TEXTURE_pressed =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/small_button_pressed.png");
    public static final Identifier SMALL_BUTTON_TEXTURE =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/small_button.png");
        
    public static final Identifier CLOSE_BUTTON_TEXTURE =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/close.png");
    public static final Identifier CLOSE_BUTTON_TEXTURE_hovered =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/close_hovered.png");
        
    public static final Identifier OPEN_BUTTON_TEXTURE =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/open.png");
    public static final Identifier OPEN_BUTTON_TEXTURE_hovered =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/open_hovered.png");

    public static final Identifier EDITOR_BUTTON_TEXTURE =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/editor.png");
    public static final Identifier EDITOR_BUTTON_TEXTURE_hovered =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/editor_hovered.png");


    public static final Identifier BOOKMARK =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/bookmarks/bookmark.png");
    public static final Identifier BOOKMARK_NOT_DEFEATED =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/bookmarks/bookmark_not_defeated.png");
    public static final Identifier BOOKMARK_DEFEATED =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/bookmarks/bookmark_defeated.png");
    public static final Identifier BOOKMARK_INFO =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/bookmarks/bookmark_info.png");
    public static final Identifier BOOKMARK_WIKI =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/bookmarks/bookmark_wiki.png");
    public static final Identifier BOOKMARK_SETTINGS = 
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/bookmarks/bookmark_settings.png");

    public static final Identifier STATS_INFO_BACK = 
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/stats_info_back.png");
    public static final Identifier BATTLE_ICON = 
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/icons/battle_icon.png");
    public static final Identifier DURATION_ICON = 
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/icons/duration_icon.png");
    public static final Identifier TOP_ICON = 
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/icons/top_icon.png");
    public static final Identifier ARROW_UP =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/icons/arrow_up.png");
    public static final Identifier ARROW_DOWN =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/icons/arrow_down.png");
    public static final Identifier TOP_1 = 
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/icons/top1_icon.png");
    public static final Identifier TOP_2 = 
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/icons/top2_icon.png");
    public static final Identifier TOP_3 = 
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/icons/top3_icon.png");

    public static final Identifier PROGRESS_BAR_BACKGROUND =
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/progress_bar_background.png");
    public static final Identifier PROGRESS_BAR_FILL = 
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/progress_bar_fill.png");

    public static final Identifier CHECKBOX = 
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/checkbox.png");
    public static final Identifier NOTICE = 
        Identifier.fromNamespaceAndPath("boss_checklist", "textures/gui/notice.png");

    // FOR 1.21.1+
    public static final Identifier PAGE_BUTTON_TEXTURE_backward =
        Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/widget/page_backward.png");
    public static final Identifier PAGE_BUTTON_TEXTURE_backward_highlighted =
        Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/widget/page_backward_highlighted.png");
    public static final Identifier PAGE_BUTTON_TEXTURE_forward =
        Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/widget/page_forward.png");
    public static final Identifier PAGE_BUTTON_TEXTURE_forward_highlighted =
        Identifier.fromNamespaceAndPath("minecraft", "textures/gui/sprites/widget/page_forward_highlighted.png");

    // in 1.20.1 these textures are located in the atlas, and I am too lazy to select their position, so I just copied them from 1.21.1
    /*public static final Identifier BUTTON_TEXTURE_backward =
        new Identifier("boss_checklist", "textures/gui/page_backward.png");
    public static final Identifier BUTTON_TEXTURE_backward_highlighted =
        new Identifier("boss_checklist", "textures/gui/page_backward_highlighted.png");
    public static final Identifier BUTTON_TEXTURE_forward =
        new Identifier("boss_checklist", "textures/gui/page_forward.png");
    public static final Identifier BUTTON_TEXTURE_forward_highlighted =
        new Identifier("boss_checklist", "textures/gui/page_forward_highlighted.png");*/


    public static final int MAX_LABEL_WIDTH = 103;

    public static final int CLOSE_BUTTON_X = 403;
    public static final int CLOSE_BUTTON_Y = 44;
    public static final int CLOSE_BUTTON_SIZE = 10;
    
    public static final int EDITOR_BUTTON_SIZE = 11;

    public static final int DROP_BUTTON_X = 265;
    public static final int DROP_BUTTON_Y = 51;
    public static final int SPAWN_BUTTON_X = 328;
    public static final int SPAWN_BUTTON_Y = 51;
    public static final int MEDIUM_BUTTONS_HEIGHT = 18;
    public static final int MEDIUM_BUTTONS_WIDTH = 50;
    public static final int MEDIUM_BUTTONS_OVERLAY_HEIGHT = 40;
    public static final int MEDIUM_BUTTONS_OVERLAY_WIDTH = 70;
    public static final int BIG_BUTTONS_HEIGHT = 18;
    public static final int BIG_BUTTONS_WIDTH = 100;
    public static final int BIG_BUTTONS_OVERLAY_HEIGHT = 40;
    public static final int BIG_BUTTONS_OVERLAY_WIDTH = 120;
    
    public static final int BAR_HEIGHT = 9;
    public static final int BAR_WIDTH = 186;

    public static final int STATS_TAB_X = 86;
    public static final int STATS_TAB_Y = 51;
    public static final int STATS_TAB_WIDTH = 17;
    public static final int STATS_TAB_HEIGHT = 68;
    public static final int ICONS_SIZE = 10;

   
}