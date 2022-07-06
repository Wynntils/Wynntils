/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wc.utils.scoreboard;

import java.util.List;

public final class Segment {
    private final ScoreboardManager.SegmentType type;
    private final String header;
    private List<String> content = null;
    private final int startIndex;
    private int endIndex = -1;

    private boolean changed;

    public Segment(ScoreboardManager.SegmentType type, String header, int startIndex) {
        this.type = type;
        this.header = header;
        this.startIndex = startIndex;
        this.changed = false;
    }

    @Override
    public String toString() {
        return "Segment[" + "type="
                + type + ", " + "header="
                + header + ", " + "content="
                + content + ", " + "startIndex="
                + startIndex + ", " + "endIndex="
                + endIndex + ']';
    }

    public List<String> getContent() {
        return content;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public ScoreboardManager.SegmentType getType() {
        return type;
    }

    public String getHeader() {
        return header;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }
}
