package com.liquidpackage.blockEntity;

import org.jspecify.annotations.Nullable;

import com.liquidpackage.LiquidPackage;
import com.liquidpackage.blockEntity.container.iface.ContainerDataEnum;
import com.liquidpackage.blocks.SublimatorBlock;
import com.liquidpackage.menu.sublimator.SublimatorMenu;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SublimatorBlockEntity extends BlockEntity implements ExtendedMenuProvider<BlockPos> {
    public static final int INPUT_BUCKET = 0;
    public static final int INPUT_SHELL = 1;
    public static final int OUTPUT_SHELL = 2;

    public static int numInputSlots() {
        return 2;
    }

    public static int numOutputSlots() {
        return 1;
    }

    private final SimpleContainer itemInventory = new SimpleContainer(3) {
        @Override
        public void setChanged() {
            super.setChanged();
            SublimatorBlockEntity.this.setChanged();
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack itemStack) {
            if (slot == OUTPUT_SHELL) {
                return false;
            }
            if (slot == INPUT_BUCKET) {
                return getItemFluidStorage(itemStack) != null;
            }
            if (slot == INPUT_SHELL) {
                ItemStack currentInSlot = this.getItem(INPUT_SHELL);

                if (currentInSlot.isEmpty()) {
                    return true;
                }

                boolean isSameType = ItemStack.isSameItemSameComponents(currentInSlot, itemStack);
                boolean hasSpace = currentInSlot.getCount() < currentInSlot.getMaxStackSize();

                return isSameType && hasSpace;
            }

            return false;
        };
    };

    private final SingleVariantStorage<FluidVariant> fluidStorage = new SingleVariantStorage<FluidVariant>() {

        @Override
        protected FluidVariant getBlankVariant() {
            return FluidVariant.blank();
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            return FluidConstants.BUCKET * 4;
        }

        @Override
        protected void onFinalCommit() {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        };
    };

    private static final int MAX_PROGRESS_DEFAULT = 72;
    private final ContainerData blockStatus;

    private int progress = 0;
    private int maxProgress = MAX_PROGRESS_DEFAULT;
    private int heatPercent = 0;

    public SublimatorBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntitiesLib.SUBLIMATOR_BE.type(), worldPosition, blockState);
        this.blockStatus = new ContainerData() {
            @Override
            public int get(int dataId) {
                return switch (ContainerDataEnum.fromId(dataId)) {
                    case PROGRESS -> SublimatorBlockEntity.this.progress;
                    case MAX_PROGRESS -> SublimatorBlockEntity.this.maxProgress;
                    case HEAT_PERCENT -> SublimatorBlockEntity.this.heatPercent;
                    default -> 0;
                };
            }

            @Override
            public void set(int dataId, int value) {
                switch (ContainerDataEnum.fromId(dataId)) {
                    case PROGRESS:
                        SublimatorBlockEntity.this.progress = value;
                        break;
                    case MAX_PROGRESS:
                        SublimatorBlockEntity.this.maxProgress = value;
                        break;
                    case HEAT_PERCENT:
                        SublimatorBlockEntity.this.heatPercent = value;
                        break;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    public BlockEntity asBlockEntity() {
        return (BlockEntity) this;
    }

    // Menu
    public SimpleContainer getItemInventory() {
        return itemInventory;
    }

    public SingleVariantStorage<FluidVariant> getFluidStorage() {
        return fluidStorage;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.liquid-package.sublimator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SublimatorMenu(containerId, playerInventory, this, this.blockStatus);
    }

    @Override
    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return this.worldPosition;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putInt("sublimator.progress", progress);
        output.putInt("sublimator.maxProgress", maxProgress);

        ContainerHelper.saveAllItems(output, this.itemInventory.getItems());
        output.store("sublimator.fluidType", FluidVariant.CODEC, this.fluidStorage.variant);
        output.putLong("sublimator.fluidAmount", this.fluidStorage.amount);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        progress = input.getIntOr("sublimator.progress", progress);
        maxProgress = input.getIntOr("sublimator.maxProgress", maxProgress);

        // ContainerHelper.loadAllItems(input, in ventory);
        ContainerHelper.loadAllItems(input, this.itemInventory.getItems());
        this.fluidStorage.variant = input.read("sublimator.fluidType", FluidVariant.CODEC)
                .orElse(this.fluidStorage.variant);
        this.fluidStorage.amount = input.getLongOr("sublimator.fluidAmount", this.fluidStorage.amount);
    }

    public void drops() {
        Containers.dropContents(this.level, this.worldPosition, this.itemInventory.getItems());
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        ItemStack bucketStack = itemInventory.getItem(INPUT_BUCKET);

        var itemStorage = getItemFluidStorage(bucketStack);
        if (itemStorage != null) {
            emptyFluidContainer(itemStorage, bucketStack);
        }

        if (isBurningBlockBelow())
            increaseHeat();
        else
            decreaseHeat();

        updateState(pos, state);

        if (state.getValue(SublimatorBlock.HEAT_STATE) != SublimatorBlock.HeatState.HOT)
            return;

        state.setValue(SublimatorBlock.LIT, false);
        // heatPercent = (heatPercent + 1) % 100;
        // maxProgress = MAX_PROGRESS_DEFAULT;
        // progress = (progress + 1) % maxProgress;

        // progressCrafting();
        // if (hasCraftingFinished())
        // resetProgress();

        // if (hasReciepe() && isOutputSlotAvailable()) {
        // progressCrafting();
        setChanged(level, pos, state);

        // if (hasCraftingFinished()) {
        // craftItem();
        // resetProgress();
        // }
        // } else {
        // resetProgress();
        // }
    }

    public void increaseHeat() {
        heatPercent = heatPercent < 100 ? heatPercent + 1 : 100;
    }

    public void decreaseHeat() {
        heatPercent = heatPercent > 0 ? heatPercent - 1 : 0;
    }

    public void updateState(BlockPos pos, BlockState state) {
        if (heatPercent < 33)
            level.setBlockAndUpdate(pos, state.setValue(SublimatorBlock.HEAT_STATE, SublimatorBlock.HeatState.COLD));
        else if (heatPercent < 66)
            level.setBlockAndUpdate(pos, state.setValue(SublimatorBlock.HEAT_STATE, SublimatorBlock.HeatState.COOL));
        else if (heatPercent < 100)
            level.setBlockAndUpdate(pos, state.setValue(SublimatorBlock.HEAT_STATE, SublimatorBlock.HeatState.WARM));
        else if (heatPercent >= 100)
            level.setBlockAndUpdate(pos, state.setValue(SublimatorBlock.HEAT_STATE, SublimatorBlock.HeatState.HOT));
    }

    public boolean isBurningBlockBelow() {
        Level level = this.getLevel();
        if (level == null) {
            return false;
        }

        BlockPos posBelow = this.getBlockPos().below();
        BlockState blockBelowState = level.getBlockState(posBelow);

        return blockBelowState.is(BlockTags.FIRE)
                || blockBelowState.is(Blocks.LAVA)
                || blockBelowState.is(Blocks.MAGMA_BLOCK)
                || CampfireBlock.isLitCampfire(blockBelowState)
                || blockBelowState.is(Blocks.LAVA_CAULDRON);
    }

    private Storage<FluidVariant> getItemFluidStorage(ItemStack item) {
        Storage<FluidVariant> itemFluidStorage = FluidStorage.ITEM.find(item, ContainerItemContext.withConstant(item));
        if (itemFluidStorage != null) {
            var viewOptional = itemFluidStorage.iterator().hasNext() ? itemFluidStorage.iterator().next() : null;
            if (viewOptional != null && !viewOptional.isResourceBlank()) {
                return itemFluidStorage;
            }
        }
        return null;
    }

    private void emptyFluidContainer(Storage<FluidVariant> ItemFluidStorage, ItemStack itemStack) {
        if (ItemFluidStorage != null) {
            var viewOptional = ItemFluidStorage.iterator().hasNext() ? ItemFluidStorage.iterator().next() : null;

            if (viewOptional != null && !viewOptional.isResourceBlank()) {
                FluidVariant fluidInBucket = viewOptional.getResource();
                long amountInBucket = viewOptional.getAmount(); // Для ванильного ведра это 81000 (1 ведро)

                if ((fluidStorage.variant.isBlank() || fluidStorage.variant.equals(fluidInBucket))
                        && (fluidStorage.getCapacity() - fluidStorage.amount >= amountInBucket)) {

                    LiquidPackage.LOGGER.warn("Before pour: {} {} units", fluidStorage.variant.toString(),
                            fluidStorage.amount);

                    fluidStorage.variant = fluidInBucket;
                    fluidStorage.amount += amountInBucket;

                    LiquidPackage.LOGGER.warn("After pour: {} {} units", fluidStorage.variant.toString(),
                            fluidStorage.amount);

                    var remainder = itemStack.getItem().getCraftingRemainder();
                    if (remainder != null) {
                        itemInventory.setItem(INPUT_BUCKET, remainder.create());
                    } else {
                        itemInventory.clearContent();
                    }

                    setChanged();
                }
            }
        }
    }

    // private boolean hasReciepe() {
    // // TODO MAKE IT READ ACTUAL RECIEPE
    // ItemStack output = new ItemStack(ItemsLib.EMPTY_SHELL.item());
    // Item input = ItemsLib.EMPTY_SHELL.item();

    // boolean isInputCorrect = inventory.get(INPUT_SHELL).is(input);
    // boolean isOutputAppropriate = inventory.get(OUTPUT_SHELL).isEmpty()
    // || inventory.get(OUTPUT_SHELL).is(output.getItem());
    // boolean isThereOutputSpace = isThereOutputSpaceFor(output.getCount());

    // return isInputCorrect && isOutputAppropriate && isThereOutputSpace;
    // }

    // private boolean isThereOutputSpaceFor(int count) {
    // int maxCount = inventory.get(OUTPUT_SHELL).isEmpty() ? 64 :
    // inventory.get(OUTPUT_SHELL).getMaxStackSize();
    // int currentCount = inventory.get(OUTPUT_SHELL).getCount();

    // return maxCount >= currentCount + count;
    // }

    // private void craftItem() {
    // // TODO CHANGE IT TO GENERATE APPROPRIATE SHELL
    // ItemStack output = new ItemStack(ItemsLib.EMPTY_SHELL.item());
    // inventory.set(INPUT_SHELL,
    // inventory.get(INPUT_SHELL).copyWithCount(inventory.get(INPUT_SHELL).getCount()
    // - 1));
    // inventory.set(OUTPUT_SHELL,
    // output.copyWithCount(inventory.get(OUTPUT_SHELL).getCount() +
    // output.getCount()));
    // }

    // private boolean isOutputSlotAvailable() {
    // return inventory.get(OUTPUT_SHELL).isEmpty() ||
    // inventory.get(OUTPUT_SHELL).getCount() <
    // inventory.get(OUTPUT_SHELL).getMaxStackSize();
    // }

    // private void progressCrafting() {
    // progress++;
    // }

    // private boolean hasCraftingFinished() {
    // return progress >= maxProgress;
    // }

    // private void resetProgress() {
    // progress = 0;
    // maxProgress = MAX_PROGRESS_DEFAULT;
    // }

    // Block-Entity sync methods
    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (!level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

}
