package de.planetic.android.memo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateFormat;

import com.google.android.maps.Overlay;

public class MemoSingleton extends Application {

	private SQL_DB_Verwaltung sqldb_db_verwaltung;

	// werden zur unterscheidung bei aktualisiereDBZugrif genutzt
	public static final int LISTE = 0;
	public static final int KARTE = 1;
	public static final int ZURUECKSETZEN = 2;
	public static final String INTENT_DB_FUELLEN = "db_fuellen";
	public static final String INTENT_DB_LEEREN = "db_leeren";
	public static final String INTENT_ZEIGE_LISTE = "zeige_liste";
	public static final String INTENT_ZEIGE_KARTE = "zeige_karte";
	public static final String INTENT_STARTE_GPS = "starte_gps";
	public static final String INTENT_STOPPE_GPS = "stoppe_gps";
	public static final String INTENT_PUNKTE_FILTERN = "punkte_filtern";

	// speichert listeneintraege
	// speichert karteneintraege
	public List<HashMap<String, Object>> list_liste_daten;
	public List<HashMap<String, Object>> list_liste_daten_temp;
	public List<Overlay> list_karte_overlays;
	public List<Overlay> list_karte_overlays_temp;

	// speichern letzten lesezugriff auf db in ms seit 1.1.1970
	private long long_letzter_db_zugriff_liste;
	private long long_letzter_db_zugriff_karte;

	public GPS_Verwaltung gps_verwaltung;
	public PunkteZeigen_Tab context_punktezeigen_tab;

	private BroadcastReceiver bcreceiver_receiver;

	@Override
	public void onCreate() {
		super.onCreate();
		long_letzter_db_zugriff_liste = 0;
		long_letzter_db_zugriff_karte = 0;

		sqldb_db_verwaltung = new SQL_DB_Verwaltung(this);

		list_liste_daten = new ArrayList<HashMap<String, Object>>();
		list_liste_daten_temp = new ArrayList<HashMap<String, Object>>();
		list_karte_overlays = new ArrayList<Overlay>();
		list_karte_overlays_temp = new ArrayList<Overlay>();

		gps_verwaltung = new GPS_Verwaltung(getApplicationContext());

		bcreceiver_receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				String string_intent_action = intent.getAction();

				if (string_intent_action
						.equals(MemoSingleton.INTENT_STARTE_GPS)) {
					gps_verwaltung.starteGPS();
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_STOPPE_GPS)) {
					gps_verwaltung.stoppeGPS();
				}
			}
		};

		IntentFilter ifilter_filter = new IntentFilter();
		ifilter_filter.addAction(MemoSingleton.INTENT_STARTE_GPS);
		ifilter_filter.addAction(MemoSingleton.INTENT_STOPPE_GPS);

		this.registerReceiver(bcreceiver_receiver, ifilter_filter);

	}

	// aktualisiert zeitstempel bei letzten zugriff der activities um nur neue
	// eintraege zu beruecksichtigen
	public void aktualisiereDBZugriff(int int_klasse, long long_id) {
		switch (int_klasse) {
		case LISTE:
			if (long_letzter_db_zugriff_liste < long_id) {
				long_letzter_db_zugriff_liste = long_id;
			}
			break;
		case KARTE:
			if (long_letzter_db_zugriff_karte < long_id) {
				long_letzter_db_zugriff_karte = long_id;
			}
			break;
		case ZURUECKSETZEN:
			long_letzter_db_zugriff_liste = 0;
			long_letzter_db_zugriff_karte = 0;
			list_liste_daten.clear();
			list_liste_daten_temp.clear();
			list_karte_overlays.clear();
			list_karte_overlays_temp.clear();
			break;
		default:
		}
	}

	// gibt letzten zugriff zeitstempel zurueck
	public long letzterDBZugriff(int int_klasse) {
		switch (int_klasse) {
		case LISTE:
			return long_letzter_db_zugriff_liste;
		case KARTE:
			return long_letzter_db_zugriff_karte;
		default:
			return 0;
		}
	}

	public void dbAbfragen(final GeoPunkt geopunkt_punkt, boolean boolean_liste) {

		SQLiteDatabase sqldb_zugriff = sqldb_db_verwaltung
				.getReadableDatabase();

		Cursor cursor_anfrage = sqldb_zugriff.query(
				SQL_DB_Verwaltung.TABELLEN_NAME_HAUPT, new String[] {
						SQL_DB_Verwaltung.NAME_SPALTE_2,
						SQL_DB_Verwaltung.NAME_SPALTE_5,
						SQL_DB_Verwaltung.NAME_SPALTE_6,
						SQL_DB_Verwaltung.NAME_SPALTE_7,
						SQL_DB_Verwaltung.NAME_SPALTE_8,
						SQL_DB_Verwaltung.NAME_SPALTE_9,
						SQL_DB_Verwaltung.NAME_SPALTE_10 },
				SQL_DB_Verwaltung.NAME_SPALTE_3 + "=? AND "
						+ SQL_DB_Verwaltung.NAME_SPALTE_4 + "=?",
				new String[] { String.valueOf(geopunkt_punkt.getLatitudeE6()),
						String.valueOf(geopunkt_punkt.getLongitudeE6()) },
				null, null, null);

		cursor_anfrage.moveToFirst();

		AlertDialog.Builder alertdialog_builder = new AlertDialog.Builder(
				this.context_punktezeigen_tab);

		alertdialog_builder.setCancelable(true);
		alertdialog_builder.setPositiveButton(
				getResources().getString(
						R.string.punktezeigen_tab_dialog_button_text_ok),
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

					}
				});
		if (boolean_liste) {
			alertdialog_builder
					.setNeutralButton(
							getResources()
									.getString(
											R.string.punktezeigen_tab_dialog_button_text_zeige_karte),
							new OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									context_punktezeigen_tab.tabhost
											.setCurrentTab(1);

									Intent intent_befehl = new Intent(
											MemoSingleton.INTENT_ZEIGE_KARTE);

									intent_befehl.putExtra("lat",
											geopunkt_punkt.getLatitudeE6());
									intent_befehl.putExtra("lon",
											geopunkt_punkt.getLongitudeE6());

									context_punktezeigen_tab
											.sendBroadcast(intent_befehl);

									dialog.dismiss();
								}
							});
		}

		alertdialog_builder.setTitle(cursor_anfrage.getString(0));

		alertdialog_builder.setIcon(cursor_anfrage.getInt(1));

		String string_datum = DateFormat.format(
				DateFormat.DATE + "." + DateFormat.MONTH + "."
						+ DateFormat.YEAR + " " + DateFormat.HOUR_OF_DAY + ":"
						+ DateFormat.MINUTE,
				new Date(cursor_anfrage.getLong(2))).toString();

		alertdialog_builder.setMessage(getResources().getString(R.string.datum)
				+ ": "
				+ string_datum
				+ "\n\n"
				+ getResources().getString(
						R.string.punktezeigen_tab_dialog_text_beschreibung)
				+ ":\n"
				+ cursor_anfrage.getString(3)
				+ "\n\n"
				+ getResources().getString(
						R.string.punktezeigen_tab_dialog_text_eigenschaften)
				+ ":\n"
				+ cursor_anfrage.getString(4)
				+ "\n\n"
				+ cursor_anfrage.getString(5)
				+ " "
				+ getResources().getString(
						R.string.punktezeigen_tab_dialog_text_preis)
				+ "\n\n"
				+ getResources().getString(
						R.string.punktezeigen_tab_dialog_text_adresse) + ":\n"
				+ cursor_anfrage.getString(6) + "\n\n"
				+ getResources().getString(R.string.lat_kurz)
				+ String.valueOf(geopunkt_punkt.getLatitudeE6()) + " "
				+ getResources().getString(R.string.lon_kurz)
				+ String.valueOf(geopunkt_punkt.getLongitudeE6()));

		alertdialog_builder.create().show();

		cursor_anfrage.close();
		sqldb_zugriff.close();
	}
}
