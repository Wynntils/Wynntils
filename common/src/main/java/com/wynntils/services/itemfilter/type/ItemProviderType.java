/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

/**
 * Represents the type of item provider.
 * Item provider types are used to determine if the current container supports the filter.
 * An item provider can have multiple types, but it must have at least one.
 */
public enum ItemProviderType {
    /**
     * A filter for all items. If this is present, the filter can be used for any item.
     */
    ALL,

    /**
     * A generic item filter. For providers that do not fit into any other category.
     */
    GENERIC,

    /**
     * A filter for gear items.
     */
    GEAR,

    /**
     * A filter for gear items, where identified gear instances are present.
     */
    GEAR_INSTANCE,

    /**
     * A filter for ingredients.
     */
    INGREDIENT,

    /**
     * A filter for materials.
     */
    MATERIAL,

    /**
     * A filter for priced items, or for items, that have an emerald value.
     */
    VALUED,

    /**
     * A filter for items that have counts or uses, such as stacks of items.
     */
    COUNTED,

    /**
     * A filter for items that have tiers.
     */
    TIERED,

    /**
     * A filter for items that have durability.
     */
    DURABLE,

    /**
     * A filter for items that have a profession.
     */
    PROFESSION
}
