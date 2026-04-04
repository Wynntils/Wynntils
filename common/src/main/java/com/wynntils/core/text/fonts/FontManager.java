/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.text.fonts;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.json.JsonManager;
import com.wynntils.core.text.FontLookup;
import com.wynntils.core.text.fonts.wynnfonts.FancyFont;
import com.wynntils.core.text.fonts.wynnfonts.WynncraftKeybindsFont;
import com.wynntils.core.text.fonts.wynnfonts.WynntilsKeybindsFont;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

public final class FontManager extends Manager {
    private static final String FONTMAP_RESOURCE_LOCATION = "fontmaps/";
    private final List<RegisteredFont> registeredFonts = new ArrayList<>();
    private final Map<Class<? extends RegisteredFont>, RegisteredFont> fontsByClass = new HashMap<>();
    private final Map<String, FontEntry> fonts = new HashMap<>();

    public FontManager() {
        super(List.of());

        registerFontsForLookup();
        registerFonts();
        registerFontMaps();
    }

    private void registerFonts() {
        registerFont(new FancyFont());
        registerFont(new WynncraftKeybindsFont());
        registerFont(new WynntilsKeybindsFont());
    }

    private void registerFont(RegisteredFont font) {
        registeredFonts.add(font);
        fontsByClass.put(font.getClass(), font);
    }

    private void registerWynnFont(String fontName, String code) {
        FontLookup.registerFontCode(new FontDescription.Resource(Identifier.tryParse(fontName)), code);
    }

    private void registerFontMaps() {
        for (RegisteredFont registeredFont : registeredFonts) {
            String key = registeredFont.key();
            String path = FONTMAP_RESOURCE_LOCATION + key + ".json";
            InputStream resource = WynntilsMod.getModResourceAsStream(path);

            if (resource == null) {
                WynntilsMod.warn("Missing fontmap: " + path);
                return;
            }

            try (InputStreamReader reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
                JsonObject root = parseFontMap(reader, path);
                if (root == null) {
                    return;
                }

                String fontId = readFontId(root, path);
                if (fontId == null) {
                    return;
                }

                Map<String, String> glyphs = readGlyphs(root);
                registerFontMap(registeredFont, fontId, glyphs);
            } catch (Exception e) {
                WynntilsMod.error("Failed to load fontmap: " + path, e);
            }
        }
    }

    private JsonObject parseFontMap(InputStreamReader reader, String path) {
        JsonObject root = JsonManager.GSON.fromJson(reader, JsonObject.class);
        if (root == null) {
            WynntilsMod.warn("Invalid fontmap json: " + path);
            return null;
        }
        return root;
    }

    private String readFontId(JsonObject root, String path) {
        String fontId = root.has("fontId") ? root.get("fontId").getAsString() : null;
        if (fontId == null || fontId.isBlank()) {
            WynntilsMod.warn("Missing fontId in fontmap: " + path);
            return null;
        }
        return fontId;
    }

    private Map<String, String> readGlyphs(JsonObject root) {
        if (!root.has("glyphs") || !root.get("glyphs").isJsonObject()) {
            return Map.of();
        }

        Type type = new TypeToken<Map<String, String>>() {}.getType();
        Map<String, String> parsed = JsonManager.GSON.fromJson(root.getAsJsonObject("glyphs"), type);
        return parsed != null ? parsed : Map.of();
    }

    private void registerFontMap(RegisteredFont registeredFont, String fontId, Map<String, String> glyphs) {
        Identifier identifier = Identifier.tryParse(fontId);
        FontDescription.Resource resource = new FontDescription.Resource(identifier);
        FontLookup.registerFontCode(resource, registeredFont.key());
        FontEntry fontEntry = new FontEntry(registeredFont.key(), fontId, glyphs);
        fonts.put(registeredFont.key(), fontEntry);
        registeredFont.setFontEntry(fontEntry);
    }

    public Map<String, FontEntry> getFonts() {
        return fonts;
    }

    public FontEntry getFontEntry(String fontKey) {
        return fonts.get(fontKey);
    }

    public <T extends RegisteredFont> FontEntry getFontEntry(Class<T> fontClass) {
        RegisteredFont font = fontsByClass.get(fontClass);
        return font != null ? font.fontEntry() : null;
    }

    private void registerFontsForLookup() {
        registerWynnFont("minecraft:default", "d");
        registerWynnFont("minecraft:banner/pill", "bp");
        registerWynnFont("minecraft:chat/prefix", "cp");
        registerWynnFont("minecraft:hud/gameplay/default/top_left", "gtl");
        registerWynnFont("minecraft:hud/gameplay/default/top_middle", "gtm");
        registerWynnFont("minecraft:hud/gameplay/default/top_right", "gtr");
        registerWynnFont("minecraft:hud/gameplay/default/center_left", "gcl");
        registerWynnFont("minecraft:hud/gameplay/default/center_middle", "gcm");
        registerWynnFont("minecraft:hud/gameplay/default/center_right", "gcr");
        registerWynnFont("minecraft:hud/gameplay/default/bottom_left", "gbl");
        registerWynnFont("minecraft:hud/gameplay/default/bottom_middle", "gbm");
        registerWynnFont("minecraft:hud/gameplay/default/bottom_right", "gbr");
        registerWynnFont("minecraft:hud/selector/default/top_left", "stl");
        registerWynnFont("minecraft:hud/selector/default/top_middle", "stm");
        registerWynnFont("minecraft:hud/selector/default/top_right", "str");
        registerWynnFont("minecraft:hud/selector/default/center_left", "scl");
        registerWynnFont("minecraft:hud/selector/default/center_middle", "scm");
        registerWynnFont("minecraft:hud/selector/default/center_right", "scr");
        registerWynnFont("minecraft:hud/selector/default/bottom_left", "sbl");
        registerWynnFont("minecraft:hud/selector/default/bottom_middle", "sbm");
        registerWynnFont("minecraft:hud/selector/default/bottom_right", "sbr");
    }
}
