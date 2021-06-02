package com.wuest.prefab.structures.items;


import com.wuest.prefab.ModRegistry;
import com.wuest.prefab.structures.gui.GuiFishPond;
import com.wuest.prefab.structures.predefined.StructureFishPond;
import net.minecraft.item.ItemUsageContext;

/**
 * @author WuestMan
 */
@SuppressWarnings("ConstantConditions")
public class ItemFishPond extends StructureItem {
    public ItemFishPond() {
        super();
    }

    @Override
    public void scanningMode(ItemUsageContext context) {
        StructureFishPond.ScanStructure(
                context.getWorld(),
                context.getBlockPos(),
                context.getPlayer().getHorizontalFacing());
    }

    /**
     * Initializes common fields/properties for this structure item.
     */
    @Override
    protected void Initialize() {
        ModRegistry.guiRegistrations.add(x -> this.RegisterGui(GuiFishPond.class));
    }
}
