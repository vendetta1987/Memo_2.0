package de.planetic.android.memo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

/**
 * {@link Activity} zur Anzeige der gespeicherten {@link GeoPunkt} in einer
 * Kartenansicht. Zeigt Punkte in {@link ItemOverlay} an und stellt Route und
 * die aktuelle Position des Nutzers dar. Zusätzlich werden
 * Navigationsanweisungen per {@link TextToSpeech} ausgegeben.
 * 
 * @see PunkteZeigen_Tab
 * @see PunkteZeigen_Tab_AsyncTask
 * @see MemoSingleton
 */
public class PunkteZeigen_Tab_Karte extends MapActivity implements
		OnInitListener {

	private BroadcastReceiver bcreceiver_receiver;
	private PunkteZeigen_Tab_AsyncTask asynctask_dbabfrage;
	private MemoSingleton memosingleton_anwendung;
	private TextToSpeech texttospeech_sprache;
	private boolean boolean_tts_aktiv, boolean_google_lizenz_beachten;

	private static final int REQUEST_CODE_TTS = 78911191;

	/**
	 * Initialisiert die Anwendung und stellt Zustand nach Drehung des Gerätes
	 * wiederher.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.punktezeigen_karte_layout);

		memosingleton_anwendung = (MemoSingleton) getApplication();

		MapView mapview_karte = (MapView) this
				.findViewById(R.id.punktezeigen_karte_layout_mapview);
		mapview_karte.setBuiltInZoomControls(true);

		memosingleton_anwendung.projection_karte = mapview_karte
				.getProjection();

		boolean_tts_aktiv = false;

		SharedPreferences sp_einstellungen = Memo_Einstellungen
				.leseEinstellungen(this);

		switch (Integer
				.decode(sp_einstellungen
						.getString(
								getResources()
										.getString(
												R.string.memo_einstellungen_gps_kartenansicht_schluessel),
								"-1"))) {
		case -1:
			mapview_karte.setSatellite(false);
			break;
		case 1:
			mapview_karte.setSatellite(true);
			break;
		}

		boolean_google_lizenz_beachten = sp_einstellungen.getBoolean(
				"boolean_google_lizenz_beachten", true);
		
		mapview_karte
				.setTraffic(sp_einstellungen
						.getBoolean(
								getResources()
										.getString(
												R.string.memo_einstellungen_gps_verkehrslage_schluessel),
								false));

		if (savedInstanceState == null) {
			dbAbfrageStarten(new Intent());
		} else if (savedInstanceState.getBoolean("karte_asynctask")) {
			karteAnzeigen(
					memosingleton_anwendung.arraylist_karte_overlays.size(),
					false, false);
		}
	}

	/**
	 * Registriert {@link BroadcastReceiver} und {@link IntentFilter}.
	 */
	@Override
	public void onStart() {
		// registriert empfaenger fuer intents und definiert filter
		bcreceiver_receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				String string_intent_action = intent.getAction();

				if (string_intent_action
						.equals(MemoSingleton.INTENT_DB_FUELLEN)) {
					dbAbfrageStarten(new Intent());
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_DB_LEEREN)) {
					karteOverlayLoeschen();
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_ZOOME_KARTE)) {
					zoomeKarte(
							new GeoPunkt(intent.getIntExtra(getPackageName()
									+ "_" + "int_lat", 0), intent.getIntExtra(
									getPackageName() + "_" + "int_lon", 0)),
							intent.getIntExtra(getPackageName() + "_"
									+ "int_latspan", -1), intent.getIntExtra(
									getPackageName() + "_" + "int_lonspan", -1));
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_PUNKTE_FILTERN)) {
					dbAbfrageStarten(intent);
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_KARTE_VERFOLGE_AKTUELLE_POS)) {
					verfolgeAktuellePosition(intent);
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_HIERHIN_NAVIGIEREN)) {
					hierhinNavigieren(intent);
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_STARTE_TTS)) {
					starteTTS();
					// }else if (string_intent_action
					// .equals(MemoSingleton.INTENT_TTS_SAGE)) {
					// sageTTS(intent);
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_STOPPE_TTS)) {
					stoppeTTS();
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_KARTE_NAVIGATIONSANWEISUNG)) {
					navigationAnweisung(intent);
				}

			}
		};

		IntentFilter ifilter_filter = new IntentFilter();
		ifilter_filter.addAction(MemoSingleton.INTENT_DB_FUELLEN);
		ifilter_filter.addAction(MemoSingleton.INTENT_DB_LEEREN);
		ifilter_filter.addAction(MemoSingleton.INTENT_ZOOME_KARTE);
		ifilter_filter.addAction(MemoSingleton.INTENT_PUNKTE_FILTERN);
		ifilter_filter
				.addAction(MemoSingleton.INTENT_KARTE_VERFOLGE_AKTUELLE_POS);
		ifilter_filter.addAction(MemoSingleton.INTENT_HIERHIN_NAVIGIEREN);
		ifilter_filter.addAction(MemoSingleton.INTENT_STARTE_TTS);
		ifilter_filter.addAction(MemoSingleton.INTENT_STOPPE_TTS);
		ifilter_filter
				.addAction(MemoSingleton.INTENT_KARTE_NAVIGATIONSANWEISUNG);

		this.registerReceiver(bcreceiver_receiver, ifilter_filter);

		Log.d("memo_debug_punktezeigen_tab_karte", "onstart");

		super.onStart();
	}

	/**
	 * Löscht die registrierten {@link BroadcastReceiver} beim Beenden der
	 * {@link Activity}.
	 */
	@Override
	public void onStop() {
		// loescht den nachrichtenempfaenger beim beenden der activity

		this.unregisterReceiver(bcreceiver_receiver);

		Log.d("memo_debug_punktezeigen_tab_karte", "onstop");

		super.onStop();
	}

	/**
	 * Startet GPS zur Verfolgung der aktuellen Position nach Wiederherstellung
	 * der Anwendung, falls nötig.
	 * 
	 * @see GPS_Verwaltung
	 */
	@Override
	public void onResume() {

		if (memosingleton_anwendung.boolean_aktuelle_position) {

			Intent intent_befehl = new Intent(MemoSingleton.INTENT_STARTE_GPS);
			intent_befehl.putExtra(getPackageName() + "_" + "int_listener",
					MemoSingleton.GPS_LISTENER_AKTUELL);
			sendBroadcast(intent_befehl);
		}

		Log.d("memo_debug_punktezeigen_tab_karte", "onresume");

		super.onResume();
	}

	/**
	 * Stoppt GPS zur Verfolgung der aktuellen Position beim Beenden der
	 * Anwendung, falls nötig.
	 * 
	 * @see GPS_Verwaltung
	 */
	@Override
	public void onPause() {

		if (memosingleton_anwendung.boolean_aktuelle_position) {

			Intent intent_befehl = new Intent(MemoSingleton.INTENT_STOPPE_GPS);
			intent_befehl.putExtra(getPackageName() + "_" + "int_listener",
					MemoSingleton.GPS_LISTENER_AKTUELL);
			sendBroadcast(intent_befehl);
		}

		Log.d("memo_debug_punktezeigen_tab_karte", "onpause");

		super.onPause();
	}

	/**
	 * Verwaltet Hintergrundthreads bei Drehung des Gerätes.
	 * 
	 * @see PunkteZeigen_Tab_AsyncTask
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {

		if ((asynctask_dbabfrage != null)
				&& (asynctask_dbabfrage.getStatus() == Status.RUNNING)) {

			asynctask_dbabfrage.cancel(false);
			outState.putBoolean("karte_asynctask", true);
		}

		outState.putBoolean("boolean_navigation_button",
				memosingleton_anwendung.boolean_navigieren);

		outState.putBoolean("boolean_tts_aktiv", boolean_tts_aktiv);
		stoppeTTS();

		Log.d("memo_debug_punktezeigen_tab_karte", "onsaveinstancestate");

		super.onSaveInstanceState(outState);
	}

	/**
	 * Stellt Oberfläche nach Drehung des Gerätes wiederher.
	 */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		// stellt auf der karte angezeigte punkte nach drehung wiederher

		karteAnzeigen(
				0,
				false,
				(memosingleton_anwendung.boolean_gefiltert || memosingleton_anwendung.boolean_navigieren));

		if (savedInstanceState.getBoolean("boolean_navigation_button")) {

			navigationButtonsVerwalten(true);
		}

		if (savedInstanceState.getBoolean("boolean_tts_aktiv")) {

			starteTTS();
		}

		Log.d("memo_debug_punktezeigen_tab_karte", "onrestoreinstancestate");

		super.onRestoreInstanceState(savedInstanceState);
	}

	/**
	 * Beendet die Navigation beim Druck auf den Knopf "Zurück".
	 */
	@Override
	public void onBackPressed() {

		Log.d("memo_debug_punktezeigen_tab_karte", "onbackpressed");

		if (memosingleton_anwendung.boolean_navigieren) {

			navigationBeenden(null);
		} else {

			super.onBackPressed();
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/**
	 * {@code private void dbAbfrageStarten(Intent intent_befehl)}
	 * <p/>
	 * Startet eine DB-Abfrage nach den Vorgaben in {@code intent_befehl} und
	 * reicht die Ergebnisse an {@link PunkteZeigen_Tab_AsyncTask} zur
	 * Verarbeitung weiter. Wird für die normale und gefilterte Darstellung
	 * genutzt.
	 * 
	 * @param intent_befehl
	 *            {@link Intent} mit den Daten für die DB-Anfrage.
	 */
	private void dbAbfrageStarten(Intent intent_befehl) {
		// initialisiert die karte mit den neu hinzugekommenen punkten seit dem
		// letzten abruf

		String string_adresse, string_name, string_groesserkleiner, string_id;
		boolean boolean_filter;

		boolean_filter = intent_befehl.getBooleanExtra(getPackageName() + "_"
				+ "boolean_filter", false);

		if (!boolean_filter) {
			string_id = SQL_DB_Verwaltung.NAME_SPALTE_1
					+ ">"
					+ Long.toString(memosingleton_anwendung
							.letzterDBZugriff(MemoSingleton.KARTE));
		} else {
			string_id = SQL_DB_Verwaltung.NAME_SPALTE_1 + ">-1";
		}

		if (intent_befehl.getStringExtra(getPackageName() + "_"
				+ "string_adresse") != null) {
			string_adresse = " AND "
					+ SQL_DB_Verwaltung.NAME_SPALTE_10
					+ " LIKE '%"
					+ intent_befehl.getStringExtra(getPackageName() + "_"
							+ "string_adresse") + "%'";
		} else {
			string_adresse = "";
		}

		if (intent_befehl
				.getStringExtra(getPackageName() + "_" + "string_name") != null) {
			string_name = " AND "
					+ SQL_DB_Verwaltung.NAME_SPALTE_2
					+ " LIKE '%"
					+ intent_befehl.getStringExtra(getPackageName() + "_"
							+ "string_name") + "%'";
		} else {
			string_name = "";
		}

		if (intent_befehl.getStringExtra(getPackageName() + "_"
				+ "string_groesserkleiner") != null) {
			string_groesserkleiner = " AND "
					+ SQL_DB_Verwaltung.NAME_SPALTE_9
					+ intent_befehl.getStringExtra(getPackageName() + "_"
							+ "string_groesserkleiner")
					+ intent_befehl.getDoubleExtra(getPackageName() + "_"
							+ "double_preis", 0);
		} else {
			string_groesserkleiner = "";
		}

		// select ... from tabellenname where id>x AND ... order by id
		Cursor cursor_db_anfrage_geopkt = memosingleton_anwendung.sqldatabase_readable
				.query(SQL_DB_Verwaltung.TABELLEN_NAME_HAUPT, new String[] {
						SQL_DB_Verwaltung.NAME_SPALTE_1,
						SQL_DB_Verwaltung.NAME_SPALTE_2,
						SQL_DB_Verwaltung.NAME_SPALTE_3,
						SQL_DB_Verwaltung.NAME_SPALTE_4,
						SQL_DB_Verwaltung.NAME_SPALTE_5 },
						string_id + string_adresse + string_groesserkleiner
								+ string_name, null, null, null,
						SQL_DB_Verwaltung.NAME_SPALTE_1);

		// select icon from tabellenname group by icon
		// erfasse symbole die den ausgelesenen punkten zugeordnet wurden
		Cursor cursor_db_anfrage_icon = memosingleton_anwendung.sqldatabase_readable
				.query(SQL_DB_Verwaltung.TABELLEN_NAME_HAUPT,
						new String[] { SQL_DB_Verwaltung.NAME_SPALTE_5 }, null,
						null, SQL_DB_Verwaltung.NAME_SPALTE_5, null, null);

		asynctask_dbabfrage = new PunkteZeigen_Tab_AsyncTask(this,
				PunkteZeigen_Tab_AsyncTask.KARTE, boolean_filter);

		asynctask_dbabfrage.execute(cursor_db_anfrage_geopkt,
				cursor_db_anfrage_icon);

		Log.d("memo_debug_punktezeigen_tab_karte", "dbabfragestarten");
	}

	/**
	 * {@code public void karteAnzeigen(int int_anzahl_punkte, boolean bool_meldung,
			boolean boolean_filter)}
	 * <p/>
	 * Zeigt die verarbeiteten Daten aus {@link MemoSingleton} auf der Karte an.
	 * 
	 * @param int_anzahl_punkte
	 *            Anzahl der angezeigten Punkte zur Ausgabe per {@link Toast}
	 * @param bool_meldung
	 *            {@link Boolean} zur Steuerung der {@link Toast}-Ausgabe
	 * @param boolean_filter
	 *            {@link Boolean} zur Signalisierung für gefilterter Inhalte
	 * 
	 * @see MemoSingleton
	 */
	public void karteAnzeigen(int int_anzahl_punkte, boolean bool_meldung,
			boolean boolean_filter) {
		// public void karteAnzeigen(
		// HashMap<Integer, ItemOverlay> hashmap_itemoverlays_temp) {

		// erfasse, zur zeit auf der karte angezeigte, overlays
		MapView mapview_karte = (MapView) this
				.findViewById(R.id.punktezeigen_karte_layout_mapview);

		if (bool_meldung) {
			Toast.makeText(
					this,
					Integer.toString(int_anzahl_punkte)
							+ " "
							+ getResources()
									.getString(
											R.string.punktezeigen_tab_karte_karteanzeigen),
					Toast.LENGTH_SHORT).show();
		}

		List<Overlay> list_karten_overlay = mapview_karte.getOverlays();

		list_karten_overlay.clear();

		Iterator<Overlay> iterator_itemoverlays;

		if (!boolean_filter) {

			iterator_itemoverlays = memosingleton_anwendung.arraylist_karte_overlays
					.iterator();
		} else {

			iterator_itemoverlays = memosingleton_anwendung.arraylist_karte_overlays_temp
					.iterator();
		}

		while ((iterator_itemoverlays != null)
				&& iterator_itemoverlays.hasNext()) {
			list_karten_overlay.add(iterator_itemoverlays.next());
		}

		// zeichne die karte neu
		mapview_karte.invalidate();

		Log.d("memo_debug_punktezeigen_tab_karte", "karteanzeigen");
	}

	/**
	 * {@code private void karteOverlayLoeschen()}
	 * <p/>
	 * Löscht die, auf der Karte engezeigten, Punkte.
	 */
	private void karteOverlayLoeschen() {
		// loescht die angezeigten punkte auf der karte

		MapView mapview_karte = (MapView) this
				.findViewById(R.id.punktezeigen_karte_layout_mapview);

		List<Overlay> list_karten_overlay = mapview_karte.getOverlays();

		list_karten_overlay.clear();

		mapview_karte.invalidate();
	}

	/**
	 * {@code private void verfolgeAktuellePosition(Intent intent_befehl)}
	 * <p/>
	 * Zeigt die aktuelle Position als {@link ItemOverlay} auf der Karte an und
	 * empfängt Positionsdaten per {@link Intent} von {@link GPS_Verwaltung}.
	 * 
	 * @param intent_befehl
	 *            {@link Intent} mit der aktuellen Position
	 */
	private void verfolgeAktuellePosition(Intent intent_befehl) {

		MapView mapview_karte = (MapView) findViewById(R.id.punktezeigen_karte_layout_mapview);

		List<Overlay> list_overlays = mapview_karte.getOverlays();

		if (!intent_befehl.getBooleanExtra(getPackageName() + "_"
				+ "boolean_aktivieren", false)) {

			list_overlays
					.remove(memosingleton_anwendung.itemoverlay_aktuelle_position);
		} else {

			if (!list_overlays
					.contains(memosingleton_anwendung.itemoverlay_aktuelle_position)) {

				list_overlays
						.add(memosingleton_anwendung.itemoverlay_aktuelle_position);
			}

			GeoPunkt geopunkt_aktuell = new GeoPunkt(intent_befehl.getIntExtra(
					getPackageName() + "_" + "int_lat", 0),
					intent_befehl.getIntExtra(getPackageName() + "_"
							+ "int_lon", 0));

			memosingleton_anwendung.itemoverlay_aktuelle_position
					.clearOverlay();
			memosingleton_anwendung.itemoverlay_aktuelle_position
					.addOverlay(new OverlayItem(geopunkt_aktuell, "", ""));
			memosingleton_anwendung.itemoverlay_aktuelle_position
					.initialisieren();

			zoomeKarte(geopunkt_aktuell, -1, -1);
		}

		mapview_karte.invalidate();

		Log.d("memo_debug_punktezeigen_tab_karte", "verfolgeaktuelleposition");
	}

	/**
	 * {@code private void zoomeKarte(GeoPunkt geopunkt_zentrum, int int_spanlat,
			int int_spanlon)}
	 * <p/>
	 * Zoomt die Karte auf eine vorgegebene Größe und zentriert sie über einem
	 * {@link GeoPunkt}, falls {@code int_spanlat} und {@code int_spanlon} >= 0
	 * sind.
	 * 
	 * @param geopunkt_zentrum
	 *            {@link GeoPunkt} über dem die Karte zentriert wird.
	 * @param int_spanlat
	 *            {@link Integer} mit der geografischen Breite des abzudeckenden
	 *            Bereiches
	 * @param int_spanlon
	 *            {@link Integer} mit der geografischen Länge des abzudeckenden
	 *            Bereiches
	 */
	private void zoomeKarte(GeoPunkt geopunkt_zentrum, int int_spanlat,
			int int_spanlon) {

		MapView mapview_karte = (MapView) findViewById(R.id.punktezeigen_karte_layout_mapview);
		MapController mapcontroller_karte = mapview_karte.getController();
		mapcontroller_karte.animateTo(geopunkt_zentrum);

		if ((int_spanlat >= 0) && (int_spanlon >= 0)) {

			mapcontroller_karte.zoomToSpan(int_spanlat, int_spanlon);
		}

		mapview_karte.invalidate();

		Log.d("memo_debug_punktezeigen_tab_karte", "zoomekarte");
	}

	/**
	 * {@code private void hierhinNavigieren(Intent intent_befehl)}
	 * <p/>
	 * Aufgerufen von {@link Navigation_AsyncTask} zur Anzeige der
	 * Routenübersicht.
	 * 
	 * @param intent_befehl
	 *            {@link Intent} mit den Angaben zur Anzeige des
	 *            Kartenausschnittes
	 */
	private void hierhinNavigieren(Intent intent_befehl) {
		// "zentrum_lat""zentrum_lon""dif_lat""dif_lon""string_status"

		String string_status, string_meldung = new String();

		if (intent_befehl.hasExtra(getPackageName() + "_" + "string_status")) {

			string_status = intent_befehl.getStringExtra(getPackageName() + "_"
					+ "string_status");

			if (string_status.equalsIgnoreCase("OK")) {

				memosingleton_anwendung.boolean_navigieren = true;
				karteAnzeigen(0, false, true);

				zoomeKarte(
						new GeoPunkt(intent_befehl.getIntExtra(getPackageName()
								+ "_" + "int_zentrum_lat", 0),
								intent_befehl.getIntExtra(getPackageName()
										+ "_" + "int_zentrum_lon", 0)),
						intent_befehl.getIntExtra(getPackageName() + "_"
								+ "int_span_lat", -1),
						intent_befehl.getIntExtra(getPackageName() + "_"
								+ "int_span_lon", -1));

				navigationButtonsVerwalten(true);

				string_meldung = intent_befehl.getStringExtra(getPackageName()
						+ "_" + "string_urheberrecht");
			} else {

				string_meldung = getResources().getString(R.string.fehler_text)
						+ ": " + string_status;

				navigationBeenden(null);
			}
		} else {

			string_meldung = getResources().getString(R.string.fehler_text)
					+ ": "
					+ intent_befehl.getStringExtra(getPackageName() + "_"
							+ "fehler");

			navigationBeenden(null);
		}

		Toast.makeText(this, string_meldung, Toast.LENGTH_SHORT).show();

		Log.d("memo_debug_punktezeigen_tab_karte", "hierhinnavigieren");
	}

	/**
	 * {@code private void navigationButtonsVerwalten(boolean boolean_aktivieren)}
	 * <p/>
	 * Verwaltet die Anzeige der beiden, während der Naviagtion angezeigten,
	 * Knöpfe. Die Funktion aktualisiert auch den Text und
	 * {@link OnClickListener} des Knopfes für die Positionsverfolgung.
	 * 
	 * @param boolean_aktivieren
	 *            {@link Boolean} zur Steuerung der Funktion
	 *            <ul>
	 *            <li>{@code true} zum Aktivieren</li><li>{@code false} zum
	 *            Deaktivieren</li>
	 *            </ul>
	 */
	private void navigationButtonsVerwalten(boolean boolean_aktivieren) {

		Button button_aktuelle_position = (Button) findViewById(R.id.punktezeigen_karte_layout_button2);

		if (boolean_aktivieren) {

			// Navigation beenden
			findViewById(R.id.punktezeigen_karte_layout_button1).setVisibility(
					View.VISIBLE);

			if (memosingleton_anwendung.gps_verwaltung.gpsVerfuegbar(false)) {

				// Position verfolgen
				button_aktuelle_position.setVisibility(View.VISIBLE);
			}
		} else {

			findViewById(R.id.punktezeigen_karte_layout_button1).setVisibility(
					View.GONE);

			button_aktuelle_position.setVisibility(View.GONE);
		}

		if (memosingleton_anwendung.boolean_aktuelle_position) {

			button_aktuelle_position
					.setText(R.string.navigation_text_aktuelle_position_aus);
		} else {

			button_aktuelle_position
					.setText(R.string.navigation_text_aktuelle_position_an);
		}

		button_aktuelle_position.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {

				Intent intent_befehl = new Intent();

				if (memosingleton_anwendung.gps_verwaltung.gpsVerfuegbar(false)) {

					if (!memosingleton_anwendung.boolean_aktuelle_position) {

						intent_befehl
								.setAction(MemoSingleton.INTENT_STARTE_GPS);
						((Button) v)
								.setText(R.string.navigation_text_aktuelle_position_aus);
					} else {

						intent_befehl
								.setAction(MemoSingleton.INTENT_STOPPE_GPS);
						((Button) v)
								.setText(R.string.navigation_text_aktuelle_position_an);
					}

					intent_befehl.putExtra(getPackageName() + "_"
							+ "int_listener",
							MemoSingleton.GPS_LISTENER_AKTUELL);
					sendBroadcast(intent_befehl);
				}
			}
		});

	}

	/**
	 * {@code public void navigationBeenden(View view)}
	 * <p/>
	 * Beendet die Navigation regulär oder im Fehlerfall. Knöpfe werden
	 * ausgeblendet, GPS und {@link TextToSpeech} abgeschaltet und die
	 * Naviagtionsanweisungen gelöscht.
	 * 
	 * @see MemoSingleton
	 */
	public void navigationBeenden(View view) {
		// TODO eventuell vom dialog intent hierher -> starte navigation,
		// anstelle von direktem aufruf von asynctask?

		navigationButtonsVerwalten(false);

		Intent intent_befehl = new Intent(MemoSingleton.INTENT_STOPPE_GPS);

		if (memosingleton_anwendung.boolean_aktuelle_position) {

			intent_befehl.putExtra(getPackageName() + "_" + "int_listener",
					MemoSingleton.GPS_LISTENER_AKTUELL);

			sendBroadcast(intent_befehl);
		}

		intent_befehl.putExtra(getPackageName() + "_" + "int_listener",
				MemoSingleton.GPS_LISTENER_NAVIGATION);
		sendBroadcast(intent_befehl);

		stoppeTTS();

		memosingleton_anwendung.hashmap_karte_navigationsanweisungen = null;
		memosingleton_anwendung.boolean_navigieren = false;

		dbAbfrageStarten(new Intent());

		Log.d("memo_debug_punktezeigen_tab_karte", "navigationbeenden");
	}

	/**
	 * Initialisiert {@link TextToSpeech} und prüft Verfügbarkeit.
	 */
	private void starteTTS() {

		// verfuegbarkeit pruefen und starten
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, REQUEST_CODE_TTS);

		Log.d("memo_debug_punktezeigen_tab_karte", "startetts");
	}

	/**
	 * Wird aufgerufen sobald {@link Activity} aus {@code starteTTS} zurückkehrt
	 * und wertet den Rückgabewert aus. Startet entweder {@link TextToSpeech}
	 * oder installiert es aus dem Markt.
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if ((requestCode == REQUEST_CODE_TTS)
				&& (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS)) {

			texttospeech_sprache = new TextToSpeech(this.getParent(), this);
		} else {

			// tts nicht vorhanden, installieren
			startActivity(new Intent(
					TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA));
		}

	}

	/**
	 * Wird nach dem Start von {@link TextToSpeech} aufgerufen und prüft, ob
	 * dieser erfolgreich war und konfiguriert anschließend die verwendete
	 * Sprache.
	 */
	public void onInit(int status) {

		String string_fehler = new String();

		if (status == TextToSpeech.SUCCESS) {

			int int_ergebnis = texttospeech_sprache.setLanguage(Locale.GERMANY);

			if (!(int_ergebnis == TextToSpeech.LANG_MISSING_DATA)
					&& !(int_ergebnis == TextToSpeech.LANG_NOT_SUPPORTED)) {

				boolean_tts_aktiv = true;
			} else {

				string_fehler = getResources().getString(
						R.string.punktezeigen_tab_karte_tts_sprache_nicht);
			}
		} else {

			string_fehler = getResources().getString(
					R.string.punktezeigen_tab_karte_tts_nicht_aktiviert);
		}

		if (!boolean_tts_aktiv) {

			Toast.makeText(
					this,
					getResources().getString(R.string.fehler_text) + ": "
							+ string_fehler, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * {@code private void sageTTS(String string_text)}
	 * <p/>
	 * Gibt den übergebenen Text per {@link TextToSpeech} aus.
	 * 
	 * @param string_text
	 *            {@link String} mit dem auszugebenden Text
	 */
	private void sageTTS(String string_text) {

		if (boolean_tts_aktiv && boolean_google_lizenz_beachten) {

			texttospeech_sprache.speak(string_text, TextToSpeech.QUEUE_ADD,
					null);
		}

		Log.d("memo_debug_punktezeigen_tab_karte", "sagetts");
	}

	/**
	 * Stoppt {@link TextToSpeech}.
	 */
	private void stoppeTTS() {
		// TODO TTS aktivieren und deaktivieren beim beenden/drehen

		if (texttospeech_sprache != null) {

			texttospeech_sprache.shutdown();
		}

		boolean_tts_aktiv = false;

		Log.d("memo_debug_punktezeigen_tab_karte", "stoppetts");
	}

	/**
	 * {@code private void navigationAnweisung(Intent intent_befehl)}
	 * <p/>
	 * Aufgerufen durch {@link GPS_Verwaltung} zur Ausgabe der
	 * Navigationsanweisungen. Liest Anweisungen aus der {@link HashMap} in
	 * {@link MemoSingleton} und prüft die Entfernung zur aktuellen Position.
	 * Die Ausgabe erfolgt durch {@link TextToSpeech}.
	 * 
	 * @param intent_befehl
	 *            {@link Intent} mit den Angaben zur aktuellen Position
	 * @see MemoSingleton
	 */
	private void navigationAnweisung(Intent intent_befehl) {

		Iterator<HashMap<String, String>> iterator_anweisungen;
		HashMap<String, String> hashmap_temp = new HashMap<String, String>();
		float[] float_entfernung = new float[3];
		String string_temp = new String(), string_schluessel;
		int int_lat, int_lon, int_lat_temp, int_lon_temp;
		double double_distanz;

		if (memosingleton_anwendung.hashmap_karte_navigationsanweisungen != null) {

			int_lat = ((Double) (intent_befehl.getDoubleExtra(getPackageName()
					+ "_" + "double_lat", 0.0) * 1e6)).intValue();
			int_lon = ((Double) (intent_befehl.getDoubleExtra(getPackageName()
					+ "_" + "double_lon", 0.0) * 1e6)).intValue();

			int_lat_temp = int_lat / 1000000;
			int_lon_temp = int_lon / 1000000;

			string_schluessel = "G_"
					+ String.valueOf(int_lat_temp)
					+ "_"
					+ String.valueOf(int_lon_temp)
					+ "_M_"
					+ String.valueOf((int_lat - (int_lat_temp * 1000000)) / 10000)
					+ "_"
					+ String.valueOf((int_lon - (int_lon_temp * 1000000)) / 10000);

			if (memosingleton_anwendung.hashmap_karte_navigationsanweisungen
					.containsKey(string_schluessel)) {

				iterator_anweisungen = memosingleton_anwendung.hashmap_karte_navigationsanweisungen
						.get(string_schluessel).iterator();

				while (iterator_anweisungen.hasNext()) {
					// lat, lon, distanz, html

					hashmap_temp = iterator_anweisungen.next();

					Location.distanceBetween(
							intent_befehl.getDoubleExtra(getPackageName() + "_"
									+ "double_lat", 0.0),
							intent_befehl.getDoubleExtra(getPackageName() + "_"
									+ "double_lon", 0.0),
							Double.parseDouble(hashmap_temp.get("string_lat")),
							Double.parseDouble(hashmap_temp.get("string_lon")),
							float_entfernung);

					if (float_entfernung[0] < 150) {

						string_temp = hashmap_temp.get("string_html") + " ";

						double_distanz = Double.parseDouble(hashmap_temp
								.get("string_distanz"));

						if (double_distanz > 1000.0) {

							string_temp = string_temp
									+ String.valueOf(double_distanz / 1000.0)
									+ " "
									+ getResources().getString(
											R.string.navigation_text_kilometer);
						} else {

							string_temp = string_temp
									+ String.valueOf(double_distanz)
									+ " "
									+ getResources().getString(
											R.string.navigation_text_meter);
						}

						sageTTS(string_temp);

						Log.d("memo_debug", string_temp);

						iterator_anweisungen.remove();
					}
				}
			}
		}

		Log.d("memo_debug_punktezeigen_tab_karte", "navigationanweisung");
	}
}
