package com.yori3o.boss_checklist.common.client.gui;


import net.minecraft.resources.ResourceLocation;


/**
 * This class contains most of the textures and button positions.
 */
public class GuiConstants {
    

    public static final ResourceLocation BOOK = 
        new ResourceLocation("boss_checklist", "textures/gui/book.png");
        
    public static final ResourceLocation LEFT_SHEET = 
        new ResourceLocation("boss_checklist", "textures/gui/left_sheet.png");


    public static final ResourceLocation BUTTON_TEXTURE =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/button.png");
    public static final ResourceLocation BUTTON_TEXTURE_hovered =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/button_hovered.png");
    public static final ResourceLocation BUTTON_TEXTURE_pressed =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/button_pressed.png");
    public static final ResourceLocation BUTTON_TEXTURE_overlay =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/button_overlay.png");
        
    public static final ResourceLocation BIG_BUTTON_TEXTURE =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/big_button.png");
    public static final ResourceLocation BIG_BUTTON_TEXTURE_hovered =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/big_button_hovered.png");
    public static final ResourceLocation BIG_BUTTON_TEXTURE_pressed =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/big_button_pressed.png");
    public static final ResourceLocation BIG_BUTTON_TEXTURE_overlay =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/big_button_overlay.png");

    public static final ResourceLocation SMALL_BUTTON_TEXTURE_hovered =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/small_button_hovered.png");
    public static final ResourceLocation SMALL_BUTTON_TEXTURE_pressed =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/small_button_pressed.png");
    public static final ResourceLocation SMALL_BUTTON_TEXTURE =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/small_button.png");
        
    public static final ResourceLocation CLOSE_BUTTON_TEXTURE =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/close.png");
    public static final ResourceLocation CLOSE_BUTTON_TEXTURE_hovered =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/close_hovered.png");
        
    public static final ResourceLocation OPEN_BUTTON_TEXTURE =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/open.png");
    public static final ResourceLocation OPEN_BUTTON_TEXTURE_hovered =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/open_hovered.png");

    public static final ResourceLocation EDITOR_BUTTON_TEXTURE =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/editor.png");
    public static final ResourceLocation EDITOR_BUTTON_TEXTURE_hovered =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/editor_hovered.png");
        
    public static final ResourceLocation CONFIG_BUTTON_TEXTURE =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/config.png");
    public static final ResourceLocation CONFIG_BUTTON_TEXTURE_highlighted =
        new ResourceLocation("boss_checklist", "textures/gui/buttons/config_highlighted.png");


    public static final ResourceLocation BOOKMARK =
        new ResourceLocation("boss_checklist", "textures/gui/bookmarks/bookmark.png");
    public static final ResourceLocation BOOKMARK_NOT_DEFEATED =
        new ResourceLocation("boss_checklist", "textures/gui/bookmarks/bookmark_not_defeated.png");
    public static final ResourceLocation BOOKMARK_DEFEATED =
        new ResourceLocation("boss_checklist", "textures/gui/bookmarks/bookmark_defeated.png");
    public static final ResourceLocation BOOKMARK_INFO =
        new ResourceLocation("boss_checklist", "textures/gui/bookmarks/bookmark_info.png");
    public static final ResourceLocation BOOKMARK_WIKI =
        new ResourceLocation("boss_checklist", "textures/gui/bookmarks/bookmark_wiki.png");
    public static final ResourceLocation BOOKMARK_SETTINGS = 
        new ResourceLocation("boss_checklist", "textures/gui/bookmarks/bookmark_settings.png");

    public static final ResourceLocation STATS_INFO_BACK = 
        new ResourceLocation("boss_checklist", "textures/gui/stats_info_back.png");
    public static final ResourceLocation BATTLE_ICON = 
        new ResourceLocation("boss_checklist", "textures/gui/icons/battle_icon.png");
    public static final ResourceLocation DURATION_ICON = 
        new ResourceLocation("boss_checklist", "textures/gui/icons/duration_icon.png");
    public static final ResourceLocation TOP_ICON = 
        new ResourceLocation("boss_checklist", "textures/gui/icons/top_icon.png");
    public static final ResourceLocation ARROW_UP =
        new ResourceLocation("boss_checklist", "textures/gui/icons/arrow_up.png");
    public static final ResourceLocation ARROW_DOWN =
        new ResourceLocation("boss_checklist", "textures/gui/icons/arrow_down.png");
    public static final ResourceLocation TOP_1 = 
        new ResourceLocation("boss_checklist", "textures/gui/icons/top1_icon.png");
    public static final ResourceLocation TOP_2 = 
        new ResourceLocation("boss_checklist", "textures/gui/icons/top2_icon.png");
    public static final ResourceLocation TOP_3 = 
        new ResourceLocation("boss_checklist", "textures/gui/icons/top3_icon.png");

    public static final ResourceLocation PROGRESS_BAR_BACKGROUND =
        new ResourceLocation("boss_checklist", "textures/gui/progress_bar_background.png");
    public static final ResourceLocation PROGRESS_BAR_FILL = 
        new ResourceLocation("boss_checklist", "textures/gui/progress_bar_fill.png");

    public static final ResourceLocation CHECKBOX = 
        new ResourceLocation("boss_checklist", "textures/gui/checkbox.png");
    public static final ResourceLocation NOTICE = 
        new ResourceLocation("boss_checklist", "textures/gui/notice.png");

    // in 1.20.1 these textures are located in the atlas, and I am too lazy to select their position, so I just copied them from 1.21.1
    public static final ResourceLocation PAGE_BUTTON_TEXTURE_backward =
        new ResourceLocation("boss_checklist", "textures/gui/page_backward.png");
    public static final ResourceLocation PAGE_BUTTON_TEXTURE_backward_highlighted =
        new ResourceLocation("boss_checklist", "textures/gui/page_backward_highlighted.png");
    public static final ResourceLocation PAGE_BUTTON_TEXTURE_forward =
        new ResourceLocation("boss_checklist", "textures/gui/page_forward.png");
    public static final ResourceLocation PAGE_BUTTON_TEXTURE_forward_highlighted =
        new ResourceLocation("boss_checklist", "textures/gui/page_forward_highlighted.png");


    public static final int MAX_LABEL_WIDTH = 103;

    public static final int CLOSE_BUTTON_X = 403;
    public static final int CLOSE_BUTTON_Y = 44;
    public static final int CLOSE_BUTTON_SIZE = 10;
    
    public static final int EDITOR_BUTTON_SIZE = 11;

    public static final int CONFIG_BUTTON_X = 387;
    public static final int CONFIG_BUTTON_Y = 65;
    public static final int CONFIG_BUTTONS_HEIGHT = 22;
    public static final int CONFIG_BUTTONS_WIDTH = 12;

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