/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.item;

import java.util.ArrayDeque;
import java.util.Queue;

final class PagedItemUpdateTracker {
    private static final long PAGE_RESPONSE_TIMEOUT = 5_000_000_000L;

    private int menuId = -1;
    private final Queue<Long> pageRequests = new ArrayDeque<>();
    private boolean pageResponseStarted;

    public void reset(int menuId) {
        this.menuId = menuId;
        pageRequests.clear();
        pageResponseStarted = false;
    }

    public void pageChangeRequested(int menuId, long now) {
        if (this.menuId != menuId) {
            reset(menuId);
        }

        expireRequests(now);
        pageRequests.add(now);
    }

    public boolean canReuse(
            int menuId, boolean playerInventory, boolean tooltipPageChanged, boolean sameLore, long now) {
        expireRequests(now);
        boolean requestedPageChange = this.menuId == menuId && !pageRequests.isEmpty();

        if (tooltipPageChanged && requestedPageChange) {
            pageResponseStarted = true;
        }

        // Two items may have the same lines on the current page but different on another.
        // So only a requested page refresh (including its following snapshot) can carry them over.
        return playerInventory && (sameLore || tooltipPageChanged)
                || requestedPageChange && pageResponseStarted && (sameLore || tooltipPageChanged);
    }

    public void contentsReceived(int menuId, long now) {
        if (this.menuId != menuId) return;

        expireRequests(now);
        // A page request first updates the hovered slot, then refreshes every item's
        // tooltip page in a full snapshot. Do not consume it on the first slot update.
        if (pageResponseStarted) {
            pageRequests.poll();
            pageResponseStarted = false;
        }
    }

    private void expireRequests(long now) {
        while (!pageRequests.isEmpty() && now - pageRequests.peek() >= PAGE_RESPONSE_TIMEOUT) {
            pageRequests.remove();
            pageResponseStarted = false;
        }
    }
}
