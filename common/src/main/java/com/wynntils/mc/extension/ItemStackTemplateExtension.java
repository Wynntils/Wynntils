/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.extension;

import com.wynntils.models.items.FakeItemStack;

public interface ItemStackTemplateExtension {
    FakeItemStack getFakeItemStack();

    void setFakeItemStack(FakeItemStack fakeItemStack);
}
