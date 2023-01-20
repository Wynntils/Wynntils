/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.models.emeralds.type;

public final class EmeraldSymbols {
    public static final char E = '\u00B2';
    public static final char B = '\u00BD';
    public static final char L = '\u00BC';

    public static final String E_STRING = Character.toString(E).intern();
    public static final String B_STRING = Character.toString(B).intern();
    public static final String L_STRING = Character.toString(L).intern();

    public static final String EMERALDS = (E_STRING).intern();
    public static final String EB = (E_STRING + B_STRING).intern();
    public static final String LE = (L_STRING + E_STRING).intern();
}
