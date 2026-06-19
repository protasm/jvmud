package io.github.protasm.jvmud.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.protasm.jvmud.engine.world.Capability;
import io.github.protasm.jvmud.engine.world.Entity;
import io.github.protasm.jvmud.engine.world.Link;
import io.github.protasm.jvmud.engine.world.Place;
import io.github.protasm.jvmud.engine.world.World;
import io.github.protasm.jvmud.engine.world.WorldRuntime;
import org.junit.jupiter.api.Test;

final class WorldRuntimeTest {
    @Test
    void createsSituatedEntitiesInPlaces() {
        WorldRuntime runtime = new WorldRuntime(new World("test", "Test World"));
        Place green = runtime.createPlace("room/village/vill_green", "Village Green");

        Entity player = runtime.createEntity(
                "local/player", "local player", green, Capability.ACTOR, Capability.PERCEPTIVE);

        assertEquals(green, runtime.locationOf(player));
        assertEquals(player, runtime.contentsOf(green).getFirst());
        assertTrue(player.hasCapability(Capability.ACTOR));
        assertTrue(player.hasCapability(Capability.PERCEPTIVE));
    }

    @Test
    void movingEntityMaintainsExactlyOneImmediateLocation() {
        WorldRuntime runtime = new WorldRuntime(new World("test", "Test World"));
        Place green = runtime.createPlace("room/village/vill_green", "Village Green");
        Place church = runtime.createPlace("room/village/church", "Church");
        Entity player = runtime.createEntity("local/player", "local player", green, Capability.ACTOR);

        runtime.move(player, church);

        assertEquals(church, runtime.locationOf(player));
        assertTrue(runtime.contentsOf(green).isEmpty());
        assertEquals(player, runtime.contentsOf(church).getFirst());
    }

    @Test
    void removingEntityReleasesItsWorldIdentifierAndContainedEntities() {
        WorldRuntime runtime = new WorldRuntime(new World("test", "Test World"));
        Place green = runtime.createPlace("room/village/vill_green", "Village Green");
        Entity player = runtime.createEntity("obj/player#clone", "player", green, Capability.ACTOR);
        Entity coin = runtime.createEntity("obj/coin", "coin", player);

        assertTrue(runtime.removeEntity("obj/player#clone"));

        assertTrue(runtime.entity("obj/player#clone") == null);
        assertTrue(runtime.entity("obj/coin") == null);
        assertTrue(runtime.contentsOf(green).isEmpty());
        runtime.createEntity("obj/player#clone", "next player", green, Capability.ACTOR);
        assertTrue(!runtime.removeEntity("obj/missing"));
        assertTrue(runtime.locationOf(coin) == null);
    }

    @Test
    void entitiesCanContainOtherEntitiesWithoutLosingWorldLocationResolution() {
        WorldRuntime runtime = new WorldRuntime(new World("test", "Test World"));
        Place green = runtime.createPlace("room/village/vill_green", "Village Green");
        Entity player = runtime.createEntity("local/player", "local player", green, Capability.ACTOR);
        Entity bag = runtime.createEntity("obj/bag", "bag", player);
        Entity coin = runtime.createEntity("obj/coin", "coin", bag);

        assertEquals(player, runtime.locationOf(bag));
        assertEquals(bag, runtime.locationOf(coin));
        assertEquals(bag, runtime.contentsOf(player).getFirst());
        assertEquals(coin, runtime.contentsOf(bag).getFirst());
    }

    @Test
    void entityContainersAreTranslucentByDefaultButCanBeOpaque() {
        WorldRuntime runtime = new WorldRuntime(new World("test", "Test World"));
        Place green = runtime.createPlace("room/village/vill_green", "Village Green");
        Entity bag = runtime.createEntity("obj/bag", "bag", green);

        assertTrue(runtime.translucent(bag));

        runtime.setTranslucent(bag, false);

        assertTrue(!runtime.translucent(bag));
    }

    @Test
    void rejectsContainmentCycles() {
        WorldRuntime runtime = new WorldRuntime(new World("test", "Test World"));
        Place green = runtime.createPlace("room/village/vill_green", "Village Green");
        Entity player = runtime.createEntity("local/player", "local player", green, Capability.ACTOR);
        Entity bag = runtime.createEntity("obj/bag", "bag", player);
        Entity coin = runtime.createEntity("obj/coin", "coin", bag);

        assertThrows(IllegalArgumentException.class, () -> runtime.move(player, player));
        assertThrows(IllegalArgumentException.class, () -> runtime.move(player, bag));
        assertThrows(IllegalArgumentException.class, () -> runtime.move(player, coin));
        assertEquals(green, runtime.locationOf(player));
    }

    @Test
    void rejectsUnknownLocations() {
        WorldRuntime runtime = new WorldRuntime(new World("test", "Test World"));
        Place green = runtime.createPlace("room/village/vill_green", "Village Green");
        Entity player = runtime.createEntity("local/player", "local player", green, Capability.ACTOR);
        Place unregistered = new Place("room/void", "Void");

        assertThrows(IllegalArgumentException.class, () -> runtime.move(player, unregistered));
    }

    @Test
    void linksConnectPlacesIntoTraversableWorld() {
        WorldRuntime runtime = new WorldRuntime(new World("test", "Test World"));
        Place green = runtime.createPlace("room/village/vill_green", "Village Green");
        Place church = runtime.createPlace("room/village/church", "Church");
        Entity player = runtime.createEntity("local/player", "local player", green, Capability.ACTOR);

        Link north = runtime.connect(green, "north", church);

        assertEquals(north, runtime.linkFrom(green, "north"));
        assertEquals(church, north.destination());
        assertTrue(north.visible());
        assertTrue(runtime.traverse(player, "north"));
        assertEquals(church, runtime.locationOf(player));
    }

    @Test
    void bidirectionalLinksCanUseDifferentActions() {
        WorldRuntime runtime = new WorldRuntime(new World("test", "Test World"));
        Place cellar = runtime.createPlace("room/cellar", "Cellar");
        Place attic = runtime.createPlace("room/village/attic", "Attic");

        runtime.connectBothWays(cellar, "climb ladder", attic, "descend ladder");

        assertEquals(attic, runtime.linkFrom(cellar, "climb ladder").destination());
        assertEquals(cellar, runtime.linkFrom(attic, "descend ladder").destination());
    }

    @Test
    void missingLinkDoesNotMoveEntity() {
        WorldRuntime runtime = new WorldRuntime(new World("test", "Test World"));
        Place green = runtime.createPlace("room/village/vill_green", "Village Green");
        Entity player = runtime.createEntity("local/player", "local player", green, Capability.ACTOR);

        assertTrue(!runtime.traverse(player, "north"));
        assertEquals(green, runtime.locationOf(player));
    }

    @Test
    void placeResolutionFollowsEntityContainmentBeforeTraversal() {
        WorldRuntime runtime = new WorldRuntime(new World("test", "Test World"));
        Place road = runtime.createPlace("room/road", "Road");
        Place station = runtime.createPlace("room/village/station", "Station");
        Entity carriage = runtime.createEntity("obj/carriage", "carriage", road);
        Entity passenger = runtime.createEntity("local/player", "local player", carriage, Capability.ACTOR);

        runtime.connect(road, "north", station);

        assertEquals(road, runtime.placeOf(passenger));
        assertTrue(runtime.traverse(passenger, "north"));
        assertEquals(station, runtime.locationOf(passenger));
    }

    @Test
    void entityContainmentDoesNotCreateEntityLinks() {
        WorldRuntime runtime = new WorldRuntime(new World("test", "Test World"));
        Place road = runtime.createPlace("room/road", "Road");
        Place station = runtime.createPlace("room/village/station", "Station");
        Entity carriage = runtime.createEntity("obj/carriage", "carriage", road);
        Entity passenger = runtime.createEntity("local/player", "local player", carriage, Capability.ACTOR);

        runtime.connect(road, "north", station);

        assertEquals(0, runtime.linksFrom(station).size());
        assertEquals(road, runtime.placeOf(carriage));
        assertEquals(road, runtime.placeOf(passenger));
        assertTrue(runtime.traverse(passenger, "north"));
        assertEquals(station, runtime.locationOf(passenger));
        assertEquals(road, runtime.locationOf(carriage));
    }

    @Test
    void linksCanBeHiddenWhileStillRepresentingWorldTopology() {
        WorldRuntime runtime = new WorldRuntime(new World("test", "Test World"));
        Place shrine = runtime.createPlace("room/shrine", "Shrine");
        Place shadowRealm = runtime.createPlace("room/shadow", "Shadow Realm");

        Link portal = runtime.connect(shrine, "enter portal", shadowRealm, false);

        assertTrue(!portal.visible());
        assertEquals(portal, runtime.linksFrom(shrine).getFirst());
    }
}
