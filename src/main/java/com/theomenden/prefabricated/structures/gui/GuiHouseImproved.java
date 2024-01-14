package com.theomenden.prefabricated.structures.gui;

import com.theomenden.prefabricated.ClientModRegistry;
import com.theomenden.prefabricated.Prefab;
import com.theomenden.prefabricated.config.ModConfiguration;
import com.theomenden.prefabricated.gui.GuiLangKeys;
import com.theomenden.prefabricated.gui.GuiUtils;
import com.theomenden.prefabricated.gui.controls.ExtendedButton;
import com.theomenden.prefabricated.gui.controls.GuiCheckBox;
import com.theomenden.prefabricated.structures.config.HouseImprovedConfiguration;
import com.theomenden.prefabricated.structures.messages.StructureTagMessage;
import com.theomenden.prefabricated.structures.predefined.StructureHouseImproved;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author WuestMan
 */
public class GuiHouseImproved extends GuiStructure {
    protected HouseImprovedConfiguration specificConfiguration;
    protected ModConfiguration serverConfiguration;
    private ExtendedButton btnHouseStyle;
    private GuiCheckBox btnAddChest;
    private GuiCheckBox btnAddChestContents;
    private GuiCheckBox btnAddMineShaft;
    private ExtendedButton btnBedColor;
    private boolean allowItemsInChestAndFurnace = true;

    private ArrayList<HouseImprovedConfiguration.HouseStyle> availableHouseStyles;

    public GuiHouseImproved() {
        super("Moderate House");

        this.structureConfiguration = StructureTagMessage.EnumStructureConfiguration.ModerateHouse;
    }

    @Override
    public @NotNull Component getNarrationMessage() {
        return Component.translatable(GuiLangKeys.translateString(GuiLangKeys.TITLE_MODERATE_HOUSE));
    }

    @Override
    protected void Initialize() {
        super.Initialize();

        this.modifiedInitialXAxis = 215;
        this.modifiedInitialYAxis = 117;
        this.shownImageHeight = 150;
        this.shownImageWidth = 268;

        if (!minecraft.player.isCreative()) {
            this.allowItemsInChestAndFurnace = !ClientModRegistry.playerConfig.builtStarterHouse;
        } else {
            this.allowItemsInChestAndFurnace = true;
        }

        this.serverConfiguration = Prefab.serverConfiguration;
        this.configuration = this.specificConfiguration = ClientModRegistry.playerConfig.getClientConfig("Moderate Houses", HouseImprovedConfiguration.class);
        this.configuration.pos = this.pos;

        this.availableHouseStyles = new ArrayList<>();
        HashMap<String, Boolean> houseConfigurationSettings = this.serverConfiguration.structureOptions.get("item.prefabricated.item_house_improved");
        boolean selectedStyleInListOfAvailable = false;

        for (HouseImprovedConfiguration.HouseStyle style : HouseImprovedConfiguration.HouseStyle.values()) {
            if (houseConfigurationSettings.containsKey(style.getTranslationString())
                    && houseConfigurationSettings.get(style.getTranslationString())) {
                this.availableHouseStyles.add(style);

                if (this.specificConfiguration.houseStyle.getDisplayName().equals(style.getDisplayName())) {
                    selectedStyleInListOfAvailable = true;
                }
            }
        }

        if (this.availableHouseStyles.isEmpty()) {
            // There are no options. Show the no options screen.
            this.showNoOptionsScreen();
            return;
        }

        if (!selectedStyleInListOfAvailable) {
            this.specificConfiguration.houseStyle = this.availableHouseStyles.get(0);
        }

        this.selectedStructure = StructureHouseImproved.CreateInstance(this.specificConfiguration.houseStyle.getStructureLocation(), StructureHouseImproved.class);

        // Get the upper left hand corner of the GUI box.
        IntIntPair adjustedXYValue = this.getAdjustedXYValue();
        int grayBoxX = adjustedXYValue.leftInt();
        int grayBoxY = adjustedXYValue.rightInt();

        // Create the buttons.
        int yOffset = 25;

        if (this.availableHouseStyles.size() > 1) {
            this.btnHouseStyle = this.createAndAddButton(grayBoxX + 8, grayBoxY + yOffset, 90, 20, this.specificConfiguration.houseStyle.getDisplayName(), false, GuiLangKeys.translateString(GuiLangKeys.HOUSE_STYLE));
            yOffset = yOffset + 35;
        }

        this.btnBedColor = this.createAndAddDyeButton(grayBoxX + 8, grayBoxY + yOffset, 90, 20, this.specificConfiguration.bedColor, GuiLangKeys.translateString(GuiLangKeys.GUI_STRUCTURE_BED_COLOR));
        yOffset = yOffset + 60;

        this.btnAddChest = this.createAndAddCheckBox(grayBoxX + 8, grayBoxY + yOffset, GuiLangKeys.HOUSE_ADD_CHEST, this.specificConfiguration.addChests, this::buttonClicked);
        yOffset = yOffset + 17;

        this.btnAddMineShaft = this.createAndAddCheckBox(grayBoxX + 8, grayBoxY + yOffset, GuiLangKeys.HOUSE_BUILD_MINESHAFT, this.specificConfiguration.addChestContents, this::buttonClicked);
        yOffset = yOffset + 17;

        this.btnAddChestContents = this.createAndAddCheckBox(grayBoxX + 8, grayBoxY + yOffset, GuiLangKeys.HOUSE_ADD_CHEST_CONTENTS, this.specificConfiguration.addMineshaft, this::buttonClicked);

        // Create the standard buttons.
        this.btnVisualize = this.createAndAddCustomButton(grayBoxX + 24, grayBoxY + 177, 90, 20, GuiLangKeys.GUI_BUTTON_PREVIEW);
        this.btnBuild = this.createAndAddCustomButton(grayBoxX + 310, grayBoxY + 177, 90, 20, GuiLangKeys.GUI_BUTTON_BUILD);
        this.btnCancel = this.createAndAddButton(grayBoxX + 154, grayBoxY + 177, 90, 20, GuiLangKeys.GUI_BUTTON_CANCEL);
    }

    @Override
    protected void preButtonRender(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
        int imagePanelUpperLeft = x + 136;
        int imagePanelWidth = 285;
        int imagePanelMiddle = imagePanelWidth / 2;

        this.renderBackground(guiGraphics);

        this.drawControlLeftPanel(guiGraphics, x + 2, y + 10, 135, 190);
        this.drawControlRightPanel(guiGraphics, imagePanelUpperLeft, y + 10, imagePanelWidth, 190);

        int middleOfImage = this.shownImageWidth / 2;
        int imageLocation = imagePanelUpperLeft + (imagePanelMiddle - middleOfImage);

        GuiUtils.bindAndDrawScaledTexture(
                this.specificConfiguration.houseStyle.getHousePicture(),
                guiGraphics,
                imageLocation,
                y + 15,
                this.shownImageWidth,
                this.shownImageHeight,
                this.shownImageWidth,
                this.shownImageHeight,
                this.shownImageWidth,
                this.shownImageHeight);

        this.btnAddChest.visible = this.serverConfiguration.starterHouseOptions.addChests;
        this.btnAddChestContents.visible = this.allowItemsInChestAndFurnace && this.serverConfiguration.starterHouseOptions.addChestContents;
        this.btnAddMineShaft.visible = this.serverConfiguration.starterHouseOptions.addMineshaft;
    }

    @Override
    protected void postButtonRender(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
        // Draw the text here.
        int yOffSet = 15;

        if (this.availableHouseStyles.size() > 1) {
            this.drawString(guiGraphics, GuiLangKeys.translateString(GuiLangKeys.HOUSE_STYLE), x + 8, y + yOffSet, this.textColor);
            yOffSet = yOffSet + 35;
        }

        this.drawString(guiGraphics, GuiLangKeys.translateString(GuiLangKeys.GUI_STRUCTURE_BED_COLOR), x + 8, y + yOffSet, this.textColor);
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    @Override
    public void buttonClicked(AbstractButton button) {
        this.specificConfiguration.addChests = this.btnAddChest.visible && this.btnAddChest.isChecked();
        this.specificConfiguration.addChestContents = this.allowItemsInChestAndFurnace && (this.btnAddChestContents.visible && this.btnAddChestContents.isChecked());
        this.specificConfiguration.addMineshaft = this.btnAddMineShaft.visible && this.btnAddMineShaft.isChecked();
        this.configuration.houseFacing = minecraft.player.getDirection().getOpposite();

        this.performCancelOrBuildOrHouseFacing(button);

        if (button == this.btnHouseStyle) {
            for (int i = 0; i < this.availableHouseStyles.size(); i++) {
                HouseImprovedConfiguration.HouseStyle option = this.availableHouseStyles.get(i);
                HouseImprovedConfiguration.HouseStyle chosenOption = null;

                if (this.specificConfiguration.houseStyle.getDisplayName().equals(option.getDisplayName())) {
                    if (i == this.availableHouseStyles.size() - 1) {
                        // This is the last option, set the text to the first option.
                        chosenOption = this.availableHouseStyles.get(0);
                    } else {
                        chosenOption = this.availableHouseStyles.get(i + 1);
                    }
                }

                if (chosenOption != null) {
                    this.specificConfiguration.houseStyle = chosenOption;
                    this.selectedStructure = StructureHouseImproved.CreateInstance(this.specificConfiguration.houseStyle.getStructureLocation(), StructureHouseImproved.class);
                    GuiUtils.setButtonText(btnHouseStyle, this.specificConfiguration.houseStyle.getDisplayName());
                    break;
                }
            }
        } else if (button == this.btnVisualize) {
            this.performPreview();
        } else if (button == this.btnBedColor) {
            this.specificConfiguration.bedColor = DyeColor.byId(this.specificConfiguration.bedColor.getId() + 1);
            GuiUtils.setButtonText(btnBedColor, GuiLangKeys.translateDye(this.specificConfiguration.bedColor));
        }
    }
}
