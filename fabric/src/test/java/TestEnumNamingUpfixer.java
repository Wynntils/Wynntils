/*
 * Copyright (c) Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.google.gson.Gson;
import com.wynntils.core.persisted.upfixers.config.EnumNamingUpfixer;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestEnumNamingUpfixer {
    private enum SampleEnum {
        DEFAULT,
        VALUE
    }

    @Test
    public void testStringValueParsesAsExpected() throws Exception {
        Gson gson = getUpfixerGson();
        Assertions.assertEquals(SampleEnum.VALUE, gson.fromJson("\"VALUE\"", SampleEnum.class));
    }

    @Test
    public void testNullValueParsesAsNull() throws Exception {
        Gson gson = getUpfixerGson();
        Assertions.assertNull(gson.fromJson("null", SampleEnum.class));
    }

    @Test
    public void testNumberValueFallsBackToFirstEnumValue() throws Exception {
        Gson gson = getUpfixerGson();
        Assertions.assertEquals(SampleEnum.DEFAULT, gson.fromJson("1", SampleEnum.class));
    }

    @Test
    public void testBooleanValueFallsBackToFirstEnumValue() throws Exception {
        Gson gson = getUpfixerGson();
        Assertions.assertEquals(SampleEnum.DEFAULT, gson.fromJson("true", SampleEnum.class));
    }

    private static Gson getUpfixerGson() throws Exception {
        Field field = EnumNamingUpfixer.class.getDeclaredField("GSON");
        field.setAccessible(true);
        return (Gson) field.get(null);
    }
}
