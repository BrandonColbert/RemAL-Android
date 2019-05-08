package exn.database.remal.events;

import exn.database.remal.core.RemalEvent;

public class DeckColorChangedEvent extends RemalEvent {
	public final boolean didReset;

	public DeckColorChangedEvent(boolean didReset) {
		super();
		this.didReset = didReset;
	}

	public DeckColorChangedEvent() {
		this(false);
	}
}
