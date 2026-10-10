package com.liquidpackage.blockEntity;

import org.jspecify.annotations.Nullable;

import com.liquidpackage.LiquidPackage;
import com.liquidpackage.blockEntity.container.iface.ContainerDataEnum;
import com.liquidpackage.blocks.SublimatorBlock;
import com.liquidpackage.items.ItemsLib;
import com.liquidpackage.items.ItemsLib.GenericShellBundle;
import com.liquidpackage.menu.sublimator.SublimatorMenu;

import java.util.List;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.FilteringStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
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

public class SublimatorBlockEntity extends BlockEntity
        implements ExtendedMenuProvider<BlockPos>, SidedStorageBlockEntity {
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
                return itemStack.is(ItemsLib.EMPTY_SHELL.item());
            }

            return false;
        };
    };

    private final SingleVariantStorage<FluidVariant> fluidStorage = new SingleVariantStorage<FluidVariant>() {

        @Override
        protected FluidVariant getBlankVariant() {
            return FluidVariant.blank();
        }

        /**
         * Only fluids that make a shell are accepted, anything else would clog the tank
         * for good.
         */
        @Override
        protected boolean canInsert(FluidVariant variant) {
            return !variant.isBlank()
                    && ItemsLib.getGenericByLiquid(BuiltInRegistries.FLUID.getKey(variant.getFluid())) != null;
        }

        @Override
        protected long getCapacity(FluidVariant variant) {
            return TANK_CAPACITY;
        }

        @Override
        protected void onFinalCommit() {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        };
    };

    private static final long TANK_CAPACITY = FluidConstants.BUCKET * 4;

    /** Crafting takes this many ticks per bucket of the shell's cost. */
    public static final int TICKS_PER_BUCKET = 20;
    /** The cost is drained evenly over the whole craft. */
    private static final long FLUID_PER_TICK = FluidConstants.BUCKET / TICKS_PER_BUCKET;

    private static final int MAX_PROGRESS_DEFAULT = 72;
    private final ContainerData blockStatus;

    private int progress = 0;
    /** Config index of the shell being crafted, -1 when idle. */
    private int craftingIndex = -1;
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

    // Pipes and hoppers (Fabric Transfer API)
    private final ContainerStorage containerStorage = ContainerStorage.of(itemInventory, null);

    private Storage<ItemVariant> shellInputStorage() {
        return FilteringStorage.insertOnlyOf(containerStorage.getSlot(INPUT_SHELL));
    }

    private Storage<ItemVariant> shellOutputStorage() {
        return FilteringStorage.extractOnlyOf(containerStorage.getSlot(OUTPUT_SHELL));
    }

    /**
     * Sides are relative to the machine: its front is the facing direction and
     * left/right are seen from the machine's own point of view. Empty shells go
     * in from the back, filled shells come out of the left and right sides.
     */
    @Override
    public @Nullable Storage<ItemVariant> getItemStorage(@Nullable Direction side) {
        if (side == null)
            return new CombinedStorage<>(List.of(shellInputStorage(), shellOutputStorage()));

        Direction facing = getBlockState().getValue(SublimatorBlock.FACING);
        if (side == facing.getOpposite())
            return shellInputStorage();
        if (side == facing.getCounterClockWise() || side == facing.getClockWise())
            return shellOutputStorage();
        return null;
    }

    /** Fluid goes in from the top only and cannot be pumped back out. */
    @Override
    public @Nullable Storage<FluidVariant> getFluidStorage(@Nullable Direction side) {
        return side == null || side == Direction.UP ? FilteringStorage.insertOnlyOf(fluidStorage) : null;
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
        output.putInt("sublimator.craftingIndex", craftingIndex);

        ContainerHelper.saveAllItems(output, this.itemInventory.getItems());
        output.store("sublimator.fluidType", FluidVariant.CODEC, this.fluidStorage.variant);
        output.putLong("sublimator.fluidAmount", this.fluidStorage.amount);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        progress = input.getIntOr("sublimator.progress", progress);
        maxProgress = input.getIntOr("sublimator.maxProgress", maxProgress);
        craftingIndex = input.getIntOr("sublimator.craftingIndex", -1);

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

        boolean working = heatPercent >= 100 && progressCrafting();
        updateBlockState(working);

        if (working)
            setChanged(level, pos, state);
    }

    public void increaseHeat() {
        heatPercent = heatPercent < 100 ? heatPercent + 1 : 100;
    }

    public void decreaseHeat() {
        heatPercent = heatPercent > 0 ? heatPercent - 1 : 0;
    }

    private SublimatorBlock.HeatState heatState() {
        if (heatPercent < 33)
            return SublimatorBlock.HeatState.COLD;
        if (heatPercent < 66)
            return SublimatorBlock.HeatState.COOL;
        if (heatPercent < 100)
            return SublimatorBlock.HeatState.WARM;
        return SublimatorBlock.HeatState.HOT;
    }

    /** Heat and lit state of the block, written only when something changed. */
    private void updateBlockState(boolean lit) {
        BlockState current = level.getBlockState(worldPosition);
        BlockState next = current
                .setValue(SublimatorBlock.HEAT_STATE, heatState())
                .setValue(SublimatorBlock.LIT, lit);
        if (next != current)
            level.setBlockAndUpdate(worldPosition, next);
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

    private static int bucketsOf(GenericShellBundle recipe) {
        return Math.max(1, LiquidPackage.CONFIG.get(recipe.index()).liquidPerPackage());
    }

    private void resetCrafting() {
        progress = 0;
        craftingIndex = -1;
        maxProgress = MAX_PROGRESS_DEFAULT;
    }

    /**
     * Cancels the current craft and empties the tank. The shells in the slots stay.
     */
    public void resetMachine() {
        resetCrafting();
        fluidStorage.amount = 0;
        fluidStorage.variant = FluidVariant.blank();
        setChanged();
    }

    private boolean outputAccepts(GenericShellBundle recipe) {
        ItemStack output = itemInventory.getItem(OUTPUT_SHELL);
        return output.isEmpty() || (output.is(recipe.item()) && output.getCount() < output.getMaxStackSize());
    }

    /**
     * One tick of crafting. The fluid is drained gradually, so the player has
     * to keep the tank topped up for expensive shells. Running out of fluid or
     * output space only pauses the craft; changing the shell or the fluid
     * type cancels it.
     *
     * @return true when the craft advanced this tick
     */
    private boolean progressCrafting() {
        if (!itemInventory.getItem(INPUT_SHELL).is(ItemsLib.EMPTY_SHELL.item())) {
            resetCrafting();
            return false;
        }

        boolean tankDry = fluidStorage.variant.isBlank();
        GenericShellBundle recipe = tankDry
                ? null
                : ItemsLib.getGenericByLiquid(BuiltInRegistries.FLUID.getKey(fluidStorage.variant.getFluid()));

        if (recipe == null) {
            // Dry tank: wait for a refill. Anything else cannot be crafted.
            if (!tankDry)
                resetCrafting();
            return false;
        }
        if (craftingIndex != -1 && craftingIndex != recipe.index())
            resetCrafting();

        if (!outputAccepts(recipe) || fluidStorage.amount < FLUID_PER_TICK)
            return false;

        fluidStorage.amount -= FLUID_PER_TICK;
        if (fluidStorage.amount <= 0) {
            fluidStorage.amount = 0;
            fluidStorage.variant = FluidVariant.blank();
        }

        craftingIndex = recipe.index();
        maxProgress = bucketsOf(recipe) * TICKS_PER_BUCKET;
        progress++;
        if (progress >= maxProgress) {
            craft(recipe);
            resetCrafting();
        }
        return true;
    }

    private void craft(GenericShellBundle recipe) {
        itemInventory.getItem(INPUT_SHELL).shrink(1);

        ItemStack output = itemInventory.getItem(OUTPUT_SHELL);
        if (output.isEmpty())
            itemInventory.setItem(OUTPUT_SHELL, new ItemStack(recipe.item()));
        else
            output.grow(1);
    }

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
