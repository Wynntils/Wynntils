/*
 * Copyright © Wynntils 2026.
 * This file is released under LGPLv3. See LICENSE for full license details.
 */
package com.wynntils.handlers.item;

import com.sun.management.ThreadMXBean;
import com.wynntils.core.WynntilsMod;
import com.wynntils.models.items.properties.PagedItemProperty;
import com.wynntils.utils.mc.McUtils;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

public final class ItemLoreProfiler {
    public enum Mode {
        COMPARE,
        BASELINE,
        OPTIMIZED
    }

    public enum UpdateReason {
        ALREADY_ANNOTATED,
        NO_EXISTING_ANNOTATION,
        STACK_CHANGED,
        NAME_CHANGED,
        LORE_COMPARISON,
        UNTRACKED_CONTAINER
    }

    @FunctionalInterface
    public interface Matcher {
        boolean matches(ItemStack first, ItemStack second, ItemAnnotation annotation, boolean allowFastPath);
    }

    private final Mode mode;
    private final int requestedSeconds;
    private final Matcher matcher;
    private final ThreadMXBean allocation;
    private final long threadId = Thread.currentThread().threadId();
    private final String threadName = Thread.currentThread().getName();
    private final MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
    private final List<GarbageCollectorMXBean> collectors = ManagementFactory.getGarbageCollectorMXBeans();
    private final Map<Class<?>, Group> groups = new LinkedHashMap<>();
    private final long[] updateReasons = new long[UpdateReason.values().length];
    private final Random random = new Random(0x51A7ED);
    private final Instant startedAt = Instant.now();
    private final long heapBefore;
    private final long collectionsBefore;
    private final long gcTimeBefore;
    private final long startedNanos;

    private long comparisons;
    private long annotationAttempts;
    private long sampledPeakHeap;
    private boolean fastPathUsed;

    public ItemLoreProfiler(Mode mode, int seconds, Matcher matcher) {
        if (seconds < 1 || seconds > 600) throw new IllegalArgumentException("Duration must be 1–600 seconds");
        this.mode = mode;
        this.requestedSeconds = seconds;
        this.matcher = matcher;
        this.allocation = allocationCounter();
        // Initialize the allocation counter outside the timed comparisons.
        allocatedBytes();
        heapBefore = memory.getHeapMemoryUsage().getUsed();
        sampledPeakHeap = heapBefore;
        collectionsBefore = collectionCount();
        gcTimeBefore = collectionTime();
        startedNanos = System.nanoTime();
    }

    public boolean compare(ItemStack first, ItemStack second, ItemAnnotation annotation) {
        Group group = groups.get(annotation.getClass());
        if (group == null) {
            group = new Group(annotation.getClass().getName(), annotation instanceof PagedItemProperty);
            groups.put(annotation.getClass(), group);
        }
        boolean optimized =
                switch (mode) {
                    case BASELINE -> false;
                    case OPTIMIZED -> true;
                    case COMPARE -> {
                        // Balance each annotation type separately, randomly flipping ABBA blocks so
                        // repeated inventory slots are not permanently assigned to one treatment.
                        if ((group.comparisons & 3) == 0) group.flipBlock = random.nextBoolean();
                        long position = group.comparisons & 3;
                        yield (position == 1 || position == 2) ^ group.flipBlock;
                    }
                };
        comparisons++;
        group.comparisons++;
        Stats stats = optimized ? group.optimized : group.baseline;
        int lines =
                first.getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines().size()
                        + second.getOrDefault(DataComponents.LORE, ItemLore.EMPTY)
                                .lines()
                                .size();
        fastPathUsed = false;
        boolean matched = false;
        boolean completed = false;
        long allocatedBefore = allocatedBytes();
        long start = System.nanoTime();
        try {
            matched = matcher.matches(first, second, annotation, optimized);
            completed = true;
            return matched;
        } finally {
            long elapsed = System.nanoTime() - start;
            long allocated = allocatedBytes() - allocatedBefore;
            stats.comparisons++;
            stats.elapsedNanos += elapsed;
            stats.allocatedBytes += allocated;
            stats.loreLines += lines;
            if (matched) stats.matches++;
            if (!completed) stats.failures++;
            if (fastPathUsed) stats.fastPathHits++;
        }
    }

    public void recordFastPath() {
        fastPathUsed = true;
    }

    public void recordUpdate(UpdateReason reason) {
        updateReasons[reason.ordinal()]++;
    }

    public void recordAnnotation() {
        annotationAttempts++;
    }

    public void sampleHeap() {
        sampledPeakHeap = Math.max(sampledPeakHeap, memory.getHeapMemoryUsage().getUsed());
    }

    public boolean hasExpired() {
        return elapsedNanos() >= TimeUnit.SECONDS.toNanos(requestedSeconds);
    }

    public MutableComponent status() {
        return Component.translatable(
                "command.wynntils.debug.profile.itemLore.status",
                mode.name(),
                String.format(Locale.ROOT, "%.1f", elapsedNanos() / 1_000_000_000.0),
                requestedSeconds,
                comparisons);
    }

    public Report snapshot() {
        double elapsedSeconds = elapsedNanos() / 1_000_000_000.0;
        sampleHeap();
        Map<String, Long> reasons = new LinkedHashMap<>();
        long updates = 0;
        for (UpdateReason reason : UpdateReason.values()) {
            long count = updateReasons[reason.ordinal()];
            reasons.put(reason.name().toLowerCase(Locale.ROOT), count);
            updates += count;
        }
        Stats baseline = new Stats();
        Stats optimized = new Stats();
        List<AnnotationResult> annotations = new ArrayList<>();
        for (Group group : groups.values()) {
            baseline.add(group.baseline);
            optimized.add(group.optimized);
            annotations.add(new AnnotationResult(
                    group.annotation, group.paged, result(group.baseline), result(group.optimized)));
        }
        return new Report(
                1,
                mode,
                startedAt.toString(),
                System.getProperty("java.runtime.version"),
                threadName,
                requestedSeconds,
                elapsedSeconds,
                allocation != null,
                updates,
                reasons,
                annotationAttempts,
                result(baseline),
                result(optimized),
                annotations,
                new RuntimeContext(
                        heapBefore,
                        memory.getHeapMemoryUsage().getUsed(),
                        sampledPeakHeap,
                        collectionCount() - collectionsBefore,
                        collectionTime() - gcTimeBefore));
    }

    public static void publish(Report report) {
        McUtils.sendWynntilsPrefixMessage(Component.translatable(
                        "command.wynntils.debug.profile.itemLore.finished",
                        report.mode().name(),
                        String.format(Locale.ROOT, "%.2f", report.elapsedSeconds()),
                        report.itemUpdates(),
                        report.annotationAttempts())
                .withStyle(ChatFormatting.AQUA));
        McUtils.sendWynntilsPrefixMessage(
                Component.literal(summary("Baseline", report.baseline())).withStyle(ChatFormatting.AQUA));
        McUtils.sendWynntilsPrefixMessage(
                Component.literal(summary("Optimized", report.optimized())).withStyle(ChatFormatting.AQUA));
        McUtils.sendWynntilsPrefixMessage(Component.translatable(
                        "command.wynntils.debug.profile.itemLore.fastReturns",
                        report.optimized().fastPathHits())
                .withStyle(ChatFormatting.AQUA));
        if (report.baseline().comparisons() + report.optimized().comparisons() == 0) {
            McUtils.sendWynntilsPrefixMessage(
                    Component.translatable("command.wynntils.debug.profile.itemLore.noComparisons")
                            .withStyle(ChatFormatting.YELLOW));
        }
        try {
            Path directory = WynntilsMod.getModStorageDir("benchmarks").toPath();
            Files.createDirectories(directory);
            Path output = Files.createTempFile(
                    directory, "item-lore-" + report.mode().name().toLowerCase(Locale.ROOT) + "-", ".json");
            Files.writeString(output, WynntilsMod.GSON.toJson(report));
            WynntilsMod.info("Item lore profile: " + output.toAbsolutePath());
            McUtils.sendWynntilsPrefixMessage(Component.translatable(
                            "command.wynntils.debug.profile.itemLore.saved",
                            output.toAbsolutePath().toString())
                    .withStyle(ChatFormatting.GREEN));
        } catch (IOException exception) {
            WynntilsMod.warn("Could not save item lore profile", exception);
            McUtils.sendWynntilsPrefixMessage(
                    Component.translatable("command.wynntils.debug.profile.itemLore.saveFailed")
                            .withStyle(ChatFormatting.RED));
        }
    }

    private static String summary(String name, Result result) {
        String allocated = result.allocatedBytes() < 0
                ? Component.translatable("command.wynntils.debug.profile.itemLore.allocationUnavailable")
                        .getString()
                : String.format(Locale.ROOT, "%.3f MiB", result.allocatedBytes() / 1048576.0);
        return String.format(
                Locale.ROOT,
                "%9.3f ms, %7d c, %13s allocated  %s",
                result.elapsedMillis(),
                result.comparisons(),
                allocated,
                name);
    }

    private Result result(Stats stats) {
        return new Result(
                stats.comparisons,
                stats.matches,
                stats.failures,
                stats.fastPathHits,
                stats.loreLines,
                stats.elapsedNanos / 1_000_000.0,
                allocation == null ? -1 : stats.allocatedBytes);
    }

    private long elapsedNanos() {
        return System.nanoTime() - startedNanos;
    }

    private long allocatedBytes() {
        return allocation == null ? 0 : allocation.getThreadAllocatedBytes(threadId);
    }

    private static ThreadMXBean allocationCounter() {
        try {
            if (ManagementFactory.getThreadMXBean() instanceof ThreadMXBean bean
                    && bean.isThreadAllocatedMemorySupported()) {
                if (!bean.isThreadAllocatedMemoryEnabled()) bean.setThreadAllocatedMemoryEnabled(true);
                return bean;
            }
        } catch (UnsupportedOperationException | SecurityException ignored) {
            // Allocation counters are not available on every JVM.
        }
        return null;
    }

    private long collectionCount() {
        long count = 0;
        for (GarbageCollectorMXBean collector : collectors) count += Math.max(0, collector.getCollectionCount());
        return count;
    }

    private long collectionTime() {
        long time = 0;
        for (GarbageCollectorMXBean collector : collectors) time += Math.max(0, collector.getCollectionTime());
        return time;
    }

    private static final class Group {
        private final String annotation;
        private final boolean paged;
        private final Stats baseline = new Stats();
        private final Stats optimized = new Stats();
        private long comparisons;
        private boolean flipBlock;

        private Group(String annotation, boolean paged) {
            this.annotation = annotation;
            this.paged = paged;
        }
    }

    private static final class Stats {
        private long comparisons;
        private long matches;
        private long failures;
        private long fastPathHits;
        private long loreLines;
        private long elapsedNanos;
        private long allocatedBytes;

        private void add(Stats other) {
            comparisons += other.comparisons;
            matches += other.matches;
            failures += other.failures;
            fastPathHits += other.fastPathHits;
            loreLines += other.loreLines;
            elapsedNanos += other.elapsedNanos;
            allocatedBytes += other.allocatedBytes;
        }
    }

    public record Result(
            long comparisons,
            long matches,
            long failures,
            long fastPathHits,
            long loreLinesAcrossBothStacks,
            double elapsedMillis,
            long allocatedBytes) {}

    public record AnnotationResult(String annotation, boolean paged, Result baseline, Result optimized) {}

    public record RuntimeContext(
            long heapBeforeBytes,
            long heapAfterBytes,
            long sampledPeakHeapBytes,
            long gcCollections,
            long gcCollectionMillis) {}

    public record Report(
            int schemaVersion,
            Mode mode,
            String startedAtUtc,
            String javaVersion,
            String measuredThread,
            int requestedSeconds,
            double elapsedSeconds,
            boolean allocationCounterAvailable,
            long itemUpdates,
            Map<String, Long> updateReasons,
            long annotationAttempts,
            Result baseline,
            Result optimized,
            List<AnnotationResult> annotations,
            RuntimeContext runtime) {}
}
