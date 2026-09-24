package cn.xintinglei.content;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

final class DrinkItem extends Item {
    private final String drinkId;

    DrinkItem(Settings settings, String drinkId) {
        super(settings);
        this.drinkId = drinkId;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.translatable("tooltip.xintinglei." + drinkId).formatted(Formatting.GRAY));
    }
}
