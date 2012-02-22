package de.planetic.android.memo;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import de.planetic.android.memo.db.ListeAsyncTaskLoader;

/**
 * {@link Activity} zur Anzeige der hinterlegten Punkte in einer Liste.
 */
public class PunkteZeigen_Tab_Liste extends FragmentActivity implements
		LoaderCallbacks<ArrayList<HashMap<String, String>>> {

	public static final String GEOPUNKT_NAME = "geopunkt_name";
	public static final String GEOPUNKT_LAT_LON = "geopunkt_lat_lon";
	public static final String GEOPUNKT_ICON = "geopunkt_icon";

	private BroadcastReceiver bcreceiver_receiver;
	private PunkteZeigen_Tab_AsyncTask asynctask_dbabfrage;
	private MemoSingleton memosingleton_anwendung;

	private ProgressDialog progressdialog_fortschritt;

	/**
	 * Initialisiert die Anwendung und konfiguriert {@link OnItemClickListener}
	 * für die Listeneinträge.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.punktezeigen_liste_layout);

		memosingleton_anwendung = (MemoSingleton) getApplication();

		ListView listview_liste = (ListView) findViewById(R.id.punktezeigen_liste_layout_listview1);

		listview_liste.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				String string_lat_lon = ((TextView) view
						.findViewById(R.id.punktezeigen_liste_listview_item_layout_textview2))
						.getText().toString();

				String[] stringarrays_lat_lon = string_lat_lon.split(" ");

				stringarrays_lat_lon[0] = stringarrays_lat_lon[0].replace(view
						.getResources().getString(R.string.lat_kurz), "");

				stringarrays_lat_lon[1] = stringarrays_lat_lon[1].replace(view
						.getResources().getString(R.string.lon_kurz), "");

				GeoPunkt geopunkt_punkt = new GeoPunkt(Integer
						.valueOf(stringarrays_lat_lon[0]), Integer
						.valueOf(stringarrays_lat_lon[1]));

				((MemoSingleton) getApplication()).dbAbfragen(geopunkt_punkt,
						true);
			}
		});

		// if (savedInstanceState == null) {
		//
		// dbAbfrageStarten(new Intent());
		// }

		if (savedInstanceState != null) {

			if (savedInstanceState.getBoolean("boolean_zeige_fortschritt")) {

				erzeugeFortschrittsDialog();
			}
		}

		Log.d("memo_debug", getSupportLoaderManager().initLoader(0, null, this)
				.toString());
	}

	/**
	 * Registriert {@link BroadcastReceiver} und {@link IntentFilter}.
	 */
	@Override
	public void onStart() {

		bcreceiver_receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				String string_intent_action = intent.getAction();

				if (string_intent_action
						.equals(MemoSingleton.INTENT_ZEIGE_LISTE)) {
					listeAnzeigen(intent.getIntExtra("int_anzahl", 0), true);
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_DB_FUELLEN)) {
					dbAbfrageStarten(intent);
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_DB_LEEREN)) {
					listeListeLoeschen();
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_PUNKTE_FILTERN)) {
					dbAbfrageStarten(intent);
				}
			}
		};

		IntentFilter ifilter_filter = new IntentFilter();
		ifilter_filter.addAction(MemoSingleton.INTENT_DB_FUELLEN);
		ifilter_filter.addAction(MemoSingleton.INTENT_DB_LEEREN);
		ifilter_filter.addAction(MemoSingleton.INTENT_PUNKTE_FILTERN);
		ifilter_filter.addAction(MemoSingleton.INTENT_ZEIGE_LISTE);

		this.registerReceiver(bcreceiver_receiver, ifilter_filter);

		Log.d("memo_debug_punktezeigen_tab_liste", "onstart");

		super.onStart();
	}

	/**
	 * Löscht die registrierten {@link BroadcastReceiver} beim Beenden der
	 * {@link Activity}.
	 */
	@Override
	public void onStop() {

		this.unregisterReceiver(bcreceiver_receiver);

		Log.d("memo_debug_punktezeigen_tab_liste", "onstop");

		super.onStop();
	}

	/**
	 * Verwaltet die Hintergrundthreads bei Drehung des Gerätes.
	 * 
	 * @see PunkteZeigen_Tab_AsyncTask
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {

		if ((asynctask_dbabfrage != null)
				&& (asynctask_dbabfrage.getStatus() == Status.RUNNING)) {

			outState.putBoolean("boolean_liste_asynctask", true);
		}

		if ((progressdialog_fortschritt != null)
				&& progressdialog_fortschritt.isShowing()) {

			outState.putBoolean("boolean_zeige_fortschritt", true);
		}

		Log.d("memo_debug_punktezeigen_tab_liste", "onsaveinstancestate");

		super.onSaveInstanceState(outState);
	}

	/**
	 * Stellt die Listenansicht nach Drehung des Gerätes wiederher.
	 */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {

		if (!savedInstanceState.getBoolean("boolean_liste_asynctask", false)) {

			// listeAnzeigen(0, false);
		}

		Log.d("memo_debug_punktezeigen_tab_liste", "onrestoreinstancestate");

		super.onRestoreInstanceState(savedInstanceState);
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

		String string_adresse, string_name, string_groesserkleiner, string_id;
		boolean boolean_filter;

		boolean_filter = intent_befehl.getBooleanExtra(getPackageName() + "_"
				+ "boolean_filter", false);

		if (!boolean_filter) {
			string_id = SQL_DB_Verwaltung.NAME_SPALTE_1
					+ ">"
					+ Long.toString(memosingleton_anwendung
							.letzterDBZugriff(MemoSingleton.LISTE));
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

		Cursor cursor_db_anfrage = memosingleton_anwendung.sqldatabase_readable
				.query(SQL_DB_Verwaltung.TABELLEN_NAME_HAUPT, new String[] {
						SQL_DB_Verwaltung.NAME_SPALTE_1,
						SQL_DB_Verwaltung.NAME_SPALTE_2,
						SQL_DB_Verwaltung.NAME_SPALTE_3,
						SQL_DB_Verwaltung.NAME_SPALTE_4,
						SQL_DB_Verwaltung.NAME_SPALTE_5 },
						string_id + string_adresse + string_groesserkleiner
								+ string_name, null, null, null,
						SQL_DB_Verwaltung.NAME_SPALTE_2);
		// select ... from tabellenname where id>letzter db zugriff AND ...
		// order by name

		asynctask_dbabfrage = new PunkteZeigen_Tab_AsyncTask(this,
				PunkteZeigen_Tab_AsyncTask.LISTE, boolean_filter);

		asynctask_dbabfrage.execute(cursor_db_anfrage);

		Log.d("memo_debug_punktezeigen_tab_liste", "dbabfragestarten");
	}

	/**
	 * {@code public void listeAnzeigen(int int_anzahl_punkte, boolean bool_meldung)}
	 * <p/>
	 * Zeigt die verarbeiteten Daten aus {@link MemoSingleton} als Liste an.
	 * 
	 * @param int_anzahl_punkte
	 *            Anzahl der angezeigten Punkte zur Ausgabe per {@link Toast}
	 * @param bool_meldung
	 *            {@link Boolean} zur Steuerung der {@link Toast}-Ausgabe
	 * 
	 * @see MemoSingleton
	 */
	public void listeAnzeigen(int int_anzahl_punkte, boolean bool_meldung) {

		ListView listview_liste = (ListView) findViewById(R.id.punktezeigen_liste_layout_listview1);

		SimpleAdapter simpleadapter_liste_adapter;

		if (bool_meldung) {
			Toast.makeText(
					this,
					Integer.toString(int_anzahl_punkte)
							+ " "
							+ getResources()
									.getString(
											R.string.punktezeigen_tab_liste_karteanzeigen),
					Toast.LENGTH_SHORT).show();
		}

		if (!memosingleton_anwendung.boolean_gefiltert) {

			simpleadapter_liste_adapter = new SimpleAdapter(
					this,
					memosingleton_anwendung.arraylist_liste_daten,
					R.layout.punktezeigen_liste_listview_item_layout,
					new String[] { GEOPUNKT_NAME, GEOPUNKT_LAT_LON,
							GEOPUNKT_ICON },
					new int[] {
							R.id.punktezeigen_liste_listview_item_layout_textview1,
							R.id.punktezeigen_liste_listview_item_layout_textview2,
							R.id.punktezeigen_liste_listview_item_layout_imageview });
		} else {

			simpleadapter_liste_adapter = new SimpleAdapter(
					this,
					memosingleton_anwendung.arraylist_liste_daten_temp,
					R.layout.punktezeigen_liste_listview_item_layout,
					new String[] { GEOPUNKT_NAME, GEOPUNKT_LAT_LON,
							GEOPUNKT_ICON },
					new int[] {
							R.id.punktezeigen_liste_listview_item_layout_textview1,
							R.id.punktezeigen_liste_listview_item_layout_textview2,
							R.id.punktezeigen_liste_listview_item_layout_imageview });
		}

		listview_liste.setAdapter(simpleadapter_liste_adapter);

		Log.d("memo_debug_punktezeigen_tab_liste", "listeanzeigen");
	}

	/**
	 * {@code private void listeListeLoeschen()}
	 * <p/>
	 * Löscht die gesamte Liste.
	 */
	private void listeListeLoeschen() {
		ListView listview_liste = (ListView) findViewById(R.id.punktezeigen_liste_layout_listview1);

		listview_liste.setAdapter(null);
		listview_liste.invalidate();

	}

	public Loader<ArrayList<HashMap<String, String>>> onCreateLoader(int id,
			Bundle args) {

		erzeugeFortschrittsDialog();

		return new ListeAsyncTaskLoader(this);
	}

	public void onLoadFinished(Loader<ArrayList<HashMap<String, String>>> arg0,
			ArrayList<HashMap<String, String>> arg1) {

		schliesseFortschrittsDialog();

		((ListView) this.findViewById(R.id.punktezeigen_liste_layout_listview1))
				.setAdapter(new SimpleAdapter(this, arg1,
						android.R.layout.simple_list_item_2, new String[] {
								"bezeichnung", "verfuegbarkeit" }, new int[] {
								android.R.id.text1, android.R.id.text2 }));
	}

	public void onLoaderReset(Loader<ArrayList<HashMap<String, String>>> arg0) {
	}

	private void erzeugeFortschrittsDialog() {

		schliesseFortschrittsDialog();

		progressdialog_fortschritt = new ProgressDialog(this);
		progressdialog_fortschritt
				.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressdialog_fortschritt.setTitle("Ladestationen laden");

		progressdialog_fortschritt.show();
	}

	private void schliesseFortschrittsDialog() {

		if (progressdialog_fortschritt != null
				&& progressdialog_fortschritt.isShowing()) {

			progressdialog_fortschritt.dismiss();
		}
	}
}
