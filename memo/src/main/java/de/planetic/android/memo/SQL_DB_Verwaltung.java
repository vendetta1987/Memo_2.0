package de.planetic.android.memo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Implementierung von {@link SQLiteOpenHelper} zur Verwaltung der Datenbank.
 * Stellt die Tabellen "Geokoordinaten" und "Synchronisierung" bereit.
 * <p/>
 * {@code DB_NAME} = GeoDB<br/>
 * {@code TABELLEN_NAME_HAUPT} = Geokoordinaten<br/>
 * {@code TABELLEN_NAME_SYNCH} = Synchronisierung<br/>
 * {@code NAME_SPALTE_n} Namen der einzelnen Spalten:
 * <ol>
 * <li>id</li>
 * <li>name</li>
 * <li>latitude</li>
 * <li>longitude</li>
 * <li>icon</li>
 * <li>zeit</li>
 * <li>beschreibung</li>
 * <li>eigenschaften</li>
 * <li>preis</li>
 * <li>adresse</li>
 * <li>verarbeitet</li>
 * </ol>
 */
public class SQL_DB_Verwaltung extends SQLiteOpenHelper {

	private static final int DB_VERSION = 1;
	public static final String DB_NAME = "GeoDB";
	public static final String TABELLEN_NAME_HAUPT = "Geokoordinaten";
	public static final String TABELLEN_NAME_SYNCH = "Synchronisierung";

	public static final String NAME_SPALTE_1 = "id";
	public static final String NAME_SPALTE_2 = "name";
	public static final String NAME_SPALTE_3 = "latitude";
	public static final String NAME_SPALTE_4 = "longitude";
	public static final String NAME_SPALTE_5 = "icon";
	public static final String NAME_SPALTE_6 = "zeit";
	public static final String NAME_SPALTE_7 = "beschreibung";
	public static final String NAME_SPALTE_8 = "eigenschaften";
	public static final String NAME_SPALTE_9 = "preis";
	public static final String NAME_SPALTE_10 = "adresse";

	public static final String NAME_SPALTE_11 = "verarbeitet";

	public static final String TYP_SPALTE_1 = "INTEGER PRIMARY KEY";
	public static final String TYP_SPALTE_2 = "TEXT";
	public static final String TYP_SPALTE_3 = "INTEGER";
	public static final String TYP_SPALTE_4 = "INTEGER";
	public static final String TYP_SPALTE_5 = "TEXT";
	public static final String TYP_SPALTE_6 = "INTEGER";
	public static final String TYP_SPALTE_7 = "TEXT";
	public static final String TYP_SPALTE_8 = "TEXT";
	public static final String TYP_SPALTE_9 = "REAL";
	public static final String TYP_SPALTE_10 = "TEXT";

	public static final String TYP_SPALTE_11 = "INTEGER";

	private static final String TEMP = " (" + NAME_SPALTE_1 + " "
			+ TYP_SPALTE_1 + ", " + NAME_SPALTE_2 + " " + TYP_SPALTE_2 + ", "
			+ NAME_SPALTE_3 + " " + TYP_SPALTE_3 + ", " + NAME_SPALTE_4 + " "
			+ TYP_SPALTE_4 + ", " + NAME_SPALTE_5 + " " + TYP_SPALTE_5 + ", "
			+ NAME_SPALTE_6 + " " + TYP_SPALTE_6 + ", " + NAME_SPALTE_7 + " "
			+ TYP_SPALTE_7 + ", " + NAME_SPALTE_8 + " " + TYP_SPALTE_8 + ", "
			+ NAME_SPALTE_9 + " " + TYP_SPALTE_9 + ", " + NAME_SPALTE_10 + " "
			+ TYP_SPALTE_10;

	private static final String CREATE_TABLE_HAUPT = "CREATE TABLE "
			+ TABELLEN_NAME_HAUPT + TEMP + ");";

	private static final String CREATE_TABLE_SYNCH = "CREATE TABLE "
			+ TABELLEN_NAME_SYNCH + TEMP + ", " + NAME_SPALTE_11 + " "
			+ TYP_SPALTE_11 + ");";

	// id=int, name=string, latitude=int, longitude=int, icon=string,
	// zeit=integer () sek
	// seit 1.1.1970, beschreibung=string, eigenschaften=string, preis=double

	SQL_DB_Verwaltung(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_HAUPT);
		db.execSQL(CREATE_TABLE_SYNCH);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}
}