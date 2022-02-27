/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.core.webapi.request.multipart;

import java.io.IOException;
import java.io.OutputStream;

public interface IMultipartFormPart {

    int getLength();

    void write(OutputStream o) throws IOException;
}
