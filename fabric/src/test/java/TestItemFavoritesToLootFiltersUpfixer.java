/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.wynntils.core.json.JsonManager;
import com.wynntils.core.persisted.upfixers.config.ItemFavoritesToLootFiltersUpfixer;
import com.wynntils.services.favorites.type.FavoriteCategory;
import com.wynntils.services.favorites.type.FavoriteIcon;
import com.wynntils.services.favorites.type.LootFilter;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestItemFavoritesToLootFiltersUpfixer {
    @Test
    public void movesFavoritesAndCloseOverrideIntoDefaultCategory() {
        JsonArray favoriteItems = new JsonArray();
        favoriteItems.add("Cataclysm");
        favoriteItems.add("Warp");

        JsonObject configObject = new JsonObject();
        configObject.add("itemFavoriteFeature.favoriteItems", favoriteItems);
        configObject.addProperty("itemFavoriteFeature.lootChestCloseOverride", 5);

        boolean changed = new ItemFavoritesToLootFiltersUpfixer().apply(configObject, Set.of());

        Assertions.assertTrue(changed);
        Assertions.assertFalse(configObject.has("itemFavoriteFeature.favoriteItems"));
        Assertions.assertFalse(configObject.has("itemFavoriteFeature.lootChestCloseOverride"));

        List<LootFilter> lootFilters = JsonManager.GSON.fromJson(
                configObject.get("itemFavoriteFeature.lootFilters"), new TypeToken<List<LootFilter>>() {}.getType());

        Assertions.assertEquals(1, lootFilters.size());
        Assertions.assertEquals(LootFilter.DEFAULT_NAME, lootFilters.getFirst().getName());
        Assertions.assertEquals(1, lootFilters.getFirst().getCategories().size());

        FavoriteCategory category = lootFilters.getFirst().getCategories().getFirst();
        Assertions.assertEquals(FavoriteCategory.DEFAULT_NAME, category.getName());
        Assertions.assertEquals(Set.of("Cataclysm", "Warp"), category.getItems());
        Assertions.assertEquals(5, category.getLootChestCloseOverride());

        FavoriteCategory defaultCategory = new FavoriteCategory(FavoriteCategory.DEFAULT_NAME);
        Assertions.assertEquals(FavoriteIcon.STAR, category.getIcon());
        Assertions.assertEquals(defaultCategory.getIconColor(), category.getIconColor());
        Assertions.assertEquals(defaultCategory.getBorderColor(), category.getBorderColor());
    }

    @Test
    public void incompleteLootFiltersAreInvalid() {
        Assertions.assertFalse(JsonManager.GSON.fromJson("{}", LootFilter.class).isValid());
        Assertions.assertFalse(JsonManager.GSON
                .fromJson("{\"name\":\"Raids\",\"categories\":[]}", LootFilter.class)
                .isValid());
        Assertions.assertFalse(JsonManager.GSON
                .fromJson(
                        "{\"name\":\"Raids\",\"categories\":[{\"name\":\"Mythics\",\"iconColor\":null}]}",
                        LootFilter.class)
                .isValid());
        Assertions.assertTrue(JsonManager.GSON
                .fromJson("{\"name\":\"Raids\",\"categories\":[{\"name\":\"Mythics\"}]}", LootFilter.class)
                .isValid());
    }

    @Test
    public void oldNeverCloseValueIsConverted() {
        JsonObject configObject = new JsonObject();
        configObject.addProperty("itemFavoriteFeature.lootChestCloseOverride", 0);

        new ItemFavoritesToLootFiltersUpfixer().apply(configObject, Set.of());

        List<LootFilter> lootFilters = JsonManager.GSON.fromJson(
                configObject.get("itemFavoriteFeature.lootFilters"), new TypeToken<List<LootFilter>>() {}.getType());
        Assertions.assertEquals(
                FavoriteCategory.NEVER_CLOSE,
                lootFilters.getFirst().getCategories().getFirst().getLootChestCloseOverride());
    }

    @Test
    public void doesNothingWithoutOldValues() {
        JsonObject configObject = new JsonObject();

        boolean changed = new ItemFavoritesToLootFiltersUpfixer().apply(configObject, Set.of());

        Assertions.assertFalse(changed);
        Assertions.assertFalse(configObject.has("itemFavoriteFeature.lootFilters"));
    }

    @Test
    public void lootFiltersSurviveSerialization() {
        LootFilter lootFilter = new LootFilter("Raids");
        FavoriteCategory category = lootFilter.getCategories().getFirst();
        category.getItems().add("Warp");
        category.setIcon(FavoriteIcon.HOLLOW_CIRCLE);
        category.setLootChestCloseOverride(0);

        LootFilter restored = JsonManager.GSON.fromJson(JsonManager.GSON.toJson(lootFilter), LootFilter.class);

        FavoriteCategory restoredCategory = restored.getCategories().getFirst();
        Assertions.assertTrue(restored.isValid());
        Assertions.assertEquals("Raids", restored.getName());
        Assertions.assertEquals(Set.of("Warp"), restoredCategory.getItems());
        Assertions.assertEquals(FavoriteIcon.HOLLOW_CIRCLE, restoredCategory.getIcon());
        Assertions.assertEquals(0, restoredCategory.getLootChestCloseOverride());
        Assertions.assertEquals(category.getIconColor(), restoredCategory.getIconColor());
        Assertions.assertEquals(category.getOverlayColor(), restoredCategory.getOverlayColor());
    }
}
