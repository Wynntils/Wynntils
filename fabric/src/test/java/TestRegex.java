/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.wynntils.features.ui.BulkBuyFeature;
import com.wynntils.handlers.actionbar.ActionBarHandler;
import com.wynntils.handlers.chat.ChatHandler;
import com.wynntils.models.character.CharacterModel;
import com.wynntils.models.character.CharacterSelectionModel;
import com.wynntils.models.items.annotators.gui.AbilityTreeAnnotator;
import com.wynntils.models.items.annotators.gui.ArchetypeAbilitiesAnnotator;
import java.lang.reflect.Field;
import java.util.regex.Pattern;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestRegex {
    @BeforeAll
    public static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    public static final class PatternTester {
        private final Class<?> clazz;
        private final String fieldName;
        private final Pattern pattern;

        public PatternTester(Class<?> clazz, String fieldName) {
            this.clazz = clazz;
            this.fieldName = fieldName;
            Pattern pattern = null;

            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                pattern = (Pattern) field.get(null);
            } catch (NoSuchFieldException e) {
                Assertions.fail("Pattern field " + clazz.getSimpleName() + "." + fieldName + " does not exist");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            this.pattern = pattern;
        }

        public void shouldMatch(String s) {
            Assertions.assertTrue(
                    pattern.matcher(s).matches(),
                    "Regex failure: " + clazz.getSimpleName() + "." + fieldName + " should have matched " + s
                            + ", but it did not.");
        }

        public void shouldNotMatch(String s) {
            Assertions.assertFalse(
                    pattern.matcher(s).matches(),
                    "Regex failure: " + clazz.getSimpleName() + "." + fieldName + " should NOT have matched " + s
                            + ", but it did.");
        }
    }

    @Test
    public void AbilityTreeAnnotator_TREE_ABILITY_POINTS_PATTERN() {
        PatternTester p = new PatternTester(AbilityTreeAnnotator.class, "TREE_ABILITY_POINTS_PATTERN");
        p.shouldMatch("§b✦ Available Points: §f0§7/45");
        p.shouldMatch("§b✦ Available Points: §f15§7/45");
    }

    @Test
    public void ActionBarHandler_ACTIONBAR_PATTERN() {
        PatternTester p = new PatternTester(ActionBarHandler.class, "ACTIONBAR_PATTERN");
        p.shouldMatch("§c❤ 14930/14930§0      §b❉ 100%      ✺ 175/175");
        p.shouldMatch("§c❤ 14930/14930§0      §7❉ 48%      §b✺ 175/175");
        p.shouldMatch("§c❤ 14930/14930§0      §7❉ 48%      §b✺ 175/175");
    }

    @Test
    public void ArchetypeAbilitiesAnnotator_ARCHETYPE_NAME() {
        PatternTester p = new PatternTester(ArchetypeAbilitiesAnnotator.class, "ARCHETYPE_NAME");
        p.shouldMatch("§e§lBoltslinger Archetype");
        p.shouldMatch("§d§lSharpshooter Archetype");
        p.shouldMatch("§2§lTrapper Archetype");
        p.shouldMatch("§d§lLight Bender Archetype");
    }

    @Test
    public void ArchetypeAbilitiesAnnotator_ARCHETYPE_PATTERN() {
        PatternTester p = new PatternTester(ArchetypeAbilitiesAnnotator.class, "ARCHETYPE_PATTERN");
        p.shouldMatch("§a✔ §7Unlocked Abilities: §f14§7/15");
        p.shouldMatch("§a✔ §7Unlocked Abilities: §f2§7/16");
        p.shouldMatch("§a✔ §7Unlocked Abilities: §f2§7/16");
        p.shouldMatch("§a✔ §7Unlocked Abilities: §f5§7/16");
        p.shouldMatch("§a✔ §7Unlocked Abilities: §f14§7/15");
        p.shouldMatch("§a✔ §7Unlocked Abilities: §f0§7/15");
    }

    @Test
    public void BulkBuyFeature_PRICE_PATTERN() {
        PatternTester p = new PatternTester(BulkBuyFeature.class, "PRICE_PATTERN");
        p.shouldMatch("§6 - §a✔ §f24§7²");
        p.shouldMatch("§6 - §a✔ §f648§7²");
        p.shouldMatch("§6 - §c✖ §f24§7²");
    }

    @Test
    public void CharacterModel_SILVERBULL_PATTERN() {
        PatternTester p = new PatternTester(CharacterModel.class, "SILVERBULL_PATTERN");
        p.shouldMatch("§7Subscription: §c✖ Inactive");
        p.shouldMatch("§7Subscription: §a✔ Active");
    }

    @Test
    public void CharacterModel_SILVERBULL_DURATION_PATTERN() {
        PatternTester p = new PatternTester(CharacterModel.class, "SILVERBULL_DURATION_PATTERN");
        p.shouldMatch("§7Expiration: §f1 week 5 days");
        p.shouldMatch("§7Expiration: §f5 days");
        p.shouldMatch("§7Expiration: §f1 week");
        p.shouldMatch("§7Expiration: §f2 days 12 hours");
    }

    @Test
    public void CharacterModel_VETERAN_PATTERN() {
        PatternTester p = new PatternTester(CharacterModel.class, "VETERAN_PATTERN");
        p.shouldMatch("§7Rank: §6Vet"); // Champion
        p.shouldMatch("§7Rank: §dVet"); // Hero
        p.shouldMatch("§7Rank: §bVet"); // VIP+
        p.shouldMatch("§7Rank: §aVet"); // VIP
    }

    @Test
    public void CharacterSelectionModel_CLASS_ITEM_CLASS_PATTERN() {
        PatternTester p = new PatternTester(CharacterSelectionModel.class, "CLASS_ITEM_CLASS_PATTERN");
        p.shouldMatch("§e- §7Class: §fHunter"); // Hunter
        p.shouldMatch("§e- §7Class: §fMage"); // Mage
        p.shouldMatch("§e- §7Class: §3\uE026§r §fDark Wizard"); // Craftsman Dark Wizard
        p.shouldMatch("§e- §7Class: §c\uE027§r §fAssassin"); // Hardcore Assassin
        p.shouldMatch("§e- §7Class: §5\uE028§r §fNinja"); // Hunted Ninja
        p.shouldMatch("§e- §7Class: §b\uE083§r §fShaman"); // Ultimate Ironman Shaman
        p.shouldMatch("§e- §7Class: §c\uE027§b\uE083§3\uE026§5\uE028§r §fWarrior"); // Ultimate HIC Warrior
        p.shouldMatch("§e- §7Class: §c\uE027§6\uE029§3\uE026§5\uE028§r §fSkyseer"); // HIC Skyseer
        p.shouldMatch("§e- §7Class: §6\uE029§r §fArcher"); // Ironman Archer
    }

    @Test
    public void ChatHandler_NPC_CONFIRM_PATTERN() {
        PatternTester p = new PatternTester(ChatHandler.class, "NPC_CONFIRM_PATTERN");
        p.shouldMatch("§7Press §fSHIFT §7to continue");
        p.shouldMatch("§4Press §cSNEAK §4to continue");
    }

    @Test
    public void ChatHandler_NPC_SELECT_PATTERN() {
        PatternTester p = new PatternTester(ChatHandler.class, "NPC_SELECT_PATTERN");
        p.shouldMatch("§7Select §fan option §7to continue");
        p.shouldMatch("§cCLICK §4an option to continue");
    }
}
