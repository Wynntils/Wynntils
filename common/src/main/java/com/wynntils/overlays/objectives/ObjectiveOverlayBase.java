/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.overlays.objectives;

import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.consumers.overlays.OverlaySize;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.ObjectivesTextures;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;

public abstract class ObjectiveOverlayBase extends Overlay {
    protected static final float SPACE_BETWEEN = 10;

    @Persisted(i18nKey = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.hideOnInactivity")
    public final Config<Boolean> hideOnInactivity = new Config<>(false);

    @Persisted(i18nKey = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.enableProgressBar")
    public final Config<Boolean> enableProgressBar = new Config<>(true);

    @Persisted(i18nKey = "overlay.wynntils.objectivesTexture")
    public final Config<ObjectivesTextures> objectivesTexture = new Config<>(ObjectivesTextures.A);

    @Persisted(i18nKey = "feature.wynntils.objectivesOverlay.overlay.objectiveOverlayBase.textShadow")
    public final Config<TextShadow> textShadow = new Config<>(TextShadow.OUTLINE);

    protected ObjectiveOverlayBase(
            OverlayPosition position,
            OverlaySize size,
            HorizontalAlignment horizontalAlignmentOverride,
            VerticalAlignment verticalAlignmentOverride) {
        super(position, size, horizontalAlignmentOverride, verticalAlignmentOverride);
    }
}
