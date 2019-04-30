package exn.database.remal.deck;

import org.json.JSONException;
import org.json.JSONObject;

import exn.database.remal.devices.IRemoteDevice;

public class ResizableDeckTile extends DeckTile {
    private int widthScale, heightScale;

    public ResizableDeckTile(IRemoteDevice device, int row, int column, int widthScale, int heightScale) {
        super(device, row, column);
        this.widthScale = widthScale;
        this.heightScale = heightScale;
    }

    public ResizableDeckTile() {
        super();
    }

    @Override
    public JSONObject save(JSONObject data) throws JSONException {
        data.put("scaleX", widthScale);
        data.put("scaleY", heightScale);

        return super.save(data);
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        super.load(data);

        widthScale = data.getInt("scaleX");
        heightScale = data.getInt("scaleY");
    }
}
