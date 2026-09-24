package cn.xintinglei.content;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

final class MooncakeItem extends BlockItem {
    private final int stage;
    private final boolean waxed;
    private final boolean cut;
    private final MooncakeBlock.Flavor flavor;

    MooncakeItem(Block block, Settings settings, int stage, boolean waxed, boolean cut) {
        this(block, settings, stage, waxed, cut, MooncakeBlock.Flavor.ORIGINAL);
    }

    MooncakeItem(Block block, Settings settings, int stage, boolean waxed, boolean cut,
                 MooncakeBlock.Flavor flavor) {
        super(block, settings);
        this.stage = stage;
        this.waxed = waxed;
        this.cut = cut;
        this.flavor = flavor;
    }

    boolean matchesSlice(BlockState state) {
        return cut && flavor == state.get(MooncakeBlock.FLAVOR)
            && stage == state.get(MooncakeBlock.OXIDATION)
            && waxed == state.get(MooncakeBlock.WAXED);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (cut && context.getSide() == Direction.UP) {
            World world = context.getWorld();
            BlockPos aboveSupport = context.getBlockPos().up();
            BlockState state = world.getBlockState(aboveSupport);
            if (state.isOf(getBlock())) {
                ActionResult result = MooncakeBlock.addSlice(context.getStack(), state, world,
                    aboveSupport, context.getPlayer());
                // The placement space is occupied even when this slice does not match or the cake is full.
                return result == ActionResult.PASS ? ActionResult.FAIL : result;
            }
        }
        return super.useOnBlock(context);
    }

    @Override
    protected BlockState getPlacementState(ItemPlacementContext context) {
        BlockState state = super.getPlacementState(context);
        return state == null ? null : state
            .with(MooncakeBlock.FLAVOR, flavor)
            .with(MooncakeBlock.OXIDATION, stage)
            .with(MooncakeBlock.WAXED, waxed)
            .with(MooncakeBlock.SERVINGS, cut ? 1 : 0);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.translatable(flavor != MooncakeBlock.Flavor.ORIGINAL
            ? switch (flavor) {
                case SWEET_BERRY -> "tooltip.xintinglei.mooncake.ham";
                case HONEY -> "tooltip.xintinglei.mooncake.custard";
                default -> "tooltip.xintinglei.mooncake.snow";
            }
            : waxed ? "tooltip.xintinglei.mooncake.waxed"
            : cut ? "tooltip.xintinglei.mooncake.slice" : "tooltip.xintinglei.mooncake.general")
            .formatted(Formatting.GRAY));
    }
}
