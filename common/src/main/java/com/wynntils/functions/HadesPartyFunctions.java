/*
 * Copyright © Wynntils 2025-2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.functions;

import com.wynntils.core.components.Models;
import com.wynntils.core.components.Services;

import com.wynntils.services.hades.HadesUser;
import com.wynntils.templates.annotations.TemplateFunction;
import com.wynntils.utils.mc.type.Location;
import com.wynntils.utils.type.CappedValue;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("unused") // Functions are accessed via reflection
public class HadesPartyFunctions {

    private static Optional<HadesUser> hadesPartyFunctionBase(int index) {
        // Get War members first
        List<HadesUser> members = Models.War.getHadesUsers();

        // If there are no War members get regular party members and order them
        if (members.isEmpty()) {
            List<String> partyMembers = Models.Party.getPartyMembers();
            members = Services.Hades.getHadesUsers()
                    .filter(hadesUser -> partyMembers.contains(hadesUser.getName()))
                    .sorted(Comparator.comparing(hadesUser -> partyMembers.indexOf(hadesUser.getName())))
                    .toList();
        }
        return !members.isEmpty() && index >= 0 && index < members.size()
                ? Optional.of(members.get(index))
                : Optional.empty();
    }

    @TemplateFunction(name = "hades_party_member_health")
    public static CappedValue hadesPartyMemberHealthFunction(int index) {
        return hadesPartyFunctionBase(index)
                .map(u -> u.getHealth())
                .orElse(CappedValue.EMPTY);
    }

    @TemplateFunction(name = "hades_party_member_mana")
    public static CappedValue hadesPartyMemberManaFunction(int index) {
        return hadesPartyFunctionBase(index)
                .map(u -> u.getMana())
                .orElse(CappedValue.EMPTY);
    }

    @TemplateFunction(name = "hades_party_member_location")
    public static Location hadesPartyMemberLocationFunction(int index) {
        return hadesPartyFunctionBase(index)
                .map(u -> u.getMapLocation().asLocation())
                .orElse(Location.ZERO);
    }

    @TemplateFunction(name = "hades_party_member_name")
    public static String hadesPartyMemberNameFunction(int index) {
        return hadesPartyFunctionBase(index)
                .map(u -> u.getName())
                .orElse("");
    }

    @TemplateFunction(name = "hades_party_member_uuid")
    public static String hadesPartyMemberUuidFunction(int index) {
        return hadesPartyFunctionBase(index)
                .map(u -> u.getUuid().toString())
                .orElse("");
    }
}
