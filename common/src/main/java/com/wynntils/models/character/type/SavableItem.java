package com.wynntils.models.character.type;

public record SavableItem(String encoded, String itemName) implements SavableBasicItem {
    public SavableItem() {
        this("", null);
    }
}
