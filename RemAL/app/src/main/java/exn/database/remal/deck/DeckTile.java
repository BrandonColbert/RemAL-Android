package exn.database.remal.deck;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import exn.database.remal.core.RemAL;
import exn.database.remal.devices.IRemoteDevice;

public class DeckTile extends RemoteRequest implements ITile {
    private int index;
    private String name;

    public DeckTile(IRemoteDevice device, int index) {
        super(device);
        this.index = index;
        this.name = "Unset";
    }

    public DeckTile() {
        super();
    }

    @Override
    public JSONObject save(JSONObject data) throws JSONException {
        data.put("index", index);
        data.put("name", name);

        return super.save(data);
    }

    @Override
    public void load(JSONObject data) throws JSONException {
        super.load(data);

        index = data.getInt("index");
        name = data.getString("name");
    }

    public void setPosition(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static final Parcelable.Creator<DeckTile> CREATOR = new Parcelable.Creator<DeckTile>() {
        public DeckTile createFromParcel(Parcel source) {
            DeckTile tile = new DeckTile();
            tile.readFromParcel(source);

            return tile;
        }

        public DeckTile[] newArray(int size) {
            return new DeckTile[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getTargetDevice() == null ? "" : getTargetDevice().getName());
        parcel.writeString(getRequest());
        parcel.writeInt(index);
        parcel.writeString(name);
    }

    public void readFromParcel(Parcel parcel) {
        String deviceName = parcel.readString();

        if(!deviceName.isEmpty())
            setTargetDevice(RemAL.getDevice(deviceName));

        setRequest(parcel.readString());
        index = parcel.readInt();
        name = parcel.readString();
    }
}