/*
 *  * Copyright © Wynntils - 2022.
 */

package com.wynntils.core.features.overlays;

import com.wynntils.core.features.Feature;
import com.wynntils.core.framework.FrameworkManager;
import com.wynntils.core.framework.instances.Module;
import com.wynntils.core.framework.instances.PlayerInfo;
import com.wynntils.core.framework.instances.containers.ModuleContainer;
import com.wynntils.core.framework.instances.containers.PlayerData;
import com.wynntils.core.framework.rendering.ScreenRenderer;
import com.wynntils.core.framework.rendering.SmartFontRenderer;
import com.wynntils.core.framework.settings.annotations.Setting;
import com.wynntils.core.framework.settings.instances.SettingsHolder;
import com.wynntils.core.utils.objects.Position;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;

import static com.wynntils.core.framework.rendering.SmartFontRenderer.TextAlignment.*;

public abstract class Overlay extends Feature { // extends ScreenRenderer implements SettingsHolder {

    //public transient ModuleContainer module = null;
    public transient String displayName;
    public transient Point staticSize;
    public transient boolean visible;
    public transient OverlayGrowFrom growth;
    //public transient RenderGameOverlayEvent.ElementType[] overrideElements;

    //@Setting
    //public boolean active = true;
    //@Setting
    //public Position position = new Position();

    public Overlay(String displayName, int sizeX, int sizeY, boolean visible, float anchorX, float anchorY, int offsetX, int offsetY, OverlayGrowFrom growth, RenderGameOverlayEvent.ElementType... overrideElements) {
        this.displayName = displayName;
        this.staticSize = new Point(sizeX, sizeY);
        this.visible = visible;
    //    this.overrideElements = overrideElements;
    //    this.position.anchorX = anchorX;
    //    this.position.anchorY = anchorY;
    //    this.position.offsetX = offsetX;
    //    this.position.offsetY = offsetY;
        this.growth = growth;
    //    this.position.refresh(screen);
    }

    //public void render(RenderGameOverlayEvent.Pre event) {}
    //public void render(RenderGameOverlayEvent.Post event) {}
    //public void tick(TickEvent.ClientTickEvent event, long ticks) {}

    //public <T extends PlayerData> T get(Class<T> clazz) {
    //    return PlayerInfo.get(clazz);
    //}

    //public SmartFontRenderer.TextAlignment getAlignment() {
    //    return growth.getAlignment();
    //}

    /*
    @Override
    public void saveSettings(Module m) {
        try {
            FrameworkManager.getSettings(m == null ? module.getModule() : m, this).saveSettings();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    @Override
    public void onSettingChanged(String name) {

    }

    public enum OverlayGrowFrom {

        TOP_LEFT    (LEFT_RIGHT), TOP_CENTRE    (MIDDLE), TOP_RIGHT    (RIGHT_LEFT),
        MIDDLE_LEFT (LEFT_RIGHT), MIDDLE_CENTRE (MIDDLE), MIDDLE_RIGHT (RIGHT_LEFT),
        BOTTOM_LEFT (LEFT_RIGHT), BOTTOM_CENTRE (MIDDLE), BOTTOM_RIGHT (RIGHT_LEFT);

        SmartFontRenderer.TextAlignment alignment;

        OverlayGrowFrom(SmartFontRenderer.TextAlignment alignment) {
            this.alignment = alignment;
        }

        public SmartFontRenderer.TextAlignment getAlignment() {
            return alignment;
        }

    }
    
     */

}