package exn.database.remal.config;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;

import exn.database.remal.R;
import exn.database.remal.core.RemAL;
import exn.database.remal.deck.TileLevelTracker;

import static exn.database.remal.config.PersistenceUtils.preferences;
import static exn.database.remal.config.PersistenceUtils.removeValue;

/**
 * Stores single values that stay relatively consistent
 */
public class PersistentValues {
	private static final String COLOR_DECK_TEXT = "color_deck_text",
								COLOR_DECK_TILE = "color_deck_tile",
								COLOR_DECK_BACKGROUND = "color_deck_background",
								COLUMNS = "appearance_columns",
								MAX_TILES = "max_tiles";

	public static final int DEFAULT_COLUMNS = 3,
							DEFAULT_MAX_TILES = 100;

	/**
	 * @return The amount of columns
	 */
	public static int getColumns() {
		return preferences.getInt(COLUMNS, DEFAULT_COLUMNS);
	}

	/**
	 * Set the amount of columns
	 * @param value Number of columns
	 */
	public static void setColumns(int value) {
		preferences.edit().putInt(COLUMNS, value).apply();
		TileLevelTracker.rebuild();
	}

	/**
	 * @return The maximum number of tiles that should be displayed
	 */
	public static int getMaxTiles() {
		return preferences.getInt(MAX_TILES, DEFAULT_MAX_TILES);
	}

	/**
	 * Set the amount of max tiles
	 * @param value Number of max tiles
	 */
	public static void setMaxTiles(int value) {
		preferences.edit().putInt(MAX_TILES, value).apply();
	}

	/**
	 * @return The color as a hexadecimal String
	 */
	public static String getDeckTextColor(@Nullable Context context) {
		return context != null ? preferences.getString(COLOR_DECK_TEXT, RemAL.convertColorToCode(context.getResources().getColor(R.color.colorAltText))) : RemAL.convertColorToCode(Color.WHITE);
	}

	/**
	 * @return The color as a hexadecimal String
	 */
	public static String getDeckTileColor(@Nullable Context context) {
		return context != null ? preferences.getString(COLOR_DECK_TILE, RemAL.convertColorToCode(context.getResources().getColor(R.color.colorAccent))) : RemAL.convertColorToCode(Color.BLUE);
	}

	/**
	 * @return The color as a hexadecimal String
	 */
	public static String getDeckBackgroundColor(@Nullable Context context) {
		return context != null ? preferences.getString(COLOR_DECK_BACKGROUND, RemAL.convertColorToCode(context.getResources().getColor(R.color.colorPrimaryDark))) : RemAL.convertColorToCode(Color.BLACK);
	}

	public static void setDeckTextColor(String value) {
		preferences.edit().putString(COLOR_DECK_TEXT, value).apply();
	}

	public static void setDeckTileColor(String value) {
		preferences.edit().putString(COLOR_DECK_TILE, value).apply();
	}

	public static void setDeckBackgroundColor(String value) {
		preferences.edit().putString(COLOR_DECK_BACKGROUND, value).apply();
	}

	public static void resetDeckTextColor(Context context) {
		removeValue(COLOR_DECK_TEXT);
		getDeckTextColor(context);
	}

	public static void resetDeckTileColor(Context context) {
		removeValue(COLOR_DECK_TILE);
		getDeckTileColor(context);
	}

	public static void resetDeckBackgroundColor(Context context) {
		removeValue(COLOR_DECK_BACKGROUND);
		getDeckBackgroundColor(context);
	}
}
