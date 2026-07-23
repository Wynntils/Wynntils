package com.wynntils.functions;

import com.wynntils.core.WynntilsMod;
import com.wynntils.templates.annotations.TemplateFunction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class InventoryFunctionsTest {

    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    void allPublicStaticMethodsAreTemplateFunctions() {
        Arrays.stream(InventoryFunctions.class.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .forEach(method -> assertNotNull(
                        method.getAnnotation(TemplateFunction.class),
                        () -> method.getName() + " is missing @TemplateFunction"));
    }

    @Test
    void invalidEquipmentNamesReturnFallbackValues() {
        assertNotNull(InventoryFunctions.accessoryDurabilityFunction("not_an_accessory"));
        assertNotNull(InventoryFunctions.armorDurabilityFunction("not_armor"));
        assertEquals("NONE", InventoryFunctions.equippedAccessoryNameFunction("not_an_accessory"));
        assertEquals("NONE", InventoryFunctions.equippedArmorNameFunction("not_armor"));
    }

    @Test
    void emeraldPartsRecombineIntoTotalMoney() {
        int money = InventoryFunctions.moneyFunction();

        assertEquals(money / 4096, InventoryFunctions.liquidEmeraldFunction());
        assertEquals((money % 4096) / 64, InventoryFunctions.emeraldBlockFunction());
        assertEquals(money % 64, InventoryFunctions.emeraldsFunction());
    }
}
