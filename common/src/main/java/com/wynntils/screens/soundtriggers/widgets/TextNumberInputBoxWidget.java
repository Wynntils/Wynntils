/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.screens.soundtriggers.widgets;

import com.wynntils.core.WynntilsMod;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import java.util.function.Consumer;
import net.minecraft.client.input.CharacterEvent;

class TextNumberInputBoxWidget extends TextInputBoxWidget {
    TextNumberInputBoxWidget(
            int x, int y, int width, int height, Consumer<String> onUpdateConsumer, TextboxScreen textboxScreen) {
        super(x, y, width, height, onUpdateConsumer, textboxScreen);
        this.visible = false;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        WynntilsMod.error(String.valueOf(event.codepoint()));
        WynntilsMod.warn(event.codepointAsString());
        if (!(event.codepoint() >= 48 && event.codepoint() <= 57)) return false;
        return super.charTyped(event);
    }
}
