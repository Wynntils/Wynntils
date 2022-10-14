/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package testUtils;

import com.wynntils.utils.objects.bvh.BoundingVolumeHierarchy;
import java.lang.reflect.Field;
import java.util.Set;

public class BvhTestUtils {
    private BvhTestUtils() {}

    /**
     * Inspects the BVH via reflection to check the rebuild status.
     * @param bvh to wait for.
     */
    public static boolean waitForBvhRebuild(BoundingVolumeHierarchy<?> bvh) {
        final Class<BoundingVolumeHierarchy> bvhClass = BoundingVolumeHierarchy.class;
        final Field dataLockField;
        final Field rebuildInProgressField;
        final Field pendingInsertsField;
        final Field pendingDeletesField;
        final Object dataLock;
        try {
            dataLockField = bvhClass.getDeclaredField("dataLock");
            dataLockField.setAccessible(true);
            rebuildInProgressField = bvhClass.getDeclaredField("rebuildInProgress");
            rebuildInProgressField.setAccessible(true);
            pendingInsertsField = bvhClass.getDeclaredField("pendingInserts");
            pendingInsertsField.setAccessible(true);
            pendingDeletesField = bvhClass.getDeclaredField("pendingDeletes");
            pendingDeletesField.setAccessible(true);
            dataLock = dataLockField.get(bvh);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            return false;
        }
        int rebuildDoneCounter = 0;
        while (true) {
            synchronized (dataLock) {
                boolean rebuildInProgress;
                Set<?> pendingInserts;
                Set<?> pendingDeletes;
                try {
                    rebuildInProgress = rebuildInProgressField.getBoolean(bvh);
                    pendingInserts = (Set<?>) pendingInsertsField.get(bvh);
                    pendingDeletes = (Set<?>) pendingDeletesField.get(bvh);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    return false;
                }
                if (!rebuildInProgress && pendingInserts.size() == 0 && pendingDeletes.size() == 0) {
                    rebuildDoneCounter++;
                    if (rebuildDoneCounter == 10) {
                        return true;
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // don't count this iteration and wait again
                    rebuildDoneCounter--;
                }
            }
        }
    }
}
