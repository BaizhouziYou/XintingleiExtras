package cn.xintinglei.content;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

/** Registers the uncooked intermediate used by the mooncake furnace recipe. */
public final class MooncakeBaking {
    public static final Map<String, Item> RAW_MOONCAKES = new LinkedHashMap<>();
    private static final String[] RAW_IDS = {
        "raw_mooncake", "raw_sweet_berry_mooncake", "raw_honey_mooncake"
    };

    private MooncakeBaking() {
    }

    public static Map<String, Item> registerRawMooncakes() {
        for (String name : RAW_IDS) {
            if (RAW_MOONCAKES.containsKey(name)) {
                continue;
            }
            Identifier id = XintingleiExtras.id(name);
            Item item = Registry.register(Registries.ITEM, id,
                new Item(new Item.Settings()
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, id))
                    .maxCount(16)) {
                    @Override
                    public void appendTooltip(ItemStack stack, TooltipContext context,
                                              List<Text> tooltip, TooltipType type) {
                        super.appendTooltip(stack, context, tooltip, type);
                        tooltip.add(Text.translatable("tooltip.xintinglei.mooncake.raw")
                            .formatted(Formatting.GRAY));
                    }
                });
            RAW_MOONCAKES.put(name, item);
        }
        return RAW_MOONCAKES;
    }
}
