package com.wynntils.core.consumers.functions.vm;

import com.wynntils.core.WynntilsMod;

public class ExpandedClassLoader extends ClassLoader {

    public ExpandedClassLoader() {
        super(WynntilsMod.class.getClassLoader());
    }

    public Class<?> define(byte[] bytes) {
        return defineClass(null, bytes, 0, bytes.length);
    }
}
