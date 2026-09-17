package io.github.protasm.jvmud.instance;

import java.util.List;
import java.util.Optional;

/** Menu and selection of peer mudlibs; owns no world, persona, execution queue, or shared clock. */
public final class MudlibRouter {
    private final List<MudInstance> mudlibs;

    /** Creates a stable menu in engine configuration order. */
    public MudlibRouter(List<MudInstance> mudlibs) {
        this.mudlibs = List.copyOf(mudlibs);
        if (this.mudlibs.isEmpty()) throw new IllegalArgumentException("The mudlib menu cannot be empty.");
        if (this.mudlibs.stream().map(MudInstance::gameId).distinct().count() != this.mudlibs.size()) {
            throw new IllegalArgumentException("Mudlib game ids must be unique.");
        }
    }

    /** Returns the available menu entries, with no default selection. */
    public List<MudInstance> mudlibs() { return mudlibs; }

    /** Resolves an exact game id or a one-based menu number; invalid input stays at the menu. */
    public Optional<MudInstance> select(String selection) {
        Optional<MudInstance> byId = mudlibs.stream().filter(mud -> mud.gameId().equals(selection)).findFirst();
        if (byId.isPresent()) return byId;
        try {
            int index = Integer.parseInt(selection) - 1;
            if (index >= 0 && index < mudlibs.size()) return Optional.of(mudlibs.get(index));
        } catch (NumberFormatException ignored) { /* Not a menu number. */ }
        return Optional.empty();
    }
}
