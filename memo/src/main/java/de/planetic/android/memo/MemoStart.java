package de.planetic.android.memo;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
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

		// Intent int_intent = new Intent(this, PunkteZeigen_Tab.class);
		// this.startActivity(int_intent);

		initialisiereVokabular();
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

	private void initialisiereVokabular() {

		if (!Memo_Einstellungen.leseEinstellungen(this).getBoolean(
				"boolean_vokabular_initialisiert", false)) {

			SQLiteDatabase sqldatabase_writeable = new SQLDB_Verwaltung_neu(
					this).getWritableDatabase();
			ContentValues cv_dbeintrag = new ContentValues();

			// Stecker
			sqldatabase_writeable.delete(SQLDB_Verwaltung_neu.TABELLE_STECKER,
					null, null);

			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_NAME,
					"IEC60309_32A_400V");
			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_BEZEICHNUNG,
					"roter Stecker nach IEC60309 mit maximal 32A bei 400V");
			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_STECKER_FOTO,
					leseBlob(R.drawable.iec60309_32a_400v));

			sqldatabase_writeable.insert(SQLDB_Verwaltung_neu.TABELLE_STECKER,
					null, cv_dbeintrag);

			cv_dbeintrag.clear();

			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_NAME,
					"IEC60309_16A_230V");
			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_BEZEICHNUNG,
					"blauer Stecker nach IEC60309 mit maximal 16A bei 230V");
			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_STECKER_FOTO,
					leseBlob(R.drawable.iec60309_16a_230v));

			sqldatabase_writeable.insert(SQLDB_Verwaltung_neu.TABELLE_STECKER,
					null, cv_dbeintrag);

			cv_dbeintrag.clear();

			// Abrechung
			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_BEZEICHNUNG,
					"Flatrate");
			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_PREIS, 0);

			sqldatabase_writeable
					.insert(SQLDB_Verwaltung_neu.TABELLE_ABRECHNUNG, null,
							cv_dbeintrag);

			cv_dbeintrag.clear();

			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_BEZEICHNUNG, "Teuer");
			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_PREIS, 15.0);

			sqldatabase_writeable
					.insert(SQLDB_Verwaltung_neu.TABELLE_ABRECHNUNG, null,
							cv_dbeintrag);

			cv_dbeintrag.clear();

			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_BEZEICHNUNG, "Billig");
			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_PREIS, 1.5);

			sqldatabase_writeable
					.insert(SQLDB_Verwaltung_neu.TABELLE_ABRECHNUNG, null,
							cv_dbeintrag);

			cv_dbeintrag.clear();

			// Betreiber

			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_NAME, "Eon");
			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_LOGO,
					leseBlob(R.drawable.eon));
			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_ABRECHNUNG_ID, 1);
			cv_dbeintrag
					.put(SQLDB_Verwaltung_neu.SPALTE_WEBSITE,
							"http://www.eon-energie.com/pages/eea_de/Innovation/Innovation/E-Mobilitaet/Uebersicht/index.htm");

			sqldatabase_writeable.insert(
					SQLDB_Verwaltung_neu.TABELLE_BETREIBER, null, cv_dbeintrag);

			cv_dbeintrag.clear();

			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_NAME, "RWE");
			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_LOGO,
					leseBlob(R.drawable.rwe));
			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_ABRECHNUNG_ID, 2);
			cv_dbeintrag
					.put(SQLDB_Verwaltung_neu.SPALTE_WEBSITE,
							"http://www.rwe-mobility.com/web/cms/de/240690/rwemobility/was-ist-elektromobilitaet/standorte-rwe-smart-station/");

			sqldatabase_writeable.insert(
					SQLDB_Verwaltung_neu.TABELLE_BETREIBER, null, cv_dbeintrag);

			cv_dbeintrag.clear();

			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_NAME, "Vattenfall");
			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_LOGO,
					leseBlob(R.drawable.vattenfall));
			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_ABRECHNUNG_ID, 3);
			cv_dbeintrag.put(SQLDB_Verwaltung_neu.SPALTE_WEBSITE,
					"http://www.vattenfall.de/de/batterieantrieb.htm");

			sqldatabase_writeable.insert(
					SQLDB_Verwaltung_neu.TABELLE_BETREIBER, null, cv_dbeintrag);

			sqldatabase_writeable.close();

			Memo_Einstellungen.leseEinstellungen(this).edit()
					.putBoolean("boolean_vokabular_initialisiert", true)
					.apply();
		}
	}

	private byte[] leseBlob(int id) {

		ByteArrayOutputStream baos_ausgabe = new ByteArrayOutputStream();
		((BitmapDrawable) getResources().getDrawable(id)).getBitmap().compress(
				Bitmap.CompressFormat.PNG, 100, baos_ausgabe);
		return baos_ausgabe.toByteArray();
	}
}
