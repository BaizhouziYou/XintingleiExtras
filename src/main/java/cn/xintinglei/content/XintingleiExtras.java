package cn.xintinglei.content;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LightningEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public final class XintingleiExtras implements ModInitializer {
    public static final String MOD_ID = "xintinglei";
    public static final Map<String, Item> DRINKS = new LinkedHashMap<>();
    public static final Map<String, Item> MOONCAKES = new LinkedHashMap<>();
    public static final String[] MOONCAKE_STAGES = {"", "oxidizing_", "mottled_", "patinated_"};
    private static final String[] DRINK_IDS = {
        "coffee", "energy_drink", "herbal_tea", "berry_juice", "mint_cooler", "miner_soda",
        "ocean_tonic", "blaze_brew", "monster_black", "monster_white", "monster_green", "monster_pink",
        "apple_carrot_juice", "clear_soda", "vodka", "almond_water", "bean_juice", "mega_boba_tea"
    };
    public static final RegistryKey<ItemGroup> CONTENT_GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, id("content"));
    public static final Block MOONCAKE_BLOCK = Registry.register(Registries.BLOCK, id("mooncake"),
        new MooncakeBlock(AbstractBlock.Settings.create()
            .registryKey(RegistryKey.of(RegistryKeys.BLOCK, id("mooncake")))
            .strength(0.3f).sounds(BlockSoundGroup.WOOL).nonOpaque().ticksRandomly()));

    @Override
    public void onInitialize() {
        for (String drink : DRINK_IDS) {
            Identifier id = id(drink);
            RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
            Item.Settings settings = new Item.Settings()
                .registryKey(key)
                .maxCount(16)
                .food(new FoodComponent(0, 0f, true), ConsumableComponent.builder()
                    .consumeSeconds(DrinkUseTimes.seconds(drink))
                    .useAction(UseAction.DRINK)
                    .sound(SoundEvents.ENTITY_GENERIC_DRINK)
                    .consumeParticles(false)
                    .build())
                .useRemainder(Items.GLASS_BOTTLE);
            DRINKS.put(drink, Registry.register(Registries.ITEM, id, new DrinkItem(settings, drink)));
        }

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries ->
            DRINKS.values().forEach(entries::add));

        for (int stage = 0; stage < MOONCAKE_STAGES.length; stage++) {
            for (boolean waxed : new boolean[] {false, true}) {
                for (boolean cut : new boolean[] {false, true}) {
                    String name = mooncakeName(stage, waxed, cut);
                    RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id(name));
                    Item.Settings settings = new Item.Settings().registryKey(key).maxCount(16);
                    if (!waxed) {
                        settings.food(MooncakeEating.food(cut ? 2 : 8),
                            MooncakeEating.component());
                    }
                    MOONCAKES.put(name, Registry.register(Registries.ITEM, id(name),
                        new MooncakeItem(MOONCAKE_BLOCK, settings, stage, waxed, cut)));
                }
            }
        }
        MooncakeBaking.registerRawMooncakes();
        MooncakeFlavors.register();
        Registry.register(Registries.ITEM_GROUP, CONTENT_GROUP, FabricItemGroup.builder()
            .icon(() -> new ItemStack(MOONCAKES.get("mooncake")))
            .displayName(Text.translatable("itemGroup.xintinglei.content"))
            .entries((context, entries) -> {
                DRINKS.values().forEach(entries::add);
                MOONCAKES.values().forEach(entries::add);
                MooncakeFlavors.items().values().forEach(entries::add);
                MooncakeBaking.RAW_MOONCAKES.values().forEach(entries::add);
            })
            .build());
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            MOONCAKES.values().forEach(entries::add);
            MooncakeFlavors.items().values().forEach(entries::add);
            MooncakeBaking.RAW_MOONCAKES.values().forEach(entries::add);
        });
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (entity instanceof LightningEntity) {
                MooncakeBlock.refreshNearLightning(world, entity.getBlockPos());
            }
        });
    }

    public static String mooncakeName(int stage, boolean waxed, boolean cut) {
        return (waxed ? "waxed_" : "") + (cut ? "cut_" : "") + MOONCAKE_STAGES[stage] + "mooncake";
    }

    public static Item mooncakeItem(int stage, boolean waxed, boolean cut) {
        return MOONCAKES.get(mooncakeName(stage, waxed, cut));
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
