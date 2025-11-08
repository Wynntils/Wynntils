/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.wynntils.utils.type.Time;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestUtils {
    @Test
    public void testTypes() {
        Time now = Time.now();
        long offset = now.getOffset(now.offset(10));
        Assertions.assertEquals(10, offset);
    }
}
