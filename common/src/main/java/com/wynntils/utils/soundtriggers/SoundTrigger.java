/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.soundtriggers;

import com.google.common.collect.ComparisonChain;
import com.wynntils.core.components.Managers;
import com.wynntils.utils.type.ErrorOr;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class SoundTrigger {
    private boolean enabled;
    private String controllerFunction;
    private String identifierFunction;
    private String name;
    private int volume;
    private int pitch;

    private TriggerType type;
    private int interval;

    private transient Boolean lastResult = true;
    private transient Instant lastPlayed = Instant.now();

    private SoundTrigger(
            TriggerType type,
            String controllerFunction,
            String identifierFunction,
            String name,
            int volume,
            int interval,
            int pitch) {
        this.enabled = true;
        this.type = type;
        this.controllerFunction = controllerFunction;
        this.identifierFunction = identifierFunction;
        this.name = name;
        this.volume = volume;
        this.interval = interval;
        this.pitch = pitch;
    }

    public SoundTrigger(String controllerFunction, String identifierFunction, String name, int volume, int interval) {
        this(TriggerType.CONTINUOUS, controllerFunction, identifierFunction, name, volume, interval, 100);
    }

    public SoundTrigger(String controllerFunction, String identifierFunction, String name, int volume) {
        this(TriggerType.SINGULAR, controllerFunction, identifierFunction, name, volume, 5, 100);
    }

    public boolean shouldPlay() {
        switch (type) {
            case SINGULAR -> {
                ErrorOr<Boolean> value = getControllerFunctionResult();
                if (value.hasError()) {
                    this.lastResult = false;
                    return false;
                }
                boolean shouldPlay = !getLastResult() && value.getValue();
                this.lastResult = value.getValue();
                return shouldPlay;
            }

            case CONTINUOUS -> {
                ErrorOr<Boolean> value = getControllerFunctionResult();
                if (value.hasError()) {
                    return false;
                }
                boolean shouldPlay = value.getValue()
                        && Instant.now().isAfter(getLastPlayed().plus(Duration.of(interval, ChronoUnit.SECONDS)));
                if (shouldPlay) this.lastPlayed = Instant.now();
                return shouldPlay;
            }

            case null, default -> {
                return false;
            }
        }
    }

    public String getControllerFunction() {
        return controllerFunction;
    }

    public void setControllerFunction(String controllFunction) {
        this.controllerFunction = controllFunction;
    }

    public String getIdentifierFunction() {
        return identifierFunction;
    }

    public void setIdentifierFunction(String identifierFunction) {
        this.identifierFunction = identifierFunction;
    }

    public String getName() {
        if (name.isBlank()) return "New Sound Trigger";
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVolume() {
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public ErrorOr<Boolean> getControllerFunctionResult() {
        return Managers.Function.tryGetRawValueOfType(getControllerFunction(), Boolean.class);
    }

    public ErrorOr<String> getIdentifierFunctionResult() {
        return Managers.Function.tryGetRawValueOfType(getIdentifierFunction(), String.class);
    }

    public TriggerType getType() {
        return type;
    }

    public void setType(TriggerType type) {
        this.type = type;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    private boolean getLastResult() {
        if (lastResult == null) {
            lastResult = true;
        }
        return lastResult;
    }

    private Instant getLastPlayed() {
        if (lastPlayed == null) {
            lastPlayed = Instant.now();
        }
        return lastPlayed;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int compareTo(SoundTrigger trigger) {
        return ComparisonChain.start()
                .compareTrueFirst(this.isEnabled(), trigger.isEnabled())
                .compareFalseFirst(
                        this.getControllerFunctionResult().hasError(),
                        trigger.getControllerFunctionResult().hasError())
                .compareFalseFirst(
                        this.getIdentifierFunctionResult().hasError(),
                        trigger.getIdentifierFunctionResult().hasError())
                .compare(this.getName(), trigger.getName())
                .result();
    }

    public int getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }
}
