package de.planetic.android.memo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;

/**
 * {@link AsyncTask} für die Verarbeitung aller Punkte die in der Tabelle Synch
 * hinterlegt wurden und noch nicht georeferenziert werden konnten. Ruft für
 * jeden Eintrag {@link PunkteHinzufuegen_Service} per {@link Intent} auf und
 * fügt die Daten des Punktes hinzu.
 * 
 * @see PunkteHinzufuegen_Service
 * 
 */
public class PunkteHinzufuegen_Service_AsyncTask extends
		AsyncTask<Void, Integer, Void> {

	private Context context_con;

	public PunkteHinzufuegen_Service_AsyncTask(Context con) {

		context_con = con;
	}

	/**
	 * Liest die Tabelle Synch aus und erfässt alle bisher unverarbeiteten
	 * Einträge.
	 */
	@Override
	protected Void doInBackground(Void... params) {

		Cursor cursor_anfrage = ((MemoSingleton) context_con
				.getApplicationContext()).sqldatabase_readable.query(
				SQL_DB_Verwaltung.TABELLEN_NAME_SYNCH, null, "NOT "
						+ SQL_DB_Verwaltung.NAME_SPALTE_11 + "=?",
				new String[] { String
						.valueOf(PunkteHinzufuegen_Service.VERARBEITET) },
				null, null, null);

		int int_notification_zaehler = 0;

		if (cursor_anfrage.moveToFirst()) {

			do {
				Intent intent_service = new Intent(context_con,
						PunkteHinzufuegen_Service.class);

				intent_service.putExtra(context_con.getPackageName() + "_"
						+ "long_id", cursor_anfrage.getLong(cursor_anfrage
						.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_1)));
				intent_service
						.putExtra(
								context_con.getPackageName() + "_"
										+ "string_name",
								cursor_anfrage.getString(cursor_anfrage
										.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_2)));
				intent_service
						.putExtra(
								context_con.getPackageName() + "_"
										+ "string_icon",
								cursor_anfrage.getString(cursor_anfrage
										.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_5)));
				intent_service
						.putExtra(
								context_con.getPackageName() + "_"
										+ "string_beschreibung",
								cursor_anfrage.getString(cursor_anfrage
										.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_7)));
				intent_service
						.putExtra(
								context_con.getPackageName() + "_"
										+ "string_radio_eigenschaften",
								cursor_anfrage.getString(cursor_anfrage
										.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_8)));
				intent_service
						.putExtra(
								context_con.getPackageName() + "_"
										+ "double_preis",
								cursor_anfrage.getDouble(cursor_anfrage
										.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_9)));
				intent_service
						.putExtra(
								context_con.getPackageName() + "_"
										+ "string_adresse",
								cursor_anfrage.getString(cursor_anfrage
										.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_10)));
				intent_service
						.putExtra(
								context_con.getPackageName() + "_"
										+ "boolean_pos_aus_gps",
								(cursor_anfrage.getInt(cursor_anfrage
										.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_11)) == PunkteHinzufuegen_Service.NICHT_VERARBEITET_GPS) ? true
										: false);
				intent_service.putExtra(context_con.getPackageName() + "_"
						+ "boolean_erneut_verarbeiten", true);
				intent_service.putExtra(context_con.getPackageName() + "_"
						+ "int_notification_zaehler", int_notification_zaehler);

				if (cursor_anfrage.isLast()) {

					intent_service.putExtra(context_con.getPackageName() + "_"
							+ "boolean_letzte_zeile", true);
				}

				int_notification_zaehler++;

				context_con.startService(intent_service);

			} while (cursor_anfrage.moveToNext());
		}

		cursor_anfrage.close();

		return null;
	}
}
