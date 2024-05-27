/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.map;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.wynntils.utils.StringUtils;
import com.wynntils.utils.mc.type.Location;
import java.lang.reflect.Type;
import java.util.Optional;

public class Label {
    private final String name;
    private final int x;
    private final int z;
    private final int layer;
    private final Integer level;

    public Label(String name, int x, int z, int layer, Integer level) {
        this.name = name;
        this.x = x;
        this.z = z;
        this.layer = layer;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    /**
     * The relative importance of this place
     */
    public LabelLayer getLayer() {
        return LabelLayer.values()[layer - 1];
    }

    public Optional<Integer> getLevel() {
        return Optional.ofNullable(level);
    }

    public Location getLocation() {
        return new Location(x, 0, z);
    }

    public static final class LabelDeserializer implements JsonDeserializer<Label> {
        @Override
        public Label deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            JsonElement levelObject = jsonObject.get("level");

            Integer level = null;

            if (levelObject != null) {
                String levelString = levelObject.getAsString();

                // Level is either a single number or a range
                String minLevel = levelString.split("-")[0];

                // Or it can be a range with no upper bound (e.g. "100+")
                minLevel = minLevel.replaceAll("[^0-9]", "");

                level = Integer.parseInt(minLevel);
            }

            return new Label(
                    jsonObject.get("name").getAsString(),
                    jsonObject.get("x").getAsInt(),
                    jsonObject.get("z").getAsInt(),
                    jsonObject.get("layer").getAsInt(),
                    level);
        }
    }

    public enum LabelLayer {
        PROVINCE("province"),
        CITY("city"),
        TOWN_OR_PLACE("place");

        private final String mapDataId;

        LabelLayer(String mapDataId) {
            this.mapDataId = mapDataId;
        }

        public String getMapDataId() {
            return mapDataId;
        }

        public String getName() {
            return StringUtils.capitalized(mapDataId);
        }
    }
}
