package exn.database.remal.events;

import exn.database.remal.core.RemalEvent;
import exn.database.remal.deck.ITile;

public class TileEvent extends RemalEvent {
    public final ITile tile;

    public TileEvent(ITile tile) {
        this.tile = tile;
    }
}
