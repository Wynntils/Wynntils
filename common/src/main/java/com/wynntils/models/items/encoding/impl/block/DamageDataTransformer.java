/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.gear.type.GearAttackSpeed;
import com.wynntils.models.items.encoding.data.DamageData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.models.stats.type.DamageType;
import com.wynntils.utils.UnsignedByteUtils;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.RangedValue;
import com.wynntils.utils.type.UnsignedByte;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DamageDataTransformer extends DataTransformer<DamageData> {
    @Override
    protected ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, DamageData data) {
        return switch (version) {
            case VERSION_1, VERSION_2 -> encodeDamageData(data);
        };
    }

    @Override
    public ErrorOr<DamageData> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1, VERSION_2 -> decodeDamageData(byteReader);
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.DAMAGE_DATA_TRANSFORMER.getId();
    }

    @Override
    protected boolean shouldEncodeData(ItemTransformingVersion version, DamageData data) {
        return !data.damages().isEmpty() || data.attackSpeed().isPresent();
    }

    private ErrorOr<UnsignedByte[]> encodeDamageData(DamageData data) {
        List<UnsignedByte> bytes = new ArrayList<>();

        if (data.attackSpeed().isEmpty()) {
            return ErrorOr.error("Attack speed is not present, but damage data is present.");
        }

        // The first byte is the id of the attack speed of the item.
        bytes.add(UnsignedByte.of((byte) data.attackSpeed().get().getEncodingId()));

        // The next byte is the number of attack damages present on the item.
        bytes.add(UnsignedByte.of((byte) data.damages().size()));

        // An attack damage is encoded the following way:
        for (Pair<DamageType, RangedValue> damage : data.damages()) {
            // The first byte is the id of the skill (`ETWFAN`, where N represents Neutral).
            DamageType damageType = damage.a();

            byte damageTypeId;
            if (damageType != DamageType.NEUTRAL && damageType.getElement().isEmpty()) {
                return ErrorOr.error("Damage type " + damageType + " does not have an element");
            } else {
                damageTypeId = (byte) damageType.getEncodingId();
            }
            bytes.add(UnsignedByte.of(damageTypeId));

            // The next bytes are the minimum damage bytes, which are assembled into an integer.
            UnsignedByte[] unsignedBytes =
                    UnsignedByteUtils.encodeVariableSizedInteger(damage.b().low());
            bytes.addAll(List.of(unsignedBytes));

            // The next bytes are the maximum damage bytes, which are assembled into an integer.
            unsignedBytes =
                    UnsignedByteUtils.encodeVariableSizedInteger(damage.b().high());
            bytes.addAll(List.of(unsignedBytes));
        }

        return ErrorOr.of(bytes.toArray(new UnsignedByte[0]));
    }

    private ErrorOr<DamageData> decodeDamageData(ArrayReader<UnsignedByte> byteReader) {
        // The first byte is the id of the attack speed of the item.
        int attackSpeedId = byteReader.read().value();
        GearAttackSpeed attackSpeed = GearAttackSpeed.fromEncodingId(attackSpeedId);

        if (attackSpeed == null) { // Sometimes null when users mess with custom encoding
            return ErrorOr.error("Invalid attack speed encoding: " + attackSpeedId);
        }

        // The next byte is the number of attack damages present on the item.
        int damageCount = byteReader.read().value();

        List<Pair<DamageType, RangedValue>> damages = new ArrayList<>();

        for (int i = 0; i < damageCount; i++) {
            // The first byte is the id of the skill (`ETWFAN`, where N represents Neutral).
            int damageTypeId = byteReader.read().value();
            DamageType damageType = DamageType.fromEncodingId(damageTypeId);

            if (damageType == null) { // Sometimes null when users mess with custom encoding
                return ErrorOr.error("Invalid damage type encoding: " + damageTypeId);
            }

            // The next bytes are the minimum damage bytes, which are assembled into an integer.
            int minDamage = (int) UnsignedByteUtils.decodeVariableSizedInteger(byteReader);

            // The next bytes are the maximum damage bytes, which are assembled into an integer.
            int maxDamage = (int) UnsignedByteUtils.decodeVariableSizedInteger(byteReader);

            damages.add(new Pair<>(damageType, new RangedValue(minDamage, maxDamage)));
        }

        return ErrorOr.of(new DamageData(Optional.of(attackSpeed), damages));
    }
}
