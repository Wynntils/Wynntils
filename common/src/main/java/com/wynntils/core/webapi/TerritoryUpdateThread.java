/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi;

import com.wynntils.core.WynntilsMod;
import com.wynntils.core.webapi.request.RequestHandler;

public class TerritoryUpdateThread extends Thread {
    public TerritoryUpdateThread(String name) {
        super(name);
    }

    @Override
    public void run() {
        RequestHandler handler = new RequestHandler();

        try {
            Thread.sleep(30000);
            while (!isInterrupted()) {
                WebManager.tryLoadTerritories(handler);
                handler.dispatch();

                // TODO: Add events
                Thread.sleep(30000);
            }
        } catch (InterruptedException ignored) {
        }

        WynntilsMod.info("Terminating territory update thread.");
    }
}
