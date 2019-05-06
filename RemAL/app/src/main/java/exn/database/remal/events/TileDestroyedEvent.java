package exn.database.remal.events;

import exn.database.remal.deck.ITile;

public class TileDestroyedEvent extends TileEvent {
    public TileDestroyedEvent(ITile tile) {
        super(tile);
    }
}
