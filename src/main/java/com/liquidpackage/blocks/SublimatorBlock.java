package com.liquidpackage.blocks;

import org.jspecify.annotations.Nullable;

import com.liquidpackage.LiquidPackage;
import com.liquidpackage.blockEntity.BlockEntitiesLib;
import com.liquidpackage.blockEntity.SublimatorBlockEntity;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

public class SublimatorBlock extends BaseEntityBlock {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(LiquidPackage.MOD_ID, "sublimator");
    public static final ResourceKey<Block> KEY = ResourceKey.create(Registries.BLOCK, ID);

    public static enum HeatState implements StringRepresentable {
        COLD("cold"),
        COOL("cool"),
        WARM("warm"),
        HOT("hot");

        public static final HeatState[] STATES = new HeatState[] { COLD, COOL, WARM, HOT };

        private final String name;

        HeatState(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

    }

    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<HeatState> HEAT_STATE = EnumProperty.create("heat_state", HeatState.class,
            HeatState.STATES);

    private static MapCodec<SublimatorBlock> CODEC = simpleCodec(SublimatorBlock::new);

    public SublimatorBlock() {
        Properties props = BlockBehaviour.Properties.of()
                .setId(KEY)
                .requiresCorrectToolForDrops()
                .strength(3f);
        this(props);
    }

    public SublimatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false));
        registerDefaultState(defaultBlockState().setValue(HEAT_STATE, HeatState.COLD));
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    // STATE CONTROL
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(HEAT_STATE, FACING, LIT);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        super.animateTick(state, level, pos, random);
    }

    // Block Entity
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new SublimatorBlockEntity(worldPosition, blockState);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
            @Nullable BlockEntity blockEntity, ItemStack destroyedWith) {
        if (level.getBlockEntity(pos) instanceof SublimatorBlockEntity sublimatorBE) {
            sublimatorBE.drops();
        }
        super.playerDestroy(level, player, pos, state, blockEntity, destroyedWith);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof SublimatorBlockEntity sublimatorBE) {
            player.openMenu(sublimatorBE);
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState,
            BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }

        return createTickerHelper(type, BlockEntitiesLib.SUBLIMATOR_BE.type(),
                (final Level level_, final BlockPos pos, final BlockState state,
                        final SublimatorBlockEntity entity) -> entity.tick(level_, pos, blockState));

    }
}
