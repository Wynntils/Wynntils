/*
 * Copyright © Wynntils 2021.
 * This file is released under AGPLv3. See LICENSE for full license details.
 */
package com.wynntils.framework.events;

import com.wynntils.framework.Subscriber;
import com.wynntils.framework.feature.Feature;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

// TODO integrate forge's event bus or add asm for performance
public class EventBus {
    private final ConcurrentMap<Class<?>, ListenerHandler> subscribers = new ConcurrentHashMap<>();

    public void register(Feature obj) {
        Arrays.stream(obj.getClass().getDeclaredMethods())
                .forEach(
                        m -> {
                            if (!m.isAnnotationPresent(Subscriber.class)) return;
                            if (!Modifier.isStatic(m.getModifiers())
                                    || !Modifier.isPublic(m.getModifiers())) return;

                            if (m.getParameters().length != 1) return;

                            Class<?> parameterType = m.getParameterTypes()[0];
                            if (!Event.class.isAssignableFrom(parameterType)) return;

                            ListenerHandler handler =
                                    subscribers.computeIfAbsent(
                                            parameterType, c -> new ListenerHandler());
                            handler.listeners.add(new EventListener(m, obj.getClass()));
                        });
    }

    public void unregister(Feature obj) {
        List<Class<?>> toCheck = new ArrayList<>();

        Arrays.stream(obj.getClass().getDeclaredMethods())
                .forEach(
                        m -> {
                            if (!m.isAnnotationPresent(Subscriber.class)) return;
                            if (!Modifier.isStatic(m.getModifiers())
                                    || !Modifier.isPublic(m.getModifiers())) return;

                            if (m.getParameters().length != 1) return;

                            Class<?> parameterType = m.getParameterTypes()[0];
                            if (!Event.class.isAssignableFrom(parameterType)) return;

                            toCheck.add(parameterType);
                        });

        for (Class<?> clazz : toCheck) {
            ListenerHandler handler = subscribers.get(clazz);
            handler.listeners.removeIf(l -> l.getParent().equals(obj.getClass()));
            if (handler.listeners.isEmpty()) subscribers.remove(clazz);
        }
    }

    public <T extends Event> boolean postEvent(T event) {
        if (!subscribers.containsKey(event.getClass())) return true;

        subscribers.get(event.getClass()).postListeners(event);

        return event.isCanceled();
    }

    public static class ListenerHandler {
        public final List<EventListener> listeners = new ArrayList<>();

        public void postListeners(Object value) {
            listeners.forEach(
                    l -> {
                        try {
                            l.accept(value);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
        }
    }
}
