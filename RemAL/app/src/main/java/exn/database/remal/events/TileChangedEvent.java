package exn.database.remal.events;

import exn.database.remal.deck.ITile;

public class TileChangedEvent extends TileEvent {
    public TileChangedEvent(ITile tile) {
        super(tile);
    }
}
