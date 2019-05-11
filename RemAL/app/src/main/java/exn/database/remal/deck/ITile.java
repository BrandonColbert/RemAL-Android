package exn.database.remal.deck;

import android.os.Parcelable;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;

import exn.database.remal.config.IPersistent;

/**
 * An element that can be shown on the {@link exn.database.remal.Deck Deck}
 */
public interface ITile extends IPersistent, IRemoteRequest, Parcelable {
    /**
     * Sets the index to be used in determining the row and column
     * @param index The position of this tile
     */
    void setPosition(int index);

    /**
     * @return The index where this tile should be displayed
     */
    int getPosition();

    /**
     * @return The name of the tile seen by the user
     */
    String getName();

    /**
     * Set the name of the tile as seen by the user
     * @param string Name to set to
     */
    void setName(String string);
}