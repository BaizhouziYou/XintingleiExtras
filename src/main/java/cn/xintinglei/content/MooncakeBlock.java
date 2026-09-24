package cn.xintinglei.content;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.List;

/** One placed mooncake; its 40 states describe oxidation, wax and remaining servings. */
final class MooncakeBlock extends Block {
    enum Flavor implements StringIdentifiable {
        ORIGINAL("original"), SWEET_BERRY("sweet_berry"), HONEY("honey"), SNOW_SKIN("snow_skin");

        private final String name;

        Flavor(String name) { this.name = name; }

        @Override public String asString() { return name; }
    }

    static final MapCodec<MooncakeBlock> CODEC = createCodec(MooncakeBlock::new);
    static final EnumProperty<Flavor> FLAVOR = EnumProperty.of("flavor", Flavor.class);
    static final IntProperty OXIDATION = IntProperty.of("oxidation", 0, 3);
    static final BooleanProperty WAXED = BooleanProperty.of("waxed");
    // 0 = uncut; 4..1 = that many quarters remain.
    static final IntProperty SERVINGS = IntProperty.of("servings", 0, 4);

    MooncakeBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
            .with(FLAVOR, Flavor.ORIGINAL).with(OXIDATION, 0).with(WAXED, false).with(SERVINGS, 0));
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FLAVOR, OXIDATION, WAXED, SERVINGS);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return MooncakeShapes.get(state.get(FLAVOR), state.get(SERVINGS));
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos.down(), net.minecraft.util.math.Direction.UP);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return state.get(FLAVOR) == Flavor.ORIGINAL && !state.get(WAXED) && state.get(OXIDATION) < 3;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (hasRandomTicks(state) && random.nextInt(8) == 0) {
            world.setBlockState(pos, state.with(OXIDATION, state.get(OXIDATION) + 1), Block.NOTIFY_ALL);
        }
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos,
                                         PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hand == Hand.MAIN_HAND && stack.isEmpty() && state.get(SERVINGS) > 0) {
            if (!canChangeSlice(world, pos, player)) return ActionResult.FAIL;
            if (!world.isClient) {
                int servings = state.get(SERVINGS);
                boolean changed = servings == 1
                    ? world.removeBlock(pos, false)
                    : world.setBlockState(pos, state.with(SERVINGS, servings - 1), Block.NOTIFY_ALL);
                if (!changed) return ActionResult.FAIL;
                ItemStack slice = new ItemStack(state.get(FLAVOR) == Flavor.ORIGINAL
                    ? XintingleiExtras.mooncakeItem(state.get(OXIDATION), state.get(WAXED), true)
                    : MooncakeFlavors.item(state.get(FLAVOR), true));
                player.getInventory().offerOrDrop(slice);
                world.playSound(null, pos, SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.BLOCKS, 1f, 1f);
            }
            return ActionResult.SUCCESS;
        }
        ActionResult sliceResult = addSlice(stack, state, world, pos, player);
        if (sliceResult != ActionResult.PASS) {
            return sliceResult;
        }
        if (state.get(FLAVOR) == Flavor.ORIGINAL && stack.isOf(Items.HONEYCOMB) && !state.get(WAXED)) {
            if (!world.isClient) {
                world.setBlockState(pos, state.with(WAXED, true), Block.NOTIFY_ALL);
                if (!player.isCreative()) stack.decrement(1);
                world.playSound(null, pos, SoundEvents.ITEM_HONEYCOMB_WAX_ON, SoundCategory.BLOCKS, 1f, 1f);
            }
            return ActionResult.SUCCESS;
        }
        if (state.get(FLAVOR) == Flavor.ORIGINAL && stack.isIn(ItemTags.AXES)
            && (state.get(WAXED) || state.get(OXIDATION) > 0)) {
            if (!world.isClient) {
                boolean waxed = state.get(WAXED);
                world.setBlockState(pos, waxed ? state.with(WAXED, false)
                    : state.with(OXIDATION, state.get(OXIDATION) - 1), Block.NOTIFY_ALL);
                stack.damage(1, player);
                world.playSound(null, pos, waxed ? SoundEvents.ITEM_AXE_WAX_OFF : SoundEvents.ITEM_AXE_SCRAPE,
                    SoundCategory.BLOCKS, 1f, 1f);
            }
            return ActionResult.SUCCESS;
        }
        if (stack.isIn(ItemTags.SWORDS) && state.get(SERVINGS) == 0) {
            if (!world.isClient) {
                world.setBlockState(pos, state.with(SERVINGS, 4), Block.NOTIFY_ALL);
                stack.damage(1, player);
                world.playSound(null, pos, SoundEvents.BLOCK_WOOL_BREAK, SoundCategory.BLOCKS, 1f, 1f);
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    static ActionResult addSlice(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (!state.isOf(XintingleiExtras.MOONCAKE_BLOCK) || state.get(SERVINGS) < 1
            || state.get(SERVINGS) >= 4 || !(stack.getItem() instanceof MooncakeItem mooncake)
            || !mooncake.matchesSlice(state)) {
            return ActionResult.PASS;
        }
        if (!canChangeSlice(world, pos, player)) return ActionResult.FAIL;
        if (!world.isClient) {
            if (!world.setBlockState(pos, state.with(SERVINGS, state.get(SERVINGS) + 1), Block.NOTIFY_ALL)) {
                return ActionResult.FAIL;
            }
            if (!player.isCreative()) stack.decrement(1);
            world.playSound(null, pos, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 1f, 1f);
        }
        return ActionResult.SUCCESS;
    }

    private static boolean canChangeSlice(World world, BlockPos pos, PlayerEntity player) {
        return player != null && player.canModifyBlocks() && world.canPlayerModifyAt(player, pos)
            && (world.isClient || player.canModifyAt((ServerWorld) world, pos));
    }

    @Override
    protected List<ItemStack> getDroppedStacks(BlockState state, LootWorldContext.Builder builder) {
        int servings = state.get(SERVINGS);
        if (state.get(FLAVOR) != Flavor.ORIGINAL) {
            return List.of(new ItemStack(MooncakeFlavors.item(state.get(FLAVOR), servings > 0),
                servings == 0 ? 1 : servings));
        }
        if (servings > 0) {
            return List.of(new ItemStack(XintingleiExtras.mooncakeItem(
                state.get(OXIDATION), state.get(WAXED), true), servings));
        }
        return List.of(new ItemStack(XintingleiExtras.mooncakeItem(
            state.get(OXIDATION), state.get(WAXED), false)));
    }

    static void refreshNearLightning(ServerWorld world, BlockPos center) {
        for (BlockPos pos : BlockPos.iterateOutwards(center, 3, 2, 3)) {
            BlockState state = world.getBlockState(pos);
            if (state.isOf(XintingleiExtras.MOONCAKE_BLOCK) && state.get(FLAVOR) == Flavor.ORIGINAL
                && (state.get(OXIDATION) > 0 || state.get(WAXED))) {
                world.setBlockState(pos, state.with(OXIDATION, 0).with(WAXED, false), Block.NOTIFY_ALL);
            }
        }
    }
}
