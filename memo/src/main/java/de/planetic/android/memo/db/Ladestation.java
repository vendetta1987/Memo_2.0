package de.planetic.android.memo.db;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.format.Time;
import android.util.Log;

import com.google.android.maps.GeoPoint;

import de.planetic.android.memo.R;

public class Ladestation {

	public long long_id;
	public GeoPoint geopoint_standort;
	public String string_kommentar;
	public String string_bezeichnung;
	public Drawable drawable_ladestation_foto;
	public Time time_verfuegbarkeit_anfang;
	public Time time_verfuegbarkeit_ende;
	public String string_verfuegbarkeit_kommentar;
	public int int_zugangstyp;
	public double double_preis;

	public ArrayList<Stecker> arraylist_stecker;
	public Adresse adresse_ort;
	public Betreiber betreiber_anbieter;

	private Context context_application;

	public Ladestation(Context context) {

		context_application = context.getApplicationContext();

		long_id = 1;
		setzeStandort(0, 0);
		string_kommentar = "";
		string_bezeichnung = "";
		setzeLadestationFoto(R.drawable.icon);
		time_verfuegbarkeit_anfang = new Time();
		time_verfuegbarkeit_ende = new Time();
		string_verfuegbarkeit_kommentar = "";
		int_zugangstyp = 0;
		double_preis = 0;

		arraylist_stecker = new ArrayList<Stecker>();
		adresse_ort = new Adresse();
		betreiber_anbieter = new Betreiber(context_application);
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

	public String leseVerfuegbarkeit() {

		return time_verfuegbarkeit_anfang.format("%H:%M") + " - "
				+ time_verfuegbarkeit_ende.format("%H:%M");
	}
}
