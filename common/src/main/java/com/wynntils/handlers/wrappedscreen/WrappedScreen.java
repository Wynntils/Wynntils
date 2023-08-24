/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.wrappedscreen;

import com.wynntils.handlers.wrappedscreen.type.WrappedScreenInfo;

public interface WrappedScreen {
    /**
     * @return The original screen's info that is wrapped.
     */
    WrappedScreenInfo getWrappedScreenInfo();
}
