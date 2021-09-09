package com.wuest.prefab.structures.config.enums;

public class ModerateFarmOptions extends BaseOption{
    public static ModerateFarmOptions AutomatedChickenCoop = new ModerateFarmOptions(
            "prefab.gui.moderate.farm.chicken",
            "assets/prefab/structures/advancedcoop.zip",
            "textures/gui/advanced_chicken_coop_topdown.png",
            false,
            false);

    public static ModerateFarmOptions AutomatedFarm = new ModerateFarmOptions(
            "prefab.gui.moderate.farm.automated",
            "assets/prefab/structures/automated_farm.zip",
            "textures/gui/automated_farm_topdown.png",
            false,
            false);

    public static ModerateFarmOptions FishPond = new ModerateFarmOptions(
            "prefab.gui.moderate.farm.fish",
            "assets/prefab/structures/fishpond.zip",
            "textures/gui/fish_pond_top_down.png",
            false,
            false);

    public static ModerateFarmOptions BeeFarm = new ModerateFarmOptions(
            "prefab.gui.moderate.farm.bee",
            "assets/prefab/structures/bee_farm.zip",
            "textures/gui/bee_farm_topdown.png",
            false,
            false);

    public static ModerateFarmOptions SugarCaneFarm = new ModerateFarmOptions(
            "prefab.gui.moderate.farm.sugar_cane",
            "assets/prefab/structures/sugar_cane_farm.zip",
            "textures/gui/sugar_cane_farm_topdown.png",
            false,
            true);

    protected ModerateFarmOptions(String translationString,
                                  String assetLocation,
                                  String pictureLocation,
                                  boolean hasBedColor,
                                  boolean hasGlassColor) {
        super(
                translationString,
                assetLocation,
                pictureLocation,
                hasBedColor,
                hasGlassColor);
    }
}