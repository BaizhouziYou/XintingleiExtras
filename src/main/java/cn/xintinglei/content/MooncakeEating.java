package cn.xintinglei.content;

import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.consume.UseAction;
import net.minecraft.sound.SoundEvents;

/** Shared vanilla eating behavior for every edible mooncake and single serving. */
final class MooncakeEating {
    private static final float EATING_SECONDS = 1.6f;

    private MooncakeEating() {
    }

    static FoodComponent food(int nutrition) {
        // The record constructor takes absolute saturation, not a multiplier.
        return new FoodComponent.Builder().nutrition(nutrition).saturationModifier(0.6f).build();
    }

    static ConsumableComponent component() {
        return ConsumableComponent.builder()
            .consumeSeconds(EATING_SECONDS)
            .useAction(UseAction.EAT)
            .sound(SoundEvents.ENTITY_GENERIC_EAT)
            .consumeParticles(true)
            .build();
    }
}
