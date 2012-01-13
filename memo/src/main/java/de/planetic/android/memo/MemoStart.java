package de.planetic.android.memo;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MemoStart extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.memostart_layout);
	}

	public void punkteZeigen(View v_view) {

		Intent int_intent = new Intent(this, PunkteZeigen_Tab.class);
		this.startActivity(int_intent);
	}

	public void serverSynchronisieren(View v_view) {
		// platzhalter für "Server synchronisieren". wird waehrenddessen zum
		// abgleich der db-tabellen genutzt

		ContentValues contentvalues_werte;
		MemoSingleton memosingleton_anwendung = (MemoSingleton) getApplication();
		int int_anzahl = 0;

		Cursor cursor_synch = memosingleton_anwendung.sqldatabase_readable
				.query(SQL_DB_Verwaltung.TABELLEN_NAME_SYNCH,
						null,
						SQL_DB_Verwaltung.NAME_SPALTE_11 + "=?",
						new String[] { String
								.valueOf(PunkteHinzufuegen_Service.VERARBEITET) },
						null, null, null);

		if (cursor_synch.moveToFirst()) {

			do {

				contentvalues_werte = new ContentValues(10);

				contentvalues_werte
						.put(SQL_DB_Verwaltung.NAME_SPALTE_1,
								cursor_synch.getLong(cursor_synch
										.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_1)));
				contentvalues_werte
						.put(SQL_DB_Verwaltung.NAME_SPALTE_2,
								cursor_synch.getString(cursor_synch
										.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_2)));
				contentvalues_werte
						.put(SQL_DB_Verwaltung.NAME_SPALTE_3,
								cursor_synch.getInt(cursor_synch
										.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_3)));
				contentvalues_werte
						.put(SQL_DB_Verwaltung.NAME_SPALTE_4,
								cursor_synch.getInt(cursor_synch
										.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_4)));
				contentvalues_werte
						.put(SQL_DB_Verwaltung.NAME_SPALTE_5,
								cursor_synch.getString(cursor_synch
										.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_5)));
				contentvalues_werte
						.put(SQL_DB_Verwaltung.NAME_SPALTE_6,
								cursor_synch.getLong(cursor_synch
										.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_6)));
				contentvalues_werte
						.put(SQL_DB_Verwaltung.NAME_SPALTE_7,
								cursor_synch.getString(cursor_synch
										.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_7)));
				contentvalues_werte
						.put(SQL_DB_Verwaltung.NAME_SPALTE_8,
								cursor_synch.getString(cursor_synch
										.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_8)));
				contentvalues_werte
						.put(SQL_DB_Verwaltung.NAME_SPALTE_9,
								cursor_synch.getDouble(cursor_synch
										.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_9)));
				contentvalues_werte
						.put(SQL_DB_Verwaltung.NAME_SPALTE_10,
								cursor_synch.getString(cursor_synch
										.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_10)));

				memosingleton_anwendung.sqldatabase_writeable.insert(
						SQL_DB_Verwaltung.TABELLEN_NAME_HAUPT, null,
						contentvalues_werte);

				int_anzahl++;
			} while (cursor_synch.moveToNext());
		}

		Toast.makeText(
				this,
				String.valueOf(int_anzahl)
						+ " "
						+ getResources().getString(
								R.string.elemente_hinzugefuegt_text),
				Toast.LENGTH_SHORT).show();

		Log.d("memo_debug_memostart", "tabellen synchronisiert");
	}

	public void einstellungenZeigen(View v_view) {

		startActivity(new Intent(this, Memo_Einstellungen.class));
	}
}
