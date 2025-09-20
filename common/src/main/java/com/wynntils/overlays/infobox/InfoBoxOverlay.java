/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.infobox;

import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.overlays.CustomNameProperty;
import com.wynntils.core.consumers.overlays.TextOverlay;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.persisted.config.HiddenConfig;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.type.ErrorOr;

public class InfoBoxOverlay extends TextOverlay implements CustomNameProperty {
    @Persisted
    private final HiddenConfig<String> customName = new HiddenConfig<>("");

    @Persisted
    private final Config<String> content = new Config<>("");

    @Persisted
    final Config<String> colorTemplate = new Config<>("");

    private CustomColor colorCache = CommonColors.WHITE;

    public InfoBoxOverlay(int id) {
        super(id);
    }

    @Override
    public String getTemplate() {
        return content.get();
    }

    @Override
    public String getPreviewTemplate() {
        if (!content.get().isEmpty()) {
            return content.get();
        }

        return "&cX: {x(my_loc):0}, &9Y: {y(my_loc):0}, &aZ: {z(my_loc):0}";
    }

    @Override
    protected CustomColor getRenderColor() {
        return colorCache;
    }

    @Override
    public void tick() {
        super.tick();
        if (!isRendered()) return;

        String template = colorTemplate.get();
        if (template.isBlank()) {
            colorCache = CommonColors.WHITE;
            return;
        }

        String formattedText =
                StyledText.join("", Managers.Function.doFormatLines(template)).getString();
        ErrorOr<CustomColor> colorOrError = Managers.Function.tryGetRawValueOfType(formattedText, CustomColor.class);

        if (colorOrError.hasError()) {
            colorCache = CommonColors.WHITE;
            return;
        }
        colorCache = colorOrError.getValue();
    }

    @Override
    public Config<String> getCustomName() {
        return customName;
    }

    @Override
    public void setCustomName(String newName) {
        customName.setValue(newName);
    }
}
