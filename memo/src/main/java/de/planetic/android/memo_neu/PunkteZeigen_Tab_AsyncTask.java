package de.planetic.android.memo_neu;

import java.util.HashMap;
import java.util.Iterator;

import android.app.ProgressDialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class PunkteZeigen_Tab_AsyncTask extends
		AsyncTask<Cursor, Integer, Integer> {

	private PunkteZeigen_Tab_Liste context_liste;
	private PunkteZeigen_Tab_Karte context_karte;
	private MemoSingleton memosingleton_anwendung;
	private ProgressDialog progress_fortschritt;
	private int int_modus;
	private int int_prozent_temp;

	public static final int LISTE = 0;
	public static final int KARTE = 1;

	private static final int PROGRESS_MIN = 0;
	private static final int PROGRESS_UPDATE = 1;
	private static final int PROGRESS_SET_MAX = 2;
	private static final int PROGRESS_MAX = 3;
	private static final int PROGRESS_PROZENT_SCHRITTE = 5;

	public PunkteZeigen_Tab_AsyncTask(Object con, MemoSingleton con_app,
			int int_mod) {
		int_modus = int_mod;

		memosingleton_anwendung = con_app;

		switch (int_modus) {
		case LISTE:
			context_liste = (PunkteZeigen_Tab_Liste) con;
			break;
		case KARTE:
			context_karte = (PunkteZeigen_Tab_Karte) con;
			break;
		default:
		}
	}

	@Override
	protected void onPreExecute() {

		switch (int_modus) {
		case LISTE:
			progress_fortschritt = new ProgressDialog((Context) context_liste);
			progress_fortschritt
					.setTitle(R.string.punktezeigen_tab_liste_asynctask_progressdialog_title);
			break;
		case KARTE:
			progress_fortschritt = new ProgressDialog((Context) context_karte);
			progress_fortschritt
					.setTitle(R.string.punktezeigen_tab_karte_asynctask_progressdialog_title);
			break;
		default:
		}

		progress_fortschritt.setCancelable(false);
		progress_fortschritt.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

		progress_fortschritt.show();
	}

	@Override
	protected void onProgressUpdate(Integer... int_progress) {

		try {
			switch (int_progress[0]) {
			case PROGRESS_UPDATE:
				if (progress_fortschritt.getMax() < 100) {
					progress_fortschritt.incrementProgressBy(1);
				} else {
					if ((int_progress[1] % int_prozent_temp) == 0) {
						progress_fortschritt.setProgress(int_progress[1]);
					}
				}
				break;
			case PROGRESS_MIN:
				progress_fortschritt.setProgress(0);
				break;
			case PROGRESS_MAX:
				progress_fortschritt.setProgress(progress_fortschritt.getMax());
				break;
			case PROGRESS_SET_MAX:
				progress_fortschritt.setMax(int_progress[1]);
				int_prozent_temp = (progress_fortschritt.getMax() / 100)
						* PROGRESS_PROZENT_SCHRITTE;
				break;
			default:
			}
		} catch (Exception e) {
			Log.d("memo_debug", "onProgressUpdate: " + e.toString());
		}
	}

	@Override
	protected Integer doInBackground(Cursor... cursor_db_anfrage) {

		switch (int_modus) {
		case LISTE:
			return listeBearbeiten(cursor_db_anfrage[0]);
		case KARTE:
			return karteBearbeiten(cursor_db_anfrage);
		default:
			return -1;
		}

	}

	private int listeBearbeiten(Cursor cursor_db_anfrage) {

		publishProgress(PROGRESS_SET_MAX, cursor_db_anfrage.getCount());

		if (cursor_db_anfrage.moveToFirst()) {

			GeoPunkt geopkt_geopunkt = new GeoPunkt();
			HashMap<String, Object> hashmap_liste_daten_datum;

			do {
				geopkt_geopunkt.long_id = cursor_db_anfrage.getLong(0);
				geopkt_geopunkt.string_name = cursor_db_anfrage.getString(1);
				geopkt_geopunkt = geopkt_geopunkt.setLatitude(cursor_db_anfrage
						.getInt(2));
				geopkt_geopunkt = geopkt_geopunkt
						.setLongitude(cursor_db_anfrage.getInt(3));
				geopkt_geopunkt.int_icon = cursor_db_anfrage.getInt(4);

				hashmap_liste_daten_datum = new HashMap<String, Object>(3);

				hashmap_liste_daten_datum.put("geopkt_name",
						geopkt_geopunkt.string_name);
				hashmap_liste_daten_datum.put(
						"geopkt_lat_lon",
						"Lat:"
								+ Integer.toString(geopkt_geopunkt
										.getLatitudeE6())
								+ " "
								+ "Lon:"
								+ Integer.toString(geopkt_geopunkt
										.getLongitudeE6()));
				hashmap_liste_daten_datum.put("geopkt_icon",
						geopkt_geopunkt.int_icon);

				memosingleton_anwendung.list_liste_daten
						.add(hashmap_liste_daten_datum);

				memosingleton_anwendung.aktualisiereDBZugriff(
						MemoSingleton.LISTE, geopkt_geopunkt.long_id);

				if (isCancelled()) {
					return cursor_db_anfrage.getPosition();
				}

				publishProgress(PROGRESS_UPDATE,
						cursor_db_anfrage.getPosition());

			} while (cursor_db_anfrage.moveToNext());

		}

		publishProgress(PROGRESS_MAX);

		return cursor_db_anfrage.getCount();
	}

	private int karteBearbeiten(Cursor... cursor_db_anfrage) {

		// cursor_db_anfrage[0] geopkt, cursor_db_anfrage[1] icon

		publishProgress(PROGRESS_SET_MAX, cursor_db_anfrage[0].getCount());

		// falls neue punkte vorhanden sind
		if (cursor_db_anfrage[0].moveToFirst()) {

			// temporaere hashmap fuer overlays (sammlung von punkten) die der
			// karte hinzugefuegt werden.
			HashMap<Integer, ItemOverlay_neu> hashmap_itemoverlays_temp = new HashMap<Integer, ItemOverlay_neu>();

			ItemOverlay_neu itemoverlay_temp;

			GeoPunkt geopkt_geopunkt = new GeoPunkt();

			OverlayItem overlayitem_temp;

			// fuer alle gefundenen symbole
			if (cursor_db_anfrage[1].moveToFirst()) {

				do {

					if (isCancelled()) {
						return cursor_db_anfrage[1].getPosition();
					}

					// erzeuge overlay mit zugeordnetem symbol
					itemoverlay_temp = new ItemOverlay_neu(
							memosingleton_anwendung.getResources().getDrawable(
									cursor_db_anfrage[1].getInt(0)),
							memosingleton_anwendung);

					// fuege das overlay in die temporaere hashmap ein
					hashmap_itemoverlays_temp.put(
							cursor_db_anfrage[1].getInt(0), itemoverlay_temp);

					// hashmap aus dem singleton speichert alle overlays mit den
					// entsprechenden geopunkten, temporaere hashmap speichert
					// nur neue geopunkte
				} while (cursor_db_anfrage[1].moveToNext());
			}

			// fuer alle erfassten neuen punkte
			do {
				geopkt_geopunkt.long_id = cursor_db_anfrage[0].getLong(0);
				geopkt_geopunkt.string_name = cursor_db_anfrage[0].getString(1);
				geopkt_geopunkt = geopkt_geopunkt
						.setLatitude(cursor_db_anfrage[0].getInt(2));
				geopkt_geopunkt = geopkt_geopunkt
						.setLongitude(cursor_db_anfrage[0].getInt(3));
				geopkt_geopunkt.int_icon = cursor_db_anfrage[0].getInt(4);

				// erzeuge overlayitem (geopunkt mit zusaetzlichen daten) zum
				// einfuegen in overlays
				overlayitem_temp = new OverlayItem((GeoPoint) geopkt_geopunkt,
						geopkt_geopunkt.string_name, "");

				itemoverlay_temp = hashmap_itemoverlays_temp
						.get(geopkt_geopunkt.int_icon);
				itemoverlay_temp.addOverlay(overlayitem_temp);

				if (isCancelled()) {
					return cursor_db_anfrage[0].getPosition();
				}

				publishProgress(PROGRESS_UPDATE,
						cursor_db_anfrage[0].getPosition());

			} while (cursor_db_anfrage[0].moveToNext());

			publishProgress(PROGRESS_MAX);

			// ruft populate() fuer die overlays auf, um sie spaeter anzeigen
			// zu koennen
			Iterator<ItemOverlay_neu> iterator_itemoverlays = hashmap_itemoverlays_temp
					.values().iterator();

			publishProgress(PROGRESS_MIN);
			publishProgress(PROGRESS_SET_MAX, hashmap_itemoverlays_temp.size());

			// initialisiert alle overlays -> abbruch möglich
			while (iterator_itemoverlays.hasNext()) {

				if (isCancelled()) {
					return 0;
				}

				itemoverlay_temp = iterator_itemoverlays.next();

				itemoverlay_temp.initialisieren();

				publishProgress(PROGRESS_UPDATE, 1);
			}

			iterator_itemoverlays = hashmap_itemoverlays_temp.values()
					.iterator();

			// speichert alle overlays im singleton -> kein abbruch möglich
			while (iterator_itemoverlays.hasNext()) {
				memosingleton_anwendung.list_karte_overlays
						.add(iterator_itemoverlays.next());
			}

			memosingleton_anwendung.aktualisiereDBZugriff(MemoSingleton.KARTE,
					geopkt_geopunkt.long_id);

			publishProgress(PROGRESS_MAX);
		}

		return cursor_db_anfrage[0].getCount();

	}

	@Override
	protected void onPostExecute(Integer int_result) {
		switch (int_modus) {
		case LISTE:
			context_liste.listeAnzeigen(int_result, true);
			break;
		case KARTE:
			context_karte.karteAnzeigen(int_result, true);
			break;
		default:
		}

		progress_fortschritt.setProgress(progress_fortschritt.getMax());
		progress_fortschritt.dismiss();
	}
}