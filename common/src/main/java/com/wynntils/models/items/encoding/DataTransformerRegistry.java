/*
 * Copyright © Wynntils 2023-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.items.encoding;

import com.wynntils.models.items.encoding.data.CustomConsumableTypeData;
import com.wynntils.models.items.encoding.data.CustomGearTypeData;
import com.wynntils.models.items.encoding.data.CustomIdentificationsData;
import com.wynntils.models.items.encoding.data.DamageData;
import com.wynntils.models.items.encoding.data.DefenseData;
import com.wynntils.models.items.encoding.data.DurabilityData;
import com.wynntils.models.items.encoding.data.EffectsData;
import com.wynntils.models.items.encoding.data.EndData;
import com.wynntils.models.items.encoding.data.IdentificationData;
import com.wynntils.models.items.encoding.data.NameData;
import com.wynntils.models.items.encoding.data.PowderData;
import com.wynntils.models.items.encoding.data.RequirementsData;
import com.wynntils.models.items.encoding.data.RerollData;
import com.wynntils.models.items.encoding.data.ShinyData;
import com.wynntils.models.items.encoding.data.StartData;
import com.wynntils.models.items.encoding.data.TypeData;
import com.wynntils.models.items.encoding.data.UsesData;
import com.wynntils.models.items.encoding.impl.block.CustomConsumableTypeDataTransformer;
import com.wynntils.models.items.encoding.impl.block.CustomGearTypeTransformer;
import com.wynntils.models.items.encoding.impl.block.CustomIdentificationDataTransformer;
import com.wynntils.models.items.encoding.impl.block.DamageDataTransformer;
import com.wynntils.models.items.encoding.impl.block.DefenseDataTransformer;
import com.wynntils.models.items.encoding.impl.block.DurabilityDataTransformer;
import com.wynntils.models.items.encoding.impl.block.EffectsDataTransformer;
import com.wynntils.models.items.encoding.impl.block.EndDataTransformer;
import com.wynntils.models.items.encoding.impl.block.IdentificationDataTransformer;
import com.wynntils.models.items.encoding.impl.block.NameDataTransformer;
import com.wynntils.models.items.encoding.impl.block.PowderDataTransformer;
import com.wynntils.models.items.encoding.impl.block.RequirementsDataTransformer;
import com.wynntils.models.items.encoding.impl.block.RerollDataTransformer;
import com.wynntils.models.items.encoding.impl.block.ShinyDataTransformer;
import com.wynntils.models.items.encoding.impl.block.StartDataTransformer;
import com.wynntils.models.items.encoding.impl.block.TypeDataTransformer;
import com.wynntils.models.items.encoding.impl.block.UsesDataTransformer;
import com.wynntils.models.items.encoding.type.DataTransformer;
import com.wynntils.models.items.encoding.type.ItemData;
import com.wynntils.models.items.encoding.type.ItemTransformingVersion;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.type.ArrayReader;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.UnsignedByte;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for registering and storing all data transformers.
 * Data transformers are used for transforming between {@link ItemData} and {@link UnsignedByte} arrays.
 */
public final class DataTransformerRegistry {
    private final DataTransformerMap dataTransformers = new DataTransformerMap();

    public DataTransformerRegistry() {
        registerAllTransformers();
    }

    public ErrorOr<EncodedByteBuffer> encodeData(ItemTransformingVersion version, List<ItemData> data) {
        List<UnsignedByte> bytes = new ArrayList<>();

        for (ItemData itemData : data) {
            try {
                ErrorOr<UnsignedByte[]> errorOrEncodedData = encodeData(version, itemData);
                if (errorOrEncodedData.hasError()) {
                    return ErrorOr.error(errorOrEncodedData.getError());
                }

                bytes.addAll(Arrays.asList(errorOrEncodedData.getValue()));
            } catch (Exception e) {
                return ErrorOr.<EncodedByteBuffer>error("Failed to encode data class "
                                + itemData.getClass().getSimpleName() + "!")
                        .logged();
            }
        }

        return ErrorOr.of(EncodedByteBuffer.fromBytes(bytes.toArray(new UnsignedByte[0])));
    }

    public ErrorOr<List<ItemData>> decodeData(EncodedByteBuffer encodedByteBuffer) {
        ArrayReader<UnsignedByte> byteReader = encodedByteBuffer.getReader();

        // Handle start data specially
        ErrorOr<StartData> errorOrStartData = StartDataTransformer.decodeData(byteReader);
        if (errorOrStartData.hasError()) {
            return ErrorOr.error(errorOrStartData.getError());
        }

        return decodeData(errorOrStartData.getValue().version(), byteReader);
    }

    private ErrorOr<UnsignedByte[]> encodeData(ItemTransformingVersion version, ItemData data) {
        DataTransformer<ItemData> dataTransformer = (DataTransformer<ItemData>) dataTransformers.get(data.getClass());
        if (dataTransformer == null) {
            return ErrorOr.<UnsignedByte[]>error(
                            "No data transformer found for " + data.getClass().getSimpleName())
                    .logged();
        }

        return dataTransformer.encode(version, data);
    }

    private ErrorOr<List<ItemData>> decodeData(ItemTransformingVersion version, ArrayReader<UnsignedByte> byteReader) {
        List<ItemData> dataList = new ArrayList<>();

        while (byteReader.hasRemaining()) {
            UnsignedByte dataBlockId = byteReader.read();

            try {
                DataTransformer<ItemData> dataTransformer = dataTransformers.get(dataBlockId.toByte());

                if (dataTransformer == null) {
                    return ErrorOr.<List<ItemData>>error("No data transformer found for id " + dataBlockId.value())
                            .logged();
                }

                ErrorOr<ItemData> errorOrData = dataTransformer.decodeData(version, byteReader);

                if (errorOrData.hasError()) {
                    return ErrorOr.error(errorOrData.getError());
                }

                dataList.add(errorOrData.getValue());
            } catch (Exception e) {
                return ErrorOr.<List<ItemData>>error("Failed to decode data block with id " + dataBlockId.value() + "!")
                        .logged();
            }
        }

        boolean foundEndData = dataList.stream().anyMatch(data -> data instanceof EndData);
        if (!foundEndData) {
            return ErrorOr.error("No end data found in item data!");
        }
        dataList.removeIf(data -> data instanceof EndData);

        return ErrorOr.of(dataList);
    }

    private <T extends ItemData> void registerDataTransformer(Class<T> dataClass, DataTransformer<T> dataTransformer) {
        dataTransformers.put(dataClass, dataTransformer.getId(), dataTransformer);
    }

    private void registerAllTransformers() {
        registerDataTransformer(StartData.class, new StartDataTransformer());

        // Order is irrelevant here, keep it ordered as the ids are
        registerDataTransformer(TypeData.class, new TypeDataTransformer());
        registerDataTransformer(NameData.class, new NameDataTransformer());
        registerDataTransformer(IdentificationData.class, new IdentificationDataTransformer());
        registerDataTransformer(PowderData.class, new PowderDataTransformer());
        registerDataTransformer(RerollData.class, new RerollDataTransformer());
        registerDataTransformer(ShinyData.class, new ShinyDataTransformer());
        registerDataTransformer(CustomGearTypeData.class, new CustomGearTypeTransformer());
        registerDataTransformer(DurabilityData.class, new DurabilityDataTransformer());
        registerDataTransformer(RequirementsData.class, new RequirementsDataTransformer());
        registerDataTransformer(DamageData.class, new DamageDataTransformer());
        registerDataTransformer(DefenseData.class, new DefenseDataTransformer());
        registerDataTransformer(CustomIdentificationsData.class, new CustomIdentificationDataTransformer());
        registerDataTransformer(CustomConsumableTypeData.class, new CustomConsumableTypeDataTransformer());
        registerDataTransformer(UsesData.class, new UsesDataTransformer());
        registerDataTransformer(EffectsData.class, new EffectsDataTransformer());

        registerDataTransformer(EndData.class, new EndDataTransformer());
    }

    private static final class DataTransformerMap {
        private final Map<Class<? extends ItemData>, DataTransformer<? extends ItemData>> dataTransformers =
                new HashMap<>();

        private final Map<Byte, DataTransformer<? extends ItemData>> idToTransformerMap = new HashMap<>();

        public void put(
                Class<? extends ItemData> dataClass, byte id, DataTransformer<? extends ItemData> dataTransformer) {
            if (dataTransformers.put(dataClass, dataTransformer) != null) {
                throw new IllegalStateException("Duplicate data class: " + dataClass.getSimpleName());
            }
            if (idToTransformerMap.put(id, dataTransformer) != null) {
                throw new IllegalStateException("Duplicate id: " + id);
            }
        }

        public <T extends ItemData> DataTransformer<T> get(Class<T> dataClass) {
            return (DataTransformer<T>) dataTransformers.get(dataClass);
        }

        public <T extends ItemData> DataTransformer<T> get(byte id) {
            return (DataTransformer<T>) idToTransformerMap.get(id);
        }
    }
}
