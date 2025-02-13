/*
 * Copyright © Wynntils 2025.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.services.athena.type;

public record ModUpdateInfo(String version, String supportedMcVersion, String md5, String url, String changelog) {}
