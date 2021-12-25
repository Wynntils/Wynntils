/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.framework.minecraft;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

/** A fake StringTag that gives a dynamic value for toString */
public class DynamicTag implements Tag {
    public static final TagType<StringTag> TYPE =
            new TagType<StringTag>() {
                public StringTag load(DataInput input, int depth, NbtAccounter accounter)
                        throws IOException {
                    accounter.accountBits(288L);
                    String string = input.readUTF();
                    accounter.accountBits((long) (16 * string.length()));
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

    private final Supplier<String> data;

    public DynamicTag(Supplier<String> data) {
        Objects.requireNonNull(data, "Null string not allowed");
        this.data = data;
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
        return quoteAndEscape(this.data.get());
    }

    public StringTag copy() {
        return StringTag.valueOf(data.get());
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else {
            return other instanceof StringTag && other.equals(StringTag.valueOf(data.get()));
        }
    }

    public int hashCode() {
        return this.data.get().hashCode();
    }

    public String getAsString() {
        return this.data.get();
    }

    public Component getPrettyDisplay(String string, int i) {
        String string2 = quoteAndEscape(this.data.get());
        String string3 = string2.substring(0, 1);
        Component component =
                (new TextComponent(string2.substring(1, string2.length() - 1)))
                        .withStyle(SYNTAX_HIGHLIGHTING_STRING);
        return (new TextComponent(string3)).append(component).append(string3);
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

        stringBuilder.setCharAt(0, (char) c);
        stringBuilder.append((char) c);
        return stringBuilder.toString();
    }
}
