package de.planetic.android.memo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import de.planetic.android.memo_neu.R;

public class MemoStart extends Activity {

	public SQLDatenbank sqldb_klasse;

	// public Application app_memosingleton;

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            If the activity is being re-initialized after previously being
	 *            shut down then this Bundle contains the data it most recently
	 *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
	 *            is null.</b>
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.memostart_layout);

		// app_memosingleton = getApplication();
		// Application zum speichern von daten, unabhaengig vom
		// lebenszyklus

		sqldb_klasse = new SQLDatenbank(this);

		ListView lv_liste = (ListView) this
				.findViewById(R.id.memostart_listView1);
		// listview finden

		lv_liste.setOnItemClickListener(new OnItemClickListener() {
			// listener für klick auf eintraege hinzufuegen

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				HashMap<String, String> hm_daten = (HashMap<String, String>) arg0
						.getItemAtPosition(arg2);

				// Daten des geklickten Elementes abrufen -> HashMap s.
				// dbZeigen

				zeigeKarte(hm_daten);
				// Karte mit ausgelesenen Daten aufrufen
			}

		});
	}

	@Override
	protected void onResume() {
		dbZeigen();
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.memostart_menu, menu);
		// Menue mit gegebenem Layout erstellen
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.memostart_menu_item1:// DB zeigen
			// dbZeigen();
			return true;
		case R.id.memostart_menu_item2:// DB leeren
			dbLeeren();
			dbZeigen();
			return true;
		case R.id.memostart_menu_item3:// DB fuellen
			dbFuellen();
			dbZeigen();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void zeigeKarte(HashMap<String, String> hm_daten) {

		Intent int_intent = new Intent(this, ZeigeKarte.class);
		// intent fuer aufruf der kartenklasse per listeneintrag

		try {

			if ((hm_daten != null) && hm_daten.containsKey("name")
					&& hm_daten.containsKey("latlon")) {
				// wenn uebergebene daten schluessel enthalten

				int_intent.putExtra("name", hm_daten.get("name").toString());
				// extra zum intent hinzufuegen

				// "latlon", "Lat:" + Integer.toString(lat)
				// + " Lon:" + Integer.toString(lon)

				String[] sa_latlon = hm_daten.get("latlon").toString()
						.split(" ");
				// string auslesen und bei " " auftrennen -> [0] lat:xxx,
				// [1]lon:yyy

				int_intent.putExtra("lat",
						Integer.valueOf(sa_latlon[0].substring(4)));
				int_intent.putExtra("lon",
						Integer.valueOf(sa_latlon[1].substring(4)));
				// extra hinzufuegen, string nach "lat:"/"lon:" in int wandeln
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		this.startActivity(int_intent);

	}

	public void zeigeKarte(View v_view) {
		Intent int_intent = new Intent(this, ZeigeKarte.class);
		// intent fuer aufruf der kartenklasse per knopf

		this.startActivity(int_intent);
	}

	public void dbZeigen() {
		SQLiteDatabase sqldb = sqldb_klasse.getReadableDatabase();
		// datenbank zum lesen anfordern

		Cursor c_ergebnis = sqldb.query(SQLDatenbank.TABELLEN_NAME, null, null,
				null, null, null, null);
		// select * from tabellen.name

		int id, lat, lon;
		String name, icon;

		List<Map<String, String>> l_data = new ArrayList<Map<String, String>>(
				c_ergebnis.getCount());
		// liste aus maps erstellen, maps aus schluessel-wert-paaren, mit
		// ergebnisanzahl der anfrage initialisieren

		Map<String, String> m_datum;
		// map mit schluessel-wert-paar fuer name und latlon

		ListView lv_liste = (ListView) this
				.findViewById(R.id.memostart_listView1);

		if (c_ergebnis.moveToFirst()) {
			// gehe zum ersten eintrag, falls cursor leer -> false
			do {
				// id = c_ergebnis.getInt(0);
				name = c_ergebnis.getString(1);
				lat = c_ergebnis.getInt(2);
				lon = c_ergebnis.getInt(3);
				// icon = c_ergebnis.getString(4);

				// werte der spalten aus cursor lesen

				m_datum = new HashMap<String, String>(2);

				m_datum.put("name", name);

				m_datum.put("latlon", "Lat:" + Integer.toString(lat) + " Lon:"
						+ Integer.toString(lon));
				// map erstellen und fuellen

				l_data.add(m_datum);
				// zur liste hinzufuegen; new hashmap, da scheinbar flach
				// kopiert wird

			} while (c_ergebnis.moveToNext());
			// solang weitere ergebnisse vorliegen
		}

		SimpleAdapter sa_adapter = new SimpleAdapter(this, l_data,
				R.layout.memostart_listview_item, new String[] { "name",
						"latlon" }, new int[] {
						R.id.memostart_listview_item_textview1,
						R.id.memostart_listview_item_textview2 });
		// simpleadapter um listview-eintraege mit untertitel zu fuellen
		// daten aus l_data werden nach string vorgabe auf int layout-elemente
		// geschrieben

		lv_liste.setAdapter(sa_adapter);
	}

	public void dbLeeren() {
		SQLiteDatabase sqldb = sqldb_klasse.getWritableDatabase();

		sqldb.delete(SQLDatenbank.TABELLEN_NAME, null, null);
	}

	public void dbFuellen() {
		// fuellt db mit zufaelligen werten

		SQLiteDatabase sqldb = sqldb_klasse.getWritableDatabase();

		Cursor c_ergebnis;

		ContentValues cv_werte = new ContentValues(5);

		Random ran = new Random();
		int i, j;

		c_ergebnis = sqldb.rawQuery("select max(id) from "
				+ SQLDatenbank.TABELLEN_NAME, null);
		c_ergebnis.moveToFirst();

		i = c_ergebnis.getInt(0) + 1;
		j = i + 3;

		for (int x = i; x < j; x++) {
			cv_werte.put("id", x);
			cv_werte.put("name", "Schwerin" + Integer.toString(x));
			// cv_werte.put("lat", 53600337);
			// cv_werte.put("lon", 11418141);
			cv_werte.put("lat", ran.nextInt(80000000));
			// beschraenkung von GeoPoint -> max +-80° lat
			cv_werte.put("lon", ran.nextInt(180000000));
			// beschraenkung von GeoPoint -> max +-180° lon
			cv_werte.put("icon", "icon");

			sqldb.insert(SQLDatenbank.TABELLEN_NAME, null, cv_werte);
		}
	}
}
