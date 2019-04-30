package exn.database.remal.events;

import exn.database.remal.core.RemalEvent;
import exn.database.remal.requests.IRemoteRequest;

public class RequestEvent extends RemalEvent {
    public final IRemoteRequest request;

    public RequestEvent(IRemoteRequest request) {
        this.request = request;
    }
}
