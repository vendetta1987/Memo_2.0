package de.planetic.android.memo_neu;

import java.util.Iterator;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class PunkteZeigen_Tab_Karte extends MapActivity {

	private SQL_DB_Verwaltung sqldb_db_verwaltung;
	private BroadcastReceiver bcreceiver_receiver;
	private PunkteZeigen_Tab_AsyncTask asynctask_dbabfrage;
	private MemoSingleton memosingleton_anwendung;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.punktezeigen_karte_layout_neu);

		sqldb_db_verwaltung = new SQL_DB_Verwaltung(this);

		memosingleton_anwendung = (MemoSingleton) getApplication();

		MapView mapview_karte = (MapView) this
				.findViewById(R.id.punktezeigen_karte_layout_mapview_neu);
		mapview_karte.setBuiltInZoomControls(true);

		if (savedInstanceState == null) {
			dbAbfrageStarten();
		} else if (savedInstanceState.getBoolean("karte_asynctask")) {
			karteAnzeigen(memosingleton_anwendung.list_karte_overlays.size(),
					false);
		}
	}

	@Override
	public void onStart() {
		// registriert empfaenger fuer intents und definiert filter
		bcreceiver_receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				String string_intent_action = intent.getAction();

				if (string_intent_action
						.equals(MemoSingleton.INTENT_DB_FUELLEN)) {
					dbAbfrageStarten();
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_DB_LEEREN)) {
					karteOverlayLoeschen();
				}

			}
		};

		IntentFilter ifilter_filter = new IntentFilter();
		ifilter_filter.addAction(MemoSingleton.INTENT_DB_FUELLEN);
		ifilter_filter.addAction(MemoSingleton.INTENT_DB_LEEREN);

		this.registerReceiver(bcreceiver_receiver, ifilter_filter);

		super.onStart();
	}

	// loescht den nachrichtenempfänger beim beenden der activity
	@Override
	public void onStop() {

		this.unregisterReceiver(bcreceiver_receiver);
		super.onStop();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {

		if ((asynctask_dbabfrage != null)
				&& (asynctask_dbabfrage.getStatus() == Status.RUNNING)) {
			asynctask_dbabfrage.cancel(false);
			outState.putBoolean("karte_asynctask", true);
		}

		super.onSaveInstanceState(outState);
	}

	// stellt auf der karte angezeigte punkte nach drehung wiederher
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {

		karteAnzeigen(memosingleton_anwendung.list_karte_overlays.size(), false);

		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	// initialisiert die karte mit den neu hinzugekommenen punkten seit dem
	// letzten abruf
	private void dbAbfrageStarten() {

		SQLiteDatabase sqldb_zugriff = sqldb_db_verwaltung
				.getReadableDatabase();

		// select * from tabellenname where id>x order by id
		Cursor cursor_db_anfrage_geopkt = sqldb_zugriff.query(
				SQL_DB_Verwaltung.TABELLEN_NAME,
				null,
				SQL_DB_Verwaltung.NAME_SPALTE_1
						+ ">"
						+ Long.toString(memosingleton_anwendung
								.letzterDBZugriff(MemoSingleton.KARTE)), null,
				null, null, SQL_DB_Verwaltung.NAME_SPALTE_1);

		// select icon from tabellenname group by icon
		// erfasse symbole die den ausgelesenen punkten zugeordnet wurden
		Cursor cursor_db_anfrage_icon = sqldb_zugriff.query(
				SQL_DB_Verwaltung.TABELLEN_NAME,
				new String[] { SQL_DB_Verwaltung.NAME_SPALTE_5 }, null, null,
				SQL_DB_Verwaltung.NAME_SPALTE_5, null, null);

		asynctask_dbabfrage = new PunkteZeigen_Tab_AsyncTask(this,
				(MemoSingleton) getApplication(),
				PunkteZeigen_Tab_AsyncTask.KARTE);

		asynctask_dbabfrage.execute(cursor_db_anfrage_geopkt,
				cursor_db_anfrage_icon);
	}

	// public void karteAnzeigen(
	// HashMap<Integer, ItemOverlay_neu> hashmap_itemoverlays_temp) {
	public void karteAnzeigen(int int_anzahl_punkte, boolean bool_meldung) {

		// erfasse, zur zeit auf der karte angezeigte, overlays
		MapView mapview_karte = (MapView) this
				.findViewById(R.id.punktezeigen_karte_layout_mapview_neu);

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
		iterator_itemoverlays = memosingleton_anwendung.list_karte_overlays
				.iterator();

		while ((iterator_itemoverlays != null)
				&& iterator_itemoverlays.hasNext()) {
			list_karten_overlay.add(iterator_itemoverlays.next());
		}

		// zeichne die karte neu
		mapview_karte.invalidate();
	}

	// loescht die angezeigten punkte auf der karte
	private void karteOverlayLoeschen() {
		MapView mapview_karte = (MapView) this
				.findViewById(R.id.punktezeigen_karte_layout_mapview_neu);

		List<Overlay> list_karten_overlay = mapview_karte.getOverlays();

		list_karten_overlay.clear();

		mapview_karte.invalidate();
	}
}
