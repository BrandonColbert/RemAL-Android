package exn.database.remal.deck;

import android.util.SparseIntArray;

import java.util.HashSet;

import exn.database.remal.config.PersistentValues;
import exn.database.remal.core.RemAL;
import exn.database.remal.events.MaxTilesChangedEvent;

/**
 * Tracks the position of tiles and updates the maximum amount based their positions and the amount of columns
 */
public class TileLevelTracker {
	private static HashSet<Integer> tileIndices = new HashSet<>();
	private static SparseIntArray tilesInLevel = new SparseIntArray();
	private static int levels, lastLevels, maxTiles, levelCount, columns;

	/**
	 * Used to determine if a tile exists at that position
	 * @param index Index to check
	 * @return True if a tile exists there and false otherwise
	 */
	public static boolean tileAt(int index) {
		return tileIndices.contains(index);
	}

	/**
	 * @return The number of tiles
	 */
	public static int getTileCount() {
		return tileIndices.size();
	}

	/**
	 * Notifies whether a tile currently exists at this position
	 * @param index Position of the tile
	 * @param exists Whether the tile exists
	 */
	public static void notify(int index, boolean exists) {
		//On
		if(tileIndices.contains(index) != exists) {
			if(exists)
				tileIndices.add(index);
			else
				tileIndices.remove(index);

			final int level = getLevel(index);
			tilesInLevel.put(level, tilesInLevel.get(level) + (exists ? 1 : -1));

			updateMaxTileCount();
		}
	}

	/**
	 * Notifies that a tile was moved
	 * @param then Original position or -1 for just added
	 * @param now Current position or -1 for removed
	 */
	public static void notify(int then, int now) {
		int oldLevel = getLevel(then), newLevel = getLevel(now);

		tileIndices.remove(then);
		tileIndices.add(now);

		if(oldLevel != newLevel) {
			tilesInLevel.put(oldLevel, tilesInLevel.get(oldLevel) - 1);
			tilesInLevel.put(newLevel, tilesInLevel.get(newLevel) + 1);

			updateMaxTileCount();
		}
	}

	/**
	 * Reconstructs the tracker using existing memory of tile positions
	 */
	public static void rebuild() {
		maxTiles = PersistentValues.getMaxTiles();
		columns = PersistentValues.getColumns();

		tilesInLevel.clear();
		for(int i : tileIndices)
			tilesInLevel.put(getLevel(i), tilesInLevel.get(i, 0) + 1);

		refresh();
		updateMaxTileCount();
	}

	/**
	 * @param index
	 * @return The level which the index is in
	 */
	private static int getLevel(int index) {
		int level = 1;

		while(Math.floor(level * levelCount * 0.75) < index)
			level++;

		if(level > levels)
			levels = level;

		return level;
	}

	/**
	 * Updates the maximum amount of columns
	 */
	private static void updateMaxTileCount() {
		final int oldMax = maxTiles;

		while(levels > 1 && tilesInLevel.get(levels, 0) <= 0)
			levels--;

		if(lastLevels != levels) {
			maxTiles = levels * levelCount;
			refresh();
			PersistentValues.setMaxTiles(maxTiles);
			RemAL.post(new MaxTilesChangedEvent(oldMax, maxTiles));
			lastLevels = levels;
		}
	}

	/**
	 * Refreshes the values of the levelCount and number of levels based on the amount of columns and max tiles
	 */
	private static void refresh() {
		levelCount = (int)Math.ceil((double)PersistentValues.DEFAULT_MAX_TILES / (double)columns) * columns;
		lastLevels = -1;
		levels = Math.max(1, (int)Math.ceil((double)maxTiles / (double)levelCount));
	}
}
