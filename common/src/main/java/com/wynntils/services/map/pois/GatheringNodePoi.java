/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map.pois;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import com.wynntils.models.profession.type.MaterialType;
import com.wynntils.models.profession.type.SourceMaterial;
import com.wynntils.services.map.type.DisplayPriority;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.type.PoiLocation;
import com.wynntils.utils.render.Texture;
import com.wynntils.utils.type.Pair;
import java.util.Locale;
import java.util.Optional;

public class GatheringNodePoi extends StaticIconPoi {
    private final Optional<Pair<MaterialType, SourceMaterial>> resourceProfile;
    private final ResourceType resourceType;
    private final String rawName;
    private final int angle;
    private final int level;

    public GatheringNodePoi(int x, int y, int z, String resourceName, String resourceType, int angle, int level) {
        super(new PoiLocation(x, y, z));
        String capitalizedName = StringUtils.capitalized(resourceName);

        this.resourceProfile = Models.Profession.findMaterialBySourceName(capitalizedName);
        this.resourceType = ResourceType.fromString(resourceType);
        this.rawName = capitalizedName;
        this.angle = angle;
        this.level = level;
    }

    @Override
    public Texture getIcon() {
        return getMaterialType() != null ? getMaterialType().getMaterialTexture() : Texture.QUESTION_MARK_ICON;
    }

    @Override
    protected float getMinZoomForRender() {
        return 0;
    }

    @Override
    public DisplayPriority getDisplayPriority() {
        return DisplayPriority.HIGH;
    }

    @Override
    public String getName() {
        return (getSourceMaterial() != null
                        ? getSourceMaterial().name() + " "
                                + StringUtils.capitalized(getMaterialType().name())
                        : rawName)
                + " (" + getLevel() + ")";
    }

    public MaterialType getMaterialType() {
        return resourceProfile.map(Pair::a).orElse(null);
    }

    public SourceMaterial getSourceMaterial() {
        return resourceProfile.map(Pair::b).orElse(null);
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public int getAngle() {
        return angle;
    }

    public int getLevel() {
        return level;
    }

    public enum ResourceType {
        NODE,
        WALL,
        CORNER;

        public static ResourceType fromString(String type) {
            try {
                return valueOf(type.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                WynntilsMod.error("Unknown Gathering Node type: \"" + type + "\"");
                return null;
            }
        }
    }
}
