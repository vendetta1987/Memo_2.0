package de.planetic.android.memo;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;

import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class MemoSingleton extends Application {

	public static final String INTENT_DB_FUELLEN = "db_fuellen";
	public static final String INTENT_DB_LEEREN = "db_leeren";
	public static final String INTENT_ZEIGE_LISTE = "zeige_liste";
	public static final String INTENT_ZOOME_KARTE = "zeige_karte";
	public static final String INTENT_STARTE_GPS = "starte_gps";
	public static final String INTENT_STOPPE_GPS = "stoppe_gps";
	public static final String INTENT_PUNKTE_FILTERN = "punkte_filtern";
	public static final String INTENT_KARTE_VERFOLGE_AKTUELLE_POS = "karte_verfolge_aktuelle_pos";
	public static final String INTENT_HIERHIN_NAVIGIEREN = "hierhin_navigieren";
	// werden zur unterscheidung bei aktualisiereDBZugrif genutzt
	public static final int LISTE = 0;
	public static final int KARTE = 1;
	public static final int ZURUECKSETZEN = 2;
	public static final int GPS_LISTENER_ALLE = -1;
	public static final int GPS_LISTENER_NORMAL = 0;
	public static final int GPS_LISTENER_AKTUELL = 1;
	public static final int GPS_LISTENER_NAVIGATION = 2;

	public boolean boolean_gefiltert;
	public boolean boolean_aktuelle_position;
	public boolean boolean_navigieren;
	public boolean boolean_gedreht;

	public GPS_Verwaltung gps_verwaltung;
	public PunkteZeigen_Tab context_punktezeigen_tab;
	public SQLiteDatabase sqldatabase_writeable, sqldatabase_readable;
	public Projection projection_karte;
	// speichert listeneintraege
	public ArrayList<HashMap<String, Object>> arraylist_liste_daten;
	public ArrayList<HashMap<String, Object>> arraylist_liste_daten_temp;
	// speichert karteneintraege
	public ArrayList<Overlay> arraylist_karte_overlays;
	public ArrayList<Overlay> arraylist_karte_overlays_temp;

	// speichern letzten lesezugriff auf db in ms seit 1.1.1970
	private long long_letzter_db_zugriff_liste;
	private long long_letzter_db_zugriff_karte;

	private BroadcastReceiver bcreceiver_receiver;

	@Override
	public void onCreate() {
		super.onCreate();
		long_letzter_db_zugriff_liste = 0;
		long_letzter_db_zugriff_karte = 0;

		SQL_DB_Verwaltung sqldb_db_verwaltung = new SQL_DB_Verwaltung(this);

		sqldatabase_readable = sqldb_db_verwaltung.getReadableDatabase();
		sqldatabase_writeable = sqldb_db_verwaltung.getWritableDatabase();

		arraylist_liste_daten = new ArrayList<HashMap<String, Object>>();
		arraylist_liste_daten_temp = new ArrayList<HashMap<String, Object>>();
		arraylist_karte_overlays = new ArrayList<Overlay>();
		arraylist_karte_overlays_temp = new ArrayList<Overlay>();

		gps_verwaltung = new GPS_Verwaltung(getApplicationContext());

		boolean_gefiltert = false;
		boolean_navigieren = false;
		boolean_gedreht = false;

		bcreceiver_receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				String string_intent_action = intent.getAction();

				if (string_intent_action
						.equals(MemoSingleton.INTENT_STARTE_GPS)) {
					gps_verwaltung.starteGPS(intent);
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_STOPPE_GPS)) {
					gps_verwaltung.stoppeGPS(intent);
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
			arraylist_liste_daten.clear();
			arraylist_liste_daten_temp.clear();
			arraylist_karte_overlays.clear();
			arraylist_karte_overlays_temp.clear();
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

		Cursor cursor_anfrage = sqldatabase_readable.query(
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
											.setCurrentTab(PunkteZeigen_Tab.TAB_KARTE);

									Intent intent_befehl = new Intent(
											MemoSingleton.INTENT_ZOOME_KARTE);

									intent_befehl.putExtra("int_lat",
											geopunkt_punkt.getLatitudeE6());
									intent_befehl.putExtra("int_lon",
											geopunkt_punkt.getLongitudeE6());

									context_punktezeigen_tab
											.sendBroadcast(intent_befehl);

									dialog.dismiss();
								}
							});
		}
		alertdialog_builder.setNegativeButton(
				getResources().getString(
						R.string.punktezeigen_tab_dialog_text_navigiere),
				new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						new Navigation_AsyncTask(context_punktezeigen_tab)
								.execute(geopunkt_punkt);

						dialog.dismiss();
					}
				});

		alertdialog_builder.setTitle(cursor_anfrage.getString(cursor_anfrage
				.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_2)));

		alertdialog_builder
				.setIcon((Drawable) getSymbol(
						cursor_anfrage
								.getString(cursor_anfrage
										.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_5)),
						true));

		String string_datum = DateFormat.format(
				DateFormat.DATE + "." + DateFormat.MONTH + "."
						+ DateFormat.YEAR + " " + DateFormat.HOUR_OF_DAY + ":"
						+ DateFormat.MINUTE,
				new Date(cursor_anfrage.getLong(cursor_anfrage
						.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_6))))
				.toString();

		alertdialog_builder.setMessage(getResources().getString(
				R.string.datum_text)
				+ ": "
				+ string_datum
				+ "\n\n"
				+ getResources().getString(
						R.string.punktezeigen_tab_dialog_text_beschreibung)
				+ ":\n"
				+ cursor_anfrage.getString(cursor_anfrage
						.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_7))
				+ "\n\n"
				+ getResources().getString(
						R.string.punktezeigen_tab_dialog_text_eigenschaften)
				+ ":\n"
				+ cursor_anfrage.getString(cursor_anfrage
						.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_8))
				+ "\n\n"
				+ cursor_anfrage.getString(cursor_anfrage
						.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_9))
				+ " "
				+ getResources().getString(
						R.string.punktezeigen_tab_dialog_text_preis)
				+ "\n\n"
				+ getResources().getString(
						R.string.punktezeigen_tab_dialog_text_adresse)
				+ ":\n"
				+ cursor_anfrage.getString(cursor_anfrage
						.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_10))
				+ "\n\n"
				+ getResources().getString(R.string.lat_kurz)
				+ String.valueOf(geopunkt_punkt.getLatitudeE6())
				+ " "
				+ getResources().getString(R.string.lon_kurz)
				+ String.valueOf(geopunkt_punkt.getLongitudeE6()));

		alertdialog_builder.create().show();

		cursor_anfrage.close();
	}

	public Object getSymbol(String string_name, boolean boolean_drawable) {

		File file_iconsordner = getDir("icons", Context.MODE_PRIVATE);

		// FileOutputStream fos_ausgabe = null;
		//
		// try {
		// fos_ausgabe = new FileOutputStream(
		// file_iconsordner.getAbsolutePath() + "/" + "testdatei");
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		//
		// BitmapFactory.decodeResource(getResources(),
		// R.drawable.icon).compress(
		// CompressFormat.PNG, 100, fos_ausgabe);

		File file_bild = new File(file_iconsordner.getAbsolutePath() + "/"
				+ string_name);

		if (file_bild.exists()) {

			if (boolean_drawable) {

				return new BitmapDrawable(getResources(),
						BitmapFactory.decodeFile(file_bild.getAbsolutePath()));
			} else {

				return file_bild.getAbsoluteFile().toURI();
			}

		} else {

			int i = 0;

			try {

				i = (Integer) R.drawable.class.getDeclaredField(string_name)
						.get(null);
			} catch (Exception e) {
				e.printStackTrace();

				// TODO mindestens ein symbol mitliefern
				i = R.drawable.icon;
			}

			if (boolean_drawable) {

				return getResources().getDrawable(i);
			} else {

				return i;
			}
		}
	}
}
