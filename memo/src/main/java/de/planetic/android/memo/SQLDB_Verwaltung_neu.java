package de.planetic.android.memo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLDB_Verwaltung_neu extends SQLiteOpenHelper {

	private static final String DB_NAME = "MemoDB";
	private static final int DB_VERSION = 1;

	private static final String PRIMARY_KEY = "PRIMARY KEY AUTOINCREMENT";

	public static final String TABELLE_LADESTATION = "ladestation";

	public static final String SPALTE_ID = "id";
	public static final String SPALTE_ADRESS_ID = "adress_id";
	public static final String SPALTE_STANDORT_LAENGE = "standort_laenge";
	public static final String SPALTE_STANDORT_BREITE = "standort_breite";
	public static final String SPALTE_KOMMENTAR = "kommentar";
	public static final String SPALTE_BEZEICHNUNG = "bezeichnung";
	public static final String SPALTE_LADESTATION_FOTO = "ladestation_foto";
	public static final String SPALTE_VERFUEGBARKEIT_ANFANG = "verfuegbarkeit_anfang";
	public static final String SPALTE_VERFUEGBARKEIT_ENDE = "verfuegbarkeit_ende";
	public static final String SPALTE_VERFUEGBARKEIT_KOMMENTAR = "verfuegbarkeit_kommentar";
	public static final String SPALTE_ZUGANGSTYP = "zugangstyp";
	public static final String SPALTE_BETREIBER_ID = "betreiber_id";
	public static final String SPALTE_PREIS = "preis";

	public static final String TYP_ID = "INTEGER";
	public static final String TYP_ADRESS_ID = "INTEGER";
	public static final String TYP_STANDORT_LAENGE = "REAL";
	public static final String TYP_STANDORT_BREITE = "REAL";
	public static final String TYP_KOMMENTAR = "TEXT";
	public static final String TYP_BEZEICHNUNG = "TEXT";
	public static final String TYP_LADESTATION_FOTO = "BLOB";
	public static final String TYP_VERFUEGBARKEIT_ANFANG = "INTEGER";
	public static final String TYP_VERFUEGBARKEIT_ENDE = "INTEGER";
	public static final String TYP_VERFUEGBARKEIT_KOMMENTAR = "TEXT";
	public static final String TYP_ZUGANGSTYP = "INTEGER";
	public static final String TYP_BETREIBER_ID = "INTEGER";
	public static final String TYP_PREIS = "REAL";

	public static final String TABELLE_ADRESSE = "adresse";

	// public static final String SPALTE_ID = "id";
	public static final String SPALTE_LAND = "land";
	public static final String SPALTE_PLZ = "plz";
	public static final String SPALTE_ORT = "ort";
	public static final String SPALTE_STR_NR = "str_nr";

	// public static final String TYP_ID = "INTEGER";
	public static final String TYP_LAND = "TEXT";
	public static final String TYP_PLZ = "TEXT";
	public static final String TYP_ORT = "TEXT";
	public static final String TYP_STR_NR = "TEXT";

	public static final String TABELLE_STECKER_ANZAHL = "stecker_anzahl";

	// public static final String SPALTE_ID = "id";
	public static final String SPALTE_STECKER_ID = "stecker_id";
	public static final String SPALTE_ANZAHL = "anzahl";
	public static final String SPALTE_LADESTATION_ID = "ladestation_id";

	// public static final String TYP_ID = "INTEGER";
	public static final String TYP_STECKER_ID = "INTEGER";
	public static final String TYP_ANZAHL = "INTEGER";
	public static final String TYP_LADESTATION_ID = "INTEGER";

	public static final String TABELLE_STECKER = "stecker";

	// public static final String SPALTE_ID = "id";
	public static final String SPALTE_NAME = "name";
	// public static final String SPALTE_BEZEICHNUNG = "bezeichnung";
	public static final String SPALTE_STECKER_FOTO = "stecker_foto";

	// public static final String TYP_ID = "INTEGER";
	public static final String TYP_NAME = "TEXT";
	// public static final String TYP_BEZEICHNUNG = "TEXT";
	public static final String TYP_STECKER_FOTO = "BLOB";

	public static final String TABELLE_BETREIBER = "betreiber";

	// public static final String SPALTE_ID = "id";
	// public static final String SPALTE_NAME = "name";
	public static final String SPALTE_LOGO = "logo";
	public static final String SPALTE_ABRECHNUNG_ID = "abrechnung_id";
	public static final String SPALTE_WEBSITE = "website";

	// public static final String TYP_ID = "INTEGER";
	// public static final String TYP_NAME = "TEXT";
	public static final String TYP_LOGO = "BLOB";
	public static final String TYP_ABRECHNUNG_ID = "INTEGER";
	public static final String TYP_WEBSITE = "TEXT";

	public static final String TABELLE_ABRECHNUNG = "abrechnung";

	// public static final String SPALTE_ID = "id";
	// public static final String SPALTE_BEZEICHNUNG = "bezeichnung";
	// public static final String SPALTE_PREIS = "preis";

	// public static final String TYP_ID = "INTEGER";
	// public static final String TYP_BEZEICHNUNG = "TEXT";
	// public static final String TYP_PREIS = "REAL";

	// ---------------------------------------------------------------------------------

	private static final String CREATE_TABLE_LADESTATION = "CREATE TABLE "
			+ TABELLE_LADESTATION + " (" + SPALTE_ID + " " + TYP_ID + " "
			+ PRIMARY_KEY + "," + SPALTE_ADRESS_ID + " " + TYP_ADRESS_ID
			+ " REFERENCES " + TABELLE_ADRESSE + " (" + SPALTE_ID + ")" + ","
			+ SPALTE_STANDORT_LAENGE + " " + TYP_STANDORT_LAENGE + ","
			+ SPALTE_STANDORT_BREITE + " " + TYP_STANDORT_BREITE + ","
			+ SPALTE_KOMMENTAR + " " + TYP_KOMMENTAR + "," + SPALTE_BEZEICHNUNG
			+ " " + TYP_BEZEICHNUNG + "," + SPALTE_LADESTATION_FOTO + " "
			+ TYP_LADESTATION_FOTO + "," + SPALTE_VERFUEGBARKEIT_ANFANG + " "
			+ TYP_VERFUEGBARKEIT_ANFANG + "," + SPALTE_VERFUEGBARKEIT_ENDE
			+ " " + TYP_VERFUEGBARKEIT_ENDE + ","
			+ SPALTE_VERFUEGBARKEIT_KOMMENTAR + " "
			+ TYP_VERFUEGBARKEIT_KOMMENTAR + "," + SPALTE_ZUGANGSTYP + " "
			+ TYP_ZUGANGSTYP + "," + SPALTE_BETREIBER_ID + " "
			+ TYP_BETREIBER_ID + " REFERENCES " + TABELLE_BETREIBER + " ("
			+ SPALTE_ID + ")" + "," + SPALTE_PREIS + " " + TYP_PREIS + ");";

	private static final String CREATE_TABLE_ADRESSE = "CREATE TABLE "
			+ TABELLE_ADRESSE + " (" + SPALTE_ID + " " + TYP_ID + " "
			+ PRIMARY_KEY + "," + SPALTE_LAND + " " + TYP_LAND + ","
			+ SPALTE_PLZ + " " + TYP_PLZ + "," + SPALTE_ORT + " " + TYP_ORT
			+ "," + SPALTE_STR_NR + " " + TYP_STR_NR + ");";

	private static final String CREATE_TABLE_STECKER_ANZAHL = "CREATE TABLE "
			+ TABELLE_STECKER_ANZAHL + " (" + SPALTE_ID + " " + TYP_ID + " "
			+ PRIMARY_KEY + "," + SPALTE_STECKER_ID + " " + TYP_STECKER_ID
			+ " REFERENCES " + TABELLE_STECKER + " (" + SPALTE_ID + ")" + ","
			+ SPALTE_ANZAHL + " " + TYP_ANZAHL + "," + SPALTE_LADESTATION_ID
			+ " " + TYP_LADESTATION_ID + " REFERENCES " + TABELLE_LADESTATION
			+ " (" + SPALTE_ID + ")" + ");";

	private static final String CREATE_TABLE_STECKER = "CREATE TABLE "
			+ TABELLE_STECKER + " (" + SPALTE_ID + " " + TYP_ID + " "
			+ PRIMARY_KEY + "," + SPALTE_NAME + " " + TYP_NAME + ","
			+ SPALTE_BEZEICHNUNG + " " + TYP_BEZEICHNUNG + ","
			+ SPALTE_STECKER_FOTO + " " + TYP_STECKER_FOTO + ");";

	private static final String CREATE_TABLE_BETREIBER = "CREATE TABLE "
			+ TABELLE_BETREIBER + " (" + SPALTE_ID + " " + TYP_ID + " "
			+ PRIMARY_KEY + "," + SPALTE_NAME + " " + TYP_NAME + ","
			+ SPALTE_LOGO + " " + TYP_LOGO + "," + SPALTE_ABRECHNUNG_ID + " "
			+ TYP_ABRECHNUNG_ID + " REFERENCES " + TABELLE_ABRECHNUNG + " ("
			+ SPALTE_ID + ")," + SPALTE_WEBSITE + " " + TYP_WEBSITE + ");";

	private static final String CREATE_TABLE_ABRECHNUNG = "CREATE TABLE "
			+ TABELLE_ABRECHNUNG + " (" + SPALTE_ID + " " + TYP_ID + " "
			+ PRIMARY_KEY + "," + SPALTE_BEZEICHNUNG + " " + TYP_BEZEICHNUNG
			+ "," + SPALTE_PREIS + " " + TYP_PREIS + ");";

	public SQLDB_Verwaltung_neu(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_LADESTATION);
		db.execSQL(CREATE_TABLE_ADRESSE);
		db.execSQL(CREATE_TABLE_STECKER_ANZAHL);
		db.execSQL(CREATE_TABLE_STECKER);
		db.execSQL(CREATE_TABLE_BETREIBER);
		db.execSQL(CREATE_TABLE_ABRECHNUNG);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
		if (!db.isReadOnly()) {

			db.execSQL("PRAGMA foreign_keys=ON;");
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}