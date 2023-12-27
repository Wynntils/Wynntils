/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.type;

/**
 * Settings for encoding items.
 * @param extendedIdentificationEncoding Whether to use extended identification encoding. This only affects identifiable items.
 * @param shareItemName Whether to share the item name between the item name and the item lore. This only affects crafted and custom items.
 */
public record EncodingSettings(boolean extendedIdentificationEncoding, boolean shareItemName) {}
