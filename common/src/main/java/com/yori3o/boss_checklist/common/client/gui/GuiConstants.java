package com.yori3o.boss_checklist.common.client.gui;


import net.minecraft.resources.ResourceLocation;


/**
 * This class contains most of the textures and button positions.
 */
public class GuiConstants {
    

    public static final ResourceLocation BOOK = 
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/book.png");


    public static final ResourceLocation BUTTON_TEXTURE =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/button.png");
    public static final ResourceLocation BUTTON_TEXTURE_hovered =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/button_hovered.png");
    public static final ResourceLocation BUTTON_TEXTURE_pressed =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/button_pressed.png");
    public static final ResourceLocation BUTTON_TEXTURE_overlay =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/button_overlay.png");

    public static final ResourceLocation SMALL_BUTTON_TEXTURE_hovered =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/small_button_hovered.png");
    public static final ResourceLocation SMALL_BUTTON_TEXTURE_pressed =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/small_button_pressed.png");
    public static final ResourceLocation SMALL_BUTTON_TEXTURE =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/small_button.png");
        
    public static final ResourceLocation CLOSE_BUTTON_TEXTURE =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/close.png");
    public static final ResourceLocation CLOSE_BUTTON_TEXTURE_hovered =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/close_hovered.png");
        
    public static final ResourceLocation OPEN_BUTTON_TEXTURE =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/open.png");
    public static final ResourceLocation OPEN_BUTTON_TEXTURE_hovered =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/buttons/open_hovered.png");


    public static final ResourceLocation BOOKMARK =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/bookmarks/bookmark.png");
    public static final ResourceLocation BOOKMARK_NOT_DEFEATED =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/bookmarks/bookmark_not_defeated.png");
    public static final ResourceLocation BOOKMARK_DEFEATED =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/bookmarks/bookmark_defeated.png");
    public static final ResourceLocation BOOKMARK_INFO =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/bookmarks/bookmark_info.png");
    public static final ResourceLocation BOOKMARK_WIKI =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/bookmarks/bookmark_wiki.png");
    public static final ResourceLocation BOOKMARK_SETTINGS = 
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/bookmarks/bookmark_settings.png");

    public static final ResourceLocation STATS_INFO_BACK = 
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/stats_info_back.png");
    public static final ResourceLocation BATTLE_ICON = 
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/icons/battle_icon.png");
    public static final ResourceLocation DURATION_ICON = 
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/icons/duration_icon.png");
    public static final ResourceLocation TOP_ICON = 
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/icons/top_icon.png");
    public static final ResourceLocation ARROW_UP =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/icons/arrow_up.png");
    public static final ResourceLocation ARROW_DOWN =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/icons/arrow_down.png");
    public static final ResourceLocation TOP_1 = 
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/icons/top1_icon.png");
    public static final ResourceLocation TOP_2 = 
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/icons/top2_icon.png");
    public static final ResourceLocation TOP_3 = 
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/icons/top3_icon.png");

    public static final ResourceLocation PROGRESS_BAR_BACKGROUND =
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/progress_bar_background.png");
    public static final ResourceLocation PROGRESS_BAR_FILL = 
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/progress_bar_fill.png");

    public static final ResourceLocation CHECKBOX = 
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/checkbox.png");
    public static final ResourceLocation NOTICE = 
        ResourceLocation.fromNamespaceAndPath("boss_checklist", "textures/gui/notice.png");

    // FOR 1.21.1+
    public static final ResourceLocation PAGE_BUTTON_TEXTURE_backward =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/sprites/widget/page_backward.png");
    public static final ResourceLocation PAGE_BUTTON_TEXTURE_backward_highlighted =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/sprites/widget/page_backward_highlighted.png");
    public static final ResourceLocation PAGE_BUTTON_TEXTURE_forward =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/sprites/widget/page_forward.png");
    public static final ResourceLocation PAGE_BUTTON_TEXTURE_forward_highlighted =
        ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/sprites/widget/page_forward_highlighted.png");

    // in 1.20.1 these textures are located in the atlas, and I am too lazy to select their position, so I just copied them from 1.21.1
    /*public static final ResourceLocation BUTTON_TEXTURE_backward =
        new ResourceLocation("boss_checklist", "textures/gui/page_backward.png");
    public static final ResourceLocation BUTTON_TEXTURE_backward_highlighted =
        new ResourceLocation("boss_checklist", "textures/gui/page_backward_highlighted.png");
    public static final ResourceLocation BUTTON_TEXTURE_forward =
        new ResourceLocation("boss_checklist", "textures/gui/page_forward.png");
    public static final ResourceLocation BUTTON_TEXTURE_forward_highlighted =
        new ResourceLocation("boss_checklist", "textures/gui/page_forward_highlighted.png");*/


    public static final int CloseButtonX = 403;
    public static final int CloseButtonY = 44;
    public static final int CloseButtonHeight = 10;
    public static final int CloseButtonWidth = 10;

    public static final int DropButtonX = 265;
    public static final int DropButtonY = 51;
    public static final int SpawnButtonX = 328;
    public static final int SpawnButtonY = 51;
    public static final int BigButtonsHeight = 18;
    public static final int BigButtonsWidth = 50;
    public static final int BigButtonsOverlayHeight = 40;
    public static final int BigButtonsOverlayWidth = 70;
    
    public static final int BarHeight = 9;
    public static final int BarWidth = 186;

   
}