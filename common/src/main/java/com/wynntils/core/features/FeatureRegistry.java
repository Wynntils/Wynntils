/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.features;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.config.ConfigManager;
import com.wynntils.core.features.properties.FeatureCategory;
import com.wynntils.core.features.properties.FeatureInfo;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.features.properties.StartDisabled;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.core.managers.CrashReportManager;
import com.wynntils.features.debug.ConnectionProgressFeature;
import com.wynntils.features.debug.LogItemInfoFeature;
import com.wynntils.features.debug.PacketDebuggerFeature;
import com.wynntils.features.statemanaged.DataStorageFeature;
import com.wynntils.features.statemanaged.FixSpellOverwriteFeature;
import com.wynntils.features.statemanaged.LootrunFeature;
import com.wynntils.features.user.AddCommandExpansionFeature;
import com.wynntils.features.user.AutoApplyResourcePackFeature;
import com.wynntils.features.user.BeaconBeamFeature;
import com.wynntils.features.user.ChatItemFeature;
import com.wynntils.features.user.CommandsFeature;
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
import com.wynntils.features.user.HealthPotionBlockerFeature;
import com.wynntils.features.user.HighlightMatchingItemsFeature;
import com.wynntils.features.user.InfoMessageFilterFeature;
import com.wynntils.features.user.IngredientPouchHotkeyFeature;
import com.wynntils.features.user.ItemFavoriteFeature;
import com.wynntils.features.user.ItemLockFeature;
import com.wynntils.features.user.ItemScreenshotFeature;
import com.wynntils.features.user.LobbyUptimeFeature;
import com.wynntils.features.user.MountHorseHotkeyFeature;
import com.wynntils.features.user.MythicBlockerFeature;
import com.wynntils.features.user.QuickCastFeature;
import com.wynntils.features.user.SoulPointTimerFeature;
import com.wynntils.features.user.TradeMarketAutoOpenChatFeature;
import com.wynntils.features.user.TradeMarketPriceConversionFeature;
import com.wynntils.features.user.TranslationFeature;
import com.wynntils.features.user.UpdateReminderFeature;
import com.wynntils.features.user.WynncraftButtonFeature;
import com.wynntils.features.user.WynncraftPauseScreenFeature;
import com.wynntils.features.user.WynntilsQuestBookFeature;
import com.wynntils.features.user.inventory.AbilityTreeScrollFeature;
import com.wynntils.features.user.inventory.DurabilityArcFeature;
import com.wynntils.features.user.inventory.HidePotionGlintFeature;
import com.wynntils.features.user.inventory.InventoryEmeraldCountFeature;
import com.wynntils.features.user.inventory.ItemHighlightFeature;
import com.wynntils.features.user.inventory.ItemTextOverlayFeature;
import com.wynntils.features.user.inventory.UnidentifiedItemIconFeature;
import com.wynntils.features.user.overlays.CustomBarsOverlayFeature;
import com.wynntils.features.user.overlays.GameNotificationOverlayFeature;
import com.wynntils.features.user.overlays.InfoBoxFeature;
import com.wynntils.features.user.overlays.NpcDialogueOverlayFeature;
import com.wynntils.features.user.overlays.ObjectivesOverlayFeature;
import com.wynntils.features.user.overlays.PowderAbilityBarOverlayFeature;
import com.wynntils.features.user.overlays.QuestInfoOverlayFeature;
import com.wynntils.features.user.overlays.map.MapFeature;
import com.wynntils.features.user.overlays.map.MinimapFeature;
import com.wynntils.features.user.players.PlayerGhostTransparencyFeature;
import com.wynntils.features.user.redirects.AbilityRefreshRedirectFeature;
import com.wynntils.features.user.redirects.PouchRedirectFeature;
import com.wynntils.features.user.tooltips.ItemCompareFeature;
import com.wynntils.features.user.tooltips.ItemGuessFeature;
import com.wynntils.features.user.tooltips.ItemStatInfoFeature;
import com.wynntils.features.user.tooltips.TooltipFittingFeature;
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
    private static final List<Feature> FEATURES = new ArrayList<>();

    public static void init() {
        // debug
        registerFeature(new ConnectionProgressFeature());
        registerFeature(new LogItemInfoFeature());
        registerFeature(new PacketDebuggerFeature());

        // always on
        registerFeature(new FixSpellOverwriteFeature());
        registerFeature(new LootrunFeature());
        registerFeature(new DataStorageFeature());

        // user
        registerFeature(new AbilityRefreshRedirectFeature());
        registerFeature(new AbilityTreeScrollFeature());
        registerFeature(new AddCommandExpansionFeature());
        registerFeature(new AutoApplyResourcePackFeature());
        registerFeature(new BeaconBeamFeature());
        registerFeature(new ChatItemFeature());
        registerFeature(new CommandsFeature());
        registerFeature(new CosmeticsPreviewFeature());
        registerFeature(new CustomNametagRendererFeature());
        registerFeature(new HighlightMatchingItemsFeature());
        registerFeature(new CustomBarsOverlayFeature());
        registerFeature(new CustomCharacterSelectionScreenFeature());
        registerFeature(new CustomCommandKeybindsFeature());
        registerFeature(new DialogueOptionOverrideFeature());
        registerFeature(new DurabilityArcFeature());
        registerFeature(new EmeraldPouchHotkeyFeature());
        registerFeature(new FilterAdminCommandsFeature());
        registerFeature(new FixPacketBugsFeature());
        registerFeature(new FixCastingSpellsFromInventoryFeature());
        registerFeature(new GameNotificationOverlayFeature());
        registerFeature(new GammabrightFeature());
        registerFeature(new GearViewerFeature());
        registerFeature(new HealthPotionBlockerFeature());
        registerFeature(new HidePotionGlintFeature());
        registerFeature(new InfoBoxFeature());
        registerFeature(new InfoMessageFilterFeature());
        registerFeature(new IngredientPouchHotkeyFeature());
        registerFeature(new InventoryEmeraldCountFeature());
        registerFeature(new ItemCompareFeature());
        registerFeature(new ItemFavoriteFeature());
        registerFeature(new ItemGuessFeature());
        registerFeature(new ItemHighlightFeature());
        registerFeature(new ItemLockFeature());
        registerFeature(new ItemScreenshotFeature());
        registerFeature(new ItemStatInfoFeature());
        registerFeature(new ItemTextOverlayFeature());
        registerFeature(new LobbyUptimeFeature());
        registerFeature(new MapFeature());
        registerFeature(new MinimapFeature());
        registerFeature(new MountHorseHotkeyFeature());
        registerFeature(new MythicBlockerFeature());
        registerFeature(new NpcDialogueOverlayFeature());
        registerFeature(new ObjectivesOverlayFeature());
        registerFeature(new PlayerGhostTransparencyFeature());
        registerFeature(new PouchRedirectFeature());
        registerFeature(new PowderAbilityBarOverlayFeature());
        registerFeature(new QuestInfoOverlayFeature());
        registerFeature(new QuickCastFeature());
        registerFeature(new SoulPointTimerFeature());
        registerFeature(new TooltipFittingFeature());
        registerFeature(new TradeMarketAutoOpenChatFeature());
        registerFeature(new TradeMarketPriceConversionFeature());
        registerFeature(new TranslationFeature());
        registerFeature(new UpdateReminderFeature());
        registerFeature(new UnidentifiedItemIconFeature());
        registerFeature(new WynncraftButtonFeature());
        registerFeature(new WynncraftPauseScreenFeature());
        registerFeature(new WynntilsQuestBookFeature());

        // save/create config file after loading all features' options
        ConfigManager.saveConfig();

        // save/create default config file containing all config holders
        ConfigManager.saveDefaultConfig();

        // Reload Minecraft's config files so our own keybinds get loaded
        // This is needed because we are late to register the keybinds,
        // but we cannot move it earlier to the init process because of I18n
        McUtils.mc().options.load();

        addCrashCallbacks();
    }

    private static void registerFeature(Feature feature) {
        FEATURES.add(feature);

        initializeFeature(feature);
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
        ConfigManager.registerFeature(feature);

        // initialize & enable
        feature.init();

        if (feature instanceof UserFeature userFeature) {
            if (!userFeature.userEnabled) return; // not enabled by user

            userFeature.tryEnable();
        } else if (!startDisabled) {
            feature.tryEnable();
        }
    }

    public static List<Feature> getFeatures() {
        return FEATURES;
    }

    public static Optional<Feature> getFeatureFromString(String featureName) {
        return FeatureRegistry.getFeatures().stream()
                .filter(feature -> feature.getShortName().equals(featureName))
                .findFirst();
    }

    private static void addCrashCallbacks() {
        CrashReportManager.registerCrashContext(new CrashReportManager.ICrashContext("Loaded Features") {
            @Override
            public Object generate() {
                StringBuilder result = new StringBuilder();

                for (Feature feature : FEATURES) {
                    if (feature.isEnabled()) {
                        result.append("\n\t\t").append(feature.getTranslatedName());
                    }
                }

                return result.toString();
            }
        });
    }
}
