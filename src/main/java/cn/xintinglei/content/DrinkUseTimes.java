package cn.xintinglei.content;

/** Kept in sync with the original DrinksDataPack recipe consumable components. */
final class DrinkUseTimes {
    private DrinkUseTimes() {
    }

    static float seconds(String drink) {
        return switch (drink) {
            case "coffee" -> 1.6f;
            case "energy_drink" -> 1.2f;
            case "herbal_tea" -> 1.8f;
            case "berry_juice" -> 1.4f;
            case "mint_cooler" -> 1.4f;
            case "miner_soda" -> 1.3f;
            case "ocean_tonic" -> 1.5f;
            case "blaze_brew" -> 1.5f;
            case "monster_black" -> 1.0f;
            case "monster_white" -> 1.0f;
            case "monster_green" -> 1.0f;
            case "monster_pink" -> 1.0f;
            case "apple_carrot_juice" -> 1.5f;
            case "clear_soda" -> 1.2f;
            case "vodka" -> 1.8f;
            case "almond_water" -> 1.7f;
            case "bean_juice" -> 2.0f;
            case "mega_boba_tea" -> 2.4f;
            default -> throw new IllegalArgumentException("Unknown drink: " + drink);
        };
    }
}
