/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.wynntils.features.chat.GuildRankReplacementFeature;
import com.wynntils.features.chat.MessageFilterFeature;
import com.wynntils.features.redirects.ChatRedirectFeature;
import com.wynntils.features.ui.BulkBuyFeature;
import com.wynntils.handlers.actionbar.ActionBarHandler;
import com.wynntils.handlers.chat.ChatHandler;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.models.character.CharacterModel;
import com.wynntils.models.character.CharacterSelectionModel;
import com.wynntils.models.characterstats.actionbar.CoordinatesSegment;
import com.wynntils.models.characterstats.actionbar.ManaSegment;
import com.wynntils.models.characterstats.actionbar.PowderSpecialSegment;
import com.wynntils.models.containers.ContainerModel;
import com.wynntils.models.damage.DamageModel;
import com.wynntils.models.items.annotators.game.IngredientAnnotator;
import com.wynntils.models.items.annotators.gui.AbilityTreeAnnotator;
import com.wynntils.models.items.annotators.gui.ArchetypeAbilitiesAnnotator;
import com.wynntils.models.players.FriendsModel;
import com.wynntils.models.players.GuildModel;
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
        private final String regexName;
        private final Pattern pattern;

        private PatternTester(String regexName, Class<?> clazz, Object obj, String fieldName) {
            this.regexName = regexName;
            Pattern pattern = null;

            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                pattern = (Pattern) field.get(obj);
            } catch (NoSuchFieldException e) {
                Assertions.fail("Pattern field " + regexName + " does not exist");
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            this.pattern = pattern;
        }

        public PatternTester(Class<?> clazz, String fieldName) {
            this(clazz.getSimpleName() + "." + fieldName, clazz, null, fieldName);
        }

        public PatternTester(Enum<?> enumType, String fieldName) {
            this(
                    enumType.getDeclaringClass().getSimpleName() + "." + enumType.name() + "." + fieldName,
                    enumType.getDeclaringClass(),
                    enumType,
                    fieldName);
        }

        public void shouldMatch(String s) {
            Assertions.assertTrue(
                    pattern.matcher(s).matches(),
                    "Regex failure: " + regexName + " should have matched " + s + ", but it did not.");
        }

        public void shouldNotMatch(String s) {
            Assertions.assertFalse(
                    pattern.matcher(s).matches(),
                    "Regex failure: " + regexName + " should NOT have matched " + s + ", but it did.");
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

    @Test
    public void ChatRedirectFeature_LoginRedirector_FOREGROUND_PATTERN() {
        PatternTester p = new PatternTester(ChatRedirectFeature.LoginRedirector.class, "FOREGROUND_PATTERN");
        p.shouldMatch("\uE017 §#ffe60000v8j§6 has just logged in!"); // champion
        p.shouldMatch("\uE01B §#a344aa00v8j§d has just logged in!"); // hero
        p.shouldMatch("\uE024 §#8a99ee00v8j§3 has just logged in!"); // vip+
        p.shouldMatch("\uE023 §#44aa3300v8j§a has just logged in!"); // vip
        p.shouldMatch("\uE017 §#ffe60000§ocharlie268IsAWizard§6 has just logged in!"); // champion nickname
    }

    @Test
    public void ContainerModel_ABILITY_TREE_PATTERN() {
        PatternTester p = new PatternTester(ContainerModel.class, "ABILITY_TREE_PATTERN");
        p.shouldMatch("Warrior Abilities"); // Warrior
        p.shouldMatch("Shaman Abilities"); // Shaman
        p.shouldMatch("Mage Abilities"); // Mage
        p.shouldMatch("Assassin Abilities"); // Assassin
        p.shouldMatch("Archer Abilities"); // Archer
    }

    @Test
    public void ContainerModel_GUILD_BANK_PATTERN() {
        PatternTester p = new PatternTester(ContainerModel.class, "GUILD_BANK_PATTERN");
        p.shouldMatch("Very Cool Guild Name: Bank (Everyone)");
        p.shouldMatch("Other very cool guild name: Bank (High Ranked)");
    }

    @Test
    public void ContainerModel_LOOT_CHEST_PATTERN() {
        PatternTester p = new PatternTester(ContainerModel.class, "LOOT_CHEST_PATTERN");
        p.shouldMatch("Loot Chest §7[§f✫§8✫✫✫§7]"); // Tier 1
        p.shouldMatch("Loot Chest §e[§6✫✫§8✫✫§e]"); // Tier 2
        p.shouldMatch("Loot Chest §5[§d✫✫✫§8✫§5]"); // Tier 3
        p.shouldMatch("Loot Chest §3[§b✫✫✫✫§3]"); // Tier 4
    }

    @Test
    public void ContainerModel_PERSONAL_STORAGE_PATTERN() {
        PatternTester p = new PatternTester(ContainerModel.class, "PERSONAL_STORAGE_PATTERN");
        p.shouldMatch("§0[Pg. 1] §8v8j's§0 Bank");
        p.shouldMatch("§0[Pg. 29] §8aA9a9G_g0g4G's§0 Bank");
        p.shouldMatch("§0[Pg. 1] §8mag_icus'§0 Bank");
        p.shouldMatch("§0[Pg. 29] §8aA9a9G_g0g4G's§0 Block Bank");
        p.shouldMatch("§0[Pg. 1] §8v8j's§0 Misc. Bucket");
        p.shouldMatch("§0[Pg. 1] §8mag_icus'§0 Misc. Bucket");
        p.shouldMatch("§0[Pg. 1] §8Housing Island's§0 Block Bank");
    }

    @Test
    public void ContainerModel_TRADE_MARKET_FILTER_TITLE() {
        PatternTester p = new PatternTester(ContainerModel.class, "TRADE_MARKET_FILTER_TITLE");
        p.shouldMatch("[Pg. 1] Filter Items"); // Page 1
        p.shouldMatch("[Pg. 7] Filter Items"); // Page 7
    }

    @Test
    public void CoordinatesSegment_COORDINATES_PATTERN() {
        PatternTester p = new PatternTester(CoordinatesSegment.class, "COORDINATES_PATTERN");
        p.shouldMatch("§7457§f N§7 -1576");
        p.shouldMatch("§7-1§f NW§7 154");
        p.shouldMatch("§7-736§f S§7 -1575");
    }

    @Test
    public void DamageModel_DAMAGE_LABEL_PATTERN() {
        PatternTester p = new PatternTester(DamageModel.class, "DAMAGE_LABEL_PATTERN");
        p.shouldMatch("§4-13 ❤ ");
        p.shouldMatch("§4-10 ❤ ");
        p.shouldMatch("§c-8 ✹ ");
        p.shouldMatch("§e-30 ✦ ");
        p.shouldMatch("§2-41 ✤ ");
        p.shouldMatch("§b-21 ❉ ");
        p.shouldMatch("§f-32 ❋ ");
        p.shouldMatch("§c-28 ✹ ");
    }

    @Test
    public void DamageModel_DAMAGE_BAR_PATTERN() {
        PatternTester p = new PatternTester(DamageModel.class, "DAMAGE_BAR_PATTERN");
        p.shouldMatch("§aTravelling Merchant§r - §c5985§4❤");
        p.shouldMatch("§aGrook§r - §c23§4❤");
        p.shouldMatch("§cZombie§r - §c43§4❤");
        p.shouldMatch("§cFeligember Frog§r - §c1553§4❤ - §7§e✦Weak §c✹Dam §c✹Def§7");
    }

    @Test
    public void FriendsModel_ONLINE_FRIENDS_HEADER() {
        PatternTester p = new PatternTester(FriendsModel.class, "ONLINE_FRIENDS_HEADER");
        p.shouldMatch("§2Online §aFriends:");
    }

    @Test
    public void FriendsModel_ONLINE_FRIEND() {
        PatternTester p = new PatternTester(FriendsModel.class, "ONLINE_FRIEND");
        p.shouldMatch("§2 - §auserName914__§2 [Server: §aWC3§2]");
        p.shouldMatch("§2 - §av8j§2 [Server: §aWC103§2]");
        p.shouldMatch("§2 - §a__asdf__§2 [Server: §aWC91§2]");
    }

    @Test
    public void FriendsModel_JOIN_PATTERN() {
        PatternTester p = new PatternTester(FriendsModel.class, "JOIN_PATTERN");
        p.shouldMatch("§aMirvun§2 has logged into server §aWC1§2 as §aan Archer");
        p.shouldMatch("§aMirvun§2 has logged into server §aWC27§2 as §aa Mage");
    }

    @Test
    public void FriendsModel_LEAVE_PATTERN() {
        PatternTester p = new PatternTester(FriendsModel.class, "LEAVE_PATTERN");
        p.shouldMatch("§aMirvun left the game.");
    }

    @Test
    public void GuildModel_GUILD_NAME_MATCHER() {
        PatternTester p = new PatternTester(GuildModel.class, "GUILD_NAME_MATCHER");
        p.shouldMatch("§3guildName§b [aAaA]");
        p.shouldMatch("§3guild Name§b [aaaa]");
        p.shouldMatch("§3GUILD NAME§b [wynn]");
    }

    @Test
    public void GuildModel_GUILD_RANK_MATCHER() {
        PatternTester p = new PatternTester(GuildModel.class, "GUILD_RANK_MATCHER");
        p.shouldMatch("§7Rank: §fRecruit");
        p.shouldMatch("§7Rank: §fRecruiter");
        p.shouldMatch("§7Rank: §fCaptain");
        p.shouldMatch("§7Rank: §fStrategist");
        p.shouldMatch("§7Rank: §fChief");
        p.shouldMatch("§7Rank: §fOwner");
    }

    @Test
    public void GuildModel_MSG_LEFT_GUILD() {
        PatternTester p = new PatternTester(GuildModel.class, "MSG_LEFT_GUILD");
        p.shouldMatch("§3You have left §bExample Guild§3!");
    }

    @Test
    public void GuildModel_MSG_JOINED_GUILD() {
        PatternTester p = new PatternTester(GuildModel.class, "MSG_JOINED_GUILD");
        p.shouldMatch("§3You have joined §bExample Guild§3!");
    }

    @Test
    public void GuildModel_MSG_RANK_CHANGED() {
        PatternTester p = new PatternTester(GuildModel.class, "MSG_RANK_CHANGED");
        p.shouldMatch("§3[INFO]§b v8j has set USERNAME's guild rank from Recruit to Chief");
        p.shouldMatch("§3[INFO]§b v8j has set USERNAMES' guild rank from Recruiter to Chief");
    }

    @Test
    public void GuildRankReplacementFeature_GUILD_MESSAGE_PATTERN() {
        PatternTester p = new PatternTester(GuildRankReplacementFeature.class, "GUILD_MESSAGE_PATTERN");
        /* FIXME: These tests fail
        p.shouldMatch("§3[§b★★★★★§3§oDisco reroller§3]");
        p.shouldMatch("§3[§b★★★★★§3§oafKing§r§3]§");
        p.shouldMatch("§3[§b★★★★§3§obol§r§3]");
        */
    }

    @Test
    public void GuildRankReplacementFeature_RECRUIT_USERNAME_PATTERN() {
        PatternTester p = new PatternTester(GuildRankReplacementFeature.class, "RECRUIT_USERNAME_PATTERN");
        p.shouldMatch("§3[_user0name_");
    }

    @Test
    public void IngredientAnnotator_INGREDIENT_PATTERN() {
        PatternTester p = new PatternTester(IngredientAnnotator.class, "INGREDIENT_PATTERN");
        p.shouldMatch("§7Perkish Potato [§8✫✫✫§7]");
        p.shouldMatch("§7Sylphid Tears§6 [§e✫§8✫✫§6]");
        p.shouldMatch("§7Bob's Tear§5 [§d✫✫§8✫§5]");
        p.shouldMatch("§7Contorted Stone§3 [§b✫✫✫§3]");
    }

    @Test
    public void ManaSegment_MANA_PATTERN() {
        PatternTester p = new PatternTester(ManaSegment.class, "MANA_PATTERN");
        p.shouldMatch("§b✺ 175/175");
        p.shouldMatch("§b✺ 56/175");
        p.shouldMatch("✺ 175/175");
    }

    @Test
    public void MessageFilterFeature_PARTY_FINDER_FG() {
        PatternTester p = new PatternTester(MessageFilterFeature.class, "PARTY_FINDER_FG");
        p.shouldMatch(
                "§5Party Finder:§d Hey Rafii2198, over here! Join the §bThe Canyon Colossus§d queue and match up with §e2 other players§d!"); // Name 2 players
        p.shouldMatch(
                "§5Party Finder:§d Hey Rafii2198, over here! Join the §bThe Canyon Colossus§d queue and match up with §e1 other player§d!"); // Name 1 player
        p.shouldMatch(
                "§5Party Finder:§d Hey nickname spaces, over here! Join the §bThe Canyon Colossus§d queue and match up with §e1 other player§d!"); // Nickname 1 player
        p.shouldMatch(
                "§5Party Finder:§d Hey nickname spaces 20cr, over here! Join the §bThe Canyon Colossus§d queue and match up with §e11 other players§d!"); // Nickname 11 players
    }

    @Test
    public void PowderSpecialSegment_POWDER_SPECIAL_PATTERN() {
        PatternTester p = new PatternTester(PowderSpecialSegment.class, "POWDER_SPECIAL_PATTERN");
        p.shouldMatch("§7❉ 87%"); // curse/partial charge
        p.shouldMatch("§b❉ 100%"); // curse/full charge
        p.shouldMatch("§7✹ 78%"); // courage/partial charge
        p.shouldMatch("§c✹ 100%"); // courage/full charge
    }

    @Test
    public void RecipientType_NPC_foregroundPattern() {
        PatternTester p = new PatternTester(RecipientType.NPC, "foregroundPattern");
        p.shouldMatch("§7[3/5]§r§2 Jesp:§r§a Keep fighting! We're almost halfway to the other side!");
        /* FIXME: These tests fail
        p.shouldMatch("§7[1/11]§r§0 §r§2Scientist Ynnos:§r§a *Ahem* Welcome, everyone.");
        p.shouldMatch(
                "§7[3/11]§r§0 §r§2Scientist Ynnos:§r§0 §r§aAllow me to explain the situation we’re in. It’s a series of unfortunate events. I study the amazing properties of crystals and other geodes.");
        p.shouldMatch(
                "§7[6/6] §r§5Aster: §r§dSo remember, find a balance in elements! Whether you invest in one, or select many to excell in, I trust you may succeed. Thank you for you time.§r");
        */
    }

    @Test
    public void RecipientType_GLOBAL_foregroundPattern() {
        PatternTester p = new PatternTester(RecipientType.GLOBAL, "foregroundPattern");
        // wc5 106(archer)VAI CHAMPION v8j: test
        p.shouldNotMatch(
                "§f\uE056\uE042\uE065§r §8\uE010\u2064\uE070§f\uE071\uE061§8\uE00F§f\uE012\uE060§8\uE00F§f\uE012\uE066§8\uE00E§f\uE012\uE012\uE013\uE02C§8\uE00F§f\uE012\uE055§8\uE00F§f\uE012\uE040§8\uE00F§f\uE012\uE048§8\uE011\u2064§r\uE013\uE017\uE013\u2064\u2064§#ffe60000v8j: §ftest");
        // wc5 106(archer)VAI VIP v8j: hello 2
        p.shouldNotMatch(
                "§f\uE056\uE042\uE065§r §8\uE010\u2064\uE070§f\uE071\uE061§8\uE00F§f\uE012\uE060§8\uE00F§f\uE012\uE066§8\uE00E§f\uE012\uE012\uE013\uE02C§8\uE00F§f\uE012\uE055§8\uE00F§f\uE012\uE040§8\uE00F§f\uE012\uE048§8\uE011\u2064§r\uE013\uE023\uE013\u2064\u2064§#44aa3300v8j: §fhello 2");
        // REMOTE wc6 105(mage) UNVERIFIED Sebastiankungen: 4
        p.shouldMatch(
                "§7\uE056\uE042\uE066§r §8\uE010\u2064\uE070§f\uE071\uE061§8\uE00F§f\uE012\uE060§8\uE00F§f\uE012\uE065§8\uE00F§f\uE012\uE013\uE013\u2064\u2064\u2064\u2064§8\uE011§f\uE012\u2064\uE030\u2064\u2064\u2064§r\uE013\uE013\u2064\u2064\u2064§7Sebastiankungen: §f4");
        // wc37 104(skyseer)VAI silverbullVIP+ v8j: test
        p.shouldNotMatch(
                "§f\uE056\uE042\uE063\uE067§r §8\uE010\u2064\uE070§f\uE071\uE061§8\uE00F§f\uE012\uE060§8\uE00F§f\uE012\uE064§8\uE00E§f\uE012\uE012\uE013\uE02F§8\uE00F§f\uE012\uE055§8\uE00F§f\uE012\uE040§8\uE00F§f\uE012\uE048§8\uE011\u2064§r  \u2064\u2064\u2064§f\uE02B\u2064\u2064§r\uE024\uE013\u2064\u2064§#8a99ee00v8j: §ftest");
        // REMOTE wc3 105(archer)BXP VERIFIED moumbear: oblivion
        p.shouldMatch(
                "§7\uE056\uE042\uE063§r §8\uE010\u2064\uE070§f\uE071\uE061§8\uE00F§f\uE012\uE060§8\uE00F§f\uE012\uE065§8\uE00E§f\uE012\uE012\uE013\uE02C§8\uE00F§f\uE012\uE041§8\uE00F§f\uE012\uE057§8\uE00F§f\uE012\uE04F§8\uE011\u2064§r\uE013\uE013\u2064\u2064\u2064§fmoumbear: oblivion");
        // REMOTE wc1 105(archer)SEQ GuildMilestone2 VERIFIED warpo: i love bolt
        p.shouldMatch(
                "§7\uE056\uE042\uE061§r §8\uE010\u2064\uE070§f\uE071\uE061§8\uE00F§f\uE012\uE060§8\uE00F§f\uE012\uE065§8\uE00E§f\uE012\uE012\uE013\uE02C§8\uE00F§f\uE012\uE052§8\uE00F§f\uE012\uE044§8\uE00F§f\uE012\uE050§8\uE011\u2064§r §#54fcfc00\uE07C \u2064§r\uE013\u2064\u2064\u2064§fwarpo: i love bolt");
        // REMOTE wc25 106(warrior)FURS GuildMilestone1 silverbullCHAMPION angycathy: 6500
        p.shouldMatch(
                "§7\uE056\uE042\uE062\uE065§r §8\uE010\u2064\uE070§f\uE071\uE061§8\uE00F§f\uE012\uE060§8\uE00F§f\uE012\uE066§8\uE00E§f\uE012\uE012\uE013\uE030§8\uE00F§f\uE012\uE045§8\uE00F§f\uE012\uE054§8\uE00F§f\uE012\uE051§8\uE00F§f\uE012\uE052§8\uE011\u2064§r §#54fcfc00\uE07B \u2064\u2064\u2064§f\uE02B\u2064\u2064§r\uE017\uE013\u2064\u2064§#ffe60000angycathy: §f6500");
        // REMOTE wc1 105(warrior)MAG GuildMilestone2 HERO Mythicized: gaming
        p.shouldMatch(
                "§7\uE056\uE042\uE061§r §8\uE010\u2064\uE070§f\uE071\uE061§8\uE00F§f\uE012\uE060§8\uE00F§f\uE012\uE065§8\uE00E§f\uE012\uE012\uE013\uE030§8\uE00F§f\uE012\uE04C§8\uE00F§f\uE012\uE040§8\uE00F§f\uE012\uE046§8\uE011\u2064§r §#54fcfc00\uE07C \u2064§r\uE01B\uE013\u2064\u2064§#a344aa00Mythicized: §fgaming");
    }

    @Test
    public void RecipientType_LOCAL_foregroundPattern() {
        PatternTester p = new PatternTester(RecipientType.LOCAL, "foregroundPattern");
        // wc5 106(archer)VAI CHAMPION v8j: test
        p.shouldMatch(
                "§f\uE056\uE042\uE065§r §8\uE010\u2064\uE070§f\uE071\uE061§8\uE00F§f\uE012\uE060§8\uE00F§f\uE012\uE066§8\uE00E§f\uE012\uE012\uE013\uE02C§8\uE00F§f\uE012\uE055§8\uE00F§f\uE012\uE040§8\uE00F§f\uE012\uE048§8\uE011\u2064§r\uE013\uE017\uE013\u2064\u2064§#ffe60000v8j: §ftest");
        // wc5 (friend)58(mage)VAI UNVERIFIED Mirvun: test
        p.shouldMatch(
                "§f\uE056\uE042\uE065§r §#00a80000\uE010\u2064\uE00F§f\uE012\uE065§#00a80000\uE00F§f\uE012\uE068§#00a80000\uE00E§f\uE012\uE012\uE013\uE02E§#00a80000\uE00F§f\uE012\uE055§#00a80000\uE00F§f\uE012\uE040§#00a80000\uE00F§f\uE012\uE048§#00a80000\uE011\u2064§r\uE013\uE013\u2064\u2064\u2064§7Mirvun: §f.");
        // wc5 (guild)58(mage)VAI UNVERIFIED Mirvun: bingbing
        p.shouldMatch(
                "§f\uE056\uE042\uE065§r §#4ec7c700\uE010\u2064\uE00F§f\uE012\uE065§#4ec7c700\uE00F§f\uE012\uE068§#4ec7c700\uE00E§f\uE012\uE012\uE013\uE02E§#4ec7c700\uE00F§f\uE012\uE055§#4ec7c700\uE00F§f\uE012\uE040§#4ec7c700\uE00F§f\uE012\uE048§#4ec7c700\uE011\u2064§r\uE013\uE013\u2064\u2064\u2064§7Mirvun: §fbingbing)");
        // wc5 106(archer)VAI VIP v8j: hello 2
        p.shouldMatch(
                "§f\uE056\uE042\uE065§r §8\uE010\u2064\uE070§f\uE071\uE061§8\uE00F§f\uE012\uE060§8\uE00F§f\uE012\uE066§8\uE00E§f\uE012\uE012\uE013\uE02C§8\uE00F§f\uE012\uE055§8\uE00F§f\uE012\uE040§8\uE00F§f\uE012\uE048§8\uE011\u2064§r\uE013\uE023\uE013\u2064\u2064§#44aa3300v8j: §fhello 2");
        // wc5 105(warrior)VAI HERO v8j: 4
        p.shouldMatch(
                "§f\uE056\uE042\uE065§r §8\uE010\u2064\uE070§f\uE071\uE061§8\uE00F§f\uE012\uE060§8\uE00F§f\uE012\uE065§8\uE00E§f\uE012\uE012\uE013\uE030§8\uE00F§f\uE012\uE055§8\uE00F§f\uE012\uE040§8\uE00F§f\uE012\uE048§8\uE011\u2064§r\uE013\uE01B\uE013\u2064\u2064§#a344aa00v8j: §f4");
        // wc37 105(assassin)VAI VIP+ v8j: 5test4
        p.shouldMatch(
                "§f\uE056\uE042\uE063\uE067§r §8\uE010\u2064\uE070§f\uE071\uE061§8\uE00F§f\uE012\uE060§8\uE00F§f\uE012\uE065§8\uE00E§f\uE012\uE012\uE013\uE02D§8\uE00F§f\uE012\uE055§8\uE00F§f\uE012\uE040§8\uE00F§f\uE012\uE048§8\uE011\u2064§r\uE013\uE024\uE013\u2064\u2064§#8a99ee00v8j: §f5test4");
        // REMOTE wc6 105(mage) UNVERIFIED Sebastiankungen: 4
        p.shouldNotMatch(
                "§7\uE056\uE042\uE066§r §8\uE010\u2064\uE070§f\uE071\uE061§8\uE00F§f\uE012\uE060§8\uE00F§f\uE012\uE065§8\uE00F§f\uE012\uE013\uE013\u2064\u2064\u2064\u2064§8\uE011§f\uE012\u2064\uE030\u2064\u2064\u2064§r\uE013\uE013\u2064\u2064\u2064§7Sebastiankungen: §f4");
        // wc37 104(skyseer)VAI silverbullVIP+ v8j: test
        p.shouldMatch(
                "§f\uE056\uE042\uE063\uE067§r §8\uE010\u2064\uE070§f\uE071\uE061§8\uE00F§f\uE012\uE060§8\uE00F§f\uE012\uE064§8\uE00E§f\uE012\uE012\uE013\uE02F§8\uE00F§f\uE012\uE055§8\uE00F§f\uE012\uE040§8\uE00F§f\uE012\uE048§8\uE011\u2064§r  \u2064\u2064\u2064§f\uE02B\u2064\u2064§r\uE024\uE013\u2064\u2064§#8a99ee00v8j: §ftest");
    }

    @Test
    public void RecipientType_GUILD_foregroundPattern() {
        PatternTester p = new PatternTester(RecipientType.GUILD, "foregroundPattern");
        p.shouldMatch("§3[§b★★★★§3bolyai§3] test 3");
        p.shouldMatch("§3[§b★★★★§3§obol§r§3]§b test ");
        p.shouldMatch("§3[kristof345]§b test");
        p.shouldNotMatch("§3[INFO]§b bolyai has kicked kristof345 from the guild");
    }
}
