package de.planetic.android.memo_neu;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQL_DB_Verwaltung extends SQLiteOpenHelper {

	private static final int DB_VERSION = 1;
	public static final String DB_NAME = "GeoDB";
	public static final String TABELLEN_NAME = "Geokoordinaten";

	public static final String NAME_SPALTE_1 = "id";
	public static final String NAME_SPALTE_2 = "name";
	public static final String NAME_SPALTE_3 = "lat";
	public static final String NAME_SPALTE_4 = "lon";
	public static final String NAME_SPALTE_5 = "icon";
	public static final String NAME_SPALTE_6 = "zeit";

	public static final String TYP_SPALTE_1 = "INTEGER PRIMARY KEY";
	public static final String TYP_SPALTE_2 = "TEXT";
	public static final String TYP_SPALTE_3 = "INTEGER";
	public static final String TYP_SPALTE_4 = "INTEGER";
	public static final String TYP_SPALTE_5 = "INTEGER";
	public static final String TYP_SPALTE_6 = "INTEGER";

	private static final String CREATE_TABLE = "CREATE TABLE " + TABELLEN_NAME
			+ " (" + NAME_SPALTE_1 + " " + TYP_SPALTE_1 + ", " + NAME_SPALTE_2
			+ " " + TYP_SPALTE_2 + ", " + NAME_SPALTE_3 + " " + TYP_SPALTE_3
			+ ", " + NAME_SPALTE_4 + " " + TYP_SPALTE_4 + ", " + NAME_SPALTE_5
			+ " " + TYP_SPALTE_5 + ", " + NAME_SPALTE_6 + " " + TYP_SPALTE_6
			+ ");";

	// id=int, name=string, lat=int, lon=int, icon=string, zeit=integer () sek
	// seit 1.1.1970

	SQL_DB_Verwaltung(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}
}