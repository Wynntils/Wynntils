package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class SocialFunctionsTest {

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(SocialFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void invalidPartyMemberIndexReturnsFallbackValues() {
        assertEquals("", SocialFunctions.partyMemberNameFunction(-1));
        assertEquals(0, SocialFunctions.partyMemberHealthFunction(-1));
        assertEquals(0, SocialFunctions.partyMemberLevelFunction(-1));
        assertFalse(SocialFunctions.isPartyMemberOnlineFunction(-1));
        assertFalse(SocialFunctions.isPartyMemberAliveFunction(-1));
    }

    @Test
    void defaultPartyCountExcludesOfflineMembers() {
        assertEquals(SocialFunctions.partyMembersFunction(false), SocialFunctions.partyMembersFunction());
    }
}
