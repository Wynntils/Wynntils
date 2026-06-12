/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.type.StyleType;
import com.wynntils.models.statuseffects.StatusEffectModel;
import com.wynntils.models.statuseffects.type.StatusEffect;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestStatusEffectParsing {
    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    public void parseStatusEffectsFromComponentJson() {
        StyledText footer = StyledText.fromComponent(componentFromJson(STATUS_EFFECT_LIST_1_JSON));

        List<StatusEffect> effects = StatusEffectModel.parseStatusEffects(footer);

        Assertions.assertEquals(23, effects.size());
        assertEffect(effects.getFirst(), "Ⓔ", "+22", "", "󏿿󐀂", "Intelligence", "(03:28)", 208);
        assertEffect(effects.get(1), "Ⓔ", "+40", "%", "", "Spell Damage", "(03:28)", 208);
        assertEffect(effects.get(2), "Ⓔ", "+28", "", "󐀂", "Agility", "(03:43)", 223);
        assertEffect(effects.get(14), "Ⓔ", "+29", "", "󏿿󐀁󐀂", "Defence", "(03:56)", 236);
        assertEffect(effects.get(15), "Ⓔ", "+3,050", "", "", "Health", "(03:56)", 236);
        assertEffect(effects.get(16), "Ⓔ", "+12", "/3s", "", "Mana Steal", "(06:08)", 368);
        assertEffect(effects.get(17), "Ⓔ", "+31", "/5s", "", "Mana Regen", "(06:08)", 368);
        assertEffect(effects.get(18), "⚔", "", "", "", "Vengeful Spirit", "(00:00)", 0);
        assertEffect(effects.get(19), "❤", "10599", "", "", "Extra Health", "(**:**)", -1);
        assertEffect(effects.get(20), "❁", "20", "%", "", "Resistance", "(00:28)", 28);
        assertEffect(effects.get(21), "➲", "", "", "", "Windy Feet", "(01:09)", 69);
        assertEffect(effects.get(22), "☗", "", "", "", "Guardian", "(**:**)", -1);

        Assertions.assertEquals("§fⒺ", effects.getFirst().getPrefix().getString(StyleType.INCLUDE_FONTS));
        Assertions.assertEquals(
                "§7§{fr:minecraft:tooltip/attribute/sprite}󏿿󐀂",
                effects.getFirst().getNameIcon().getString(StyleType.INCLUDE_FONTS));
        Assertions.assertEquals("§7Intelligence", effects.getFirst().getName().getString(StyleType.INCLUDE_FONTS));
        Assertions.assertEquals(
                "§8(03:28)", effects.getFirst().getDisplayedTime().getString(StyleType.INCLUDE_FONTS));
        Assertions.assertEquals("§4(00:00)", effects.get(18).getDisplayedTime().getString(StyleType.INCLUDE_FONTS));
    }

    @Test
    public void parsePrefixlessInfiniteStatusEffectFromComponentJson() {
        StyledText footer = StyledText.fromComponent(componentFromJson(SINGLE_EFFECT_NO_PREFIX_JSON));

        List<StatusEffect> effects = StatusEffectModel.parseStatusEffects(footer);

        Assertions.assertEquals(1, effects.size());
        assertEffect(effects.getFirst(), "", "", "", "", "Disabled Jump", "(**:**)", -1);
        Assertions.assertEquals("§7Disabled Jump", effects.getFirst().getName().getString(StyleType.INCLUDE_FONTS));
        Assertions.assertEquals(
                "§8(**:**)", effects.getFirst().getDisplayedTime().getString(StyleType.INCLUDE_FONTS));
    }

    @Test
    public void parseCustomFontPrefixAndTimerStylesFromComponentJson() {
        StyledText footer = StyledText.fromComponent(componentFromJson(FONT_PREFIX_JSON));

        List<StatusEffect> effects = StatusEffectModel.parseStatusEffects(footer);

        Assertions.assertEquals(3, effects.size());
        assertEffect(effects.getFirst(), "", "8", "%", "", "Strength", "(00:21)", 21);
        assertEffect(effects.get(1), "❁", "25", "%", "", "Resistance", "(00:21)", 21);
        assertEffect(effects.get(2), "⬤", "", "", "", "Shield", "(00:11)", 11);
        Assertions.assertEquals(
                "§a§{fr:minecraft:common}", effects.getFirst().getPrefix().getString(StyleType.INCLUDE_FONTS));
        Assertions.assertEquals("§a(00:11)", effects.get(2).getDisplayedTime().getString(StyleType.INCLUDE_FONTS));
    }

    @Test
    public void parseEffectsWithoutPrefixAndWithCustomFontPrefixFromComponentJson() {
        StyledText footer = StyledText.fromComponent(componentFromJson(STATUS_EFFECT_LIST_2_JSON));

        List<StatusEffect> effects = StatusEffectModel.parseStatusEffects(footer);

        Assertions.assertEquals(5, effects.size());
        assertEffect(effects.getFirst(), "❤", "", "", "", "Corrupted", "(01:54)", 114);
        assertEffect(effects.get(1), "", "5", "", "", "Harmony", "(00:05)", 5);
        assertEffect(effects.get(2), "❋", "78", "%", "", "Dodge", "(00:19)", 19);
        assertEffect(effects.get(3), "", "", "", "", "Distorted", "(00:03)", 3);
        assertEffect(effects.get(4), "⚔", "", "", "", "Vengeful Spirit", "(00:00)", 0);
        Assertions.assertEquals("§bDistorted", effects.get(3).getName().getString(StyleType.INCLUDE_FONTS));
        Assertions.assertEquals(
                "§7§{fr:minecraft:common}", effects.get(1).getPrefix().getString(StyleType.INCLUDE_FONTS));
    }

    private static Component componentFromJson(String json) {
        JsonElement jsonElement = JsonParser.parseString(json);
        return ComponentSerialization.CODEC
                .parse(JsonOps.INSTANCE, jsonElement)
                .result()
                .orElseThrow(() -> new AssertionError("Unable to parse component json."));
    }

    private static void assertEffect(
            StatusEffect effect,
            String prefix,
            String modifier,
            String modifierSuffix,
            String icon,
            String name,
            String displayedTime,
            int duration) {
        Assertions.assertEquals(prefix, effect.getPrefix().getStringWithoutFormatting());
        Assertions.assertEquals(modifier, effect.getModifier().getStringWithoutFormatting());
        Assertions.assertEquals(modifierSuffix, effect.getModifierSuffix().getStringWithoutFormatting());
        Assertions.assertEquals(icon, effect.getNameIcon().getStringWithoutFormatting());
        Assertions.assertEquals(name, effect.getName().getStringWithoutFormatting());
        Assertions.assertEquals(displayedTime, effect.getDisplayedTime().getStringWithoutFormatting());
        Assertions.assertEquals(duration, effect.getDuration());
    }

    private static final String STATUS_EFFECT_LIST_1_JSON = """
            {"text":"","extra":[
            {"text":"Status Effects","color":"#FF55FF","bold":true,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"\n ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Ⓔ","color":"#FFFFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":" +22 ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"󏿿󐀂","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:tooltip/attribute/sprite"},{"text":"Intelligence ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(03:28)  ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Ⓔ","color":"#FFFFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":" +40% Spell Damage ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(03:28)  ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Ⓔ","color":"#FFFFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":" +28 ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"󐀂","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:tooltip/attribute/sprite"},{"text":"Agility ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(03:43) \n ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Ⓔ","color":"#FFFFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":" +44% Walk Speed ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(03:43)  ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Ⓔ","color":"#FFFFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":" +118 Spell Damage ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(05:48)  ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Ⓔ","color":"#FFFFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":" +39 ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"󐀂","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:tooltip/attribute/sprite"},{"text":"Dexterity ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(02:52) \n ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Ⓔ","color":"#FFFFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":" +30 ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"󏿿󐀂","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:tooltip/attribute/sprite"},{"text":"Strength ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(03:54)  ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Ⓔ","color":"#FFFFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":" +36% Main Attack Damage ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(03:54)  ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Ⓔ","color":"#FFFFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":" +67% Earth Damage ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(08:24) \n ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Ⓔ","color":"#FFFFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":" +66% Thunder Damage ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(08:24)  ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Ⓔ","color":"#FFFFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":" +65% Water Damage ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(08:24)  ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Ⓔ","color":"#FFFFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":" +70% Fire Damage ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(08:24) \n ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Ⓔ","color":"#FFFFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":" +64% Air Damage ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(08:24)  ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Ⓔ","color":"#FFFFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":" +263/3s Life Steal ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(03:47)  ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Ⓔ","color":"#FFFFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":" +29 ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"󏿿󐀁󐀂","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:tooltip/attribute/sprite"},{"text":"Defence ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(03:56) \n ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Ⓔ","color":"#FFFFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":" +3,050 Health ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(03:56)  ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Ⓔ","color":"#FFFFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":" +12/3s Mana Steal ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(06:08)  ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Ⓔ","color":"#FFFFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":" +31/5s Mana Regen ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(06:08) \n ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"⚔ ","color":"#55FF55","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"Vengeful Spirit ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(00:00)  ","color":"#AA0000","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"❤ ","color":"#FFAA00","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"10599 Extra Health ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(**:**)  ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"❁ ","color":"#55FF55","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"20% Resistance ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(00:28) \n ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"➲ ","color":"#55FF55","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"Windy Feet ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(01:09)    ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"☗ ","color":"#55FF55","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"Guardian ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},{"text":"(**:**)                            ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"}
            ]}
            """;

    private static final String STATUS_EFFECT_LIST_2_JSON = """
            {"text":"","extra":[
            {"text":"Status Effects","color":"#FF55FF","bold":true,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"\n ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"❤","color":"#55FFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":" Corrupted ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"(01:54)  ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:common"},
            {"text":" ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"5 Harmony ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"(00:05)  ","color":"#AA0000","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"❋ ","color":"#55FF55","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"78% Dodge ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"(00:19) \n  ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Distorted ","color":"#55FFFF","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"(00:03)   ","color":"#AA0000","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"⚔ ","color":"#55FF55","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Vengeful Spirit ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"(00:00)                          ","color":"#AA0000","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"}
            ]}
            """;

    private static final String SINGLE_EFFECT_NO_PREFIX_JSON = """
            {"text":"","extra":[
            {"text":"Status Effects","color":"#FF55FF","bold":true,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"\n ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":" ","color":"#55FF55","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Disabled Jump ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"(**:**)                                                   ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"}
            ]}
            """;

    private static final String FONT_PREFIX_JSON = """
            {"text":"","extra":[
            {"text":"Status Effects","color":"#FF55FF","bold":true,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"\n ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"","color":"#55FF55","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:common"},
            {"text":" ","color":"#55FF55","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"8% Strength ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"(00:21)  ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"❁ ","color":"#55FF55","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"25% Resistance ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"(00:21)    ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"⬤ ","color":"#555555","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"Shield ","color":"#AAAAAA","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"},
            {"text":"(00:11)   ","color":"#55FF55","bold":false,"italic":false,"underlined":false,"strikethrough":false,"obfuscated":false,"font":"minecraft:default"}
            ]}
            """;
}
