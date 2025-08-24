/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding.impl.block;

import com.wynntils.models.character.type.ClassType;
import com.wynntils.models.elements.type.Element;
import com.wynntils.models.elements.type.Skill;
import com.wynntils.models.gear.type.GearRequirements;
import com.wynntils.models.items.encoding.data.RequirementsData;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformerType;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.UnsignedByteUtils;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.Pair;
import com.wynntils.utils.type.UnsignedByte;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RequirementsDataTransformer extends DataTransformer<RequirementsData> {
    @Override
    protected ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, RequirementsData data) {
        return switch (version) {
            case VERSION_1, VERSION_2 -> encodeRequirementsData(data);
        };
    }

    @Override
    public ErrorOr<RequirementsData> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        return switch (version) {
            case VERSION_1, VERSION_2 -> decodeRequirementsData(byteReader);
        };
    }

    @Override
    public byte getId() {
        return DataTransformerType.REQUIREMENTS_DATA_TRANSFORMER.getId();
    }

    private ErrorOr<UnsignedByte[]> encodeRequirementsData(RequirementsData data) {
        List<UnsignedByte> bytes = new ArrayList<>();

        // The first byte is the level requirement.
        int level = data.requirements().level();
        if (level > 255 || level < 0) {
            return ErrorOr.error("Level requirement does not fit in a byte.");
        }
        bytes.add(UnsignedByte.of((byte) level));

        // The second byte is the class requirement, represented with an id.
        byte classId = 0;
        if (data.requirements().classType().isPresent()) {
            for (ClassType classType : ClassType.values()) {
                if (classType == data.requirements().classType().get()) {
                    classId = (byte) classType.getEncodingId();
                    break;
                }
            }
        }
        bytes.add(UnsignedByte.of(classId));

        // The next byte is the number of skill requirements.
        bytes.add(UnsignedByte.of((byte) data.requirements().skills().size()));

        for (Pair<Skill, Integer> skillPair : data.requirements().skills()) {
            // A skill requirement encoded as an id byte, representing the skill (`ETWFA` order).
            int id = skillPair.a().getAssociatedElement().getEncodingId();
            bytes.add(UnsignedByte.of((byte) id));

            // The next bytes are the skill requirement bytes, which are assembled into an integer.
            int skillRequirement = skillPair.b();
            UnsignedByte[] encodedRequirement = UnsignedByteUtils.encodeVariableSizedInteger(skillRequirement);
            bytes.addAll(List.of(encodedRequirement));
        }

        return ErrorOr.of(bytes.toArray(new UnsignedByte[0]));
    }

    private ErrorOr<RequirementsData> decodeRequirementsData(ArrayReader<UnsignedByte> byteReader) {
        // The first byte is the level requirement.
        int level = byteReader.read().value();

        // The second byte is the class requirement, represented with an id.
        byte classId = byteReader.read().toByte();
        ClassType decodedClassType = ClassType.NONE;
        for (ClassType classType : ClassType.values()) {
            if (classType.getEncodingId() == classId) {
                decodedClassType = classType;
                break;
            }
        }
        // NONE should be represented as null.
        decodedClassType = decodedClassType == ClassType.NONE ? null : decodedClassType;

        // The next byte is the number of skill requirements.
        int skillCount = byteReader.read().value();

        List<Pair<Skill, Integer>> skills = new ArrayList<>();

        for (int i = 0; i < skillCount; i++) {
            // A skill requirement encoded as an id byte, representing the skill (`ETWFA` order).
            int id = byteReader.read().value();
            Skill skill = Skill.fromElement(Element.fromEncodingId(id));

            if (skill == null) { // Sometimes null when users mess with custom encoding
                return ErrorOr.error("Invalid skill encoding: " + id);
            }

            // The next bytes are the skill requirement bytes, which are assembled into an integer.
            int requirement = (int) UnsignedByteUtils.decodeVariableSizedInteger(byteReader);

            skills.add(Pair.of(skill, requirement));
        }

        return ErrorOr.of(new RequirementsData(
                new GearRequirements(level, Optional.ofNullable(decodedClassType), skills, Optional.empty())));
    }
}
