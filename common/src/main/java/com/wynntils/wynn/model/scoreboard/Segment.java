/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.wynn.model.scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Segment {
    private final ScoreboardModel.SegmentType type;
    private final String header;
    private List<String> content = null;

    public Segment(ScoreboardModel.SegmentType type, String header) {
        this.type = type;
        this.header = header;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Segment segment = (Segment) obj;

        if (type != segment.getType()) return false;
        if (!Objects.equals(header, segment.getHeader())) return false;
        return Objects.equals(content, segment.getContent());
    }

    public List<String> getContent() {
        return content;
    }

    public ScoreboardModel.SegmentType getType() {
        return type;
    }

    public String getHeader() {
        return header;
    }

    public List<String> getScoreboardLines() {
        List<String> lines = new ArrayList<>();
        lines.add(this.header);
        lines.addAll(this.content);

        return lines;
    }

    public void setContent(List<String> content) {
        this.content = content;
    }
}
