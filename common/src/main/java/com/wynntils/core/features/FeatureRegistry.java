/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.features.debug.ConnectionProgressFeature;
import com.wynntils.features.debug.ItemDebugTooltipsFeature;
import com.wynntils.features.debug.LogItemInfoFeature;
import com.wynntils.features.debug.PacketDebuggerFeature;
import com.wynntils.features.statemanaged.DataStorageFeature;
import com.wynntils.features.statemanaged.FixSpellOverwriteFeature;
import com.wynntils.features.statemanaged.LootrunFeature;
import com.wynntils.features.user.AbbreviateMobHealthFeature;
import com.wynntils.features.user.AddCommandExpansionFeature;
import com.wynntils.features.user.AutoApplyResourcePackFeature;
import com.wynntils.features.user.BeaconBeamFeature;
import com.wynntils.features.user.BombBellTrackingFeature;
import com.wynntils.features.user.ChatCoordinatesFeature;
import com.wynntils.features.user.ChatItemFeature;
import com.wynntils.features.user.ChatTabsFeature;
import com.wynntils.features.user.ChatTimestampFeature;
import com.wynntils.features.user.CombatXpGainMessageFeature;
import com.wynntils.features.user.CommandAliasesFeature;
import com.wynntils.features.user.CommandsFeature;
import com.wynntils.features.user.ContainerSearchFeature;
import com.wynntils.features.user.CosmeticsPreviewFeature;
import com.wynntils.features.user.CustomCharacterSelectionScreenFeature;
import com.wynntils.features.user.CustomCommandKeybindsFeature;
import com.wynntils.features.user.CustomNametagRendererFeature;
import com.wynntils.features.user.DialogueOptionOverrideFeature;
import com.wynntils.features.user.EmeraldPouchHotkeyFeature;
import com.wynntils.features.user.FilterAdminCommandsFeature;
import com.wynntils.features.user.FixCastingSpellsFromInventoryFeature;
import com.wynntils.features.user.FixPacketBugsFeature;
import com.wynntils.features.user.GammabrightFeature;
import com.wynntils.features.user.GearViewerFeature;
import com.wynntils.features.user.HadesFeature;
import com.wynntils.features.user.HealthPotionBlockerFeature;
import com.wynntils.features.user.InfoMessageFilterFeature;
import com.wynntils.features.user.IngredientPouchHotkeyFeature;
import com.wynntils.features.user.ItemFavoriteFeature;
import com.wynntils.features.user.ItemLockFeature;
import com.wynntils.features.user.ItemScreenshotFeature;
import com.wynntils.features.user.LobbyUptimeFeature;
import com.wynntils.features.user.LowHealthVignetteFeature;
import com.wynntils.features.user.MountHorseHotkeyFeature;
import com.wynntils.features.user.MythicBlockerFeature;
import com.wynntils.features.user.MythicBoxScalerFeature;
import com.wynntils.features.user.QuickCastFeature;
import com.wynntils.features.user.overlays.ShamanTotemTrackingFeature;
import com.wynntils.features.user.SoulPointTimerFeature;
import com.wynntils.features.user.StatusOverlayFeature;
import com.wynntils.features.user.TerritoryDefenseMessageFeature;
import com.wynntils.features.user.TradeMarketAutoOpenChatFeature;
import com.wynntils.features.user.TradeMarketPriceConversionFeature;
import com.wynntils.features.user.TranslationFeature;
import com.wynntils.features.user.UpdatesFeature;
import com.wynntils.features.user.WorldWaypointDistanceFeature;
import com.wynntils.features.user.WynncraftButtonFeature;
import com.wynntils.features.user.WynncraftPauseScreenFeature;
import com.wynntils.features.user.WynntilsQuestBookFeature;
import com.wynntils.features.user.inventory.AbilityTreeScrollFeature;
import com.wynntils.features.user.inventory.DurabilityArcFeature;
import com.wynntils.features.user.inventory.ExtendedItemCountFeature;
import com.wynntils.features.user.inventory.HidePotionGlintFeature;
import com.wynntils.features.user.inventory.InventoryEmeraldCountFeature;
import com.wynntils.features.user.inventory.ItemHighlightFeature;
import com.wynntils.features.user.inventory.ItemTextOverlayFeature;
import com.wynntils.features.user.inventory.UnidentifiedItemIconFeature;
import com.wynntils.features.user.map.GuildMapFeature;
import com.wynntils.features.user.map.MapFeature;
import com.wynntils.features.user.map.MinimapFeature;
import com.wynntils.features.user.overlays.AuraTimerOverlayFeature;
import com.wynntils.features.user.overlays.CustomBarsOverlayFeature;
import com.wynntils.features.user.overlays.GameNotificationOverlayFeature;
import com.wynntils.features.user.overlays.GuildAttackTimerOverlayFeature;
import com.wynntils.features.user.overlays.InfoBoxFeature;
import com.wynntils.features.user.overlays.NpcDialogueOverlayFeature;
import com.wynntils.features.user.overlays.ObjectivesOverlayFeature;
import com.wynntils.features.user.overlays.PowderSpecialBarOverlayFeature;
import com.wynntils.features.user.overlays.QuestInfoOverlayFeature;
import com.wynntils.features.user.overlays.ShamanMasksOverlayFeature;
import com.wynntils.features.user.players.PlayerArmorHidingFeature;
import com.wynntils.features.user.players.PlayerGhostTransparencyFeature;
import com.wynntils.features.user.players.PreventTradesDuelsFeature;
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
import com.wynntils.mc.utils.McUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

/** Loads {@link Feature}s */
public final class FeatureRegistry {
    private static boolean initCompleted = false;
    private static final List<Feature> FEATURES = new ArrayList<>();

    public static void init() {
        // debug
        registerFeature(new ConnectionProgressFeature());
        registerFeature(new ItemDebugTooltipsFeature());
        registerFeature(new LogItemInfoFeature());
        registerFeature(new PacketDebuggerFeature());

        // always on
        registerFeature(new FixSpellOverwriteFeature());
        registerFeature(new LootrunFeature());
        registerFeature(new DataStorageFeature());

        // user
        registerFeature(new AbbreviateMobHealthFeature());
        registerFeature(new AbilityRefreshRedirectFeature());
        registerFeature(new AbilityTreeScrollFeature());
        registerFeature(new AddCommandExpansionFeature());
        registerFeature(new AuraTimerOverlayFeature());
        registerFeature(new AutoApplyResourcePackFeature());
        registerFeature(new BeaconBeamFeature());
        registerFeature(new BlacksmithRedirectFeature());
        registerFeature(new BombBellTrackingFeature());
        registerFeature(new ChatCoordinatesFeature());
        registerFeature(new ChatItemFeature());
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
        registerFeature(new MountHorseHotkeyFeature());
        registerFeature(new MythicBlockerFeature());
        registerFeature(new MythicBoxScalerFeature());
        registerFeature(new NpcDialogueOverlayFeature());
        registerFeature(new ObjectivesOverlayFeature());
        registerFeature(new PlayerArmorHidingFeature());
        registerFeature(new PlayerGhostTransparencyFeature());
        registerFeature(new PowderSpecialBarOverlayFeature());
        registerFeature(new PreventTradesDuelsFeature());
        registerFeature(new QuestInfoOverlayFeature());
        registerFeature(new QuickCastFeature());
        registerFeature(new ShamanMasksOverlayFeature());
        registerFeature(new ShamanTotemTrackingFeature());
        registerFeature(new SoulPointTimerFeature());
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

        initCompleted = true;
    }

    private static void registerFeature(Feature feature) {
        FEATURES.add(feature);

        try {
            initializeFeature(feature);
        } catch (Throwable exception) {
            // Log and fail gracefully, don't make other features fail to init
            WynntilsMod.error(
                    "Failed to initialize feature " + feature.getClass().getSimpleName(), exception);
        }
    }

    private static void initializeFeature(Feature feature) {
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
        FeatureInfo featureInfo = feature.getClass().getAnnotation(FeatureInfo.class);
        FeatureCategory category = featureInfo != null ? featureInfo.category() : FeatureCategory.UNCATEGORIZED;
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

    public static List<Feature> getFeatures() {
        return FEATURES;
    }

    public static boolean isInitCompleted() {
        return initCompleted;
    }

    public static Optional<Feature> getFeatureFromString(String featureName) {
        return FeatureRegistry.getFeatures().stream()
                .filter(feature -> feature.getShortName().equals(featureName))
                .findFirst();
    }

    private static void addCrashCallbacks() {
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
