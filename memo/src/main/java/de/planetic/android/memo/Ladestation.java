package de.planetic.android.memo;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class Ladestation {

	public int int_adress_id;
	public GeoPoint geopoint_standort;
	public String string_kommentar;
	public String string_bezeichnung;
	public Drawable drawable_ladestation_foto;
	public int int_verfuegbarkeit_anfang;
	public int int_verfuegbarkeit_ende;
	public String string_verfuegbarkeit_kommentar;
	public int int_zugangstyp;
	public int int_betreiber_id;
	public double double_preis;

	private Context context_application;

	public Ladestation(Context context) {

		context_application = context.getApplicationContext();

		int_adress_id = 1;
		geopoint_standort = new GeoPoint(0, 0);
		string_kommentar = "";
		string_bezeichnung = "";
		drawable_ladestation_foto = context_application.getResources()
				.getDrawable(R.drawable.icon);
		int_verfuegbarkeit_anfang = 0;
		int_verfuegbarkeit_ende = 0;
		string_verfuegbarkeit_kommentar = "";
		int_zugangstyp = 0;
		int_betreiber_id = 1;
		double_preis = 0;
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
