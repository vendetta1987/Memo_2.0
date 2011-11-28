package de.planetic.android.memo_neu;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class PunkteZeigen_Tab_Liste extends Activity {

	private SQL_DB_Verwaltung sqldb_db_verwaltung;
	private BroadcastReceiver bcreceiver_receiver;
	private PunkteZeigen_Tab_AsyncTask asynctask_dbabfrage;
	private MemoSingleton memosingleton_anwendung;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.punktezeigen_liste_layout_neu);

		sqldb_db_verwaltung = new SQL_DB_Verwaltung(this);

		memosingleton_anwendung = (MemoSingleton) getApplication();

		if (savedInstanceState == null) {
			dbAbfrageStarten();
		} else if (savedInstanceState.getBoolean("liste_asynctask")) {
			listeAnzeigen(memosingleton_anwendung.list_liste_daten.size(),
					false);
		}
	}

	@Override
	public void onStart() {
		bcreceiver_receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				String string_intent_action = intent.getAction();

				if (string_intent_action
						.equals(MemoSingleton.INTENT_DB_FUELLEN)) {
					dbAbfrageStarten();
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_DB_LEEREN)) {
					listeListeLoeschen();
				}
			}
		};

		IntentFilter ifilter_filter = new IntentFilter();
		ifilter_filter.addAction(MemoSingleton.INTENT_DB_FUELLEN);
		ifilter_filter.addAction(MemoSingleton.INTENT_DB_LEEREN);

		this.registerReceiver(bcreceiver_receiver, ifilter_filter);

		super.onStart();
	}

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
			outState.putBoolean("liste_asynctask", true);
		}

		super.onSaveInstanceState(outState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {

		listeAnzeigen(memosingleton_anwendung.list_liste_daten.size(), false);

		super.onRestoreInstanceState(savedInstanceState);
	}

	private void dbAbfrageStarten() {

		SQLiteDatabase sqldb_zugriff = sqldb_db_verwaltung
				.getReadableDatabase();

		Cursor cursor_db_anfrage = sqldb_zugriff.query(
				SQL_DB_Verwaltung.TABELLEN_NAME,
				null,
				SQL_DB_Verwaltung.NAME_SPALTE_1
						+ ">"
						+ Long.toString(memosingleton_anwendung
								.letzterDBZugriff(MemoSingleton.LISTE)), null,
				null, null, SQL_DB_Verwaltung.NAME_SPALTE_1);
		// select * from tabellenname where id>letzter db zugriff order by id

		asynctask_dbabfrage = new PunkteZeigen_Tab_AsyncTask(this,
				(MemoSingleton) getApplication(),
				PunkteZeigen_Tab_AsyncTask.LISTE);

		asynctask_dbabfrage.execute(cursor_db_anfrage);
	}

	public void listeAnzeigen(int int_anzahl_punkte, boolean bool_meldung) {

		ListView listview_liste = (ListView) findViewById(R.id.punktezeigen_liste_layout_listview1_neu);

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

		simpleadapter_liste_adapter = new SimpleAdapter(
				this,
				memosingleton_anwendung.list_liste_daten,
				R.layout.punktezeigen_liste_listview_item_layout_neu,
				new String[] { "geopkt_name", "geopkt_lat_lon", "geopkt_icon" },
				new int[] {
						R.id.punktezeigen_liste_listview_item_layout_textview1_neu,
						R.id.punktezeigen_liste_listview_item_layout_textview2_neu,
						R.id.punktezeigen_liste_listview_item_layout_imageview_neu });

		listview_liste.setAdapter(simpleadapter_liste_adapter);

	}

	private void listeListeLoeschen() {
		ListView listview_liste = (ListView) findViewById(R.id.punktezeigen_liste_layout_listview1_neu);

		listview_liste.setAdapter(null);
		listview_liste.invalidate();

	}
}
