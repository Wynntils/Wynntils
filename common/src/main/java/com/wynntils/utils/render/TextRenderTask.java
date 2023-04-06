/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.render;

import com.wynntils.core.text.StyledText2;

public final class TextRenderTask {
    private StyledText2 text;
    private TextRenderSetting setting;

    public TextRenderTask(StyledText2 codedText, TextRenderSetting setting) {
        this.text = codedText;
        this.setting = setting;
    }

    public TextRenderTask(String text, TextRenderSetting setting) {
        this.text = new StyledText2(text);
        this.setting = setting;
    }

    public StyledText2 getText() {
        return text;
    }

    public TextRenderSetting getSetting() {
        return setting;
    }

    public TextRenderTask setSetting(TextRenderSetting setting) {
        this.setting = setting;
        return this;
    }

    public void setText(StyledText2 codedText) {
        this.text = codedText;
    }

    public void setText(String text) {
        this.text = StyledText2.of(text);
    }

    @Override
    public String toString() {
        return "TextRenderTask[" + "text=" + text + ", " + "setting=" + setting + ']';
    }
}
