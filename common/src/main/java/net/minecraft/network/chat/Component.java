/*
 * Copyright © Wynntils 2022.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package net.minecraft.network.chat;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.stream.JsonReader;
import com.mojang.brigadier.Message;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LowerCaseEnumTypeAdapterFactory;

public interface Component extends Message, FormattedText {
    static TextComponent literal(String s) {
        return new TextComponent(s);
    }

    static TranslatableComponent translatable(String string) {
        return new TranslatableComponent(string);
    }

    static TranslatableComponent translatable(String string, Object... objects) {
        return new TranslatableComponent(string, objects);
    }

    /**
     * Gets the style of this component.
     */
    Style getStyle();

    /**
     * Gets the raw content of this component if possible. For special components (like {@link TranslatableComponent} this usually returns the empty string.
     */
    String getContents();

    @Override
    default String getString() {
        return FormattedText.super.getString();
    }

    /**
     * Get the plain text of this FormattedText, without any styling or formatting codes, limited to {@code maxLength} characters.
     */
    default String getString(int maxLength) {
        StringBuilder stringBuilder = new StringBuilder();
        this.visit(string -> {
            int j = maxLength - stringBuilder.length();
            if (j <= 0) {
                return STOP_ITERATION;
            } else {
                stringBuilder.append(string.length() <= j ? string : string.substring(0, j));
                return Optional.empty();
            }
        });
        return stringBuilder.toString();
    }

    /**
     * Gets the sibling components of this one.
     */
    List<Component> getSiblings();

    /**
     * Creates a copy of this component, losing any style or siblings.
     */
    MutableComponent plainCopy();

    /**
     * Creates a copy of this component and also copies the style and siblings. Note that the siblings are copied shallowly, meaning the siblings themselves are not copied.
     */
    MutableComponent copy();

    FormattedCharSequence getVisualOrderText();

    @Override
    default <T> Optional<T> visit(StyledContentConsumer<T> acceptor, Style style) {
        Style style2 = this.getStyle().applyTo(style);
        Optional<T> optional = this.visitSelf(acceptor, style2);
        if (optional.isPresent()) {
            return optional;
        } else {
            for (Component component : this.getSiblings()) {
                Optional<T> optional2 = component.visit(acceptor, style2);
                if (optional2.isPresent()) {
                    return optional2;
                }
            }

            return Optional.empty();
        }
    }

    @Override
    default <T> Optional<T> visit(ContentConsumer<T> acceptor) {
        Optional<T> optional = this.visitSelf(acceptor);
        if (optional.isPresent()) {
            return optional;
        } else {
            for (Component component : this.getSiblings()) {
                Optional<T> optional2 = component.visit(acceptor);
                if (optional2.isPresent()) {
                    return optional2;
                }
            }

            return Optional.empty();
        }
    }

    default <T> Optional<T> visitSelf(StyledContentConsumer<T> consumer, Style style) {
        return consumer.accept(style, this.getContents());
    }

    default <T> Optional<T> visitSelf(ContentConsumer<T> consumer) {
        return consumer.accept(this.getContents());
    }

    default List<Component> toFlatList(Style style) {
        List<Component> list = Lists.<Component>newArrayList();
        this.visit(
                (stylex, string) -> {
                    if (!string.isEmpty()) {
                        list.add(literal(string).withStyle(stylex));
                    }

                    return Optional.empty();
                },
                style);
        return list;
    }

    static Component nullToEmpty(String text) {
        return (Component) (text != null ? literal(text) : TextComponent.EMPTY);
    }

    public static class Serializer implements JsonDeserializer<MutableComponent>, JsonSerializer<Component> {
        private static final Gson GSON = Util.make(() -> {
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.disableHtmlEscaping();
            gsonBuilder.registerTypeHierarchyAdapter(Component.class, new Serializer());
            gsonBuilder.registerTypeHierarchyAdapter(Style.class, new Style.Serializer());
            gsonBuilder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
            return gsonBuilder.create();
        });
        private static final Field JSON_READER_POS = Util.make(() -> {
            try {
                new JsonReader(new StringReader(""));
                Field field = JsonReader.class.getDeclaredField("pos");
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException var1) {
                throw new IllegalStateException("Couldn't get field 'pos' for JsonReader", var1);
            }
        });
        private static final Field JSON_READER_LINESTART = Util.make(() -> {
            try {
                new JsonReader(new StringReader(""));
                Field field = JsonReader.class.getDeclaredField("lineStart");
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException var1) {
                throw new IllegalStateException("Couldn't get field 'lineStart' for JsonReader", var1);
            }
        });

        public MutableComponent deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            if (json.isJsonPrimitive()) {
                return literal(json.getAsString());
            } else if (!json.isJsonObject()) {
                if (json.isJsonArray()) {
                    JsonArray jsonArray3 = json.getAsJsonArray();
                    MutableComponent mutableComponent = null;

                    for (JsonElement jsonElement : jsonArray3) {
                        MutableComponent mutableComponent2 =
                                this.deserialize(jsonElement, jsonElement.getClass(), context);
                        if (mutableComponent == null) {
                            mutableComponent = mutableComponent2;
                        } else {
                            mutableComponent.append(mutableComponent2);
                        }
                    }

                    return mutableComponent;
                } else {
                    throw new JsonParseException("Don't know how to turn " + json + " into a Component");
                }
            } else {
                JsonObject jsonObject = json.getAsJsonObject();
                MutableComponent mutableComponent;
                if (jsonObject.has("text")) {
                    mutableComponent = literal(GsonHelper.getAsString(jsonObject, "text"));
                } else if (jsonObject.has("translate")) {
                    String string = GsonHelper.getAsString(jsonObject, "translate");
                    if (jsonObject.has("with")) {
                        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "with");
                        Object[] objects = new Object[jsonArray.size()];

                        for (int i = 0; i < objects.length; ++i) {
                            objects[i] = this.deserialize(jsonArray.get(i), typeOfT, context);
                            if (objects[i] instanceof TextComponent textComponent
                                    && textComponent.getStyle().isEmpty()
                                    && textComponent.getSiblings().isEmpty()) {
                                objects[i] = textComponent.getText();
                            }
                        }

                        mutableComponent = translatable(string, objects);
                    } else {
                        mutableComponent = translatable(string);
                    }
                } else if (jsonObject.has("score")) {
                    JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "score");
                    if (!jsonObject2.has("name") || !jsonObject2.has("objective")) {
                        throw new JsonParseException("A score component needs a least a name and an objective");
                    }

                    mutableComponent = new ScoreComponent(
                            GsonHelper.getAsString(jsonObject2, "name"),
                            GsonHelper.getAsString(jsonObject2, "objective"));
                } else if (jsonObject.has("selector")) {
                    Optional<Component> optional = this.parseSeparator(typeOfT, context, jsonObject);
                    mutableComponent = new SelectorComponent(GsonHelper.getAsString(jsonObject, "selector"), optional);
                } else if (jsonObject.has("keybind")) {
                    mutableComponent = new KeybindComponent(GsonHelper.getAsString(jsonObject, "keybind"));
                } else {
                    if (!jsonObject.has("nbt")) {
                        throw new JsonParseException("Don't know how to turn " + json + " into a Component");
                    }

                    String string = GsonHelper.getAsString(jsonObject, "nbt");
                    Optional<Component> optional2 = this.parseSeparator(typeOfT, context, jsonObject);
                    boolean bl = GsonHelper.getAsBoolean(jsonObject, "interpret", false);
                    if (jsonObject.has("block")) {
                        mutableComponent = new NbtComponent.BlockNbtComponent(
                                string, bl, GsonHelper.getAsString(jsonObject, "block"), optional2);
                    } else if (jsonObject.has("entity")) {
                        mutableComponent = new NbtComponent.EntityNbtComponent(
                                string, bl, GsonHelper.getAsString(jsonObject, "entity"), optional2);
                    } else {
                        if (!jsonObject.has("storage")) {
                            throw new JsonParseException("Don't know how to turn " + json + " into a Component");
                        }

                        mutableComponent = new NbtComponent.StorageNbtComponent(
                                string,
                                bl,
                                new ResourceLocation(GsonHelper.getAsString(jsonObject, "storage")),
                                optional2);
                    }
                }

                if (jsonObject.has("extra")) {
                    JsonArray jsonArray2 = GsonHelper.getAsJsonArray(jsonObject, "extra");
                    if (jsonArray2.size() <= 0) {
                        throw new JsonParseException("Unexpected empty array of components");
                    }

                    for (int j = 0; j < jsonArray2.size(); ++j) {
                        mutableComponent.append(this.deserialize(jsonArray2.get(j), typeOfT, context));
                    }
                }

                mutableComponent.setStyle(context.deserialize(json, Style.class));
                return mutableComponent;
            }
        }

        private Optional<Component> parseSeparator(
                Type type, JsonDeserializationContext jsonContext, JsonObject jsonObject) {
            return jsonObject.has("separator")
                    ? Optional.of(this.deserialize(jsonObject.get("separator"), type, jsonContext))
                    : Optional.empty();
        }

        private void serializeStyle(Style style, JsonObject object, JsonSerializationContext ctx) {
            JsonElement jsonElement = ctx.serialize(style);
            if (jsonElement.isJsonObject()) {
                JsonObject jsonObject = (JsonObject) jsonElement;

                for (Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    object.add((String) entry.getKey(), (JsonElement) entry.getValue());
                }
            }
        }

        public JsonElement serialize(Component src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            if (!src.getStyle().isEmpty()) {
                this.serializeStyle(src.getStyle(), jsonObject, context);
            }

            if (!src.getSiblings().isEmpty()) {
                JsonArray jsonArray = new JsonArray();

                for (Component component : src.getSiblings()) {
                    jsonArray.add(this.serialize(component, component.getClass(), context));
                }

                jsonObject.add("extra", jsonArray);
            }

            if (src instanceof TextComponent) {
                jsonObject.addProperty("text", ((TextComponent) src).getText());
            } else if (src instanceof TranslatableComponent translatableComponent) {
                jsonObject.addProperty("translate", translatableComponent.getKey());
                if (translatableComponent.getArgs() != null && translatableComponent.getArgs().length > 0) {
                    JsonArray jsonArray2 = new JsonArray();

                    for (Object object : translatableComponent.getArgs()) {
                        if (object instanceof Component) {
                            jsonArray2.add(this.serialize((Component) object, object.getClass(), context));
                        } else {
                            jsonArray2.add(new JsonPrimitive(String.valueOf(object)));
                        }
                    }

                    jsonObject.add("with", jsonArray2);
                }
            } else if (src instanceof ScoreComponent scoreComponent) {
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.addProperty("name", scoreComponent.getName());
                jsonObject2.addProperty("objective", scoreComponent.getObjective());
                jsonObject.add("score", jsonObject2);
            } else if (src instanceof SelectorComponent selectorComponent) {
                jsonObject.addProperty("selector", selectorComponent.getPattern());
                this.serializeSeparator(context, jsonObject, selectorComponent.getSeparator());
            } else if (src instanceof KeybindComponent keybindComponent) {
                jsonObject.addProperty("keybind", keybindComponent.getName());
            } else {
                if (!(src instanceof NbtComponent)) {
                    throw new IllegalArgumentException("Don't know how to serialize " + src + " as a Component");
                }

                NbtComponent nbtComponent = (NbtComponent) src;
                jsonObject.addProperty("nbt", nbtComponent.getNbtPath());
                jsonObject.addProperty("interpret", nbtComponent.isInterpreting());
                this.serializeSeparator(context, jsonObject, nbtComponent.separator);
                if (src instanceof NbtComponent.BlockNbtComponent blockNbtComponent) {
                    jsonObject.addProperty("block", blockNbtComponent.getPos());
                } else if (src instanceof NbtComponent.EntityNbtComponent entityNbtComponent) {
                    jsonObject.addProperty("entity", entityNbtComponent.getSelector());
                } else {
                    if (!(src instanceof NbtComponent.StorageNbtComponent)) {
                        throw new IllegalArgumentException("Don't know how to serialize " + src + " as a Component");
                    }

                    NbtComponent.StorageNbtComponent storageNbtComponent = (NbtComponent.StorageNbtComponent) src;
                    jsonObject.addProperty(
                            "storage", storageNbtComponent.getId().toString());
                }
            }

            return jsonObject;
        }

        private void serializeSeparator(
                JsonSerializationContext context, JsonObject json, Optional<Component> separator) {
            separator.ifPresent(
                    component -> json.add("separator", this.serialize(component, component.getClass(), context)));
        }

        /**
         * Serializes a component into JSON.
         */
        public static String toJson(Component component) {
            return GSON.toJson(component);
        }

        public static JsonElement toJsonTree(Component component) {
            return GSON.toJsonTree(component);
        }

        public static MutableComponent fromJson(String json) {
            return GsonHelper.fromJson(GSON, json, MutableComponent.class, false);
        }

        public static MutableComponent fromJson(JsonElement json) {
            return GSON.fromJson(json, MutableComponent.class);
        }

        public static MutableComponent fromJsonLenient(String json) {
            return GsonHelper.fromJson(GSON, json, MutableComponent.class, true);
        }

        public static MutableComponent fromJson(com.mojang.brigadier.StringReader reader) {
            try {
                JsonReader jsonReader = new JsonReader(new StringReader(reader.getRemaining()));
                jsonReader.setLenient(false);
                MutableComponent mutableComponent = GSON.<MutableComponent>getAdapter(MutableComponent.class)
                        .read(jsonReader);
                reader.setCursor(reader.getCursor() + getPos(jsonReader));
                return mutableComponent;
            } catch (StackOverflowError | IOException var3) {
                throw new JsonParseException(var3);
            }
        }

        private static int getPos(JsonReader reader) {
            try {
                return JSON_READER_POS.getInt(reader) - JSON_READER_LINESTART.getInt(reader) + 1;
            } catch (IllegalAccessException var2) {
                throw new IllegalStateException("Couldn't read position of JsonReader", var2);
            }
        }
    }
}
