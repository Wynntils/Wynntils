/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.scoreboard;

import com.wynntils.core.text.StyledText;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;

public final class ScoreboardSegment {
    private final ScoreboardPart scoreboardPart;
    private final StyledText header;
    private final List<StyledText> content;
    private boolean visible = true;

    public ScoreboardSegment(ScoreboardPart scoreboardPart, StyledText header, List<StyledText> content) {
        this.scoreboardPart = scoreboardPart;
        this.header = header;
        this.content = content;
    }

    public ScoreboardPart getScoreboardPart() {
        return scoreboardPart;
    }

    public StyledText getHeader() {
        return header;
    }

    public List<StyledText> getContent() {
        return content;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisibility(boolean visible) {
        this.visible = visible;
    }

    @Override
    public String toString() {
        return "ScoreboardSegment{" + "scoreboardPart="
                + scoreboardPart + ", header='"
                + header + '\'' + ", content="
                + ArrayUtils.toString(content.toArray()) + ", visible="
                + visible + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScoreboardSegment segment = (ScoreboardSegment) o;
        return visible == segment.visible
                && Objects.equals(scoreboardPart, segment.scoreboardPart)
                && Objects.equals(header, segment.header)
                && Objects.deepEquals(content, segment.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scoreboardPart, header, content, visible);
    }
}
