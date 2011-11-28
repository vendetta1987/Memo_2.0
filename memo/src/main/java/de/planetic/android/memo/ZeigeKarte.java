package de.planetic.android.memo;

import java.util.List;

import android.app.Dialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import de.planetic.android.memo_neu.R;

public class ZeigeKarte extends MapActivity {

	public SQLDatenbank sqldb_klasse;
	public GPS gps_Klasse;

	private static final int DIALOGID_AKTUELLE_POSITION_SPEICHERN = 0;
	private static final int DIALOGID_BELIEBIGE_POSITION_SPEICHERN = 1;

	// instanz der klasse fuer karten-overlay mit punkten

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zeigekarte_layout);

		sqldb_klasse = new SQLDatenbank(this);
		gps_Klasse = new GPS(this);

		gps_Klasse.starteGPS();

		MapView mv_mapView = (MapView) findViewById(R.id.zeigekarte_mapview);
		// mv_mapView zeigt karte auf oberflaeche an
		mv_mapView.setBuiltInZoomControls(true);
		// mv_mapView finden und zoom aktivieren

		if (mv_mapView != null) {
			initialisieren(mv_mapView);
		}

		if (this.getIntent().hasExtra("name")
				&& this.getIntent().hasExtra("lat")
				&& this.getIntent().hasExtra("lon") && (mv_mapView != null)) {

			mv_mapView.getController().setCenter(
					new GeoPoint(this.getIntent().getIntExtra("lat", 0), this
							.getIntent().getIntExtra("lon", 0)));
			// karte auf punkt zentrieren

			mv_mapView.getController().setZoom(11);
		} else {
			mv_mapView.getController().setZoom(2);
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.zeigekarte_menu, menu);
		// Menue mit gegebenem Layout erstellen
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle item selection
		switch (item.getItemId()) {
		case R.id.zeigekarte_menu_item1:// aktuelle position
			this.showDialog(DIALOGID_AKTUELLE_POSITION_SPEICHERN);
			return true;
		case R.id.zeigekarte_menu_item2:// beliebige position
			this.showDialog(DIALOGID_BELIEBIGE_POSITION_SPEICHERN);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOGID_AKTUELLE_POSITION_SPEICHERN:
			return speicherePositionDialog(DIALOGID_AKTUELLE_POSITION_SPEICHERN);
		case DIALOGID_BELIEBIGE_POSITION_SPEICHERN:
			return speicherePositionDialog(DIALOGID_BELIEBIGE_POSITION_SPEICHERN);
		default:
			return null;
		}
	}

	public void initialisieren(MapView mv_mapView) {

		SQLiteDatabase sqldb = sqldb_klasse.getReadableDatabase();

		List<Overlay> li_mapOverlays = mv_mapView.getOverlays();
		// aktuelle overlays auslesen
		Drawable d_drawable = this.getResources().getDrawable(R.drawable.icon);
		// grafik fuer hinterlegte punkte festlegen
		ItemOverlay io_itemizedOverlay = new ItemOverlay(d_drawable, this);
		// instanzieren und grafik festlegen

		String name, icon;
		int id, lat, lon;

		Cursor c_anfrage = sqldb.rawQuery("select * from "
				+ SQLDatenbank.TABELLEN_NAME, null);

		if (c_anfrage.moveToFirst()) {
			// gehe zum ersten eintrag, falls cursor leer -> false
			do {
				// id = c_anfrage.getInt(0);
				name = c_anfrage.getString(1);
				lat = c_anfrage.getInt(2);
				lon = c_anfrage.getInt(3);
				// icon = c_anfrage.getString(4);

				io_itemizedOverlay.addOverlay(new OverlayItem(new GeoPoint(lat,
						lon), name, ""));

			} while (c_anfrage.moveToNext());
			// solang weitere ergebnisse vorliegen
		}

		li_mapOverlays.add(io_itemizedOverlay);
		// overlay zur karte hinzufuegen
	}

	public Dialog speicherePositionDialog(final int i_modus) {

		final Dialog dialog;
		final int laty, lony;

		dialog = new Dialog(this);
		dialog.setContentView(R.layout.memo_dialog_geopkt);
		dialog.setTitle(R.string.memo_dialog_geopkt_title);
		final EditText et_lat = (EditText) dialog
				.findViewById(R.id.memo_dialog_geopkt_edittext1_lat);
		final EditText et_lon = (EditText) dialog
				.findViewById(R.id.memo_dialog_geopkt_edittext2_lon);

		if (i_modus == DIALOGID_AKTUELLE_POSITION_SPEICHERN) {
			laty = gps_Klasse.aktuellePosition()[0];
			lony = gps_Klasse.aktuellePosition()[1];
			et_lat.setText(String.valueOf(laty));
			et_lon.setText(String.valueOf(lony));
			et_lat.setFocusable(false);
			et_lon.setFocusable(false);
		} else {
			laty = -1;
			lony = -1;
		}

		dialog.findViewById(R.id.memo_dialog_geopkt_button1)
				.setOnClickListener(new OnClickListener() {
					// OK
					@Override
					public void onClick(View arg0) {

						EditText et_name = (EditText) dialog
								.findViewById(R.id.memo_dialog_geopkt_edittext3_name);

						if (i_modus == DIALOGID_BELIEBIGE_POSITION_SPEICHERN) {
							int latx = Integer.valueOf(et_lat.getText()
									.toString());
							int lonx = Integer.valueOf(et_lon.getText()
									.toString());

							speicherePosition(et_name.getText().toString(),
									latx, lonx);
						} else {
							speicherePosition(et_name.getText().toString(),
									laty, lony);
						}

						dialog.dismiss();
						gps_Klasse.stoppeGPS();
					}
				});
		dialog.findViewById(R.id.memo_dialog_geopkt_button2)
				.setOnClickListener(new OnClickListener() {
					// Abbrechen
					@Override
					public void onClick(View arg0) {
						dialog.dismiss();
						gps_Klasse.stoppeGPS();
					}
				});
		return dialog;
	}

	public void speicherePosition(String name, int lat, int lon) {
		SQLiteDatabase sqldb = sqldb_klasse.getWritableDatabase();
		// id=int, name=string, lat=int, lon=int, icon=string

		Cursor c_ergebnis = sqldb.rawQuery("select max(id) from "
				+ SQLDatenbank.TABELLEN_NAME, null);

		c_ergebnis.moveToFirst();

		ContentValues cv_werte = new ContentValues();

		cv_werte.put("id", c_ergebnis.getInt(0) + 1);
		cv_werte.put("name", name);
		cv_werte.put("lat", lat);
		cv_werte.put("lon", lon);
		cv_werte.put("icon", "icon");

		sqldb.insert(SQLDatenbank.TABELLEN_NAME, null, cv_werte);
	}
}
