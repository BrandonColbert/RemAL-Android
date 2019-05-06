package exn.database.remal.events;

import exn.database.remal.core.RemalEvent;

public class ColumnAmountChangedEvent extends RemalEvent {
    public final int columns;

    public ColumnAmountChangedEvent(int columns) {
        this.columns = columns;
    }
}
