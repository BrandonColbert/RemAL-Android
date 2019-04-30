package exn.database.remal.deck;

import exn.database.remal.config.IPersistent;

public interface ITile extends IPersistent {
    /**
     * Set the row and column for this tile
     * @param row Get the row for this tile
     * @param column Get the column for this tile
     */
    void setPos(int row, int column);

    /**
     * @return The row where this tile should be displayed
     */
    int getRow();

    /**
     * @return The column where this tile should be displayed
     */
    int getColumn();

    String getName();

    void setName(String string);
}
