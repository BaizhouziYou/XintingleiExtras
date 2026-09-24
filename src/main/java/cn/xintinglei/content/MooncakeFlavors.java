package cn.xintinglei.content;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Additional filled mooncakes. IDs are stable so recipes and saved inventories keep working. */
public final class MooncakeFlavors {
    private static final Map<String, Item> ITEMS = new LinkedHashMap<>();

    private MooncakeFlavors() {
    }

    /** The flavored variants share the mooncake block but never oxidize or accept wax. */
    public static void register() {
        registerFood(MooncakeBlock.Flavor.SNOW_SKIN, false, 8);
        registerFood(MooncakeBlock.Flavor.SNOW_SKIN, true, 2);
        registerFood(MooncakeBlock.Flavor.SWEET_BERRY, false, 8);
        registerFood(MooncakeBlock.Flavor.SWEET_BERRY, true, 2);
        registerFood(MooncakeBlock.Flavor.HONEY, false, 12);
        registerFood(MooncakeBlock.Flavor.HONEY, true, 3);
    }

    public static Map<String, Item> items() {
        return Collections.unmodifiableMap(ITEMS);
    }

    public static Item item(MooncakeBlock.Flavor flavor, boolean cut) {
        return ITEMS.get((cut ? "cut_" : "") + flavor.asString() + "_mooncake");
    }

    private static void registerFood(MooncakeBlock.Flavor flavor, boolean cut, int nutrition) {
        String path = (cut ? "cut_" : "") + flavor.asString() + "_mooncake";
        Identifier id = XintingleiExtras.id(path);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        Item.Settings settings = new Item.Settings().registryKey(key).maxCount(16)
            .food(MooncakeEating.food(nutrition),
                MooncakeEating.component());
        ITEMS.put(path, Registry.register(Registries.ITEM, id,
            new MooncakeItem(XintingleiExtras.MOONCAKE_BLOCK, settings, 0, false, cut, flavor)));
    }
}
