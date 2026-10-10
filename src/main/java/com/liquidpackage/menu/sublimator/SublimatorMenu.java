// SPDX-License-Identifier: MIT
package com.liquidpackage.menu.sublimator;

import org.joml.Vector2i;

import com.liquidpackage.LiquidPackage;
import com.liquidpackage.blockEntity.SublimatorBlockEntity;
import com.liquidpackage.blockEntity.container.iface.ContainerDataEnum;
import com.liquidpackage.menu.MenuLib;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class SublimatorMenu extends AbstractContainerMenu {
    public static final int BUTTON_RESET = 0;

    private final Vector2i INPUT_BUCKET_POS = new Vector2i(26, 19);
    private final Vector2i INPUT_SHELL_POS = new Vector2i(134, 32);
    private final Vector2i OUTPUT_SHELL_POS = new Vector2i(80, 41);

    private final SublimatorBlockEntity blockEntity;
    private final ContainerData blockStatus;
    private final Container beInventory;

    public SublimatorMenu(int containerId, Inventory playerInventory, BlockPos blockPos) {
        super(MenuLib.SUBLIMATOR_MENU, containerId);

        BlockEntity entity = playerInventory.player.level().getBlockEntity(blockPos);

        if (entity instanceof SublimatorBlockEntity sublimator) {
            this.blockEntity = sublimator;
            this.beInventory = sublimator.getItemInventory();
        } else {
            this.blockEntity = null;
            this.beInventory = new SimpleContainer(3);
        }

        this.blockStatus = new SimpleContainerData(3);

        initMenu(playerInventory);
    }

    public SublimatorMenu(int containerId, Inventory inventory, BlockEntity entity, ContainerData blockStatus) {
        super(MenuLib.SUBLIMATOR_MENU, containerId);
        this.blockEntity = (SublimatorBlockEntity) entity;
        this.blockStatus = blockStatus;
        this.beInventory = this.blockEntity.getItemInventory();

        initMenu(inventory);
    }

    private void initMenu(Inventory playerInventory) {

        this.addStandardInventorySlots(playerInventory, INVENTORY_START_X, INVENTORY_START_Y);

        this.addSlot(new Slot(beInventory,
                SublimatorBlockEntity.INPUT_BUCKET,
                INPUT_BUCKET_POS.x(),
                INPUT_BUCKET_POS.y()) {

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                if (blockEntity != null) {
                    return beInventory.canPlaceItem(SublimatorBlockEntity.INPUT_BUCKET, itemStack);
                }
                return true;
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(beInventory,
                SublimatorBlockEntity.INPUT_SHELL,
                INPUT_SHELL_POS.x(),
                INPUT_SHELL_POS.y()) {
            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return beInventory.canPlaceItem(SublimatorBlockEntity.INPUT_SHELL, itemStack);
            }
        });

        this.addSlot(new Slot(beInventory,
                SublimatorBlockEntity.OUTPUT_SHELL,
                OUTPUT_SHELL_POS.x(),
                OUTPUT_SHELL_POS.y()) {
            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return false;
            }
        });

        addDataSlots(blockStatus);
    }

    public SublimatorBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public boolean isCrafting() {
        return blockStatus.get(ContainerDataEnum.PROGRESS.id()) > 0;
    }

    public int getArrowProgress() {
        int progress = blockStatus.get(ContainerDataEnum.PROGRESS.id());
        int maxProgress = blockStatus.get(ContainerDataEnum.MAX_PROGRESS.id());

        int arrowPixelSize = 24;
        return maxProgress == 0 ? 0 : progress * arrowPixelSize / maxProgress;
    }

    public int getHeatProgress() {
        int heatPercent = blockStatus.get(ContainerDataEnum.HEAT_PERCENT.id());
        int thermometerSize = 28;

        return heatPercent * thermometerSize / 100;
    }

    /** Runs on the server when the client presses a button of this menu. */
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_RESET && blockEntity != null) {
            blockEntity.resetMachine();
            return true;
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity == null || Container.stillValidBlockEntity(blockEntity, player);
    }

    private static final int INVENTORY_ROWS = 3;
    private static final int INVENTORY_COLS = 9;
    private static final int INVENTORY_START_X = 8;
    private static final int INVENTORY_START_Y = 102;

    private static final int INVENTORY_SLOT_COUNT = INVENTORY_ROWS * INVENTORY_COLS;
    private static final int INVENTORY_HOTBAR_SLOT_COUNT = INVENTORY_COLS;
    private static final int VANILLA_SLOT_COUNT = INVENTORY_SLOT_COUNT + INVENTORY_HOTBAR_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int MACHINE_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    private static final int MACHINE_SLOT_COUNT = 3;

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);

        if (!slot.hasItem())
            return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copyOfStack = stack.copy();

        if (slotIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            if (!moveItemStackTo(stack, MACHINE_FIRST_SLOT_INDEX, MACHINE_FIRST_SLOT_INDEX + MACHINE_SLOT_COUNT, false))
                return ItemStack.EMPTY;
        } else if (slotIndex < MACHINE_FIRST_SLOT_INDEX + MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT,
                    false)) {
                return ItemStack.EMPTY;
            }
        } else {
            LiquidPackage.LOGGER.debug("Invalid slot index {}", slotIndex);
            return ItemStack.EMPTY;
        }

        // Whole stack moved: clear the slot
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        return copyOfStack;
    }
}
