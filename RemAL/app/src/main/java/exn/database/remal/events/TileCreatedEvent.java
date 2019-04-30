package exn.database.remal.events;

import exn.database.remal.deck.ITile;

public class TileCreatedEvent extends TileEvent {
    public TileCreatedEvent(ITile tile) {
        super(tile);
    }
}
