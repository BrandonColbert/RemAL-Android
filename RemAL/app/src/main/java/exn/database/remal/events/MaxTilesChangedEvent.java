package exn.database.remal.events;

import exn.database.remal.core.RemalEvent;

public class MaxTilesChangedEvent extends RemalEvent {
	public final int oldCount, count;

	public MaxTilesChangedEvent(int oldCount, int count) {
		super();
		this.oldCount = oldCount;
		this.count = count;
	}
}
