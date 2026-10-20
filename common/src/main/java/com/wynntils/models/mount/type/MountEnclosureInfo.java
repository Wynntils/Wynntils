/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.mount.type;

import com.wynntils.core.components.Models;
import com.wynntils.models.items.WynnItem;
import com.wynntils.models.items.items.game.MountItem;
import com.wynntils.utils.EncodedByteBuffer;
import com.wynntils.utils.type.ErrorOr;
import com.wynntils.utils.type.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record MountEnclosureInfo(String encodedMountItem, List<Object> encodedMountItems, Time time) {
    private static MountItem decodedMountItem = null;
    private static List<Object> decodedMountItems = null;
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MountEnclosureInfo that = (MountEnclosureInfo) obj;
        return Objects.equals(this.encodedMountItem(), that.encodedMountItem()) && Objects.deepEquals(this.encodedMountItems(), that.encodedMountItems()) &&
                // It's only considered equals if both Time is equal to NONE or not equal to NONE and the offset is below or equal to 50 seconds
                (this.time() == Time.NONE ? that.time() == Time.NONE : that.time() != Time.NONE && this.time().getOffset(that.time()) <= 50);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(encodedMountItem(), encodedMountItems(), time());
    }

    public Optional<MountItem> decodedMountItem()
    {
        if (this.decodedMountItem != null)
            return Optional.of(this.decodedMountItem);
        ErrorOr<WynnItem> errorOrDecodedMountItem =
                Models.ItemEncoding.decodeItem(EncodedByteBuffer.fromBase64String(encodedMountItem()), null);
        if (errorOrDecodedMountItem.hasError())
            return Optional.empty();
        if (errorOrDecodedMountItem.getValue() instanceof MountItem decodedMountItem)
        {
            this.decodedMountItem = decodedMountItem;
            return Optional.of(this.decodedMountItem);
        }
        return Optional.empty();
    }

    public List<Object> decodedMountItems()
    {
        // If it is empty or all elements are MountFood, it don't need decoding
        if (encodedMountItems().isEmpty() || encodedMountItems().stream().allMatch(obj -> obj instanceof MountFood))
            return encodedMountItems();
        if (this.decodedMountItems != null)
            return this.decodedMountItems;
        List<Object> decodedMountItems = new ArrayList<>();
        for (Object obj : encodedMountItems())
        {
            if (obj instanceof String encodedMountItem)
            {
                ErrorOr<WynnItem> errorOrDecodedMountItem =
                        Models.ItemEncoding.decodeItem(EncodedByteBuffer.fromBase64String(encodedMountItem), null);
                if (errorOrDecodedMountItem.hasError())
                    continue;
                if (errorOrDecodedMountItem.getValue() instanceof MountItem decodedMountItem)
                    decodedMountItems.add(decodedMountItem);
            }
            else
                decodedMountItems.add(obj);
        }
        this.decodedMountItems = decodedMountItems;
        return decodedMountItems;
    }
}