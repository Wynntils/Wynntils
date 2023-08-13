/*
 * Copyright © Wynntils 2022-2023.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.net;

import com.wynntils.core.components.Managers;
import com.wynntils.core.net.event.NetResultProcessedEvent;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ApiResponse extends NetResult {
    public ApiResponse(String desc, HttpRequest request, NetResultProcessedEvent processedEvent) {
        super("API:" + desc, request, processedEvent);
    }

    @Override
    protected CompletableFuture<InputStream> getInputStreamFuture() {
        return Managers.Net.HTTP_CLIENT
                .sendAsync(request, HttpResponse.BodyHandlers.ofInputStream())
                .thenApply(HttpResponse::body);
    }
}
