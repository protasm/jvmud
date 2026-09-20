package io.github.protasm.jvmud.instance;

import io.github.protasm.jvmud.compiler.exec.LPCObjectLoadObserver;
import java.nio.file.Path;
import java.util.*;

/** Worker-side compilation and load diagnostics for a single mudlib startup. */
public final class StartupObjectLoadTrace implements LPCObjectLoadObserver {
    private final boolean enabled;
    private boolean active;
    private final Set<String> loadedObjectIds = new HashSet<>();
    private final Set<String> failedObjectIds = new HashSet<>();
    private final Set<String> compiledObjectIds = new HashSet<>();
    private final Set<String> failedCompileObjectIds = new HashSet<>();
    private int loadedAttempts;
    private int failedAttempts;
    private int compiledAttempts;
    private int failedCompileAttempts;

    /** Enables load tracing for one mudlib boot. */
    public StartupObjectLoadTrace(boolean enabled) {
        this.enabled = enabled;
        this.active = enabled;
    }

    @Override
    public void objectLoadStarted(String objectId, Path sourcePath, int depth) {
        if (active) {
            System.out.println(loadIndent(depth) + "startup object /" + objectId + ": starting.");
        }
    }

    @Override
    public void objectLoadFinished(String objectId, Path sourcePath, int depth, boolean loaded, long elapsedNanos) {
        if (!active) {
            return;
        }
        if (loaded) {
            loadedAttempts++;
            loadedObjectIds.add(objectId);
        } else {
            failedAttempts++;
            failedObjectIds.add(objectId);
        }
        String outcome = loaded ? "loaded" : "failed";
        double elapsedMillis = elapsedNanos / 1_000_000.0;
        System.out.printf(
                Locale.ROOT,
                "%sstartup object /%s: %s in %.1f ms.%n",
                loadIndent(depth),
                objectId,
                outcome,
                elapsedMillis);
    }

    @Override
    public void objectCompileFinished(String objectId, Path sourcePath, boolean compiled, long elapsedNanos) {
        if (!active) {
            return;
        }
        if (compiled) {
            compiledAttempts++;
            compiledObjectIds.add(objectId);
        } else {
            failedCompileAttempts++;
            failedCompileObjectIds.add(objectId);
        }
        String outcome = compiled ? "compiled" : "failed";
        double elapsedMillis = elapsedNanos / 1_000_000.0;
        System.out.printf(
                Locale.ROOT,
                "startup compile /%s: %s in %.1f ms.%n",
                objectId,
                outcome,
                elapsedMillis);
    }

    @Override
    public void objectCompileStarted(String objectId, Path sourcePath) {
        if (active) {
            System.out.println("startup compile /" + objectId + ": starting.");
        }
    }

    @Override
    public void objectLoadFailed(String objectId, Path sourcePath, int depth, Throwable failure) {
        if (active) {
            System.out.println(loadIndent(depth)
                    + "startup object /"
                    + objectId
                    + ": "
                    + failureSummary(failure));
        }
    }

    @Override
    public void objectCompileFailed(String objectId, Path sourcePath, Throwable failure) {
        if (active) {
            System.out.println("startup compile /" + objectId + ": " + failureSummary(failure));
        }
    }

    /** Ends startup-only tracing before player sessions begin. */
    public void finishStartup() {
        active = false;
    }

    /** Summarizes distinct objects and attempts observed during startup. */
    public String summary() {
        return "startup object load summary: loaded "
                + loadedObjectIds.size()
                + " unique object(s) across "
                + loadedAttempts
                + " load attempt(s), failed "
                + failedObjectIds.size()
                + " unique object(s) across "
                + failedAttempts
                + " load attempt(s).\n"
                + "startup compile summary: compiled "
                + compiledObjectIds.size()
                + " unique object(s) across "
                + compiledAttempts
                + " compile attempt(s), failed "
                + failedCompileObjectIds.size()
                + " unique object(s) across "
                + failedCompileAttempts
                + " compile attempt(s).";
    }

    /** Writes the summary only when detailed tracing was requested. */
    public void printSummaryIfEnabled() {
        if (enabled) {
            System.out.println(summary());
        }
    }

    private static String failureSummary(Throwable failure) {
        String message = failure.getMessage();
        if (message == null || message.isBlank()) {
            message = failure.getClass().getName();
        } else {
            message = failure.getClass().getSimpleName() + ": " + message;
        }
        return "failed because " + message;
    }
    private static String loadIndent(int depth) { return "  ".repeat(Math.max(0, depth)); }
}

