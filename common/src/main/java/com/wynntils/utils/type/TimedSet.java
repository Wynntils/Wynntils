/*
 * Copyright © Wynntils 2022-2024.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.utils.type;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TimedSet<T> implements Iterable<T> {
    private final Set<TimedEntry> entries;

    private final long timeJump;
    private final boolean autoClear;

    public TimedSet(long duration, TimeUnit unit, boolean autoClear) {
        timeJump = unit.toMillis(duration);
        this.autoClear = autoClear;
        entries = new HashSet<>();
    }

    public TimedSet(long duration, TimeUnit unit, boolean autoClear, Supplier<Set<TimedEntry>> setSupplier) {
        timeJump = unit.toMillis(duration);
        this.autoClear = autoClear;
        entries = setSupplier.get();
    }

    public TimedSet(long duration, TimeUnit unit) {
        this(duration, unit, false);
    }

    private void releaseEntries() {
        Iterator<TimedEntry> it = entries.iterator();
        while (it.hasNext()) {
            TimedEntry entry = it.next();
            if (!entry.shouldRelease()) continue;

            it.remove();
        }
    }

    public void put(T entry) {
        entries.add(new TimedEntry(entry, System.currentTimeMillis(), timeJump));
    }

    public void remove(T entry) {
        entries.removeIf(e -> e.getEntry().equals(entry));
    }

    public long getLastAddedTimestamp() {
        Optional<TimedEntry> latest = entries.stream().max(Comparator.comparing(TimedEntry::getExpiration));
        if (latest.isEmpty()) return 0;

        return latest.get().getExpiration() - timeJump;
    }

    public void clear() {
        entries.clear();
    }

    public int size() {
        return entries.size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Iterator<T> iterator() {
        if (autoClear) releaseEntries();

        return new TimedSetIterator();
    }

    public Stream<T> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    public Set<TimedEntry> getEntries() {
        if (autoClear) releaseEntries();

        return Collections.unmodifiableSet(entries);
    }

    private class TimedSetIterator implements Iterator<T> {
        private final Iterator<TimedEntry> original = entries.iterator();

        @Override
        public boolean hasNext() {
            return original.hasNext();
        }

        @Override
        public T next() {
            return original.next().getEntry();
        }

        @Override
        public void remove() {
            original.remove();
        }
    }

    public final class TimedEntry {
        private final T entry;
        private final long creation;
        private final long duration;

        private TimedEntry(T entry, long creation, long duration) {
            this.entry = entry;
            this.creation = creation;
            this.duration = duration;
        }

        public long getExpiration() {
            return creation + duration;
        }

        public long getCreation() {
            return creation;
        }

        public T getEntry() {
            return entry;
        }

        private boolean shouldRelease() {
            return System.currentTimeMillis() >= getExpiration();
        }
    }
}
