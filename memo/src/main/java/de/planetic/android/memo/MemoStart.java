package de.planetic.android.memo;

import java.util.ArrayList;

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

		initialisiereVokabular();

		DBLesenSchreiben db = new DBLesenSchreiben(this);

		db.schreibeAdresse(new Adresse());
		db.schreibeLadestation(new Ladestation(this));
		
		ArrayList<Ladestation> test = db.leseLadestation(-1);

		test.add(null);

		// Intent int_intent = new Intent(this, PunkteZeigen_Tab.class);
		// this.startActivity(int_intent);
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

		// TODO Vokabular Initialisierung anpassen
		if (!Memo_Einstellungen.leseEinstellungen(this).getBoolean(
				"boolean_vokabular_initialisiert", false)) {

			DBLesenSchreiben dblesenschreiben_db = new DBLesenSchreiben(this);

			// Stecker
			Stecker stecker_typ = new Stecker(this);

			stecker_typ.string_name = "IEC60309_32A_400V";
			stecker_typ.string_bezeichnung = "roter Stecker nach IEC60309 mit maximal 32A bei 400V";
			stecker_typ.setzeSteckerFoto(R.drawable.iec60309_32a_400v);

			dblesenschreiben_db.schreibeStecker(stecker_typ);

			stecker_typ.string_name = "IEC60309_16A_230V";
			stecker_typ.string_bezeichnung = "blauer Stecker nach IEC60309 mit maximal 16A bei 230V";
			stecker_typ.setzeSteckerFoto(R.drawable.iec60309_16a_230v);

			dblesenschreiben_db.schreibeStecker(stecker_typ);

			// Abrechung
			Abrechnung abrechung_rechnung = new Abrechnung();

			abrechung_rechnung.string_bezeichnung = "Flatrate";
			abrechung_rechnung.double_preis = 0.0;

			dblesenschreiben_db.schreibeAbrechnung(abrechung_rechnung);

			abrechung_rechnung.string_bezeichnung = "Teuer";
			abrechung_rechnung.double_preis = 15.0;

			dblesenschreiben_db.schreibeAbrechnung(abrechung_rechnung);

			abrechung_rechnung.string_bezeichnung = "Billig";
			abrechung_rechnung.double_preis = 1.7;

			dblesenschreiben_db.schreibeAbrechnung(abrechung_rechnung);

			// Betreiber
			Betreiber betreiber_firma = new Betreiber(this);

			betreiber_firma.string_name = "Eon";
			betreiber_firma.setzeBetreiberLogo(R.drawable.eon);
			betreiber_firma.long_abrechnung_id = 1;
			betreiber_firma.string_website = "http://www.eon-energie.com/pages/eea_de/Innovation/Innovation/E-Mobilitaet/Uebersicht/index.htm";

			dblesenschreiben_db.schreibeBetreiber(betreiber_firma);

			betreiber_firma.string_name = "RWE";
			betreiber_firma.setzeBetreiberLogo(R.drawable.rwe);
			betreiber_firma.long_abrechnung_id = 2;
			betreiber_firma.string_website = "http://www.rwe-mobility.com/web/cms/de/240690/rwemobility/was-ist-elektromobilitaet/standorte-rwe-smart-station/";

			dblesenschreiben_db.schreibeBetreiber(betreiber_firma);

			betreiber_firma.string_name = "Vatenfall";
			betreiber_firma.setzeBetreiberLogo(R.drawable.vattenfall);
			betreiber_firma.long_abrechnung_id = 3;
			betreiber_firma.string_website = "http://www.vattenfall.de/de/batterieantrieb.htm";

			dblesenschreiben_db.schreibeBetreiber(betreiber_firma);

			Memo_Einstellungen.leseEinstellungen(this).edit()
					.putBoolean("boolean_vokabular_initialisiert", true)
					.apply();

			Toast.makeText(this, "Vokabular initialisiert.", Toast.LENGTH_SHORT)
					.show();
		}
	}
}
