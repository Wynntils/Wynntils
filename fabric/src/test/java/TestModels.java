/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.components.Models;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestModels {
    @BeforeAll
    public static void setup() {
        WynntilsMod.setupTestEnv();
    }

    @Test
    public void EmeraldModel_convertEmeraldPrice() {
        // Pure numbers will not be changed
        String eStr = Models.Emerald.convertEmeraldPrice("105");
        Assertions.assertEquals("", eStr);

        String badStr = Models.Emerald.convertEmeraldPrice("whatisthisidonteven");
        Assertions.assertEquals("", badStr);

        String badStr2 = Models.Emerald.convertEmeraldPrice("10$");
        Assertions.assertEquals("", badStr);

        String kStr = Models.Emerald.convertEmeraldPrice("10k");
        Assertions.assertEquals("10000", kStr);

        String kmStr = Models.Emerald.convertEmeraldPrice("1 m 20k");
        Assertions.assertEquals("1020000", kmStr);

        String leStr = Models.Emerald.convertEmeraldPrice("1 le");
        Assertions.assertEquals("4096", leStr);

        String eStrTax = Models.Emerald.convertEmeraldPrice("105 -t");
        Assertions.assertEquals("100", eStrTax);

        String eStrTax2 = Models.Emerald.convertEmeraldPrice("105-t");
        Assertions.assertEquals("100", eStrTax2);

        String kStrTax = Models.Emerald.convertEmeraldPrice("10k -t");
        Assertions.assertEquals("9524", kStrTax);

        String kStrTax2 = Models.Emerald.convertEmeraldPrice("10k-t");
        Assertions.assertEquals("9524", kStrTax2);
    }
}
