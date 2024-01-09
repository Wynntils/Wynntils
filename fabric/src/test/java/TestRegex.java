/*
 * Copyright © Wynntils 2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.wynntils.features.chat.GuildRankReplacementFeature;
import com.wynntils.features.chat.MessageFilterFeature;
import com.wynntils.features.redirects.ChatRedirectFeature;
import com.wynntils.features.trademarket.TradeMarketPriceMatchFeature;
import com.wynntils.features.ui.BulkBuyFeature;
import com.wynntils.handlers.actionbar.ActionBarHandler;
import com.wynntils.handlers.chat.ChatHandler;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.models.character.CharacterModel;
import com.wynntils.models.character.CharacterSelectionModel;
import com.wynntils.models.characterstats.actionbar.CoordinatesSegment;
import com.wynntils.models.characterstats.actionbar.ManaSegment;
import com.wynntils.models.characterstats.actionbar.PowderSpecialSegment;
import com.wynntils.models.characterstats.actionbar.SprintSegment;
import com.wynntils.models.containers.ContainerModel;
import com.wynntils.models.damage.DamageModel;
import com.wynntils.models.items.annotators.game.IngredientAnnotator;
import com.wynntils.models.items.annotators.game.RuneAnnotator;
import com.wynntils.models.items.annotators.gui.AbilityTreeAnnotator;
import com.wynntils.models.items.annotators.gui.ArchetypeAbilitiesAnnotator;
import com.wynntils.models.items.annotators.gui.SkillPointAnnotator;
import com.wynntils.models.players.FriendsModel;
import com.wynntils.models.players.GuildModel;
import com.wynntils.models.spells.SpellModel;
import com.wynntils.models.spells.actionbar.SpellSegment;
import com.wynntils.models.statuseffects.StatusEffectModel;
import com.wynntils.models.trademarket.TradeMarketModel;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
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
        // Champion
        p.shouldMatch("§7Rank: §6Vet");
        // Hero
        p.shouldMatch("§7Rank: §dVet");
        // VIP+
        p.shouldMatch("§7Rank: §bVet");
        // VIP
        p.shouldMatch("§7Rank: §aVet");
    }

    @Test
    public void CharacterSelectionModel_CLASS_ITEM_CLASS_PATTERN() {
        PatternTester p = new PatternTester(CharacterSelectionModel.class, "CLASS_ITEM_CLASS_PATTERN");
        // Hunter
        p.shouldMatch("§e- §7Class: §fHunter");
        // Mage
        p.shouldMatch("§e- §7Class: §fMage");
        // Craftsman Dark Wizard
        p.shouldMatch("§e- §7Class: §3\uE026§r §fDark Wizard");
        // Hardcore Assassin
        p.shouldMatch("§e- §7Class: §c\uE027§r §fAssassin");
        // Hunted Ninja
        p.shouldMatch("§e- §7Class: §5\uE028§r §fNinja");
        // Ultimate Ironman Shaman
        p.shouldMatch("§e- §7Class: §b\uE083§r §fShaman");
        // Ultimate HIC Warrior
        p.shouldMatch("§e- §7Class: §c\uE027§b\uE083§3\uE026§5\uE028§r §fWarrior");
        // HIC Skyseer
        p.shouldMatch("§e- §7Class: §c\uE027§6\uE029§3\uE026§5\uE028§r §fSkyseer");
        // Ironman Archer
        p.shouldMatch("§e- §7Class: §6\uE029§r §fArcher");
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
        // champion
        p.shouldMatch("\uE017 §#ffe60000v8j§6 has just logged in!");
        // hero
        p.shouldMatch("\uE01B §#a344aa00v8j§d has just logged in!");
        // vip+
        p.shouldMatch("\uE024 §#8a99ee00v8j§3 has just logged in!");
        // vip
        p.shouldMatch("\uE023 §#44aa3300v8j§a has just logged in!");
        // champion nickname
        p.shouldMatch("\uE017 §#ffe60000§ocharlie268IsAWizard§6 has just logged in!");
    }

    @Test
    public void ContainerModel_ABILITY_TREE_PATTERN() {
        PatternTester p = new PatternTester(ContainerModel.class, "ABILITY_TREE_PATTERN");
        // Warrior
        p.shouldMatch("Warrior Abilities");
        // Shaman
        p.shouldMatch("Shaman Abilities");
        // Mage
        p.shouldMatch("Mage Abilities");
        // Assassin
        p.shouldMatch("Assassin Abilities");
        // Archer
        p.shouldMatch("Archer Abilities");
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
        // Tier 1
        p.shouldMatch("Loot Chest §7[§f✫§8✫✫✫§7]");
        // Tier 2
        p.shouldMatch("Loot Chest §e[§6✫✫§8✫✫§e]");
        // Tier 3
        p.shouldMatch("Loot Chest §5[§d✫✫✫§8✫§5]");
        // Tier 4
        p.shouldMatch("Loot Chest §3[§b✫✫✫✫§3]");
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
        // Page 1
        p.shouldMatch("[Pg. 1] Filter Items");
        // Page 7
        p.shouldMatch("[Pg. 7] Filter Items");
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
        p.shouldMatch("§3[§b★★★★★§3§oDisco reroller§3]§b");
        p.shouldMatch("§3[§b★★★★★§3§oafKing§3]§b");
        p.shouldMatch("§3[§b★★★★§3§obol§3]§b");
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
        // curse/partial charge
        p.shouldMatch("§7❉ 87%");
        // curse/full charge
        p.shouldMatch("§b❉ 100%");
        // courage/partial charge
        p.shouldMatch("§7✹ 78%");
        // courage/full charge
        p.shouldMatch("§c✹ 100%");
    }

    @Test
    public void RecipientType_NPC_foregroundPattern() {
        PatternTester p = new PatternTester(RecipientType.NPC, "foregroundPattern");
        p.shouldMatch("§7[3/5]§2 Jesp:§a Keep fighting! We're almost halfway to the other side!");
        p.shouldMatch("§7[1/11]§0 §2Scientist Ynnos:§a *Ahem* Welcome, everyone.");
        p.shouldMatch(
                "§7[3/11]§0 §2Scientist Ynnos:§0 §r§aAllow me to explain the situation we’re in. It’s a series of unfortunate events. I study the amazing properties of crystals and other geodes.");
        p.shouldMatch(
                "§7[6/6] §5Aster: §dSo remember, find a balance in elements! Whether you invest in one, or select many to excell in, I trust you may succeed. Thank you for you time.");
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

    @Test
    public void RuneAnnotator_RUNE_PATTERN() {
        PatternTester p = new PatternTester(RuneAnnotator.class, "RUNE_PATTERN");
        p.shouldMatch("§bAz Rune");
        p.shouldMatch("§4Nii Rune");
        p.shouldMatch("§3Uth Rune");
        p.shouldMatch("§2Tol Rune");
    }

    @Test
    public void SkillPointAnnotator_SKILL_POINT_PATTERN() {
        PatternTester p = new PatternTester(SkillPointAnnotator.class, "SKILL_POINT_PATTERN");
        p.shouldMatch("§dUpgrade your §2✤ Strength§d skill");
        p.shouldMatch("§dUpgrade your §e✦ Dexterity§d skill");
        p.shouldMatch("§dUpgrade your §b❉ Intelligence§d skill");
        p.shouldMatch("§dUpgrade your §c✹ Defence§d skill");
        p.shouldMatch("§dUpgrade your §f❋ Agility§d skill");
    }

    @Test
    public void SkillPointAnnotator_LORE_PATTERN() {
        PatternTester p = new PatternTester(SkillPointAnnotator.class, "LORE_PATTERN");
        p.shouldMatch(" ÀÀ§740 points§r    ÀÀÀ           ÀÀÀ§641 points");
        p.shouldMatch(" ÀÀ§763 points§r    ÀÀÀ           ÀÀÀ§664 points");
        p.shouldMatch("ÀÀÀ§7131 points§r               §6132 points");
        p.shouldMatch(" ÀÀ§790 points§r    ÀÀÀ           ÀÀÀ§691 points");
        p.shouldMatch(" ÀÀ§762 points§r    ÀÀÀ           ÀÀÀ§663 points");
    }

    @Test
    public void SpellModel_SPELL_TITLE_PATTERN() {
        PatternTester p = new PatternTester(SpellModel.class, "SPELL_TITLE_PATTERN");
        // Lv1 R??
        p.shouldMatch("§aRight§7-§7§n?§7-§r§7?§r");
        // Lv1 RL?
        p.shouldMatch("§aRight§7-§aLeft§7-§r§7§n?§r");
        // Lv1 RLR
        p.shouldMatch("§aRight§7-§aLeft§7-§r§aRight§r");
        // Lv1 RR?
        p.shouldMatch("§aRight§7-§aRight§7-§r§7§n?§r");
        // Lv1 RRL
        p.shouldMatch("§aRight§7-§aRight§7-§r§aLeft§r");
        // Lv1 RRR
        p.shouldMatch("§aRight§7-§aRight§7-§r§aRight§r");
        // Lv1 RLL
        p.shouldMatch("§aRight§7-§aLeft§7-§r§aLeft§r");
        // L??
        p.shouldMatch("§aL§7-§7§n?§7-§r§7?§r");
        // LL?
        p.shouldMatch("§aL§7-§aL§7-§r§7§n?§r");
        // LLL
        p.shouldMatch("§aL§7-§aL§7-§r§aL§r");
        // LR?
        p.shouldMatch("§aL§7-§aR§7-§r§7§n?§r");
        // LRL
        p.shouldMatch("§aL§7-§aR§7-§r§aL§r");
        // R??
        p.shouldMatch("§aR§7-§7§n?§7-§r§7?§r");
        // RL?
        p.shouldMatch("§aR§7-§aL§7-§r§7§n?§r");
        // RRL
        p.shouldMatch("§aR§7-§aR§7-§r§aL§r");
    }

    @Test
    public void SpellSegment_SPELL_PATTERN() {
        PatternTester p = new PatternTester(SpellSegment.class, "SPELL_PATTERN");
        // L??
        p.shouldMatch("§aL§7-§n?§r§7-?§r");
        // LR?
        p.shouldMatch("§aL§7-§aR§7-§n?§r");
        // LRL
        p.shouldMatch("§aL§7-§aR§7-§aL§r");
        // LLR
        p.shouldMatch("§aL§7-§aL§7-§aR§r");
        // R??
        p.shouldMatch("§aR§7-§n?§r§7-?§r");
        // RR?
        p.shouldMatch("§aR§7-§aR§7-§n?§r");
        // RRR
        p.shouldMatch("§aR§7-§aR§7-§aR§r");
    }

    @Test
    public void SprintSegment_SPRINT_PATTERN() {
        PatternTester p = new PatternTester(SprintSegment.class, "SPRINT_PATTERN");
        // green sprint bar
        p.shouldMatch("§2[§a|||Sprint|§8||§2]");
        // green sprint text 1
        p.shouldMatch("§2[§a|||Sprin§8t|||§2]");
        // green sprint text 2
        p.shouldMatch("§6[§e|||S§8print|||§6]");
        // yellow sprint text
        p.shouldMatch("§6[§e|||§8Sprint|||§6]");
        // yellow sprint bar
        p.shouldMatch("§6[§e|§8||Sprint|||§6]");
        // no sprint grey
        p.shouldMatch("§4[§8|||Sprint|||§4]");
        // no sprint red
        p.shouldMatch("§4[§c|||Sprint|||§4]");
        // max sprint
        p.shouldMatch("§2[§a|||Sprint|||§2]");
    }

    @Test
    public void StatusEffectModel_STATUS_EFFECT_PATTERN() {
        PatternTester p = new PatternTester(StatusEffectModel.class, "STATUS_EFFECT_PATTERN");
        p.shouldMatch("§fⒺ§7 +198 Main Attack Damage §8(00:41)");
        p.shouldMatch("§f§b❤§7 Windy Feet §8(02:57)");
        p.shouldMatch("§a❉ §776.5% Concentration §4(00:13)");
        p.shouldMatch("§b➲ §718% Frenzy §8(**:**)");
        p.shouldMatch("§fⒺ§7 +54% Spell Damage §8(00:41)");
        p.shouldMatch("§8⬤ §7Vanish §a(00:04)");
        p.shouldMatch("§fⒺ§7 +250/3s Life Steal §8(00:41)");
        p.shouldMatch("§8⬤ §7Boiling Blood §a(00:02)");
    }

    @Test
    public void TradeMarketModel_PRICE_PATTERN() {
        PatternTester p = new PatternTester(TradeMarketModel.class, "PRICE_PATTERN");
        p.shouldMatch("§7 - §f525§7² §8(8²½ 13²)");
        p.shouldMatch("§7 - §f21,111§7² §8(5¼² 9²½ 55²)");
        p.shouldMatch("§7 - §f8 §7x §f127§7² §8(1²½ 63²)");
        p.shouldMatch("§7 - §f308 §7x §f§m16§7§m²§b ✮ 15§3² §8(15²)");
        p.shouldMatch("§7 - §f308 §7x §f§m16§7§m²§b ✮ 15§3² §8(15²)");
    }

    @Test
    public void TradeMarketPriceMatchFeature_HIGHEST_BUY_PATTERN() {
        PatternTester p = new PatternTester(TradeMarketPriceMatchFeature.class, "HIGHEST_BUY_PATTERN");
        p.shouldMatch("§7Highest Buy Offer: §a100000²§8 (24¼² 26²½ 32²)");
    }

    @Test
    public void TradeMarketPriceMatchFeature_LOWEST_SELL_PATTERN() {
        PatternTester p = new PatternTester(TradeMarketPriceMatchFeature.class, "LOWEST_SELL_PATTERN");
        p.shouldMatch("§7Lowest Sell Offer: §a1050000²§8 (4stx 0.35¼²)");
    }

    @Test
    public void WynnItemParser_ITEM_ATTACK_SPEED_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "ITEM_ATTACK_SPEED_PATTERN");
        p.shouldMatch("§7Normal Attack Speed");
        p.shouldMatch("§7Super Fast Attack Speed");
    }

    @Test
    public void WynnItemParser_ITEM_DAMAGE_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "ITEM_DAMAGE_PATTERN");
        p.shouldMatch("§6✣ Neutral Damage: 55-68");
    }

    @Test
    public void WynnItemParser_ITEM_DEFENCE_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "ITEM_DEFENCE_PATTERN");
        p.shouldMatch("§e✦ Thunder§7 Defence: +56");
    }

    @Test
    public void WynnItemParser_IDENTIFICATION_STAT_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "IDENTIFICATION_STAT_PATTERN");
        p.shouldMatch("§a+10% §7Health Regen");
        p.shouldMatch("§a+5%§2* §7XP Bonus");
        p.shouldMatch("§a+5/5s §7Mana Regen");
        p.shouldMatch("§a+42 §7Water Spell Damage");
        p.shouldMatch("§a+4 §7Intelligence");
        p.shouldMatch("§a+1 tier§2* §7Attack Speed");
        p.shouldMatch("§a+16%§2*** §7XP Bonus");
        p.shouldMatch("§c-28000§4 to §c-52000%§7 Spell Damage");
        p.shouldMatch("§c-28000§4 to §c-52000§7 Spell Damage");
        p.shouldMatch("§a+12§2 to §a52%§7 Main Attack Damage");
        p.shouldMatch("§c-280§4 to §c-520§7 {sp1} Cost");
        p.shouldMatch("§a+291/3s§2** §7Life Steal");
        p.shouldMatch("§c-28% §7Soul Point Regen");
        // Crafted gear 18/18% Main Attack Damage
        p.shouldMatch("§a+18%§8/18% §7Main Attack Damage");
    }

    @Test
    public void WynnItemParser_TIER_AND_REROLL_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "TIER_AND_REROLL_PATTERN");
        p.shouldMatch("§eUnique Item");
        p.shouldMatch("§dRare Item");
        p.shouldMatch("§dRare Item [2]");
        p.shouldMatch("§bLegendary Item");
        p.shouldMatch("§cFabled Item");
        p.shouldMatch("§aSet Item");

        // Crafted gear
        p.shouldMatch("§3Crafted Wand§8 [134/137 Durability]");
        p.shouldMatch("§3Crafted by player_name§8 [177/177 Durability]");
        p.shouldMatch("§3Crafted by v8j§8 [177/177 Durability]");
    }

    @Test
    public void WynnItemParser_POWDER_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "POWDER_PATTERN");
        p.shouldMatch("§7[0/1] Powder Slots");
        p.shouldMatch("§7[1/1] Powder Slots [§r§c✹§r§7]");
        p.shouldMatch("§7[3/3] Powder Slots [§r§f❋ ❋ ❋§r§7]");
        p.shouldMatch("§7[2/2] Powder Slots [§r§e✦ ✦§r§7]");
        p.shouldMatch("§7[2/2] Powder Slots [§r§b❉ ❉§r§7]");
        p.shouldMatch("§7[3/3] Powder Slots [§r§2✤ §r§c✹ §r§b❉§r§7]");
    }

    @Test
    public void WynnItemParser_EFFECT_LINE_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "EFFECT_LINE_PATTERN");
        p.shouldMatch("§6- §7Effect: §f20% XP");
    }

    @Test
    public void WynnItemParser_MIN_LEVEL_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "MIN_LEVEL_PATTERN");
        p.shouldMatch("§a✔§7 Combat Lv. Min: 35");
        p.shouldMatch("§c✖§7 Combat Lv. Min: 65");
    }

    @Test
    public void WynnItemParser_CLASS_REQ_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "CLASS_REQ_PATTERN");
        p.shouldMatch("§c✖§7 Class Req: Shaman/Skyseer§r");
    }

    @Test
    public void WynnItemParser_SKILL_REQ_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "SKILL_REQ_PATTERN");
        p.shouldMatch("§a✔§7 Intelligence Min: 38");
    }

    @Test
    public void WynnItemParser_SHINY_STAT_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "SHINY_STAT_PATTERN");
        p.shouldMatch("§f⬡ §7Raids Won: §f0");
        p.shouldMatch("§f⬡ §7Raids Won: §f297");
        p.shouldMatch("§f⬡ §7Mobs Killed: §f0");
        p.shouldNotMatch("§c✖§7 Agility Min: 70");
        p.shouldNotMatch("§f⬡ §7: §f0");
        p.shouldNotMatch("§f⬡ §7Mobs Killed: §f");
        p.shouldMatch("§f⬡ §7Wars Won: §f164");
        p.shouldMatch("§f⬡ §7Raids Won: §f0");
    }

    @Test
    public void WynnItemParser_CRAFTED_ITEM_NAME_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "CRAFTED_ITEM_NAME_PATTERN");
        p.shouldMatch("§3§otest item§b§o [24%]À");
        p.shouldMatch("§3§oAbsorbant Skirt of the Skyraider§b§o [100%]À");
    }
}
