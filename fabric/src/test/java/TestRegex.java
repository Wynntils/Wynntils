/*
 * Copyright © Wynntils 2023-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.wynntils.core.WynntilsMod;
import com.wynntils.features.chat.MessageFilterFeature;
import com.wynntils.features.inventory.PersonalStorageUtilitiesFeature;
import com.wynntils.features.redirects.ChatRedirectFeature;
import com.wynntils.features.ui.BulkBuyFeature;
import com.wynntils.handlers.chat.ChatPageProcessor;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.models.abilities.ShamanTotemModel;
import com.wynntils.models.abilities.bossbars.OphanimBar;
import com.wynntils.models.account.AccountModel;
import com.wynntils.models.activities.worldevents.WorldEventModel;
import com.wynntils.models.bonustotems.label.BonusTotemLabelParser;
import com.wynntils.models.combat.bossbar.DamageBar;
import com.wynntils.models.combat.label.DamageLabelParser;
import com.wynntils.models.combat.label.KillLabelParser;
import com.wynntils.models.containers.ContainerModel;
import com.wynntils.models.gear.GearModel;
import com.wynntils.models.guild.GuildModel;
import com.wynntils.models.items.annotators.game.IngredientAnnotator;
import com.wynntils.models.items.annotators.game.RuneAnnotator;
import com.wynntils.models.items.annotators.gui.AbilityTreeAnnotator;
import com.wynntils.models.items.annotators.gui.ArchetypeAbilitiesAnnotator;
import com.wynntils.models.items.annotators.gui.CharacterAnnotator;
import com.wynntils.models.items.annotators.gui.GambitAnnotator;
import com.wynntils.models.items.annotators.gui.LeaderboardSeasonAnnotator;
import com.wynntils.models.items.annotators.gui.SkillPointAnnotator;
import com.wynntils.models.items.annotators.gui.TerritoryUpgradeAnnotator;
import com.wynntils.models.lootrun.LootrunModel;
import com.wynntils.models.npc.label.FastTravelLabelParser;
import com.wynntils.models.npc.label.NpcLabelParser;
import com.wynntils.models.players.FriendsModel;
import com.wynntils.models.players.PartyModel;
import com.wynntils.models.profession.label.GatheringNodeHarvestLabelParser;
import com.wynntils.models.raid.RaidModel;
import com.wynntils.models.raid.bossbar.ParasiteOvertakenBar;
import com.wynntils.models.statuseffects.StatusEffectModel;
import com.wynntils.models.territories.GuildAttackTimerModel;
import com.wynntils.models.trademarket.TradeMarketModel;
import com.wynntils.models.war.bossbar.WarTowerBar;
import com.wynntils.models.worlds.BombModel;
import com.wynntils.models.worlds.WorldStateModel;
import com.wynntils.models.worlds.bossbars.InfoBar;
import com.wynntils.models.wynnitem.parsing.WynnItemParser;
import com.wynntils.utils.mc.StyledTextUtils;
import java.lang.reflect.Field;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestRegex {
    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
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
    public void AccountModel_SILVERBULL_DURATION_PATTERN() {
        PatternTester p = new PatternTester(AccountModel.class, "SILVERBULL_DURATION_PATTERN");
        p.shouldMatch("§#00a2e8ff- §7Expiration: §f1 week 5 days");
        p.shouldMatch("§#00a2e8ff- §7Expiration: §f5 days");
        p.shouldMatch("§#00a2e8ff- §7Expiration: §f1 week");
        p.shouldMatch("§#00a2e8ff- §7Expiration: §f2 days 12 hours");
    }

    @Test
    public void ArchetypeAbilitiesAnnotator_ARCHETYPE_NAME() {
        PatternTester p = new PatternTester(ArchetypeAbilitiesAnnotator.class, "ARCHETYPE_NAME");
        p.shouldMatch("§#eb3dfeff§lSharpshooter Archetype");
        p.shouldMatch("§#dae069ff§lBoltslinger Archetype");
        p.shouldMatch("§#87dd47ff§lTrapper Archetype");
        p.shouldMatch("§#60c5cdff§lRiftwalker Archetype");
        p.shouldMatch("§#eb3dfeff§lArcanist Archetype");
        p.shouldMatch("§#f0c435ff§lSummoner Archetype");
        p.shouldMatch("§#87dd47ff§lRitualist Archetype");
        p.shouldMatch("§#ffa057ff§lAcolyte Archetype");
        p.shouldMatch("§#ffa057ff§lFallen Archetype");
        p.shouldMatch("§#dae069ff§lBattle Monk Archetype");
        p.shouldMatch("§#60c5cdff§lPaladin Archetype");
        p.shouldMatch("§#ffa057ff§lShadestepper Archetype");
        p.shouldMatch("§#eb3dfeff§lTrickster Archetype");
        p.shouldMatch("§#b8b0b0ff§lAcrobat Archetype");
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
    public void BombModel_BOMB_BELL_PATTERN() {
        PatternTester p = new PatternTester(BombModel.class, "BOMB_BELL_PATTERN");

        p.shouldMatch(
                "§#fddd5cff\uE01E\uE002 Wanytails has thrown a §#f3e6b2ffProfession Speed Bomb§#fddd5cff on §#f3e6b2ff§nNA3");
        p.shouldMatch(
                "§#fddd5cff\uE001 Wanytails has thrown a §#f3e6b2ffCombat Experience Bomb§#fddd5cff on §#f3e6b2ff§nNA3");
        p.shouldMatch(
                "§#fddd5cff\uE001 xX_NoScope_Xx has thrown a §#f3e6b2ffProfession Experience Bomb§#fddd5cff  on §#f3e6b2ff§nEU5");
    }

    @Test
    public void BombModel_BOMB_THROWN_PATTERN() {
        PatternTester p = new PatternTester(BombModel.class, "BOMB_THROWN_PATTERN");
        p.shouldMatch("§#a0c84bff\uE014\uE002 §lProfession Speed Bomb");
        p.shouldMatch("§#a0c84bff\uE001 §lProfession Experience Bomb");
    }

    @Test
    public void BombModel_BOMB_EXPIRED_PATTERN() {
        PatternTester p = new PatternTester(BombModel.class, "BOMB_EXPIRED_PATTERN");
        p.shouldMatch("§#a0c84bff\uE014\uE002 §#ffd750ff§o§<1>ShadowCat§#a0c84bff Profession Speed Bomb has expired! ");
    }

    @Test
    public void BulkBuyFeature_PRICE_PATTERN() {
        PatternTester p = new PatternTester(BulkBuyFeature.class, "PRICE_PATTERN");
        p.shouldMatch("§6\uDAFF\uDFFC\uF001\uDB00\uDC06 §a✔§6 §f6² §8(6²)");
        p.shouldMatch("§6\uDAFF\uDFFC\uF001\uDB00\uDC06 §c✖§6 §f16,384² §8(4¼²)");
        p.shouldMatch("§6\uDAFF\uDFFC\uF001\uDB00\uDC06 §a✔§6 §f371² §8(5²½ 51²)");
    }

    @Test
    public void ChatPageProcessor_NPC_CONFIRM_PATTERN() {
        PatternTester p = new PatternTester(ChatPageProcessor.class, "NPC_CONFIRM_PATTERN");
        p.shouldMatch("§7Press §fSHIFT §7to continue");
        p.shouldMatch("§4Press §cSNEAK §4to continue");
    }

    @Test
    public void ChatPageProcessor_NPC_SELECT_PATTERN() {
        PatternTester p = new PatternTester(ChatPageProcessor.class, "NPC_SELECT_PATTERN");
        p.shouldMatch("§7Select §fan option §7to continue");
        p.shouldMatch("§cCLICK §4an option to continue");
    }

    @Test
    public void ChatRedirectFeature_LoginRedirector_FOREGROUND_PATTERN() {
        PatternTester p = new PatternTester(ChatRedirectFeature.LoginRedirector.class, "FOREGROUND_PATTERN");
        // hero+ nickname
        p.shouldMatch("§f\uE08A §#d4448cff§oZepart Heal§f §dhas just logged in!");
        // hero
        p.shouldMatch("§f\uE01B §#d44fe0ffShadowFRCS§f §dhas just logged in!");
        // vip+
        p.shouldMatch("§f\uE024 §#4c8dfcff§owater abso§f §3has just logged in!");
        // champion nickname
        p.shouldMatch("§f\uE017 §#e8c00cff§oInfernal Defender§f §6has just logged in!");
    }

    @Test
    public void ContainerModel_ABILITY_TREE_PATTERN() {
        PatternTester p = new PatternTester(ContainerModel.class, "ABILITY_TREE_PATTERN");
        p.shouldMatch("\uDAFF\uDFEA\uE000");
    }

    @Test
    public void DamageBar_DAMAGE_BAR_PATTERN() {
        PatternTester p = new PatternTester(DamageBar.class, "DAMAGE_BAR_PATTERN");
        p.shouldMatch("§cShrieking Observer§r - §c20.6k§4❤");
        p.shouldMatch("§cLongleg Gripper§r - §c3902§4❤§r - §2\uE001Dam §e\uE003§c\uE002Def");
        p.shouldMatch("§cBlinder§r - §c1543§4❤");
        p.shouldMatch("§cLight of Freedom§r - §c107k§4❤§r - §b\uE004Dam \uE004§e\uE003Def");
        p.shouldMatch("§cVoid Vassal§r - §c42.6k§4❤§r - §2\uE001§f\uE000Weak §b\uE004Dam ");
        p.shouldMatch("§cAzure Necromancer§r - §c104k§4❤§r - §b\uE004§e\uE003Dam §b\uE004Def");
        p.shouldMatch("§cCerulean Crustacean§r - §c7559§4❤§r - §2\uE001Weak §b\uE004Dam \uE004§c\uE002Def");
        p.shouldMatch("§cSoul Shrub§r - §c485k§4❤§r - §b\uE004Weak §e\uE003§f\uE000Dam §e\uE003§2\uE001Def");
        p.shouldMatch(
                "§cShift Singularity§r - §c159k§4❤§r - §e\uE003Weak §b\uE004§2\uE001§f\uE000§c\uE002Dam §b\uE004§2\uE001§f\uE000§c\uE002Def");
        p.shouldMatch("§cDespairing Crawler§r - §c3.4m§4❤§r - §e\uE003§c\uE002Dam §b\uE004§2\uE001Def");
        p.shouldMatch(
                "§9§lThe §1§k12345§9§l Anomaly§r - §c27.7m§4❤§r - §b\uE004§e\uE003§f\uE000Dam §b\uE004§e\uE003§f\uE000Def");
    }

    @Test
    public void DamageLabelParser_DAMAGE_LABEL_PATTERN() {
        PatternTester p = new PatternTester(DamageLabelParser.class, "DAMAGE_LABEL_PATTERN");
        p.shouldMatch("§e§l-509 §r§e\uE003 §f§l-398 §r§f\uE000 §c§l-5162 §r§c\uE002 §b§l-386 §r§b\uE004 ");
        p.shouldMatch("§c§l-608 §r§c\uE002 §2§l-219 §r§2\uE001 ");
        p.shouldMatch("§c-387 \uE002 §2-140 \uE001 ");
        p.shouldMatch("§c§l-4089 §r§c\uE002 ");
        p.shouldMatch("§c-2685 \uE002 ");
        p.shouldMatch("§4-6 ❤ ");
    }

    @Test
    public void KillLabelParser_KILL_LABEL_PATTERN() {
        PatternTester p = new PatternTester(KillLabelParser.class, "KILL_LABEL_PATTERN");
        // No guild xp
        p.shouldMatch("§7[§f+483 Combat XP§7]\n[ShadowCat117]");
        // Dxp no guild xp
        p.shouldMatch("§dx2 §7[§f+§d6§f Combat XP§7]\n[ShadowCat117]");
        // Guild xp
        p.shouldMatch("§7[§f+0 Combat XP§7]\n[§f+11 Guild XP§7]\n[ShadowCat117]");
        // Dxp guild xp
        p.shouldMatch("§dx2 §7[§f+§d0§f Combat XP§7]\n§dx2 §7[§f+§d2132§f Guild XP§7]\n[ShadowCat117]");
        // Guild xp with blessing
        p.shouldMatch("§7[§f+0 Combat XP§7]\n§bx1.1 §7[§f+§b1058§f Guild XP§7]\n[ShadowCat117]");
        // Dxp guild xp with blessing
        p.shouldMatch("§dx2 §7[§f+§d0§f Combat XP§7]\n§dx2 §bx1.1 §7[§f+§b1661§f Guild XP§7]\n[ShadowCat117]");
    }

    @Test
    public void FastTravelLabelParser_FAST_TRAVEL_LABEL_PATTERN() {
        PatternTester p = new PatternTester(FastTravelLabelParser.class, "FAST_TRAVEL_LABEL_PATTERN");

        p.shouldMatch(
                "§#8193ffff\uE060\uDAFF\uDFFF\uE035\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE042\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE061\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE045\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE03B\uDAFF\uDFFF\uE062\uDAFF\uDFBE§0\uE005\uE000\uE012\uE013 \uE013\uE011\uE000\uE015\uE004\uE00B\uDB00\uDC02§#8193ffff\n§#f9e79effGate of Recall\n§0 \n§7\uE01C §oTo Lutho§r§7 \uE01C\n§0 ");
        p.shouldMatch(
                "§#8193ffff\uE060\uDAFF\uDFFF\uE035\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE042\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE061\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE045\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE03B\uDAFF\uDFFF\uE062\uDAFF\uDFBE§0\uE005\uE000\uE012\uE013 \uE013\uE011\uE000\uE015\uE004\uE00B\uDB00\uDC02§#8193ffff\n§#f9e79effMysterious Obelisk\n§0 \n§7\uE01C §oTo Nemract§r§7 \uE01C\n§0 \n§f\uE001§7 Right-Click to interact\n§0 ");
        p.shouldMatch(
                "§#8193ffff\uE060\uDAFF\uDFFF\uE035\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE042\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE061\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE045\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE03B\uDAFF\uDFFF\uE062\uDAFF\uDFBE§0\uE005\uE000\uE012\uE013 \uE013\uE011\uE000\uE015\uE004\uE00B\uDB00\uDC02§#8193ffff\n§#f9e79effMysterious Obelisk\n§0 \n§7\uE01C §oTo Tempo Town§r§7 \uE01C\n§0 \n§f\uE001§7 Right-Click to interact\n§0 ");
        p.shouldMatch(
                "§#8193ffff\uE060\uDAFF\uDFFF\uE035\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE042\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE061\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE041\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE045\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE03B\uDAFF\uDFFF\uE062\uDAFF\uDFBE§0\uE005\uE000\uE012\uE013 \uE013\uE011\uE000\uE015\uE004\uE00B\uDB00\uDC02§#8193ffff\n§#f9e79effThe Nexus\n§0 \n§7\uE01C §oTo the Nexus Hub§r§7 \uE01C\n§0 ");
    }

    @Test
    public void FriendsModel_ONLINE_FRIENDS_HEADER() {
        PatternTester p = new PatternTester(FriendsModel.class, "ONLINE_FRIENDS_HEADER");
        p.shouldMatch("§a\uE001 Online Friends:");
        p.shouldMatch("§a\uE008\uE002 Online Friends:");
    }

    @Test
    public void FriendsModel_ONLINE_FRIEND() {
        PatternTester p = new PatternTester(FriendsModel.class, "ONLINE_FRIEND");
        p.shouldMatch("§a\uE001 §2 - §aShadowCat118§2 [Server: §aNA11§2]");
    }

    @Test
    public void FriendsModel_JOIN_PATTERN() {
        PatternTester p = new PatternTester(FriendsModel.class, "JOIN_PATTERN");
        p.shouldMatch("§aShadowCat118§2 has logged into server §aEU16§2 as §aa Shaman");
    }

    @Test
    public void FriendsModel_LEAVE_PATTERN() {
        PatternTester p = new PatternTester(FriendsModel.class, "LEAVE_PATTERN");
        p.shouldMatch("§amag_icus left the game.");
        p.shouldMatch("§aShadowCat118 left the game.");
    }

    @Test
    public void GatheringNodeHarvestLabelParser_EXPERIENCE_PATTERN() {
        PatternTester p = new PatternTester(GatheringNodeHarvestLabelParser.class, "EXPERIENCE_PATTERN");

        p.shouldMatch("§f+3852 §7Ⓑ Mining XP §6[0%]");
        p.shouldMatch("§f+2660 §7Ⓒ Woodcutting XP §6[1.75%]");
        p.shouldMatch("§#ffd750ff[§#a0c84bffx2§#ffd750ff] §#a0c84bff+4252 §7Ⓒ Woodcutting XP §6[2.13%]");
        p.shouldMatch("§#ffd750ff[§#a0c84bffx2§#ffd750ff] §#a0c84bff+3670 §7Ⓚ Fishing XP §6[1.69%]");
    }

    @Test
    public void GatheringNodeHarvestLabelParser_HARVEST_PATTERN() {
        PatternTester p = new PatternTester(GatheringNodeHarvestLabelParser.class, "HARVEST_PATTERN");

        p.shouldMatch("§f+1 §7Sky Wood§6 [§e✫§8✫✫§6]");
        p.shouldMatch("§f+1 §7Starfish Oil§6 [§e✫§8✫✫§6]");
        p.shouldMatch("§f+1 §7Hemp String§6 [§e✫§8✫✫§6]");
        p.shouldMatch("§f+1 §7Diamond Ingot§6 [§e✫§8✫✫§6]");
        p.shouldMatch("§#ffd750ff[§#a0c84bffx2§#ffd750ff] §#a0c84bff+2 §7Sky Wood§6 [§e✫§8✫✫§6]");
    }

    @Test
    public void GuildAttackTimerModel_CAPTURED_PATTERN() {
        PatternTester p = new PatternTester(GuildAttackTimerModel.class, "CAPTURED_PATTERN");
        p.shouldMatch("§c\uE001 [YCY] captured the territory Paper Trail.");
        p.shouldMatch("§c\uE001 [ANO] captured the territory Collapsed Bridge.");
        p.shouldMatch("§c\uE006\uE002 [Tsd] captured the territory Paper Trail.");
    }

    @Test
    public void GuildAttackTimerModel_WAR_MESSAGE_PATTERN() {
        PatternTester p = new PatternTester(GuildAttackTimerModel.class, "WAR_MESSAGE_PATTERN");
        p.shouldMatch("§c\uE006\uE002 The war for Detlas will start in 1 minute.");
        p.shouldMatch("§c\uE001 The war for Detlas will start in 1 minute.");
        p.shouldMatch("§c\uE006\uE002 The war for Detlas will start in 2 minutes.");
        p.shouldMatch("§c\uE006\uE002 The war for Detlas will start in 1 minute and 30 seconds.");
        p.shouldMatch("§c\uE006\uE002 The war for Detlas Close Suburbs will start in 30 seconds.");
    }

    @Test
    public void GuildModel_GUILD_NAME_MATCHER() {
        PatternTester p = new PatternTester(GuildModel.class, "GUILD_NAME_MATCHER");
        // Guild menu item
        p.shouldMatch("§3guildName§b [aAaA]");
        p.shouldMatch("§3guild Name§b [aaaa]");
        p.shouldMatch("§3GUILD NAME§b [wynn]");
        // Allied guild item
        p.shouldMatch("§a§lGUILD NAME [wynn]");
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
    public void GuildModel_LEVEL_MATCHER() {
        PatternTester p = new PatternTester(GuildModel.class, "LEVEL_MATCHER");
        p.shouldMatch("§b§lChiefs Of Corkus§3§l [Lv. 87]");
    }

    @Test
    public void GuildModel_LEVEL_PROGRESS_MATCHER() {
        PatternTester p = new PatternTester(GuildModel.class, "LEVEL_PROGRESS_MATCHER");
        p.shouldMatch("§f20,588,573,849§7/25,447,702,087 XP");
    }

    @Test
    public void GuildModel_MEMBER_LEFT() {
        PatternTester p = new PatternTester(GuildModel.class, "MEMBER_LEFT");
        p.shouldMatch("§b\uE006\uE002 ShadowCat118 has left the guild");
    }

    @Test
    public void GuildModel_MEMBER_JOIN() {
        PatternTester p = new PatternTester(GuildModel.class, "MEMBER_JOIN");
        p.shouldMatch("§b\uE001 ShadowCat118 has joined the guild, say hello!");
    }

    @Test
    public void GuildModel_MEMBER_KICKED() {
        PatternTester p = new PatternTester(GuildModel.class, "MEMBER_KICKED");
        p.shouldMatch("§b\uE006\uE002 ShadowCat117 has kicked ShadowCat118 from the guild");
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
        p.shouldMatch("§b\uE006\uE002 ShadowCat117 has set ShadowCat118 guild rank from §3 Recruit§b to §3Strategist");
    }

    @Test
    public void GuildModel_MSG_OBJECTIVE_COMPLETED() {
        PatternTester p = new PatternTester(GuildModel.class, "MSG_OBJECTIVE_COMPLETED");
        p.shouldMatch("§3[INFO]§b Flyxdre has finished their weekly objective.");
        p.shouldMatch("§3[INFO]§b ExamplePlayer1 has finished their weekly objective.");
    }

    @Test
    public void GuildModel_MSG_NEW_OBJECTIVES() {
        PatternTester p = new PatternTester(GuildModel.class, "MSG_NEW_OBJECTIVES");
        p.shouldMatch("§3[INFO]§b New Weekly Guild Objectives are being assigned.");
    }

    @Test
    public void GuildModel_MSG_TRIBUTE_SCHEDULED() {
        PatternTester p = new PatternTester(GuildModel.class, "MSG_TRIBUTE_SCHEDULED");
        p.shouldMatch("§3[INFO]§b ExamplePlayer1 scheduled 1 Emerald per hour to Example Guild");
        p.shouldMatch("§3[INFO]§b ExamplePlayer1 scheduled Ⓙ 1 Crop per hour to Example Guild");
        p.shouldMatch("§3[INFO]§b ExamplePlayer1 scheduled Ⓚ 1 Fish per hour to Example Guild");
        p.shouldMatch("§3[INFO]§b ExamplePlayer1 scheduled Ⓒ 1 Wood per hour to Example Guild");
        p.shouldMatch("§3[INFO]§b ExamplePlayer1 scheduled Ⓑ 1 Ore per hour to Example Guild");
        p.shouldMatch("§3[INFO]§b ExamplePlayer1 scheduled 2 Emeralds per hour to Example Guild");
        p.shouldMatch("§3[INFO]§b ExamplePlayer1 scheduled Ⓙ 2 Crops per hour to Example Guild");
        p.shouldMatch("§3[INFO]§b ExamplePlayer1 scheduled Ⓚ 2 Fish per hour to Example Guild");
        p.shouldMatch("§3[INFO]§b ExamplePlayer1 scheduled Ⓒ 2 Wood per hour to Example Guild");
        p.shouldMatch("§3[INFO]§b ExamplePlayer1 scheduled Ⓑ 2 Ore per hour to Example Guild");
    }

    @Test
    public void GuildModel_MSG_TRIBUTE_STOPPED() {
        PatternTester p = new PatternTester(GuildModel.class, "MSG_TRIBUTE_STOPPED");
        p.shouldMatch("§3[INFO]§b ExamplePlayer1 stopped scheduling Emeralds to Example Guild");
        p.shouldMatch("§3[INFO]§b ExamplePlayer1 stopped scheduling Fish to Example Guild");
        p.shouldMatch("§3[INFO]§b ExamplePlayer1 stopped scheduling Ore to Example Guild");
        p.shouldMatch("§3[INFO]§b ExamplePlayer1 stopped scheduling Wood to Example Guild");
        p.shouldMatch("§3[INFO]§b ExamplePlayer1 stopped scheduling Crops to Example Guild");
        p.shouldMatch("§3[INFO]§b Example Guild stopped scheduling Emeralds to Example Guild");
    }

    @Test
    public void GuildModel_MSG_ALLIANCE_FORMED() {
        PatternTester p = new PatternTester(GuildModel.class, "MSG_ALLIANCE_FORMED");
        p.shouldMatch("§3[INFO]§b ExamplePlayer1 formed an alliance with Example Guild");
        p.shouldMatch("§3[INFO]§b Example Guild formed an alliance with Example Guild");
    }

    @Test
    public void GuildModel_MSG_ALLIANCE_REVOKED() {
        PatternTester p = new PatternTester(GuildModel.class, "MSG_ALLIANCE_REVOKED");
        p.shouldMatch("§3[INFO]§b Example Guild revoked the alliance with Example Guild");
        p.shouldMatch("§3[INFO]§b ExamplePlayer1 revoked the alliance with Example Guild");
    }

    @Test
    public void GuildModel_OBJECTIVES_COMPLETED_PATTERN() {
        PatternTester p = new PatternTester(GuildModel.class, "OBJECTIVES_COMPLETED_PATTERN");
        p.shouldMatch("§6Current Guild Goal: §f23§7/30");
    }

    @Test
    public void GuildModel_OBJECTIVE_STREAK_PATTERN() {
        PatternTester p = new PatternTester(GuildModel.class, "OBJECTIVE_STREAK_PATTERN");
        p.shouldMatch("§a- §7Streak: §f14");
    }

    @Test
    public void GuildModel_TRIBUTE_PATTERN() {
        PatternTester p = new PatternTester(GuildModel.class, "TRIBUTE_PATTERN");
        p.shouldMatch("§fⒷ -1000 Ore per Hour");
        p.shouldMatch("§6Ⓒ -1000 Wood per Hour");
        p.shouldMatch("§bⓀ -11000 Fish per Hour");
        p.shouldMatch("§eⒿ -1000 Crops per Hour");
        p.shouldMatch("§fⒷ -1 Ore per Hour");
        p.shouldMatch("§6Ⓒ -1 Wood per Hour");
        p.shouldMatch("§bⓀ -1 Fish per Hour");
        p.shouldMatch("§eⒿ -1 Crop per Hour");
        p.shouldMatch("§a-1 Emerald per Hour");
        p.shouldMatch("§a-2 Emeralds per Hour");
        p.shouldMatch("§a+9000 Emeralds per Hour");
    }

    @Test
    public void GuildModel_ALLIED_GUILD_PATTERN() {
        PatternTester p = new PatternTester(GuildModel.class, "ALLIED_GUILD_PATTERN");
        p.shouldMatch("§a- §7GUILD NAME [wynn]");
    }

    @Test
    public void InfoBar_BOMB_INFO_PATTERN() {
        PatternTester p = new PatternTester(InfoBar.class, "BOMB_INFO_PATTERN");
        p.shouldMatch("§#a0c84bffProfession Speed from §#ffd750ffRoseGeckoOlaf955§#a0c84bff §7[§f3m§7]");
        p.shouldMatch("§#a0c84bffDouble Profession Experience from §#ffd750ffRoseGeckoOlaf955§#a0c84bff §7[§f13m§7]");
    }

    @Test
    public void InfoBar_GUILD_INFO_PATTERN() {
        PatternTester p = new PatternTester(InfoBar.class, "GUILD_INFO_PATTERN");
        p.shouldMatch("§7Lv. 92§f - §bKingdom Foxes§f - §762% XP");
    }

    @Test
    public void InfoBar_TERRITORY_INFO_PATTERN() {
        PatternTester p = new PatternTester(InfoBar.class, "TERRITORY_INFO_PATTERN");
        p.shouldMatch(
                "§aNexus of Light§2 \uE060\uDAFF\uDFFF\uE03C\uDAFF\uDFFF\uE034\uDAFF\uDFFF\uE03B\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE062\uDAFF\uDFE6§f\uE00C\uE004\uE00B\uE013\uDB00\uDC02"); // MELT tag
        p.shouldMatch(
                "§bFleris Cranny§3 \uE060\uDAFF\uDFFF\uE037\uDAFF\uDFFF\uE03E\uDAFF\uDFFF\uE032\uDAFF\uDFFF\uE062\uDAFF\uDFEC§f\uE007\uE00E\uE002\uDB00\uDC02"); // HOC tag
        p.shouldMatch(
                "§cCinfras§4 \uE060\uDAFF\uDFFF\uE038\uDAFF\uDFFF\uE032\uDAFF\uDFFF\uE03E\uDAFF\uDFFF\uE062\uDAFF\uDFEE§f\uE008\uE002\uE00E\uDB00\uDC02"); // ICO tag
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
    public void OphanimBar_OPHANIM_PATTERN() {
        PatternTester p = new PatternTester(OphanimBar.class, "OPHANIM_PATTERN");
        p.shouldMatch("§710s Healed: §f66% §3[§b⏺⏺⏺⏺⏺⏺§3]");
        p.shouldMatch("§710s Healed: §f0% §3[§b⏺§b⏺§b⏺§b⏺§b⏺§b⏺§3]");
        p.shouldMatch("§710s Healed: §f0% §4[§c⏺§c⏺§c⏺§c⏺§c⏺§7⏺§4]");
        p.shouldMatch("§710s Healed: §f0% §4[§e⏺§e⏺§e⏺§c⏺§c⏺§e⏺§4]");
        p.shouldMatch("§710s Healed: §f0% §6[§e⏺§e⏺§e⏺§e⏺§e⏺§e⏺§6]");
        p.shouldMatch("§710s Healed: §f22% §3[§b⏺§b⏺§b⏺§b⏺§b⏺§b⏺§3]");
        p.shouldMatch("§710s Healed: §f12% §3[§e⏺§b⏺§e⏺§b⏺⏺⏺§3]");
        p.shouldMatch("§710s Healed: §f0% §4[§c⏺⏺⏺§e⏺⏺⏺§4]");
        p.shouldMatch("§710s Healed: §f0% §8[]");
    }

    @Test
    public void LeaderboardSeasonAnnotator_SEASON_PATTERN() {
        PatternTester p = new PatternTester(LeaderboardSeasonAnnotator.class, "SEASON_PATTERN");
        p.shouldMatch("§d§lSeason 0");
        p.shouldMatch("§d§lSeason 20");
    }

    @Test
    public void LootrunModel_BEACONS_PATTERN() {
        PatternTester p = new PatternTester(LootrunModel.class, "BEACONS_PATTERN");
        p.shouldMatch("\uDB00\uDC0A§#ff00ffff§lVibrant Purple Beacon§r\uDB00\uDC1B§#5c5ce6ff§lVibrant Blue Beacon");
        p.shouldMatch("\uDB00\uDC0C§#ffff33ff§lVibrant Yellow Beacon§r\uDB00\uDC1F§#ff0000ff§lVibrant Red Beacon");
        p.shouldMatch(
                "\uDAFF\uDFFF§#808080ff§lVibrant Dark Grey Beacon§r\uDB00\uDC07§#ff9500ff§lVibrant Orange Beacon");
        p.shouldMatch("\uDB00\uDC23§#ff00ffff§lPurple Beacon§r\uDB00\uDC4B§b§lAqua Beacon");
        p.shouldMatch("\uDB00\uDC2A§#5c5ce6ff§lBlue Beacon§r\uDB00\uDC4F§#ffff33ff§lYellow Beacon");
        p.shouldMatch("\uDB00\uDC0A§#ff00ffff§lVibrant Purple Beacon§r\uDB00\uDC34§#5c5ce6ff§lBlue Beacon");
        p.shouldMatch("\uDB00\uDC25§#ffff33ff§lYellow Beacon§r\uDB00\uDC46§#ff9500ff§lOrange Beacon");
        p.shouldMatch("\uDB00\uDC72§#ffff33ff§lYellow Beacon");
        p.shouldMatch("\uDB00\uDC60§#ff0000ff§lVibrant Red Beacon");
    }

    @Test
    public void LootrunModel_ORANGE_AMOUNT_PATTERN() {
        PatternTester p = new PatternTester(LootrunModel.class, "ORANGE_AMOUNT_PATTERN");
        p.shouldMatch("§7\uDB00\uDC2DReward Pulls\uDB00\uDC50for 5 Challenges");
        p.shouldMatch("§7\uDB00\uDC70for 5 Challenges");
        p.shouldMatch("§7\uDB00\uDC4D\uDB00\uDC70for 5 Challenges");
    }

    @Test
    public void LootrunModel_RAINBOW_AMOUNT_PATTERN() {
        PatternTester p = new PatternTester(LootrunModel.class, "RAINBOW_AMOUNT_PATTERN");
        p.shouldMatch("§7\uDB00\uDC1Ethis Challenge only\uDB00\uDC3Bnext 10 Challenges");
        p.shouldMatch("§7\uDB00\uDC6Anext 20 Challenges");
    }

    @Test
    public void MessageFilterFeature_PARTY_FINDER_FG() {
        PatternTester p = new PatternTester(MessageFilterFeature.class, "PARTY_FINDER_FG");
        p.shouldMatch(
                "§5\uE00A\uE002 Party Finder:§d Hey §oShadowCat§r§d, over here! Join the §bNest of the Grootslangs§d queue and match up with §e3§d other players!");
        p.shouldMatch(
                "§5\uE00A\uE002 Party Finder:§d Hey §oShadowCat§r§d, over here! Join the §bTheNameless Anomaly§d queue and match up with §e3§d other players!");
    }

    @Test
    public void MessageFilterFeature_PARTY_FINDER_BG() {
        PatternTester p = new PatternTester(MessageFilterFeature.class, "PARTY_FINDER_BG");
        p.shouldMatch(
                "§8\uE00A\uE002 Party Finder: Hey §oShadowCat§r§8, over here! Join the TheNameless Anomaly queue and match up with 3 other players!");
    }

    @Test
    public void MessageFilterFeature_SYSTEM_INFO_FG() {
        PatternTester p = new PatternTester(MessageFilterFeature.class, "SYSTEM_INFO_FG");
        p.shouldMatch(
                "§#a0aec0ff\uE01B\uE002 Follow us on Twitter to stay up to date with Wynncraft at §#77aefcffwynn.gg/twitter");
    }

    @Test
    public void MessageFilterFeature_SYSTEM_INFO_BG() {
        PatternTester p = new PatternTester(MessageFilterFeature.class, "SYSTEM_INFO_BG");
        p.shouldMatch(
                "§#c0c0c0ff\uE01B\uE002 Follow us on Twitter to stay up to date with Wynncraft at §#fcfcfcffwynn.gg/twitter");
    }

    @Test
    public void BonusTotemLabelParser_BONUS_TOTEM_PATTERN() {
        PatternTester p = new PatternTester(BonusTotemLabelParser.class, "BONUS_TOTEM_PATTERN");
        p.shouldMatch("§#ffd750ff§oShadowCat§r§#ffd750ff's§#a0c84bff Mob Totem\n§d\uE01F §74m 59s");
        p.shouldMatch("§#ffd750ff§oShadowCat§r§#ffd750ff's§#a0c84bff Mob Totem\n§d\uE01F §749s");
        p.shouldMatch("§#ffd750ffConventionality's§#a0c84bff Gathering Totem\n§d\uE01F §74m 40s");
    }

    @Test
    public void ParasiteOvertakenBar_OVERTAKEN_PATTERN() {
        PatternTester p = new PatternTester(ParasiteOvertakenBar.class, "OVERTAKEN_PATTERN");
        p.shouldMatch(
                "§#aa00ffff|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF§r\uDAFF\uDF81§fOVERTAKEN\uDB00\uDC49");
        p.shouldMatch(
                "§#aa00ffff|\uDAFF\uDFFF§8|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF§r\uDAFF\uDF81§fOVERTAKEN\uDB00\uDC49");
        p.shouldMatch(
                "§8|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF|\uDAFF\uDFFF§r\uDAFF\uDF81§fOVERTAKEN\uDB00\uDC49");
    }

    @Test
    public void PersonalStorageUtilitiesFeature_PAGE_PATTERN() {
        PatternTester p = new PatternTester(PersonalStorageUtilitiesFeature.class, "PAGE_PATTERN");
        p.shouldMatch("§7- §f\uE006§8 Page 1");
        p.shouldMatch("§7- §f\uE007§8 Page 3");
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
    public void RecipientType_GLOBAL_backgroundPattern() {
        PatternTester p = new PatternTester(RecipientType.GLOBAL, "backgroundPattern");
        p.shouldMatch(
                "§7\uE056\uE042\uE061\uE061§r §8\uE010\u2064\uE00F\uE012\uE063\uE00E\uE012\uE02D\u2064\uE011\u2064§r\uE013\uE013\u2064\u2064\u2064§7kristof345: §8b");
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

        // The reset code after the server is not always present
        p.shouldMatch(
                "§f\uE056\uE042\uE061\uE061 §8\uE010\u2064\uE070§f\uE071\uE061§8\uE00F§f\uE012\uE060§8\uE00F§f\uE012\uE065§8\uE00E§f\uE012\u2064\u2064\uE02C§8\uE00F§f\uE012\uE040§8\uE00F§f\uE012\uE04D§8\uE00F§f\uE012\uE04E§8\uE011\u2064§r §#54fcfcff\uE07D \u2064§r\uE017\uE013\u2064\u2064§#ffe600ff§obol§r§#ffe600ff: §fc");
    }

    @Test
    public void RecipientType_LOCAL_backgroundPattern() {
        PatternTester p = new PatternTester(RecipientType.LOCAL, "backgroundPattern");
        p.shouldMatch(
                "§8\uE056\uE042\uE061\uE061§r §#a8a8a8ff\uE010\u2064\uE00F§8\uE012\uE063§#a8a8a8ff\uE00E§8\uE012\uE02D\u2064§#a8a8a8ff\uE011\u2064§r\uE013\uE013\u2064\u2064\u2064§7kristof345: §8a");
    }

    @Test
    public void RecipientType_GUILD_foregroundPattern() {
        PatternTester p = new PatternTester(RecipientType.GUILD, "foregroundPattern");
        // Message is <guild prefix> <rank background> <rank name> <player name>: <message>
        p.shouldMatch(
                "§b\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE \uE060\uDAFF\uDFFF\uE032\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE03F\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE038\uDAFF\uDFFF\uE03D\uDAFF\uDFFF\uE062\uDAFF\uDFD6§0\uE002\uE000\uE00F\uE013\uE000\uE008\uE00D\uDB00\uDC02§b §3§obol§r§3:§b test");
    }

    @Test
    public void RecipientType_GUILD_backgroundPattern() {
        PatternTester p = new PatternTester(RecipientType.GUILD, "backgroundPattern");
        p.shouldMatch(
                "§8\uDAFF\uDFFC\uE006\uDAFF\uDFFF\uE002\uDAFF\uDFFE \uE060\uDAFF\uDFFF\uE032\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE03F\uDAFF\uDFFF\uE043\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE038\uDAFF\uDFFF\uE03D\uDAFF\uDFFF\uE062\uDAFF\uDFD6\uE002\uE000\uE00F\uE013\uE000\uE008\uE00D\uDB00\uDC02 §obol§r§8: test");
    }

    @Test
    public void RecipientType_PARTY_foregroundPattern() {
        PatternTester p = new PatternTester(RecipientType.PARTY, "foregroundPattern");
        p.shouldMatch("§e\uDAFF\uDFFC\uE005\uDAFF\uDFFF\uE002\uDAFF\uDFFE You must leave your current party first.");
        p.shouldMatch("§e\uDAFF\uDFFC\uE001\uDB00\uDC06 You must leave your current party first.");
        p.shouldMatch("§e\uDAFF\uDFFC\uE005\uDAFF\uDFFF\uE002\uDAFF\uDFFE §obol§r§e: §fasd");
    }

    @Test
    public void RecipientType_PARTY_backgroundPattern() {
        PatternTester p = new PatternTester(RecipientType.PARTY, "backgroundPattern");
        p.shouldMatch("§8\uDAFF\uDFFC\uE005\uDAFF\uDFFF\uE002\uDAFF\uDFFE This player is already in your party");
        p.shouldMatch("§8\uDAFF\uDFFC\uE001\uDB00\uDC06 kristof345: asd");
    }

    @Test
    public void RecipientType_PRIVATE_foregroundpattern() {
        PatternTester p = new PatternTester(RecipientType.PRIVATE, "foregroundPattern");
        p.shouldMatch(
                "§#ddcc99ff\uDAFF\uDFFC\uE007\uDAFF\uDFFF\uE002\uDAFF\uDFFE §#e8c00cff§oShadowCat§#ddcc99ff \uE003 §#e8c00cff§oShadowCat§r§#e8c00cff§[1]:§#ddcc99ff §fHi ");
    }

    @Test
    public void RecipientType_PRIVATE_backgroundPattern() {
        PatternTester p = new PatternTester(RecipientType.PRIVATE, "backgroundPattern");
        p.shouldMatch(
                "§#ddddddff\uDAFF\uDFFC\uE001\uDB00\uDC06 §#e8e8e8ff§oShadowCat§#ddddddff \uE003 §#e8e8e8ff§oShadowCat§r§#e8e8e8ff§[1]:§#ddddddff §8Hi ");
    }

    @Test
    public void RecipientType_SHOUT_foregroundPattern() {
        PatternTester p = new PatternTester(RecipientType.SHOUT, "foregroundPattern");
        p.shouldMatch(
                "§#bd45ffff\uDAFF\uDFFC\uE015\uDAFF\uDFFF\uE002\uDAFF\uDFFE §oShadowCat§r§#bd45ffff \uE060\uDAFF\uDFFF\uE03D\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE056\uDAFF\uDFFF\uE062\uDAFF\uDFEC§0\uE00D\uE000\uE026\uDB00\uDC02§#bd45ffff shouts: §#fad9f7ffo/");
        p.shouldMatch(
                "§#bd45ffff\uDAFF\uDFFC\uE015\uDAFF\uDFFF\uE002\uDAFF\uDFFE CBI2004 \uE060\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE042\uDAFF\uDFFF\uE051\uDAFF\uDFFF\uE062\uDAFF\uDFEE§0\uE000\uE012\uE021\uDB00\uDC02§#bd45ffff shouts: §#fad9f7ffAeq recruit AS raider or join notg\n§#bd45ffff\uDAFF\uDFFC\uE001\uDB00\uDC06 §#fad9f7ffas pf or invite me as pf we got 2ppl rn. /msg CBI2004 if\n§#bd45ffff\uDAFF\uDFFC\uE001\uDB00\uDC06 §#fad9f7ffinterested");
    }

    @Test
    public void RecipientType_SHOUT_backgroundPattern() {
        PatternTester p = new PatternTester(RecipientType.SHOUT, "backgroundPattern");
        p.shouldMatch(
                "§f\uDAFF\uDFFC\uE015\uDAFF\uDFFF\uE002\uDAFF\uDFFE §oShadowCat§r§f \uE060\uDAFF\uDFFF\uE03D\uDAFF\uDFFF\uE030\uDAFF\uDFFF\uE056\uDAFF\uDFFF\uE062\uDAFF\uDFEC§8\uE00D\uE000\uE026\uDB00\uDC02§f shouts: §#fafafafftest");
    }

    @Test
    public void RecipientType_PETS_foregroundPattern() {
        PatternTester p = new PatternTester(RecipientType.PETS, "foregroundPattern");
        p.shouldMatch("§6\uDAFF\uDFFC\uE016\uDAFF\uDFFF\uE002\uDAFF\uDFFE Duck: §#ffdd99ff§oquack");
        p.shouldMatch("§6\uDAFF\uDFFC\uE001\uDB00\uDC06 §oCosmo§r§6: §#ffdd99ff§obreezy squeak");
        p.shouldMatch(
                "§6\uDAFF\uDFFC\uE016\uDAFF\uDFFF\uE002\uDAFF\uDFFE §oHanafubuki§r§6: §#ffdd99ffThose grooks look awfully... tempting.");
    }

    @Test
    public void RecipientType_PETS_backgroundPattern() {
        PatternTester p = new PatternTester(RecipientType.PETS, "backgroundPattern");
        p.shouldMatch("§f\uDAFF\uDFFC\uE001\uDB00\uDC06 §oKlutzy§r§f: §ofalls over");
        p.shouldMatch(
                "§f\uDAFF\uDFFC\uE016\uDAFF\uDFFF\uE002\uDAFF\uDFFE §oHanafubuki§r§f: Watch the eye pal, watch the eye!");
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
    public void ShamanTotemModel_SHAMAN_TOTEM_TIMER_PATTERN() {
        PatternTester p = new PatternTester(ShamanTotemModel.class, "SHAMAN_TOTEM_TIMER");
        // Timer only
        p.shouldMatch("§bShadowCat117's §7Totem\n§d\uE01F §77s");
        // Timer + regen
        p.shouldMatch("§bShadowCat117's §7Totem\n§c+1644❤§7/s §d\uE01F §753s");
        // Timer + summons attack speed
        p.shouldMatch("§bShadowCat117's §7Totem\n§e\uE013 §71s §d\uE01F §749s");
        // Timer + regen + summons attack speed
        p.shouldMatch("§bShadowCat117's §7Totem\n§c+986❤§7/s §e\uE013 §71s §d\uE01F §750s");
    }

    @Test
    public void SkillPointAnnotator_SKILL_POINT_PATTERN() {
        PatternTester p = new PatternTester(SkillPointAnnotator.class, "SKILL_POINT_PATTERN");
        p.shouldMatch("\uDB00\uDC07§dUpgrade your §2\uE001 Strength§d skill");
        p.shouldMatch("\uDB00\uDC06§dUpgrade your §e\uE003 Dexterity§d skill");
        p.shouldMatch("\uDB00\uDC00§dUpgrade your §b\uE004 Intelligence§d skill");
        p.shouldMatch("\uDB00\uDC09§dUpgrade your §c\uE002 Defence§d skill");
        p.shouldMatch("\uDB00\uDC0F§dUpgrade your §f\uE000 Agility§d skill");
    }

    @Test
    public void SkillPointAnnotator_LORE_PATTERN() {
        PatternTester p = new PatternTester(SkillPointAnnotator.class, "LORE_PATTERN");
        p.shouldMatch("\uDB00\uDC03§7116 points§r\uDB00\uDC10       \uDB00\uDC10§6117 points");
        p.shouldMatch("\uDB00\uDC03§7110 points§r\uDB00\uDC10       \uDB00\uDC10§6111 points");
        p.shouldMatch("\uDB00\uDC06§798 points§r\uDB00\uDC13       \uDB00\uDC13§699 points");
        p.shouldMatch("\uDB00\uDC03§7162 points§r\uDB00\uDC10       \uDB00\uDC10§6163 points");
        p.shouldMatch("\uDB00\uDC06§790 points§r\uDB00\uDC13       \uDB00\uDC13§691 points");
        p.shouldMatch("\uDB00\uDC03§7-29 points§r\uDB00\uDC10       \uDB00\uDC10§6-28 points");
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
    public void TerritoryUpgradeAnnotator_TERRITORY_UPGRADE_PATTERN() {
        PatternTester p = new PatternTester(TerritoryUpgradeAnnotator.class, "TERRITORY_UPGRADE_PATTERN");
        p.shouldMatch("§6§lDamage §7[Lv. 10]");
        p.shouldMatch("§d§lEmerald Rate §7[Lv. 2]");
        p.shouldMatch("§d§lTower Aura §7[Lv. 3]§8 (Max)");
        p.shouldMatch("§d§lEmerald Seeking §7[Lv. 5]§8 (Max)");
    }

    @Test
    public void TradeMarketModel_PRICE_CHECK_BID_PATTERN() {
        PatternTester p = new PatternTester(TradeMarketModel.class, "PRICE_CHECK_BID_PATTERN");
        p.shouldMatch("§7Highest Buy Offer: §f806 §8(12²½ 38²)");
    }

    @Test
    public void TradeMarketModel_PRICE_CHECK_ASK_PATTERN() {
        PatternTester p = new PatternTester(TradeMarketModel.class, "PRICE_CHECK_ASK_PATTERN");
        p.shouldMatch("§7Cheapest Sell Offer: §f806 §8(12²½ 38²)");
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
    public void WynnItemParser_ITEM_ATTACK_SPEED_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "ITEM_ATTACK_SPEED_PATTERN");
        p.shouldMatch("§7Very Fast Attack Speed§r");
        p.shouldMatch("§7Slow Attack Speed§r");
    }

    @Test
    public void WynnItemParser_ITEM_DAMAGE_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "ITEM_DAMAGE_PATTERN");
        p.shouldMatch("§c\uE002 Fire§7 Damage: 38-42§r");
        p.shouldMatch("§2\uE001 Earth§7 Damage: 105-145§r");
        p.shouldMatch("§6\uE005 Neutral Damage: 372-455§r");
    }

    @Test
    public void WynnItemParser_ITEM_DEFENCE_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "ITEM_DEFENCE_PATTERN");
        p.shouldMatch("§2\uE001 Earth§7 Defence: +40§r");
        p.shouldMatch("§e\uE003 Thunder§7 Defence: +28§r");
        p.shouldMatch("§b\uE004 Water§7 Defence: +94§r");
        p.shouldMatch("§c\uE002 Fire§7 Defence: +40§r");
        p.shouldMatch("§f\uE000 Air§7 Defence: +40§r");
        p.shouldMatch("§2\uE001 Earth§7 Defence: -100§r");
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
        p.shouldMatch("§eUnique Item [2]");
        p.shouldMatch("§cFabled Item");
        p.shouldMatch("§aSet Item [2]");
        p.shouldMatch("§fNormal Item");
        p.shouldMatch("§dRare Item");

        // Crafted gear
        p.shouldMatch("§3Crafted by AveMarisStella §8[68/68 Durability]");
        p.shouldMatch("§3Crafted by XrnThePyrolysed §8[339/339 Durability]§r");
    }

    @Test
    public void WynnItemParser_POWDER_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "POWDER_PATTERN");
        p.shouldMatch("§7[2/2] Powder Slots [§c\uE002§r §c\uE002§7]§r");
        p.shouldMatch("§7[0/3] Powder Slots§r");
        p.shouldMatch("§7[2/3] Powder Slots [§2\uE001§r §2\uE001§7]§r");
        p.shouldMatch("§7[4/4] Powder Slots [§e\uE003§r §e\uE003§r §e\uE003§r §e\uE003§7]§r");
        p.shouldMatch("§7[2/3] Powder Slots [§b\uE004§r §f\uE000§7]§r");
    }

    @Test
    public void WynnItemParser_EFFECT_LINE_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "EFFECT_LINE_PATTERN");
        p.shouldMatch("§6- §7Effect: §f20% XP");
    }

    @Test
    public void WynnItemParser_MIN_LEVEL_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "MIN_LEVEL_PATTERN");
        p.shouldMatch("§a✔§7 Combat Lv. Min: 104§r");
        p.shouldMatch("§c✖§7 Combat Lv. Min: 84§r");
        p.shouldMatch("§a✔ §7Combat Lv. Min: §f103");
    }

    @Test
    public void WynnItemParser_CLASS_REQ_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "CLASS_REQ_PATTERN");
        p.shouldMatch("§a✔§7 Class Req: Shaman/Skyseer§r");
        p.shouldMatch("§c✖§7 Class Req: Archer/Hunter§r");
        p.shouldMatch("§c✖ §7Class Req: §fAssassin/Ninja");
    }

    @Test
    public void WynnItemParser_SKILL_REQ_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "SKILL_REQ_PATTERN");
        p.shouldMatch("§c✖§7 Defence Min: 50§r");
        p.shouldMatch("§a✔§7 Intelligence Min: 45§r");
        p.shouldMatch("§a✔ §7Dexterity Min: §f15");
        p.shouldMatch("§c✖ §7Agility Min: §f110");
    }

    @Test
    public void WynnItemParser_QUEST_REQ_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "QUEST_REQ_PATTERN");
        p.shouldMatch("§c✖§7 Quest Req: The Qira Hive§r");
        p.shouldMatch("§c✖§7 Quest Req: Realm of Light V - The Realm of Light§r");
        p.shouldMatch("§a✔§7 Quest Req: Realm of Light V - The Realm of Light§r");
    }

    @Test
    public void WynnItemParser_MISC_REQ_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "MISC_REQ_PATTERN");
        p.shouldMatch("§c✖§7 Quest Req: The Qira Hive§r");
        p.shouldMatch("§c✖§7 Quest Req: Realm of Light V - The Realm of Light§r");
        p.shouldMatch("§a✔§7 Quest Req: Realm of Light V - The Realm of Light§r");
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
        p.shouldMatch("§f⬡ §7 World Events Won: §f0§8 [3]");
    }

    @Test
    public void RaidModel_RAID_BUFF_PATTERN() {
        PatternTester p = new PatternTester(RaidModel.class, "RAID_CHOOSE_BUFF_PATTERN");
        p.shouldMatch(
                "§#d6401eff\uE009\uE002 §#fa7f63ffDanzxms§#d6401eff has chosen the §#fa7f63ffStonewalker III§#d6401eff buff!");
    }

    @Test
    public void WynnItemParser_CRAFTED_ITEM_NAME_PATTERN() {
        PatternTester p = new PatternTester(WynnItemParser.class, "CRAFTED_ITEM_NAME_PATTERN");
        p.shouldMatch("§3how do i rename horses now lol §b[100%]À");
        p.shouldMatch("§3Dune Hero Fallen Chestplate §b[100%]");
        p.shouldMatch("§3I need money pls §b[1/1]À");
    }

    @Test
    public void WarTowerBar_TOWER_BAR_PATTERN() {
        PatternTester p = new PatternTester(WarTowerBar.class, "TOWER_BAR_PATTERN");
        p.shouldMatch("§3[SEPC] §bEfilim South Plains Tower§7 - §4❤ 390000§7 (§610.0%§7) - §c☠ 1300-1949§7 (§b0.5x§7)");
    }

    @Test
    public void GearModel_GEAR_PATTERN() {
        PatternTester p = new PatternTester(GearModel.class, "GEAR_PATTERN");

        // Unidentified
        p.shouldMatch("§5Unidentified §f⬡ §5Shiny Crusade Sabatons");
        p.shouldMatch("§5Unidentified §f⬡ §5Shiny Gaia");
        p.shouldMatch("§5Unidentified Idol");
        p.shouldMatch("§5Unidentified Nirvana");
        p.shouldMatch("§bUnidentified Follow the Wind");

        // Identified
        p.shouldMatch("§5Apocalypse");
        p.shouldMatch("§cRhythm of the Seasons");
        p.shouldMatch("§f⬡ §5Shiny Stratiformis");
        p.shouldMatch("§f⬡ §5Shiny Aftershock");
        p.shouldMatch("§f⬡ §5Shiny Crusade Sabatons");
    }

    @Test
    public void CharacterAnnotator_CHARACTER_MENU_CLASS_PATTERN() {
        PatternTester p = new PatternTester(CharacterAnnotator.class, "CHARACTER_MENU_CLASS_PATTERN");

        p.shouldMatch("§6- §7Class: §fMage'"); // Mage
        p.shouldMatch("§6- §7Class: §fHunter"); // Hunter
        p.shouldMatch("§6- §7Class: §fSkyseer"); // Skyseer
        p.shouldMatch("§6- §7Class: §fKnight"); // Knight
        p.shouldMatch("§6- §7Class: §fNinja"); // Ninja
        p.shouldMatch("§6- §7Class: §b\uE083§7 §fArcher"); // Ultimate Ironman Archer
        p.shouldMatch("§6- §7Class: §5\uE028§7 §fDark Wizard"); // Hunted Dark Wizard
        p.shouldMatch("§6- §7Class: §6\uE029§7 §fShaman"); // Ironman Shaman
        p.shouldMatch("§6- §7Class: §c\uE027§7 §fAssassin"); // Hardcore Assassin
        p.shouldMatch("§6- §7Class: §3\uE026§7 §fWarrior"); // Craftsman Warrior
        p.shouldMatch("§6- §7Class: §c\uE027§b\uE083§3\uE026§5\uE028§7 §fArcher"); // HUICH Archer
    }

    @Test
    public void CharacterAnnotator_CHARACTER_MENU_LEVEL_PATTERN() {
        PatternTester p = new PatternTester(CharacterAnnotator.class, "CHARACTER_MENU_LEVEL_PATTERN");

        p.shouldMatch("§6- §7Level: §f106§7 §8(0%)");
        p.shouldMatch("§6- §7Level: §f105§7 §8(3.14%)");
        p.shouldMatch("§6- §7Level: §f12§7 §8(40.54%)");
        p.shouldMatch("§6- §7Level: §f1§7 §8(0.67%)");
    }

    @Test
    public void NpcLabelParser_NPC_LABEL_PATTERN() {
        PatternTester p = new PatternTester(NpcLabelParser.class, "NPC_LABEL_PATTERN");

        p.shouldMatch("§f\uE000\n§dArmour Merchant\n§7NPC");
        p.shouldMatch("§f\uE002\n§dBlacksmith\n§7Sell and repair items");
        p.shouldMatch("§f\uE001\n§dEmerald Merchant\n§7NPC");
        p.shouldMatch("§f\uE003\n§dItem Identifier\n§7NPC");
        p.shouldMatch("§f\uE000\n§dLiquid Merchant\n§7NPC");
        p.shouldMatch("§f\uE000\n§dPotion Merchant\n§7NPC");
        p.shouldMatch("§f\uE004\n§dPowder Master\n§7NPC");
        p.shouldMatch("§f\uE000\n§dScroll Merchant\n§7NPC");
        p.shouldMatch("§f\uE000\n§dTool Merchant\n§7NPC");
        p.shouldMatch("§f\uE000\n§dWeapon Merchant\n§7NPC");
        p.shouldMatch("§f\uE008\n§cTrade Market§f\n§7Buy & sell items\non the market");
    }

    @Test
    public void StyledTextUtils_NICKNAME_PATTERN() {
        PatternTester p = new PatternTester(StyledTextUtils.class, "NICKNAME_PATTERN");

        p.shouldMatch("§fbol§7's real username is §fbolyai");
        p.shouldMatch("§fbol's§7 real username is §fbolyai");
    }

    @Test
    public void PartyModel_PARTY_LIST_ALL() {
        PatternTester p = new PatternTester(PartyModel.class, "PARTY_LIST_ALL");
        p.shouldMatch("§e\uE001 Party members: §bShadowCat118, and §fShadowCat117");
        p.shouldMatch("§e\uE005\uE002 Party members: §be_z_x, §fSaunt, Dopeul, IM_NoOne,§e §f6bccy, and ShadowCat117");
    }

    @Test
    public void WorldStateModel_HOUSING_NAME() {
        PatternTester p = new PatternTester(WorldStateModel.class, "HOUSING_NAME");
        p.shouldMatch("§f  §lChiefs Of Corkus' HQ");
        p.shouldMatch("§f  §lShadow's Home");
    }

    @Test
    public void GambitAnnotator_GAMBIT_NAME() {
        PatternTester p = new PatternTester(GambitAnnotator.class, "NAME_PATTERN");
        p.shouldMatch("§#54fffcff§lIngenuous Mage's Gambit");
        p.shouldMatch("§#ac2c01ff§lArcane Incontinent's Gambit");
    }

    @Test
    public void TradeMarketModel_PRICE_INPUT_PATTERN() {
        PatternTester p = new PatternTester(TradeMarketModel.class, "PRICE_INPUT_PATTERN");
        p.shouldMatch(
                "§5\uE00A\uE002 Type the price in emeralds or formatted (e.g '10eb', '10stx 5eb') or type 'cancel' to cancel:");
    }

    @Test
    public void WorldEventModel_ANNIHILATION_TIMER_PATTERN() {
        PatternTester p = new PatternTester(WorldEventModel.class, "ANNIHILATION_TIMER_PATTERN");
        p.shouldMatch("§#00bdbfff\uE001 §cPrepare to defend the province at the Corruption Portal in 39m 22s!");
        p.shouldMatch("§#00bdbfff\uE001 §cPrepare to defend the province at the Corruption Portal in 11h 30m!");
    }

    @Test
    public void WorldEventModel_WORLD_EVENT_PATTERN() {
        PatternTester p = new PatternTester(WorldEventModel.class, "WORLD_EVENT_PATTERN");
        p.shouldMatch(
                "§#00bdbfff\uE00D\uE002 The Shapes in the Dark World Event starts in 6m 59s! §7(509 blocks away) §d§nClick to track");
        p.shouldMatch(
                "§#00bdbfff\uE00D\uE002 The Corrupted Spring World Event starts in 2m 59s! §7(271 blocks away) §d§nClick to track");
    }
}
