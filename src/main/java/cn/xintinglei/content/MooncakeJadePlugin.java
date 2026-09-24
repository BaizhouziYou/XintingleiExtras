package cn.xintinglei.content;

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.JadeIds;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;

/** Client-only, optional integration: Jade's default name cannot reflect block-state variants. */
@WailaPlugin
public final class MooncakeJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(MooncakeNameProvider.INSTANCE, MooncakeBlock.class);
        registration.registerBlockIcon(MooncakeNameProvider.INSTANCE, MooncakeBlock.class);
    }

    private enum MooncakeNameProvider implements IBlockComponentProvider {
        INSTANCE;

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            BlockState state = accessor.getBlockState();
            int servings = state.get(MooncakeBlock.SERVINGS);
            Text name = Text.translatable("item.xintinglei."
                + Registries.ITEM.getId(mooncakeItem(state)).getPath());
            if (servings > 0 && servings < 4) {
                name = Text.translatable("jade.xintinglei.mooncake.remaining", name, servings);
            }
            tooltip.replace(JadeIds.CORE_OBJECT_NAME, name);
        }

        @Override
        public IElement getIcon(BlockAccessor accessor, IPluginConfig config, IElement currentIcon) {
            return IElementHelper.get().item(new ItemStack(mooncakeItem(accessor.getBlockState())));
        }

        private static Item mooncakeItem(BlockState state) {
            boolean cut = state.get(MooncakeBlock.SERVINGS) > 0;
            MooncakeBlock.Flavor flavor = state.get(MooncakeBlock.FLAVOR);
            return flavor == MooncakeBlock.Flavor.ORIGINAL
                ? XintingleiExtras.mooncakeItem(
                    state.get(MooncakeBlock.OXIDATION), state.get(MooncakeBlock.WAXED), cut)
                : MooncakeFlavors.item(flavor, cut);
        }

        @Override
        public Identifier getUid() {
            return XintingleiExtras.id("mooncake_jade_name");
        }

        @Override
        public boolean isRequired() {
            return true;
        }
    }
}
