/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import com.wynntils.models.mount.type.MountType;
import com.wynntils.models.timetrials.type.TimeTrialInfo;
import com.wynntils.utils.type.CappedValue;
import java.util.List;

public class TimeTrialFunctions {
    public static class CurrentTrialCheckpointsFunction extends Function<CappedValue> {
        @Override
        public CappedValue getValue(FunctionArguments arguments) {
            TimeTrialInfo currentTimeTrial = Models.TimeTrial.getCurrentTimeTrial();

            if (currentTimeTrial == null) return CappedValue.EMPTY;

            return currentTimeTrial.getCheckpoints();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("trial_checkpoints");
        }
    }

    public static class CurrentTrialElapsedTimeFunction extends Function<Long> {
        @Override
        public Long getValue(FunctionArguments arguments) {
            TimeTrialInfo currentTimeTrial = Models.TimeTrial.getCurrentTimeTrial();

            if (currentTimeTrial == null) return -1L;

            return currentTimeTrial.getElapsedTime();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("trial_time");
        }
    }

    public static class CurrentTrialMountTypeFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            MountType mountType = Models.TimeTrial.getCurrentTrialMountType();

            if (mountType == null) return "";

            return mountType.name();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("trial_mount_type");
        }
    }

    public static class CurrentTrialNameFunction extends Function<String> {
        @Override
        public String getValue(FunctionArguments arguments) {
            return Models.TimeTrial.getCurrentTrialName();
        }

        @Override
        protected List<String> getAliases() {
            return List.of("trial_name");
        }
    }
}
