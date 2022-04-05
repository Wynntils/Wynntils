/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.mc.utils.objects;

import net.minecraft.nbt.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;

/** A fake StringTag that gives a dynamic value for toString */
public class DynamicTag implements Tag {
    public static final TagType<StringTag> TYPE = new TagType<>() {
        public StringTag load(DataInput input, int depth, NbtAccounter accounter) throws IOException {
            accounter.accountBits(288L);
            String string = input.readUTF();
            accounter.accountBits(16L * string.length());
            return StringTag.valueOf(string);
        }

        public String getName() {
            return "STRING";
        }

        public String getPrettyName() {
            return "TAG_String";
        }

        public boolean isValue() {
            return true;
        }
    };
    private static final StringTag EMPTY = StringTag.valueOf("");
    private final Supplier<String> data;

    public DynamicTag(Supplier<String> string) {
        Objects.requireNonNull(string, "Null string not allowed");
        this.data = string;
    }

    public static StringTag valueOf(String data) {
        return data.isEmpty() ? EMPTY : StringTag.valueOf(data);
    }

    public void write(DataOutput output) throws IOException {
        output.writeUTF(this.data.get());
    }

    public byte getId() {
        return 8;
    }

    public TagType<StringTag> getType() {
        return TYPE;
    }

    public String toString() {
        return this.data.get();
    }

    public StringTag copy() {
        return StringTag.valueOf(this.data.get());
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object instanceof StringTag) {
            return object.equals(StringTag.valueOf(this.data.get()));
        } else {
            return object instanceof DynamicTag && Objects.equals(((DynamicTag) object).data, data);
        }
    }

    public int hashCode() {
        return this.data.hashCode();
    }

    public String getAsString() {
        return this.data.get();
    }

    public void accept(TagVisitor visitor) {
        visitor.visitString(StringTag.valueOf(this.data.get()));
    }

    public static String quoteAndEscape(String text) {
        StringBuilder stringBuilder = new StringBuilder(" ");
        char c = 0;

        for (int i = 0; i < text.length(); ++i) {
            char d = text.charAt(i);
            if (d == '\\') {
                stringBuilder.append('\\');
            } else if (d == '"' || d == '\'') {
                if (c == 0) {
                    c = (char) (d == '"' ? 39 : 34);
                }

                if (c == d) {
                    stringBuilder.append('\\');
                }
            }

            stringBuilder.append(d);
        }

        if (c == 0) {
            c = 34;
        }

        stringBuilder.setCharAt(0, c);
        stringBuilder.append(c);
        return stringBuilder.toString();
    }
}
