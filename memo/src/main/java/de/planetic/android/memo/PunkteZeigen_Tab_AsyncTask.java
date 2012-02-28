package de.planetic.android.memo;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.maps.OverlayItem;

import de.planetic.android.memo.db.Betreiber;
import de.planetic.android.memo.db.DBLesenSchreiben;
import de.planetic.android.memo.db.Ladestation;

/**
 * Eigene Implementierung von {@link AsyncTask} für das Auslesen und verarbeiten
 * der Punkte-Datenbank. Nimmt DB-Abfrage entgegen und speichert Daten in den
 * entsprechenden Variablen in {@link MemoSingleton}.
 * 
 * @see MemoSingleton
 * @see PunkteZeigen_Tab_Liste
 * @see PunkteZeigen_Tab_Karte
 */
public class PunkteZeigen_Tab_AsyncTask extends
		AsyncTask<Void, Integer, Integer> {

	public static final int LISTE = 0;
	public static final int KARTE = 1;

	private static final int PROGRESS_MIN = 0;
	private static final int PROGRESS_UPDATE = 1;
	private static final int PROGRESS_SET_MAX = 2;
	private static final int PROGRESS_MAX = 3;
	private static final int PROGRESS_PROZENT_SCHRITTE = 5;

	private Context context_con;
	private MemoSingleton memosingleton_anwendung;
	private ProgressDialog progress_fortschritt;
	private DBLesenSchreiben db_rw;
	private int int_modus, int_prozent_temp, int_progress_aktuell,
			int_progress_max;
	private boolean boolean_filter;

	/**
	 * Konstruktor unterscheidet zwischen Aufruf für Karte und Liste sowie der
	 * Filterung bestehender Einträge.
	 * 
	 * @param con
	 *            {@link Context} für die Anzeige von {@link Intent} und
	 *            {@link Toast}
	 * @param int_mod
	 *            Modus zur Unterscheidung von {@code LISTE} und {@code KARTE}
	 * @param filter
	 *            {@link Boolean} für die Nutzung von Filtern und Navigation
	 */
	public PunkteZeigen_Tab_AsyncTask(Context con, int int_mod, boolean filter) {

		int_modus = int_mod;
		boolean_filter = filter;

		context_con = con;
		memosingleton_anwendung = (MemoSingleton) context_con
				.getApplicationContext();

		db_rw = new DBLesenSchreiben(memosingleton_anwendung);

		int_progress_aktuell = 0;
		int_progress_max = 100;
	}

	/**
	 * Erzeugt einen Fortschrittsdialog und setzt dessen Titel dem Modus
	 * entsprechend für Liste oder Karte.
	 */
	@Override
	protected void onPreExecute() {

		erzeugeDialog();

		Log.d("memo_debug_punktezeigen_tab_asynctask", "onpreexecute");
	}

	/**
	 * Erzeugt den Fortschrittsdialog und zeigt ihn an. Schließt den Dialog bei
	 * Gerätedrehung und ersetzt ihn.
	 */
	private void erzeugeDialog() {

		if (progress_fortschritt != null) {

			try {

				progress_fortschritt.dismiss();
			} catch (Exception e) {

				e.printStackTrace();
			}
		}

		progress_fortschritt = new ProgressDialog(
				memosingleton_anwendung.context_punktezeigen_tab);

		switch (int_modus) {
		case LISTE:

			progress_fortschritt
					.setTitle(R.string.punktezeigen_tab_liste_asynctask_progressdialog_title);
			break;
		case KARTE:

			progress_fortschritt
					.setTitle(R.string.punktezeigen_tab_karte_asynctask_progressdialog_title);
			break;
		}

		progress_fortschritt.setCancelable(false);
		progress_fortschritt.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progress_fortschritt.setMax(int_progress_max);
		progress_fortschritt.setProgress(int_progress_aktuell);

		try {

			progress_fortschritt.show();
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * Aktualisiert den Fortschrittsdialog während der Verarbeitung. Nutzt
	 * {@code PROGRESS_UPDATE}, {@code PROGRESS_MIN}, {@code PROGRESS_MAX} und
	 * {@code PROGRESS_SET_MAX} zur Steuerung.
	 * 
	 * @param int_progress
	 *            {@link Integer}-Array mit [0]=Modus und [1]=Fortschritt
	 */
	@Override
	protected void onProgressUpdate(Integer... int_progress) {

		if (memosingleton_anwendung.boolean_gedreht) {

			erzeugeDialog();
			memosingleton_anwendung.boolean_gedreht = false;
		}

		try {
			switch (int_progress[0]) {

			case PROGRESS_UPDATE:

				progress_fortschritt.setProgress(int_progress[1]);
				int_progress_aktuell = int_progress[1];
				break;
			case PROGRESS_MIN:

				progress_fortschritt.setProgress(0);
				int_progress_aktuell = 0;
				break;
			case PROGRESS_MAX:

				progress_fortschritt.setProgress(progress_fortschritt.getMax());
				int_progress_aktuell = progress_fortschritt.getMax();
				break;
			case PROGRESS_SET_MAX:

				progress_fortschritt.setMax(int_progress[1]);
				int_progress_max = int_progress[1];
				break;
			default:
			}
		} catch (Exception e) {

			Log.d("memo_debug", "onProgressUpdate: " + e.toString());
		}
	}

	/**
	 * {@code private int berechneProzentSchritte(int int_summe)}
	 * <p/>
	 * Berechnet absolute, prozentuale Werte aus einer vorgegebenen Summe zur
	 * Nutzung in {@code onProgressUpdate}.
	 * 
	 * @param int_summe
	 *            Die Summe aller Werte von denen
	 *            {@code PROGRESS_PROZENT_SCHRITTE} berechnet werden sollen.
	 * @return {@link Integer} mit dem absoluten Wert von
	 *         {@code PROGRESS_PROZENT_SCHRITTE} bezüglich {@code int_summe}
	 */
	private int berechneProzentSchritte(int int_summe) {

		if ((int_summe * PROGRESS_PROZENT_SCHRITTE) < 100) {

			return 1;
		} else {

			return ((int_summe * PROGRESS_PROZENT_SCHRITTE) / 100);
		}
	}

	/**
	 * Ruft abhängig von {@code int_modus} die entsprechende, verarbeitende
	 * Funktion auf und schließt danach den DB-{@link Cursor}.
	 */
	@Override
	protected Integer doInBackground(Void... v) {

		int int_rueckgabe;

		switch (int_modus) {
		case LISTE:
			int_rueckgabe = listeBearbeiten();
			break;
		case KARTE:
			int_rueckgabe = karteBearbeiten();
			break;
		default:
			int_rueckgabe = -1;
		}

		Log.d("memo_debug_punktezeigen_tab_asynctask",
				"doinbackground durchlaufen");

		return int_rueckgabe;

	}

	// /**
	// * {@code private int listeBearbeiten(Cursor cursor_db_anfrage)}
	// * <p/>
	// * Verarbeitet den {@link Cursor} und füllt eine {@link ArrayList} in
	// * {@link MemoSingleton} mit den Daten für die Listenansicht.
	// *
	// * @param cursor_db_anfrage
	// * {@link Cursor} mit den Daten der DB-Abfrage
	// * @return Anzahl der Elemente im {@link Cursor}
	// */
	// private int listeBearbeiten(Cursor cursor_db_anfrage) {
	//
	// publishProgress(PROGRESS_SET_MAX, cursor_db_anfrage.getCount());
	// int_prozent_temp = berechneProzentSchritte(cursor_db_anfrage.getCount());
	//
	// if (boolean_filter) {
	// memosingleton_anwendung.arraylist_liste_daten_temp.clear();
	// }
	//
	// if (cursor_db_anfrage.moveToFirst()) {
	//
	// GeoPunkt geopkt_geopunkt = new GeoPunkt();
	// HashMap<String, Object> hashmap_liste_daten_datum;
	//
	// do {
	// geopkt_geopunkt.long_id = cursor_db_anfrage
	// .getLong(cursor_db_anfrage
	// .getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_1));
	// geopkt_geopunkt.string_name = cursor_db_anfrage
	// .getString(cursor_db_anfrage
	// .getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_2));
	// geopkt_geopunkt = geopkt_geopunkt
	// .setLatitude(cursor_db_anfrage.getInt(cursor_db_anfrage
	// .getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_3)));
	// geopkt_geopunkt = geopkt_geopunkt
	// .setLongitude(cursor_db_anfrage.getInt(cursor_db_anfrage
	// .getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_4)));
	// geopkt_geopunkt.string_icon = cursor_db_anfrage
	// .getString(cursor_db_anfrage
	// .getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_5));
	//
	// hashmap_liste_daten_datum = new HashMap<String, Object>(3);
	//
	// hashmap_liste_daten_datum.put(
	// PunkteZeigen_Tab_Liste.LADESTATION_NAME,
	// geopkt_geopunkt.string_name);
	// hashmap_liste_daten_datum.put(
	// PunkteZeigen_Tab_Liste.GEOPUNKT_LAT_LON,
	// context_con.getResources().getString(R.string.lat_kurz)
	// + Integer.toString(geopkt_geopunkt
	// .getLatitudeE6())
	// + " "
	// + context_con.getResources().getString(
	// R.string.lon_kurz)
	// + Integer.toString(geopkt_geopunkt
	// .getLongitudeE6()));
	// hashmap_liste_daten_datum.put(
	// PunkteZeigen_Tab_Liste.GEOPUNKT_ICON,
	// memosingleton_anwendung.getSymbol(
	// geopkt_geopunkt.string_icon, false));
	//
	// if (!boolean_filter) {
	// memosingleton_anwendung.arraylist_liste_daten
	// .add(hashmap_liste_daten_datum);
	//
	// memosingleton_anwendung.aktualisiereDBZugriff(
	// MemoSingleton.LISTE, geopkt_geopunkt.long_id);
	// } else {
	// memosingleton_anwendung.arraylist_liste_daten_temp
	// .add(hashmap_liste_daten_datum);
	// }
	//
	// if (isCancelled()) {
	// return cursor_db_anfrage.getPosition();
	// }
	//
	// if ((cursor_db_anfrage.getPosition() % int_prozent_temp) == 0) {
	//
	// publishProgress(PROGRESS_UPDATE,
	// cursor_db_anfrage.getPosition());
	// }
	//
	// } while (cursor_db_anfrage.moveToNext());
	//
	// }
	//
	// publishProgress(PROGRESS_MAX);
	//
	// return cursor_db_anfrage.getCount();
	// }

	/**
	 * {@code private int listeBearbeiten(Cursor cursor_db_anfrage)}
	 * <p/>
	 * Verarbeitet den {@link Cursor} und füllt eine {@link ArrayList} in
	 * {@link MemoSingleton} mit den Daten für die Listenansicht.
	 * 
	 * @param cursor_db_anfrage
	 *            {@link Cursor} mit den Daten der DB-Abfrage
	 * @return Anzahl der Elemente im {@link Cursor}
	 */
	private int listeBearbeiten() {

		if (boolean_filter) {
			memosingleton_anwendung.arraylist_liste_daten_temp.clear();
		}

		HashMap<String, String> hashmap_liste_daten_datum;
		Ladestation ladestation_saeule;
		ArrayList<Ladestation> arraylist_temp = db_rw.leseLadestation(0, false,
				true);

		publishProgress(PROGRESS_SET_MAX, arraylist_temp.size());
		int_prozent_temp = berechneProzentSchritte(arraylist_temp.size());

		for (int i = 0; i < arraylist_temp.size(); i++) {

			ladestation_saeule = arraylist_temp.get(i);

			hashmap_liste_daten_datum = new HashMap<String, String>(3);

			hashmap_liste_daten_datum.put(
					PunkteZeigen_Tab_Liste.LADESTATION_NAME,
					ladestation_saeule.string_bezeichnung);
			hashmap_liste_daten_datum.put(
					PunkteZeigen_Tab_Liste.LADESTATION_VERFUEGBARKEIT,
					ladestation_saeule.leseVerfuegbarkeit());
			hashmap_liste_daten_datum.put(
					PunkteZeigen_Tab_Liste.LADESTATION_ID,
					String.valueOf(ladestation_saeule.long_id));

			if (!boolean_filter) {
				memosingleton_anwendung.arraylist_liste_daten
						.add(hashmap_liste_daten_datum);

				memosingleton_anwendung.aktualisiereDBZugriff(
						MemoSingleton.LISTE, ladestation_saeule.long_id);
			} else {
				memosingleton_anwendung.arraylist_liste_daten_temp
						.add(hashmap_liste_daten_datum);
			}

			if (isCancelled()) {
				return -1;
			}

			if ((i % int_prozent_temp) == 0) {

				publishProgress(PROGRESS_UPDATE, i);
			}

		}

		publishProgress(PROGRESS_MAX);

		return arraylist_temp.size();
	}

	// /**
	// * {@code private int karteBearbeiten(Cursor... cursor_db_anfrage)}
	// * <p/>
	// * Verarbeitet den {@link Cursor} und füllt eine {@link ArrayList} in
	// * {@link MemoSingleton} mit den {@link Overlay} für die Kartenansicht.
	// *
	// * @param cursor_db_anfrage
	// * {@link Cursor} mit den Daten der DB-Abfrage
	// * @return Anzahl der Elemente im {@link Cursor}
	// */
	// private int karteBearbeiten(Cursor... cursor_db_anfrage) {
	//
	// // cursor_db_anfrage[0] geopkt, cursor_db_anfrage[1] icon
	//
	// publishProgress(PROGRESS_SET_MAX, cursor_db_anfrage[0].getCount());
	// int_prozent_temp = berechneProzentSchritte(cursor_db_anfrage[0]
	// .getCount());
	//
	// if (boolean_filter) {
	// memosingleton_anwendung.arraylist_karte_overlays_temp.clear();
	// }
	//
	// // falls neue punkte vorhanden sind
	// if (cursor_db_anfrage[0].moveToFirst()) {
	//
	// // temporaere hashmap fuer overlays (sammlung von punkten) die der
	// // karte hinzugefuegt werden.
	//
	// HashMap<String, ItemOverlay> hashmap_itemoverlays_temp = new
	// HashMap<String, ItemOverlay>();
	//
	// ItemOverlay itemoverlay_temp;
	//
	// GeoPunkt geopkt_geopunkt = new GeoPunkt();
	//
	// OverlayItem overlayitem_temp;
	//
	// // fuer alle gefundenen symbole
	// if (cursor_db_anfrage[1].moveToFirst()) {
	//
	// do {
	//
	// if (isCancelled()) {
	// return cursor_db_anfrage[1].getPosition();
	// }
	//
	// // erzeuge overlay mit zugeordnetem symbol
	// itemoverlay_temp = new ItemOverlay(
	// (Drawable) memosingleton_anwendung.getSymbol(
	// cursor_db_anfrage[1]
	// .getString(cursor_db_anfrage[1]
	// .getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_5)),
	// true), memosingleton_anwendung);
	//
	// // fuege das overlay in die temporaere hashmap ein
	// hashmap_itemoverlays_temp
	// .put(cursor_db_anfrage[1]
	// .getString(cursor_db_anfrage[1]
	// .getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_5)),
	// itemoverlay_temp);
	//
	// // hashmap aus dem singleton speichert alle overlays mit den
	// // entsprechenden geopunkten, temporaere hashmap speichert
	// // nur neue geopunkte
	// } while (cursor_db_anfrage[1].moveToNext());
	// }
	//
	// // fuer alle erfassten neuen punkte
	// do {
	// geopkt_geopunkt.long_id = cursor_db_anfrage[0]
	// .getLong(cursor_db_anfrage[0]
	// .getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_1));
	// geopkt_geopunkt.string_name = cursor_db_anfrage[0]
	// .getString(cursor_db_anfrage[0]
	// .getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_2));
	// geopkt_geopunkt = geopkt_geopunkt
	// .setLatitude(cursor_db_anfrage[0].getInt(cursor_db_anfrage[0]
	// .getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_3)));
	// geopkt_geopunkt = geopkt_geopunkt
	// .setLongitude(cursor_db_anfrage[0].getInt(cursor_db_anfrage[0]
	// .getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_4)));
	// geopkt_geopunkt.string_icon = cursor_db_anfrage[0]
	// .getString(cursor_db_anfrage[0]
	// .getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_5));
	//
	// // erzeuge overlayitem (geopunkt mit zusaetzlichen daten) zum
	// // einfuegen in overlays
	// overlayitem_temp = new OverlayItem((GeoPoint) geopkt_geopunkt,
	// geopkt_geopunkt.string_name, "");
	//
	// itemoverlay_temp = hashmap_itemoverlays_temp
	// .get(geopkt_geopunkt.string_icon);
	// itemoverlay_temp.addOverlay(overlayitem_temp);
	//
	// if (isCancelled()) {
	// return cursor_db_anfrage[0].getPosition();
	// }
	//
	// if ((cursor_db_anfrage[0].getPosition() % int_prozent_temp) == 0) {
	//
	// publishProgress(PROGRESS_UPDATE,
	// cursor_db_anfrage[0].getPosition());
	// }
	//
	// } while (cursor_db_anfrage[0].moveToNext());
	//
	// publishProgress(PROGRESS_MAX);
	//
	// // ruft populate() fuer die overlays auf, um sie spaeter anzeigen
	// // zu koennen
	//
	// publishProgress(PROGRESS_MIN);
	// publishProgress(PROGRESS_SET_MAX, hashmap_itemoverlays_temp.size());
	// int_prozent_temp = berechneProzentSchritte(hashmap_itemoverlays_temp
	// .size());
	//
	// // initialisiert alle overlays -> abbruch moeglich
	// int int_zaehler = 1;
	// for (ItemOverlay itemoverlay_inner : hashmap_itemoverlays_temp
	// .values()) {
	//
	// if (isCancelled()) {
	// return 0;
	// }
	//
	// itemoverlay_inner.initialisieren();
	//
	// if ((int_zaehler % int_prozent_temp) == 0) {
	//
	// publishProgress(PROGRESS_UPDATE, int_zaehler);
	//
	// SystemClock.sleep(1000);
	// }
	// int_zaehler++;
	// }
	//
	// Iterator<ItemOverlay> iterator_itemoverlays = hashmap_itemoverlays_temp
	// .values().iterator();
	//
	// if (!boolean_filter) {
	// // speichert alle overlays im singleton -> kein abbruch moeglich
	// while (iterator_itemoverlays.hasNext()) {
	//
	// itemoverlay_temp = iterator_itemoverlays.next();
	//
	// if (itemoverlay_temp.size() > 0) {
	//
	// memosingleton_anwendung.arraylist_karte_overlays
	// .add(itemoverlay_temp);
	// }
	// }
	//
	// memosingleton_anwendung.aktualisiereDBZugriff(
	// MemoSingleton.KARTE, geopkt_geopunkt.long_id);
	// } else {
	//
	// while (iterator_itemoverlays.hasNext()) {
	//
	// itemoverlay_temp = iterator_itemoverlays.next();
	//
	// if (itemoverlay_temp.size() > 0) {
	//
	// memosingleton_anwendung.arraylist_karte_overlays_temp
	// .add(itemoverlay_temp);
	// }
	// }
	// }
	//
	// publishProgress(PROGRESS_MAX);
	// }
	//
	// return cursor_db_anfrage[0].getCount();
	//
	// }

	private int karteBearbeiten() {

		long long_max_id = 0;
		HashMap<Long, ItemOverlay> hashmap_itemoverlays_temp = new HashMap<Long, ItemOverlay>();

		for (Betreiber betreiber_anbieter : db_rw.leseBetreiber(0, true, true)) {

			if (isCancelled()) {
				return -1;
			}

			// erzeuge overlay mit zugeordnetem symbol
			// fuege das overlay in die temporaere hashmap ein
			hashmap_itemoverlays_temp.put(betreiber_anbieter.long_id,
					new ItemOverlay(betreiber_anbieter.drawable_logo,
							memosingleton_anwendung));

			// hashmap aus dem singleton speichert alle overlays mit den
			// entsprechenden geopunkten, temporaere hashmap speichert
			// nur neue geopunkte
		}

		publishProgress(PROGRESS_UPDATE, 25);

		// fuer alle erfassten neuen punkte
		for (Ladestation ladestation_saeule : db_rw.leseLadestation(0, false,
				true)) {

			// erzeuge overlayitem (geopunkt mit zusaetzlichen daten) zum
			// einfuegen in overlays

			hashmap_itemoverlays_temp.get(
					ladestation_saeule.betreiber_anbieter.long_id).addOverlay(
					new OverlayItem(ladestation_saeule.geopoint_standort,
							ladestation_saeule.string_bezeichnung, String
									.valueOf(ladestation_saeule.long_id)));

			if (long_max_id < ladestation_saeule.long_id) {

				long_max_id = ladestation_saeule.long_id;
			}

			// if (isCancelled()) {
			// return cursor_db_anfrage[0].getPosition();
			// }
			//
			// if ((cursor_db_anfrage[0].getPosition() % int_prozent_temp) == 0)
			// {
			//
			// publishProgress(PROGRESS_UPDATE,
			// cursor_db_anfrage[0].getPosition());
			// }
		}

		publishProgress(PROGRESS_UPDATE, 50);

		// ruft populate() fuer die overlays auf, um sie spaeter anzeigen
		// zu koennen

		// initialisiert alle overlays -> abbruch moeglich
		// int int_zaehler = 1;
		for (ItemOverlay itemoverlay_inner : hashmap_itemoverlays_temp.values()) {

			if (isCancelled()) {
				return 0;
			}

			itemoverlay_inner.initialisieren();

			// if ((int_zaehler % int_prozent_temp) == 0) {
			//
			// publishProgress(PROGRESS_UPDATE, int_zaehler);
			//
			// // SystemClock.sleep(1000);
			// }
			// int_zaehler++;
		}

		publishProgress(PROGRESS_UPDATE, 75);

		if (!boolean_filter) {
			// speichert alle overlays im singleton -> kein abbruch moeglich
			for (ItemOverlay itemoverlay_temp : hashmap_itemoverlays_temp
					.values()) {

				if (itemoverlay_temp.size() > 0) {

					memosingleton_anwendung.arraylist_karte_overlays
							.add(itemoverlay_temp);
				}
			}

			memosingleton_anwendung.aktualisiereDBZugriff(MemoSingleton.KARTE,
					long_max_id);
		} else {

			// while (iterator_itemoverlays.hasNext()) {
			//
			// itemoverlay_temp = iterator_itemoverlays.next();
			//
			// if (itemoverlay_temp.size() > 0) {
			//
			// memosingleton_anwendung.arraylist_karte_overlays_temp
			// .add(itemoverlay_temp);
			// }
			// }
		}

		// publishProgress(PROGRESS_MAX);

		return (int) long_max_id;
	}

	// /**
	// * Wird aufgerufen sobald der Thread innerhalb von {@code
	// doInBackground()}
	// * von außen abgebrochen wird und schließt den Fortschrittdsdialog.
	// */
	// @Override
	// protected void onCancelled() {
	//
	// try {
	//
	// progress_fortschritt.dismiss();
	// } catch (Exception e) {
	//
	// e.printStackTrace();
	// }
	//
	// Log.d("memo_debug_punktezeigen_tab_asynctask", "oncancelled");
	//
	// super.onCancelled();
	// }

	/**
	 * Wird nach der Verarbeitung in {@code doInBackground} aufgerufen und ruft
	 * je nach {@code int_modus} {@link PunkteZeigen_Tab_Liste} oder
	 * {@link PunkteZeigen_Tab_Karte} auf. Angezeigte Fortschrittdialoge werden
	 * beendet.
	 * 
	 * @see PunkteZeigen_Tab_Liste
	 * @see PunkteZeigen_Tab_Karte
	 */
	@Override
	protected void onPostExecute(Integer int_result) {

		Intent intent_nachricht = new Intent();
		intent_nachricht.putExtra("int_anzahl", int_result);

		switch (int_modus) {
		case LISTE:

			intent_nachricht.setAction(MemoSingleton.INTENT_ZEIGE_LISTE);
			break;
		case KARTE:

			intent_nachricht.setAction(MemoSingleton.INTENT_ZEIGE_KARTE);
			break;
		}

		db_rw.schliessen();

		memosingleton_anwendung.sendBroadcast(intent_nachricht);

		try {

			progress_fortschritt.setProgress(progress_fortschritt.getMax());
			progress_fortschritt.dismiss();
		} catch (Exception e) {

			e.printStackTrace();
		}

		Log.d("memo_debug_punktezeigen_tab_asynctask", "onpostexecute");
	}
}