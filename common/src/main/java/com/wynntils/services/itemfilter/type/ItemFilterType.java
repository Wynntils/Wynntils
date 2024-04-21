/*
 * Copyright © Wynntils 2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.itemfilter.type;

/**
 * Represents the type of item filter.
 * Item filter types are used to determine if the current container supports the filter.
 * An item filter can have multiple types, but it must have at least one.
 */
public enum ItemFilterType {
    /**
     * A generic item filter. If this is present, the filter can be used for any item.
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
     * A filter for items that have counts, such as stacks of items.
     */
    COUNTED,

    /**
     * A filter for items that have tiers.
     */
    TIERED,

    /**
     * A filter for items that have durability or have uses.
     */
    DURABLE,

    /**
     * A filter for items that have a profession.
     */
    PROFESSION
}
