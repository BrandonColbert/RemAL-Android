package exn.database.remal.requests;

import org.json.JSONException;
import org.json.JSONObject;

import exn.database.remal.devices.IRemoteDevice;

public class DeckTile extends RemoteRequest {
    private int row, column;

    public DeckTile(IRemoteDevice device, int row, int column) {
        super(device);
        this.row = row;
        this.column = column;
    }

    public DeckTile() {
        super();
    }

    @Override
    public JSONObject save(JSONObject data) throws JSONException {
        data.put("row", row);
        data.put("column", column);

        return data;
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        row = data.getInt("row");
        column = data.getInt("column");
    }
}
