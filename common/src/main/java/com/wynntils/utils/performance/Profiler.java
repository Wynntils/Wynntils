package com.wynntils.utils.performance;

import com.wynntils.mc.event.TickEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public final class Profiler {

    private Profiler() {}

    /*
     * Per-thread call stack.
     * Prevents threads from corrupting each other's profiling state.
     */
    private static final ThreadLocal<Deque<Frame>> FRAMES =
            ThreadLocal.withInitial(ArrayDeque::new);

    /*
     * Aggregated statistics.
     */
    private static final Map<String, Stats> STATS =
            new ConcurrentHashMap<>();

    /*
     * Single frame on the stack.
     */
    private record Frame(
            String name,
            long start,
            long childTime
    ) {}

    /*
     * Aggregated method stats.
     */
    private static final class Stats {
        private final LongAdder calls = new LongAdder();
        private final LongAdder totalTime = new LongAdder();
        private final LongAdder selfTime = new LongAdder();

        void add(long total, long self) {
            calls.increment();
            totalTime.add(total);
            selfTime.add(self);
        }

        long calls() {
            return calls.sum();
        }

        long totalTime() {
            return totalTime.sum();
        }

        long selfTime() {
            return selfTime.sum();
        }
    }

    /**
     * Begin profiling a section.
     */
    public static void push(String name) {
        FRAMES.get().push(new Frame(
                name,
                System.nanoTime(),
                0
        ));
    }

    /**
     * End profiling a section.
     */
    public static void pop() {
        Deque<Frame> stack = FRAMES.get();

        if (stack.isEmpty()) {
            throw new IllegalStateException("Profiler.pop() called without matching push()");
        }

        long end = System.nanoTime();

        Frame frame = stack.pop();

        long totalTime = end - frame.start();
        long selfTime = totalTime - frame.childTime();

        STATS.computeIfAbsent(frame.name(), k -> new Stats())
                .add(totalTime, selfTime);

        /*
         * Add our total time to parent child-time.
         * This allows exclusive/self timing.
         */
        if (!stack.isEmpty()) {
            Frame parent = stack.pop();

            stack.push(new Frame(
                    parent.name(),
                    parent.start(),
                    parent.childTime() + totalTime
            ));
        }
    }

    /**
     * RAII / try-with-resources support.
     */
    public static Scope scope(String name) {
        push(name);
        return Scope.INSTANCE;
    }

    public static final class Scope implements AutoCloseable {

        private static final Scope INSTANCE = new Scope();

        private Scope() {}

        @Override
        public void close() {
            pop();
        }
    }

    /**
     * Clears all collected profiling data.
     */
    public static void reset() {
        STATS.clear();
    }

    public static void printToFile() {
        String fileName = "E:/templates/profile/profiler-" + System.currentTimeMillis() + ".txt";

        try (PrintStream out = new PrintStream(fileName)) {
            out.println("Profiler dump @ " + new Date());
            print(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Prints profiler results sorted by hottest methods first.
     */
    public static void print(PrintStream printStream) {

        List<Map.Entry<String, Stats>> sorted =
                new ArrayList<>(STATS.entrySet());

        sorted.sort((a, b) ->
                Long.compare(
                        b.getValue().selfTime(),
                        a.getValue().selfTime()
                )
        );

        long totalInclusive = 0;
        long totalSelf = 0;

        for (Stats stats : STATS.values()) {
            totalInclusive += stats.totalTime();
            totalSelf += stats.selfTime();
        }

        printStream.println("\n========== PROFILER ==========");

        printStream.printf(
                Locale.US,
                "Total Time: %.3fms | Self Time: %.3fms%n%n",
                nsToMs(totalInclusive),
                nsToMs(totalSelf)
        );

        for (Map.Entry<String, Stats> entry : sorted) {

            String name = entry.getKey();
            Stats stats = entry.getValue();

            double totalMs = nsToMs(stats.totalTime());
            double selfMs = nsToMs(stats.selfTime());
            double avgMs = totalMs / stats.calls();

            printStream.printf(
                    Locale.US,
                    "%-80s | calls=%6d | total=%9.3fms | self=%9.3fms | avg=%8.5fms%n",
                    name,
                    stats.calls(),
                    totalMs,
                    selfMs,
                    avgMs
            );
        }

        printStream.println("================================\n");
    }

    /**
     * Utility wrapper for profiling lambdas/functions.
     */
    public static void run(String name, Runnable runnable) {
        try (Scope ignored = scope(name)) {
            runnable.run();
        }
    }

    public static <T> T call(String name, java.util.function.Supplier<T> supplier) {
        try (Scope ignored = scope(name)) {
            return supplier.get();
        }
    }


    private static double nsToMs(long ns) {
        return ns / 1_000_000.0;
    }
}