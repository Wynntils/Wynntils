/*
 * Copyright © Wynntils 2022-2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.consumers.overlays;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ComparisonChain;
import com.mojang.blaze3d.platform.Window;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.features.AbstractConfigurable;
import com.wynntils.core.mod.type.CrashType;
import com.wynntils.core.persisted.Persisted;
import com.wynntils.core.persisted.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.character.type.VehicleType;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import com.wynntils.utils.type.ErrorOr;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.phys.Vec2;

public abstract class Overlay extends AbstractConfigurable implements Comparable<Overlay> {
    @Persisted(i18nKey = "overlay.wynntils.overlay.enabledTemplate")
    protected final Config<String> enabledTemplate = new Config<>("");

    @Persisted(i18nKey = "overlay.wynntils.overlay.position")
    private final Config<OverlayPosition> position = new Config<>(null);

    @Persisted(i18nKey = "overlay.wynntils.overlay.size")
    protected final Config<OverlaySize> size = new Config<>(null);

    @Persisted(i18nKey = "overlay.wynntils.overlay.userEnabled")
    protected final Config<Boolean> userEnabled = new Config<>(true);

    // This is used in rendering.
    // Initially we use the overlay position horizontal alignment
    // but the user can modify this config field to use an override.
    // Example use case: Overlay is aligned to the left in the TopRight section,
    //                   but the user wants to use right text alignment
    @Persisted(i18nKey = "overlay.wynntils.overlay.horizontalAlignmentOverride")
    protected final Config<HorizontalAlignment> horizontalAlignmentOverride = new Config<>(null);

    @Persisted(i18nKey = "overlay.wynntils.overlay.verticalAlignmentOverride")
    protected final Config<VerticalAlignment> verticalAlignmentOverride = new Config<>(null);

    private ErrorOr<Boolean> enabledTemplateCache = null;

    protected Overlay(OverlayPosition position, float width, float height) {
        this.position.store(position);
        this.size.store(new OverlaySize(width, height));
    }

    protected Overlay(OverlayPosition position, OverlaySize size) {
        this.position.store(position);
        this.size.store(size);
    }

    protected Overlay(
            OverlayPosition position,
            OverlaySize size,
            HorizontalAlignment horizontalAlignmentOverride,
            VerticalAlignment verticalAlignmentOverride) {
        this.position.store(position);
        this.size.store(size);
        this.horizontalAlignmentOverride.store(horizontalAlignmentOverride);
        this.verticalAlignmentOverride.store(verticalAlignmentOverride);
    }

    /**
     * Whether the overlay should be hidden when Wynncraft hides gui.
     */
    protected boolean hideWhenNoGui() {
        return true;
    }

    /**
     * Whether the overlay should be rendered.
     */
    protected boolean isVisible() {
        return true;
    }

    protected final boolean isRendered() {
        // When user provides Enabled Template but there is an error, render it to show the error
        if (enabledTemplateCache != null && enabledTemplateCache.hasError()) return true;

        // But if there are no errors, render according to the template
        if (enabledTemplateCache != null && !enabledTemplateCache.hasError()) {
            return enabledTemplateCache.getValue();
        }

        // Otherwise render it according to defaults
        if (!isVisible()) return false;
        boolean hasGui = Models.WorldState.onWorld() && Models.Character.getVehicle() != VehicleType.DISPLAY;
        return hasGui || !hideWhenNoGui();
    }

    @Override
    public String getTypeName() {
        return "Overlay";
    }

    public abstract void render(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window);

    public void renderPreview(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        this.render(guiGraphics, bufferSource, deltaTracker, window);
    }

    protected void renderOrErrorMessage(
            GuiGraphics guiGraphics, MultiBufferSource bufferSource, DeltaTracker deltaTracker, Window window) {
        if (this.enabledTemplateCache != null && this.enabledTemplateCache.hasError()) {
            renderEnabledTemplateErrorMessage(guiGraphics, bufferSource);
        } else {
            render(guiGraphics, bufferSource, deltaTracker, window);
        }
    }

    private void renderEnabledTemplateErrorMessage(GuiGraphics guiGraphics, MultiBufferSource bufferSource) {
        StyledText[] errorMessage = {
            StyledText.fromString(
                    "§c§l" + I18n.get("overlay.wynntils.overlay.enabledTemplate.error") + " " + getTranslatedName()),
            StyledText.fromUnformattedString(enabledTemplateCache.getError())
        };
        BufferedFontRenderer.getInstance()
                .renderAlignedTextInBox(
                        guiGraphics.pose(),
                        bufferSource,
                        errorMessage,
                        getRenderX(),
                        getRenderX() + getWidth(),
                        getRenderY(),
                        getRenderY() + getHeight(),
                        0,
                        CommonColors.WHITE,
                        HorizontalAlignment.CENTER,
                        VerticalAlignment.MIDDLE,
                        TextShadow.NORMAL,
                        1);
    }

    protected void tick() {}

    protected void updateEnabledCache() {
        String template = enabledTemplate.get();
        if (template.isBlank()) {
            this.enabledTemplateCache = null;
            return;
        }

        String formattedTemplate =
                StyledText.join("", Managers.Function.doFormatLines(template)).getString();
        this.enabledTemplateCache = Managers.Function.tryGetRawValueOfType(formattedTemplate, Boolean.class);
    }

    @Override
    public final void updateConfigOption(Config<?> config) {
        // if user toggle was changed, enable/disable overlay accordingly
        if (config.getFieldName().equals("userEnabled")) {
            if (config.get() == Boolean.FALSE) {
                Managers.Overlay.disableOverlay(this);
            } else {
                // If new state is TRUE or null, try to enable overlay
                // (worst case overlay.shouldBeEnabled() will return false)
                Managers.Overlay.enableOverlay(this);
            }
        }

        callOnConfigUpdate(config);
    }

    protected void onConfigUpdate(Config<?> config) {
        // Override this method to handle config updates
    }

    protected void callOnConfigUpdate(Config<?> config) {
        try {
            onConfigUpdate(config);
        } catch (Throwable t) {
            // We can't stop disabled overlays from getting config updates, so if it crashes again,
            // just ignore it
            if (!Managers.Overlay.isEnabled(this)) return;

            Managers.Overlay.disableOverlay(this);
            WynntilsMod.reportCrash(
                    CrashType.OVERLAY, getTranslatedName(), getClass().getName(), "config update", t);
        }
    }

    @Override
    public String getTranslation(String keySuffix, Object... parameters) {
        // Overlay translations have a non-standard name
        return I18n.get(
                "feature.wynntils." + getTranslationFeatureKeyName() + ".overlay." + getTranslationKeyName() + "."
                        + keySuffix,
                parameters);
    }

    public String getShortName() {
        return this.getClass().getSimpleName().replace("Overlay", "");
    }

    public String getDeclaringFeatureClassName() {
        return Managers.Overlay.getOverlayParent(this).getClass().getSimpleName();
    }

    private String getTranslationFeatureKeyName() {
        String name = getDeclaringFeatureClassName().replace("Feature", "");
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, name);
    }

    protected Boolean isUserEnabled() {
        return userEnabled.get();
    }

    public boolean shouldBeEnabled() {
        if (!isParentEnabled()) {
            return false;
        }

        if (this.isUserEnabled() != null) {
            return this.isUserEnabled();
        }

        return Managers.Overlay.isEnabledByDefault(this);
    }

    public final boolean isParentEnabled() {
        return Managers.Overlay.getOverlayParent(this).isEnabled();
    }

    public final String getParentTranslatedName() {
        return Managers.Overlay.getOverlayParent(this).getTranslatedName();
    }

    public float getWidth() {
        return this.size.get().getWidth();
    }

    public float getHeight() {
        return this.size.get().getHeight();
    }

    public OverlaySize getSize() {
        return size.get();
    }

    public OverlayPosition getPosition() {
        return position.get();
    }

    public void setPosition(OverlayPosition position) {
        this.position.store(position);
    }

    // Return the X where the overlay should be rendered
    public float getRenderX() {
        return getRenderX(this.position.get());
    }

    // Return the Y where the overlay should be rendered
    public float getRenderY() {
        return getRenderY(this.position.get());
    }

    public float getRenderX(OverlayPosition position) {
        final SectionCoordinates section = Managers.Overlay.getSection(position.getAnchorSection());
        return switch (position.getHorizontalAlignment()) {
            case LEFT -> section.x1() + position.getHorizontalOffset();
            case CENTER -> (section.x1() + section.x2() - this.getWidth()) / 2 + position.getHorizontalOffset();
            case RIGHT -> section.x2() + position.getHorizontalOffset() - this.getWidth();
        };
    }

    public float getRenderY(OverlayPosition position) {
        final SectionCoordinates section = Managers.Overlay.getSection(position.getAnchorSection());
        return switch (position.getVerticalAlignment()) {
            case TOP -> section.y1() + position.getVerticalOffset();
            case MIDDLE -> (section.y1() + section.y2() - this.getHeight()) / 2 + position.getVerticalOffset();
            case BOTTOM -> section.y2() + position.getVerticalOffset() - this.getHeight();
        };
    }

    public HorizontalAlignment getRenderHorizontalAlignment() {
        return horizontalAlignmentOverride.get() == null
                ? position.get().getHorizontalAlignment()
                : horizontalAlignmentOverride.get();
    }

    public VerticalAlignment getRenderVerticalAlignment() {
        return verticalAlignmentOverride.get() == null
                ? position.get().getVerticalAlignment()
                : verticalAlignmentOverride.get();
    }

    public Vec2 getCornerPoints(Corner corner) {
        return switch (corner) {
            case TOP_LEFT -> new Vec2(getRenderX(), getRenderY());
            case TOP_RIGHT -> new Vec2(getRenderX() + getWidth(), getRenderY());
            case BOTTOM_LEFT -> new Vec2(getRenderX(), getRenderY() + getHeight());
            case BOTTOM_RIGHT -> new Vec2(getRenderX() + getWidth(), getRenderY() + getHeight());
        };
    }

    @Override
    public int compareTo(Overlay other) {
        return ComparisonChain.start()
                .compareTrueFirst(this.isParentEnabled(), other.isParentEnabled())
                .compare(this.getDeclaringFeatureClassName(), other.getDeclaringFeatureClassName())
                .compare(
                        (this instanceof DynamicOverlay dynamicThis ? dynamicThis.getId() : 0),
                        (other instanceof DynamicOverlay dynamicOther ? dynamicOther.getId() : 0))
                .compare(this.getTranslatedName(), other.getTranslatedName())
                .result();
    }

    public void setHeight(float height) {
        getSize().setHeight(height);
    }

    public void setWidth(float width) {
        getSize().setWidth(width);
    }
}
