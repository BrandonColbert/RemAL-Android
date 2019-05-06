package exn.database.remal.deck;

import android.os.Parcelable;

import exn.database.remal.config.IPersistent;

public interface ITile extends IPersistent, IRemoteRequest, Parcelable {
    /**
     * Sets the index to be used in determining the row and column
     * @param index The position of this tile
     */
    void setPosition(int index);

    /**
     * @return The index where this tile should be displayed
     */
    int getIndex();

    String getName();

    void setName(String string);
}