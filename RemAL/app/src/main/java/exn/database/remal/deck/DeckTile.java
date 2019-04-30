package exn.database.remal.deck;

import org.json.JSONException;
import org.json.JSONObject;

import exn.database.remal.devices.IRemoteDevice;

public class DeckTile extends RemoteRequest implements ITile {
    private int row, column;
    private String name;

    public DeckTile(IRemoteDevice device, int row, int column) {
        super(device);
        this.row = row;
        this.column = column;
        this.name = "Button";
    }

    public DeckTile() {
        super();
    }

    @Override
    public JSONObject save(JSONObject data) throws JSONException {
        data.put("row", row);
        data.put("column", column);
        data.put("name", name);

        return super.save(data);
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        super.load(data);

        row = data.getInt("row");
        column = data.getInt("column");
        name = data.getString("name");
    }

    public void setPos(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
