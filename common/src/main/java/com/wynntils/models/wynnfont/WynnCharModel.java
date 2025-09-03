/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.wynnfont;

import com.wynntils.core.components.Model;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.resources.ResourceLocation;

public final class WynnCharModel extends Model {
    private static final Pattern SEGMENT = Pattern.compile("§\\(([^)]*)\\)");
    private static final Pattern TOKEN = Pattern.compile("\\{([^}]*)\\}|.");

    private static final Map<String, WynnCharMapper> WYNN_MAPPERS = new HashMap<>();
    private static final Map<String, String> WYNN_FONT_CODES = new HashMap<>();

    public static final WynnCharMapper NONE = new WynnCharMapper() {
        @Override
        public Optional<String> getToken(int codepoint) {
            return Optional.empty();
        }

        @Override
        public Optional<Integer> getCodePoint(String token) {
            return Optional.empty();
        }
    };

    public WynnCharModel() {
        super(List.of());

        // FIXME: Add a temporary dummy mapping
        registerDefaultMapping();
    }

    private static void registerDefaultMapping() {
        WynnCharMapping defaultMapping = new WynnCharMapping(
                "minecraft:default",
                "d",
                List.of(
                        new WynnCharRange(0xE040, 0xE059, "A"),
                        new WynnCharRange(0xE060, 0xE069, "0"),
                        new WynnCharRange(0xE080, 0xE099, "bg_A")),
                Map.of(
                        0xE017, "champion",
                        0xE026, "+"));

        registerWynnCharMapping(defaultMapping);
        registerWynnCharMapper("minecraft:banner/pill", "b", new WrappingMapper(defaultMapping));
        registerWynnCharMapper("default-wrapped", "w", new WrappingMapper(defaultMapping));
    }

    public static void registerWynnCharMapper(String font, String code, WynnCharMapper mapper) {
        WYNN_MAPPERS.put(font, mapper);
        WYNN_FONT_CODES.put(font, code);
    }

    public static void registerWynnCharMapping(WynnCharMapping mapping) {
        registerWynnCharMapper(mapping.font(), mapping.code(), mapping);
    }

    public WynnCharMapper getMapperFromFontName(String fontName) {
        return WYNN_MAPPERS.getOrDefault(fontName, NONE);
    }

    public String decodeWynnChars(String str, String fontName) {
        WynnCharMapper mapper = getMapperFromFontName(fontName);

        StringBuilder sb = new StringBuilder();
        boolean isEncoding = false;

        for (int codepoint : str.codePoints().toArray()) {
            Optional<String> tokenOpt = mapper.getToken(codepoint);
            if (tokenOpt.isPresent()) {
                if (!isEncoding) {
                    sb.append("§(");
                    isEncoding = true;
                }
                String token = tokenOpt.get();
                if (token.length() > 1) {
                    sb.append("{" + token + "}");
                } else {
                    sb.append(token);
                }
            } else {
                if (isEncoding) {
                    sb.append(")");
                    isEncoding = false;
                }

                sb.appendCodePoint(codepoint);
            }
        }

        if (isEncoding) {
            sb.append(")");
        }

        return sb.toString();
    }

    public String encodeWynnChars(String input, String fontName) {
        WynnCharMapper mapper = getMapperFromFontName(fontName);

        StringBuffer sb = new StringBuffer(input.length());
        Matcher segmentMatcher = SEGMENT.matcher(input);

        while (segmentMatcher.find()) {
            String segment = segmentMatcher.group(1);
            StringBuilder replacement = new StringBuilder(segment.length());
            Matcher tokenMatcher = TOKEN.matcher(segment);

            // Look for next {...} token, or a single character
            while (tokenMatcher.find()) {
                String token = tokenMatcher.group(1) != null ? tokenMatcher.group(1) : tokenMatcher.group(0);
                Optional<Integer> codePointOpt = mapper.getCodePoint(token);
                if (codePointOpt.isPresent()) {
                    replacement.appendCodePoint(codePointOpt.get());
                } else {
                    // Unknown token, ignore it
                }
            }
            segmentMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement.toString()));
        }
        segmentMatcher.appendTail(sb);
        return sb.toString();
    }

    public String getFontCodeFromFont(ResourceLocation font) {
        String fontName = font.toString();
        return WYNN_FONT_CODES.getOrDefault(fontName, fontName);
    }

    public interface WynnCharMapper {
        Optional<String> getToken(int codepoint);

        Optional<Integer> getCodePoint(String token);
    }

    public record WynnCharRange(int start, int end, String base) {}

    public record WynnCharMapping(String font, String code, List<WynnCharRange> ranges, Map<Integer, String> map)
            implements WynnCharMapper {
        @Override
        public Optional<String> getToken(int codepoint) {
            for (WynnCharRange range : ranges()) {
                if (codepoint >= range.start && codepoint <= range.end) {
                    StringBuilder sb = new StringBuilder();
                    // If the base is more than one character, keep the prefix and just bump
                    // the last character.
                    sb.append(range.base.substring(0, range.base.length() - 1));

                    char lastChar = range.base.charAt(range.base.length() - 1);
                    sb.append((char) (lastChar + codepoint - range.start));

                    return Optional.of(sb.toString());
                }
            }
            String mappedChar = map.get(codepoint);
            if (mappedChar != null) {
                return Optional.of(mappedChar);
            }

            return Optional.empty();
        }

        @Override
        public Optional<Integer> getCodePoint(String token) {
            // First try ranges
            for (WynnCharRange range : ranges()) {
                String base = range.base();
                int baseLen = base.length();
                // If the lengths are not the same, skip
                if (token.length() != baseLen) continue;

                if (baseLen > 1) {
                    // If we have a prefix, it must match
                    if (!token.regionMatches(0, base, 0, baseLen - 1)) continue;
                }

                int lastTok = token.charAt(baseLen - 1);
                int lastBase = base.charAt(baseLen - 1);
                int candidate = range.start() + (lastTok - lastBase);
                // If the candidate is in range, we have found a match
                if (candidate >= range.start() && candidate <= range.end()) {
                    return Optional.of(candidate);
                }
            }

            // Then try individual mappings
            return map().entrySet().stream()
                    .filter(e -> token.equals(e.getValue()))
                    .map(Map.Entry::getKey)
                    .findFirst();
        }
    }

    public static final class WrappingMapper implements WynnCharMapper {
        private final WynnCharMapper delegate;

        public WrappingMapper(WynnCharMapper delegate) {
            this.delegate = delegate;
        }

        @Override
        public Optional<String> getToken(int codepoint) {
            Optional<String> delegateToken = delegate.getToken(codepoint);
            if (delegateToken.isPresent()) return delegateToken;

            if (0 <= codepoint && codepoint <= 0x00FF) return Optional.empty();

            return Optional.of(String.format(Locale.ROOT, "+%04X", codepoint));
        }

        @Override
        public Optional<Integer> getCodePoint(String token) {
            Optional<Integer> delegateCodePoint = delegate.getCodePoint(token);
            if (delegateCodePoint.isPresent()) return delegateCodePoint;

            if (token.length() <= 1 || token.charAt(0) != '+') return Optional.empty();

            try {
                int v = Integer.parseInt(token.substring(1), 16);
                if (v < 0 || v > Character.MAX_CODE_POINT) return Optional.empty();

                return Optional.of(v);
            } catch (NumberFormatException ignored) {
                return Optional.empty();
            }
        }
    }
}
