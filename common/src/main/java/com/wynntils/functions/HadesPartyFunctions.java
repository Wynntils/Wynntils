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

@SuppressWarnings("unused") // Functions are accessed via reflection
public class HadesPartyFunctions {
    private static HadesUser hadesPartyFunctionBase(int index) {
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
                ? members.get(index)
                : null;
    }


    @TemplateFunction(name = "hades_party_member_health")
    public static CappedValue hadesPartyMemberHealthFunction(int index) {
        HadesUser user = hadesPartyFunctionBase(index);
        return user != null ? user.getHealth() : CappedValue.EMPTY;
    }

    @TemplateFunction(name = "hades_party_member_mana")
    public static CappedValue hadesPartyMemberManaFunction(int index) {
        HadesUser user = hadesPartyFunctionBase(index);
        return user != null ? user.getMana() : CappedValue.EMPTY;
    }

    @TemplateFunction(name = "hades_party_member_location")
    public static Location hadesPartyMemberLocationFunction(int index) {
        HadesUser user = hadesPartyFunctionBase(index);
        return user != null ? user.getMapLocation().asLocation() : new Location(0, 0, 0);
    }

    @TemplateFunction(name = "hades_party_member_name")
    public static String hadesPartyMemberNameFunction(int index) {
        HadesUser user = hadesPartyFunctionBase(index);
        return user != null ? user.getName() : "";
    }

    @TemplateFunction(name = "hades_party_member_uuid")
    public static String hadesPartyMemberUuidFunction(int index) {
        HadesUser user = hadesPartyFunctionBase(index);
        return user.getUuid().toString();
    }
}
