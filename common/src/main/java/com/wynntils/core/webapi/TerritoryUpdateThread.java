/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi;

import com.wynntils.core.Reference;
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
                WebManager.updateTerritories(handler);
                handler.dispatch();

                // TODO: Add events

                Thread.sleep(30000);
            }
        } catch (InterruptedException ignored) {
        }
        Reference.LOGGER.info("Terminating territory update thread.");
    }
}
