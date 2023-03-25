package com.wynntils.features.overlays;

import com.wynntils.core.components.Models;
import com.wynntils.core.config.Category;
import com.wynntils.core.config.ConfigCategory;
import com.wynntils.core.features.Feature;
import com.wynntils.core.features.overlays.Overlay;
import com.wynntils.core.features.overlays.OverlayPosition;
import com.wynntils.core.features.overlays.OverlaySize;
import com.wynntils.core.features.overlays.TextOverlay;
import com.wynntils.core.features.overlays.annotations.OverlayInfo;
import com.wynntils.core.features.properties.RegisterKeyBind;
import com.wynntils.core.keybinds.KeyBind;
import com.wynntils.mc.event.RenderEvent;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import org.lwjgl.glfw.GLFW;

@ConfigCategory(Category.OVERLAYS)
public class StopwatchFeature extends Feature {
    @RegisterKeyBind
    private final KeyBind toggleStopwatchKeybind =
            new KeyBind("Toggle Stopwatch", GLFW.GLFW_KEY_KP_0, true, this::toggleStopwatch);

    @RegisterKeyBind
    private final KeyBind resetStopwatchKeybind =
            new KeyBind("Reset Stopwatch", GLFW.GLFW_KEY_KP_DECIMAL, true, Models.Stopwatch::reset);

    @OverlayInfo(renderType = RenderEvent.ElementType.GUI)
    private final Overlay stopwatchOverlay = new StopwatchOverlay();

    private void toggleStopwatch() {
        if (Models.Stopwatch.isRunning()) {
            Models.Stopwatch.stop();
        } else {
            Models.Stopwatch.start();
        }
    }

    public static class StopwatchOverlay extends TextOverlay {
        private static final String TEMPLATE =
                "{STOPWATCH_HOURS}:{STOPWATCH_MINUTES}:{STOPWATCH_SECONDS}.{STOPWATCH_MILLISECONDS}";

        protected StopwatchOverlay() {
            super(
                    new OverlayPosition(
                            0,
                            0,
                            VerticalAlignment.Bottom,
                            HorizontalAlignment.Left,
                            OverlayPosition.AnchorSection.BottomLeft),
                    new OverlaySize(100, 20),
                    HorizontalAlignment.Center,
                    VerticalAlignment.Middle);
        }

        @Override
        public String getTemplate() {
            return TEMPLATE;
        }

        @Override
        public String getPreviewTemplate() {
            return "01:24:31.877";
        }
    }
}
