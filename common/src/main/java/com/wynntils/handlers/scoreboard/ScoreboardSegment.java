/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.scoreboard;

import com.wynntils.handlers.scoreboard.type.SegmentMatcher;
import com.wynntils.utils.mc.type.StyledText;
import java.util.ArrayList;
import java.util.List;

public final class ScoreboardSegment {
    private final SegmentMatcher matcher;
    private final StyledText header;
    private final int startIndex;

    private String end;
    private List<StyledText> content = null;
    private int endIndex = -1;
    private boolean changed;

    public ScoreboardSegment(SegmentMatcher matcher, StyledText header, int startIndex) {
        this.matcher = matcher;
        this.header = header;
        this.startIndex = startIndex;
        this.changed = false;
    }

    @Override
    public String toString() {
        return "Segment[" + "matcher="
                + matcher + ", " + "header="
                + header + ", " + "content="
                + content + ", " + "startIndex="
                + startIndex + ", " + "endIndex="
                + endIndex + ']';
    }

    public List<StyledText> getContent() {
        return content;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public SegmentMatcher getMatcher() {
        return matcher;
    }

    public StyledText getHeader() {
        return header;
    }

    public String getEnd() {
        return end;
    }

    public List<StyledText> getScoreboardLines() {
        List<StyledText> lines = new ArrayList<>(this.content);
        lines.add(this.header);
        if (this.end != null) {
            // FIXME: Note that end is without formatting!
            lines.add(StyledText.of(this.end));
        }

        return lines;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setContent(List<StyledText> content) {
        this.content = content;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}
