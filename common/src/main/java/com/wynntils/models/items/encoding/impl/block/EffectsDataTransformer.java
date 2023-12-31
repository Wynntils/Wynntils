/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.items.encoding.data.EffectsData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.models.wynnitem.type.ConsumableEffect;
import com.wynntils.models.wynnitem.type.NamedItemEffect;
import com.wynntils.utils.UnsignedByteUtils;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.UnsignedByte;
import java.util.ArrayList;
import java.util.List;

public class EffectsDataTransformer extends DataTransformer<EffectsData> {
    private static final List<Pair<ConsumableEffect, Integer>> CONSUMABLE_EFFECT_IDS = List.of(
            new Pair<>(ConsumableEffect.HEAL, 0),
            new Pair<>(ConsumableEffect.MANA, 1),
            new Pair<>(ConsumableEffect.DURATION, 2));

    @Override
    protected ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, EffectsData data) {
        return switch (version) {
            case VERSION_1 -> encodeEffectsData(data);
        };
    }

    @Override
    public ErrorOr<EffectsData> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1 -> decodeEffectsData(byteReader);
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.EFFECTS_DATA_TRANSFORMER.getId();
    }

    private ErrorOr<UnsignedByte[]> encodeEffectsData(EffectsData data) {
        List<UnsignedByte> bytes = new ArrayList<>();
        // The first byte is the number of effects.
        bytes.add(UnsignedByte.of((byte) data.namedEffects().size()));

        // An effect is encoded the following way:
        for (NamedItemEffect namedEffect : data.namedEffects()) {
            // The first byte is the id of the effect.

            UnsignedByte id = null;
            for (Pair<ConsumableEffect, Integer> consumableEffectId : CONSUMABLE_EFFECT_IDS) {
                if (consumableEffectId.key() == namedEffect.type()) {
                    id = UnsignedByte.of((byte) consumableEffectId.value().intValue());
                    break;
                }
            }

            if (id == null) {
                return ErrorOr.error("Cannot encode consumable effect: " + namedEffect.type());
            }

            bytes.add(id);

            // The next bytes are the effect's value bytes, which are assembled into an integer
            UnsignedByte[] unsignedBytes = UnsignedByteUtils.encodeVariableSizedInteger(namedEffect.value());
            bytes.addAll(List.of(unsignedBytes));
        }

        return ErrorOr.of(bytes.toArray(new UnsignedByte[0]));
    }

    private ErrorOr<EffectsData> decodeEffectsData(ArrayReader<UnsignedByte> byteReader) {
        List<NamedItemEffect> namedEffects = new ArrayList<>();

        // The first byte is the number of effects.
        int numberOfEffects = byteReader.read().value();

        for (int i = 0; i < numberOfEffects; i++) {
            // The first byte is the id of the effect.
            int effectId = byteReader.read().value();

            // The next bytes are the effect's value bytes, which are assembled into an integer
            int value = (int) UnsignedByteUtils.decodeVariableSizedInteger(byteReader);

            NamedItemEffect namedEffect = null;
            for (Pair<ConsumableEffect, Integer> consumableEffectId : CONSUMABLE_EFFECT_IDS) {
                if (consumableEffectId.value() == effectId) {
                    namedEffect = new NamedItemEffect(consumableEffectId.key(), value);
                    break;
                }
            }

            if (namedEffect == null) {
                return ErrorOr.error("Cannot decode consumable effect: " + effectId);
            }

            namedEffects.add(namedEffect);
        }

        return ErrorOr.of(new EffectsData(namedEffects));
    }
}
