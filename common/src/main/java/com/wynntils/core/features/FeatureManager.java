/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Manager;
import com.wynntils.core.components.Managers;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.mod.CrashReportManager;
import com.wynntils.features.debug.ConnectionProgressFeature;
import com.wynntils.features.debug.ItemDebugTooltipsFeature;
import com.wynntils.features.debug.LogItemInfoFeature;
import com.wynntils.features.debug.PacketDebuggerFeature;
import com.wynntils.features.statemanaged.DataStorageFeature;
import com.wynntils.features.statemanaged.LootrunFeature;
import com.wynntils.features.user.GammabrightFeature;
import com.wynntils.features.user.TerritoryDefenseMessageFeature;
import com.wynntils.features.user.TranslationFeature;
import com.wynntils.features.user.chat.ChatCoordinatesFeature;
import com.wynntils.features.user.chat.ChatItemFeature;
import com.wynntils.features.user.chat.ChatMentionFeature;
import com.wynntils.features.user.chat.ChatTabsFeature;
import com.wynntils.features.user.chat.ChatTimestampFeature;
import com.wynntils.features.user.chat.DialogueOptionOverrideFeature;
import com.wynntils.features.user.chat.InfoMessageFilterFeature;
import com.wynntils.features.user.chat.TradeMarketAutoOpenChatFeature;
import com.wynntils.features.user.chat.TradeMarketPriceConversionFeature;
import com.wynntils.features.user.combat.AbbreviateMobHealthFeature;
import com.wynntils.features.user.combat.CombatXpGainMessageFeature;
import com.wynntils.features.user.combat.FixCastingSpellsFromInventoryFeature;
import com.wynntils.features.user.combat.HealthPotionBlockerFeature;
import com.wynntils.features.user.combat.HorseMountFeature;
import com.wynntils.features.user.combat.LowHealthVignetteFeature;
import com.wynntils.features.user.combat.MythicBlockerFeature;
import com.wynntils.features.user.combat.MythicBoxScalerFeature;
import com.wynntils.features.user.combat.PreventTradesDuelsFeature;
import com.wynntils.features.user.combat.QuickCastFeature;
import com.wynntils.features.user.commands.AddCommandExpansionFeature;
import com.wynntils.features.user.commands.CommandAliasesFeature;
import com.wynntils.features.user.commands.CustomCommandKeybindsFeature;
import com.wynntils.features.user.commands.FilterAdminCommandsFeature;
import com.wynntils.features.user.inventory.ContainerSearchFeature;
import com.wynntils.features.user.inventory.DurabilityArcFeature;
import com.wynntils.features.user.inventory.EmeraldPouchHotkeyFeature;
import com.wynntils.features.user.inventory.ExtendedItemCountFeature;
import com.wynntils.features.user.inventory.HidePotionGlintFeature;
import com.wynntils.features.user.inventory.IngredientPouchHotkeyFeature;
import com.wynntils.features.user.inventory.InventoryEmeraldCountFeature;
import com.wynntils.features.user.inventory.ItemFavoriteFeature;
import com.wynntils.features.user.inventory.ItemHighlightFeature;
import com.wynntils.features.user.inventory.ItemLockFeature;
import com.wynntils.features.user.inventory.ItemScreenshotFeature;
import com.wynntils.features.user.inventory.ItemTextOverlayFeature;
import com.wynntils.features.user.inventory.UnidentifiedItemIconFeature;
import com.wynntils.features.user.map.BeaconBeamFeature;
import com.wynntils.features.user.map.GuildMapFeature;
import com.wynntils.features.user.map.MapFeature;
import com.wynntils.features.user.map.MinimapFeature;
import com.wynntils.features.user.map.WorldWaypointDistanceFeature;
import com.wynntils.features.user.overlays.ArrowShieldTrackingFeature;
import com.wynntils.features.user.overlays.AuraTimerOverlayFeature;
import com.wynntils.features.user.overlays.CustomBarsOverlayFeature;
import com.wynntils.features.user.overlays.GameNotificationOverlayFeature;
import com.wynntils.features.user.overlays.GuildAttackTimerOverlayFeature;
import com.wynntils.features.user.overlays.InfoBoxFeature;
import com.wynntils.features.user.overlays.MobTotemTrackingFeature;
import com.wynntils.features.user.overlays.NpcDialogueOverlayFeature;
import com.wynntils.features.user.overlays.ObjectivesOverlayFeature;
import com.wynntils.features.user.overlays.PowderSpecialBarOverlayFeature;
import com.wynntils.features.user.overlays.QuestInfoOverlayFeature;
import com.wynntils.features.user.overlays.ShamanMasksOverlayFeature;
import com.wynntils.features.user.overlays.ShamanTotemTrackingFeature;
import com.wynntils.features.user.overlays.SpellCastRenderFeature;
import com.wynntils.features.user.overlays.StatusOverlayFeature;
import com.wynntils.features.user.players.AutoJoinPartyFeature;
import com.wynntils.features.user.players.CustomNametagRendererFeature;
import com.wynntils.features.user.players.GearViewerFeature;
import com.wynntils.features.user.players.HadesFeature;
import com.wynntils.features.user.players.PartyManagementScreenFeature;
import com.wynntils.features.user.players.PlayerArmorHidingFeature;
import com.wynntils.features.user.players.PlayerGhostTransparencyFeature;
import com.wynntils.features.user.players.WynntilsCosmeticsFeature;
import com.wynntils.features.user.redirects.AbilityRefreshRedirectFeature;
import com.wynntils.features.user.redirects.BlacksmithRedirectFeature;
import com.wynntils.features.user.redirects.ChatRedirectFeature;
import com.wynntils.features.user.redirects.InventoryRedirectFeature;
import com.wynntils.features.user.redirects.TerritoryMessageRedirectFeature;
import com.wynntils.features.user.tooltips.ItemCompareFeature;
import com.wynntils.features.user.tooltips.ItemGuessFeature;
import com.wynntils.features.user.tooltips.ItemStatInfoFeature;
import com.wynntils.features.user.tooltips.TooltipFittingFeature;
import com.wynntils.features.user.tooltips.TooltipVanillaHideFeature;
import com.wynntils.features.user.ui.AbilityTreeScrollFeature;
import com.wynntils.features.user.ui.AutoApplyResourcePackFeature;
import com.wynntils.features.user.ui.CosmeticsPreviewFeature;
import com.wynntils.features.user.ui.CustomCharacterSelectionScreenFeature;
import com.wynntils.features.user.ui.LobbyUptimeFeature;
import com.wynntils.features.user.ui.SoulPointTimerFeature;
import com.wynntils.features.user.ui.WynncraftButtonFeature;
import com.wynntils.features.user.ui.WynncraftPauseScreenFeature;
import com.wynntils.features.user.ui.WynntilsQuestBookFeature;
import com.wynntils.features.user.wynntils.ChangelogFeature;
import com.wynntils.features.user.wynntils.CommandsFeature;
import com.wynntils.features.user.wynntils.FixPacketBugsFeature;
import com.wynntils.features.user.wynntils.UpdatesFeature;
import com.wynntils.utils.mc.McUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

/** Loads {@link Feature}s */
public final class FeatureManager extends Manager {
    private static final List<Feature> FEATURES = new ArrayList<>();

    public FeatureManager(ConfigManager configManager, CrashReportManager crashReportManager) {
        super(List.of(configManager, crashReportManager));
    }

    public void init() {
        // debug
        registerFeature(new ConnectionProgressFeature());
        registerFeature(new ItemDebugTooltipsFeature());
        registerFeature(new LogItemInfoFeature());
        registerFeature(new PacketDebuggerFeature());

        // always on
        registerFeature(new LootrunFeature());
        registerFeature(new DataStorageFeature());

        // user
        registerFeature(new AbbreviateMobHealthFeature());
        registerFeature(new AbilityRefreshRedirectFeature());
        registerFeature(new AbilityTreeScrollFeature());
        registerFeature(new AddCommandExpansionFeature());
        registerFeature(new ArrowShieldTrackingFeature());
        registerFeature(new AuraTimerOverlayFeature());
        registerFeature(new AutoApplyResourcePackFeature());
        registerFeature(new AutoJoinPartyFeature());
        registerFeature(new BeaconBeamFeature());
        registerFeature(new BlacksmithRedirectFeature());
        registerFeature(new ChangelogFeature());
        registerFeature(new ChatCoordinatesFeature());
        registerFeature(new ChatItemFeature());
        registerFeature(new ChatMentionFeature());
        registerFeature(new ChatRedirectFeature());
        registerFeature(new ChatTabsFeature());
        registerFeature(new ChatTimestampFeature());
        registerFeature(new CombatXpGainMessageFeature());
        registerFeature(new CommandAliasesFeature());
        registerFeature(new CommandsFeature());
        registerFeature(new ContainerSearchFeature());
        registerFeature(new CosmeticsPreviewFeature());
        registerFeature(new CustomBarsOverlayFeature());
        registerFeature(new CustomCharacterSelectionScreenFeature());
        registerFeature(new CustomCommandKeybindsFeature());
        registerFeature(new CustomNametagRendererFeature());
        registerFeature(new DialogueOptionOverrideFeature());
        registerFeature(new DurabilityArcFeature());
        registerFeature(new EmeraldPouchHotkeyFeature());
        registerFeature(new ExtendedItemCountFeature());
        registerFeature(new FilterAdminCommandsFeature());
        registerFeature(new FixCastingSpellsFromInventoryFeature());
        registerFeature(new FixPacketBugsFeature());
        registerFeature(new GameNotificationOverlayFeature());
        registerFeature(new GammabrightFeature());
        registerFeature(new GearViewerFeature());
        registerFeature(new GuildAttackTimerOverlayFeature());
        registerFeature(new GuildMapFeature());
        registerFeature(new HadesFeature());
        registerFeature(new HealthPotionBlockerFeature());
        registerFeature(new HidePotionGlintFeature());
        registerFeature(new InfoBoxFeature());
        registerFeature(new InfoMessageFilterFeature());
        registerFeature(new IngredientPouchHotkeyFeature());
        registerFeature(new InventoryEmeraldCountFeature());
        registerFeature(new InventoryRedirectFeature());
        registerFeature(new ItemCompareFeature());
        registerFeature(new ItemFavoriteFeature());
        registerFeature(new ItemGuessFeature());
        registerFeature(new ItemHighlightFeature());
        registerFeature(new ItemLockFeature());
        registerFeature(new ItemScreenshotFeature());
        registerFeature(new ItemStatInfoFeature());
        registerFeature(new ItemTextOverlayFeature());
        registerFeature(new LobbyUptimeFeature());
        registerFeature(new LowHealthVignetteFeature());
        registerFeature(new MapFeature());
        registerFeature(new MinimapFeature());
        registerFeature(new MobTotemTrackingFeature());
        registerFeature(new HorseMountFeature());
        registerFeature(new MythicBlockerFeature());
        registerFeature(new MythicBoxScalerFeature());
        registerFeature(new NpcDialogueOverlayFeature());
        registerFeature(new ObjectivesOverlayFeature());
        registerFeature(new PartyManagementScreenFeature());
        registerFeature(new PlayerArmorHidingFeature());
        registerFeature(new PlayerGhostTransparencyFeature());
        registerFeature(new PowderSpecialBarOverlayFeature());
        registerFeature(new PreventTradesDuelsFeature());
        registerFeature(new QuestInfoOverlayFeature());
        registerFeature(new QuickCastFeature());
        registerFeature(new ShamanMasksOverlayFeature());
        registerFeature(new ShamanTotemTrackingFeature());
        registerFeature(new SoulPointTimerFeature());
        registerFeature(new SpellCastRenderFeature());
        registerFeature(new StatusOverlayFeature());
        registerFeature(new TerritoryDefenseMessageFeature());
        registerFeature(new TerritoryMessageRedirectFeature());
        registerFeature(new TooltipFittingFeature());
        registerFeature(new TooltipVanillaHideFeature());
        registerFeature(new TradeMarketAutoOpenChatFeature());
        registerFeature(new TradeMarketPriceConversionFeature());
        registerFeature(new TranslationFeature());
        registerFeature(new UnidentifiedItemIconFeature());
        registerFeature(new UpdatesFeature());
        registerFeature(new WorldWaypointDistanceFeature());
        registerFeature(new WynncraftButtonFeature());
        registerFeature(new WynncraftPauseScreenFeature());
        registerFeature(new WynntilsCosmeticsFeature());
        registerFeature(new WynntilsQuestBookFeature());

        // save/create config file after loading all features' options
        Managers.Config.saveConfig();

        // save/create default config file containing all config holders
        Managers.Config.saveDefaultConfig();

        // Reload Minecraft's config files so our own keybinds get loaded
        // This is needed because we are late to register the keybinds,
        // but we cannot move it earlier to the init process because of I18n
        synchronized (McUtils.options()) {
            McUtils.mc().options.load();
        }

        addCrashCallbacks();
    }

    private void registerFeature(Feature feature) {
        FEATURES.add(feature);

        try {
            initializeFeature(feature);
        } catch (AssertionError ae) {
            WynntilsMod.error("Fix i18n for " + feature.getClass().getSimpleName(), ae);
            if (WynntilsMod.isDevelopmentEnvironment()) {
                System.exit(1);
            }
        } catch (Throwable exception) {
            // Log and fail gracefully, don't make other features fail to init
            WynntilsMod.error(
                    "Failed to initialize feature " + feature.getClass().getSimpleName(), exception);
        }
    }

    private void initializeFeature(Feature feature) {
        Class<? extends Feature> featureClass = feature.getClass();

        // instance field
        try {
            Field instanceField = FieldUtils.getDeclaredField(featureClass, "INSTANCE", true);
            if (instanceField != null) instanceField.set(null, feature);
        } catch (Exception e) {
            WynntilsMod.error("Failed to create instance object in " + featureClass.getName(), e);
            return;
        }

        // flag as event listener
        if (MethodUtils.getMethodsWithAnnotation(featureClass, SubscribeEvent.class).length > 0) {
            feature.setupEventListener();
        }

        // set feature category
        ConfigCategory configCategory = feature.getClass().getAnnotation(ConfigCategory.class);
        Category category = configCategory != null ? configCategory.value() : Category.UNCATEGORIZED;
        feature.setCategory(category);

        // register key binds
        for (Field f : FieldUtils.getFieldsWithAnnotation(featureClass, RegisterKeyBind.class)) {
            if (!f.getType().equals(KeyBind.class)) continue;

            try {
                KeyBind keyBind = (KeyBind) FieldUtils.readField(f, feature, true);
                feature.setupKeyHolder(keyBind);
            } catch (Exception e) {
                WynntilsMod.error("Failed to register KeyBind " + f.getName() + " in " + featureClass.getName(), e);
            }
        }

        // determine if feature should be enabled & set default enabled value for user features
        boolean startDisabled = featureClass.isAnnotationPresent(StartDisabled.class);
        if (feature instanceof UserFeature userFeature) {
            userFeature.userEnabled = !startDisabled;
        }

        // init overlays before ConfigManager
        feature.initOverlays();

        // register & load configs
        // this has to be done after the userEnabled handling above, so the default value registers properly
        Managers.Config.registerFeature(feature);

        // initialize & enable
        feature.init();

        if (feature instanceof UserFeature userFeature) {
            if (!userFeature.userEnabled) return; // not enabled by user

            userFeature.enable();
        } else if (!startDisabled) {
            feature.enable();
        }
    }

    public List<Feature> getFeatures() {
        return FEATURES;
    }

    public Optional<Feature> getFeatureFromString(String featureName) {
        return getFeatures().stream()
                .filter(feature -> feature.getShortName().equals(featureName))
                .findFirst();
    }

    private void addCrashCallbacks() {
        Managers.CrashReport.registerCrashContext("Loaded Features", () -> {
            StringBuilder result = new StringBuilder();

            for (Feature feature : FEATURES) {
                if (feature.isEnabled()) {
                    result.append("\n\t\t").append(feature.getTranslatedName());
                }
            }

            return result.toString();
        });
    }
}
