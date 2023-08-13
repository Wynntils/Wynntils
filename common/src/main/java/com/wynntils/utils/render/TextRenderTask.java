/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render;

import com.wynntils.core.text.StyledText;

public final class TextRenderTask {
    private StyledText text;
    private TextRenderSetting setting;

    public TextRenderTask(StyledText codedText, TextRenderSetting setting) {
        this.text = codedText;
        this.setting = setting;
    }

    public TextRenderTask(String text, TextRenderSetting setting) {
        this.text = StyledText.fromString(text);
        this.setting = setting;
    }

    public StyledText getText() {
        return text;
    }

    public TextRenderSetting getSetting() {
        return setting;
    }

    public TextRenderTask setSetting(TextRenderSetting setting) {
        this.setting = setting;
        return this;
    }

    public void setText(StyledText codedText) {
        this.text = codedText;
    }

    public void setText(String text) {
        this.text = StyledText.fromString(text);
    }

    @Override
    public String toString() {
        return "TextRenderTask[" + "text=" + text + ", " + "setting=" + setting + ']';
    }
}
