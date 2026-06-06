package io.github.protasm.jvmud.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class MudlibBoundaryTest {
    @Test
    void emptyBoundaryDeclaresNoCompatibilityObjectsOrLifecycleEvents() {
        MudlibBoundary boundary = MudlibBoundary.empty();

        assertFalse(boundary.declared());
        assertTrue(boundary.boundaryObjectPath().isEmpty());
        assertTrue(boundary.mfunObjectPath().isEmpty());
        assertTrue(boundary.lifecycleEvents().isEmpty());
    }

    @Test
    void boundaryNormalizesMudlibObjectPaths() {
        MudlibBoundary boundary = MudlibBoundary.builder()
                .boundaryObjectPath("/jvmud/boundary.c")
                .mfunObjectPath(" /jvmud/functions.c ")
                .handle(MudlibLifecycleEvent.OBJECT_INITIALIZED)
                .handle(MudlibLifecycleEvent.SCHEDULED_TICK)
                .build();

        assertTrue(boundary.declared());
        assertEquals("jvmud/boundary", boundary.boundaryObjectPath().orElseThrow());
        assertEquals("jvmud/functions", boundary.mfunObjectPath().orElseThrow());
        assertTrue(boundary.handles(MudlibLifecycleEvent.OBJECT_INITIALIZED));
        assertTrue(boundary.handles(MudlibLifecycleEvent.SCHEDULED_TICK));
        assertFalse(boundary.handles(MudlibLifecycleEvent.POLICY_CHECK));
    }

    @Test
    void rejectsBlankMudlibObjectPaths() {
        assertThrows(IllegalArgumentException.class, () -> MudlibBoundary.builder()
                .boundaryObjectPath(" / ")
                .build());
        assertThrows(IllegalArgumentException.class, () -> MudlibBoundary.builder()
                .mfunObjectPath(" ")
                .build());
    }

    @Test
    void lifecycleEventsAreImmutable() {
        MudlibBoundary boundary = MudlibBoundary.builder()
                .handle(MudlibLifecycleEvent.ERROR_REPORTED)
                .build();

        assertThrows(UnsupportedOperationException.class, () ->
                boundary.lifecycleEvents().add(MudlibLifecycleEvent.POLICY_CHECK));
    }

    @Test
    void worldRuntimeStoresRegisteredMudlibBoundary() {
        WorldRuntime runtime = new WorldRuntime(new World("test", "Test World"));
        MudlibBoundary boundary = MudlibBoundary.builder()
                .mfunObjectPath("jvmud/functions")
                .build();

        runtime.registerMudlibBoundary(boundary);

        assertEquals(boundary, runtime.mudlibBoundary());
    }
}
