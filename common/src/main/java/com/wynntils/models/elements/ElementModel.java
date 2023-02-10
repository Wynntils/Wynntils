/*
 * Copyright © Wynntils 2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.elements;

import com.wynntils.core.components.Model;
import com.wynntils.models.elements.type.Element;
import java.util.List;

public class ElementModel extends Model {
    public ElementModel() {
        super(List.of());
    }

    public List<Element> getElementStatOrder() {
        return List.of(Element.FIRE, Element.WATER, Element.AIR, Element.THUNDER, Element.EARTH);
    }
}
