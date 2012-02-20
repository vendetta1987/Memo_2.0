package de.planetic.android.memo;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class Ladestation {

	public long long_id;
	public long long_adress_id;
	public GeoPoint geopoint_standort;
	public String string_kommentar;
	public String string_bezeichnung;
	public Drawable drawable_ladestation_foto;
	public int int_verfuegbarkeit_anfang;
	public int int_verfuegbarkeit_ende;
	public String string_verfuegbarkeit_kommentar;
	public int int_zugangstyp;
	public long long_betreiber_id;
	public double double_preis;

	public ArrayList<Stecker> arraylist_stecker;

	private Context context_application;

	public Ladestation(Context context) {

		context_application = context.getApplicationContext();

		long_id = 1;
		long_adress_id = 1;
		setzeStandort(0, 0);
		string_kommentar = "";
		string_bezeichnung = "";
		setzeLadestationFoto(R.drawable.icon);
		int_verfuegbarkeit_anfang = 0;
		int_verfuegbarkeit_ende = 0;
		string_verfuegbarkeit_kommentar = "";
		int_zugangstyp = 0;
		long_betreiber_id = 1;
		double_preis = 0;

		arraylist_stecker = new ArrayList<Stecker>();
	}

	public void setzeStandort(int int_lat, int int_lon) {

		geopoint_standort = new GeoPoint(int_lat, int_lon);
	}

	public boolean setzeLadestationFoto(int int_id) {

		try {
			drawable_ladestation_foto = context_application.getResources()
					.getDrawable(int_id);

			return true;
		} catch (Exception e) {

			drawable_ladestation_foto = context_application.getResources()
					.getDrawable(R.drawable.icon);

			Log.d("memo_debug", "setzeLadestationFoto Fehler");

			return false;
		}
	}
}
