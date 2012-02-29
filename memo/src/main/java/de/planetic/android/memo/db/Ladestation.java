package de.planetic.android.memo.db;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.Time;
import android.util.Log;

import com.google.android.maps.GeoPoint;

import de.planetic.android.memo.R;

public class Ladestation implements Parcelable {

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

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {

		dest.writeLong(long_id);
		dest.writeIntArray(new int[] { geopoint_standort.getLatitudeE6(),
				geopoint_standort.getLongitudeE6() });

		// Bitmap bmp = Bitmap.createBitmap(
		// drawable_ladestation_foto.getIntrinsicWidth(),
		// drawable_ladestation_foto.getIntrinsicHeight(),
		// Config.ARGB_8888);
		// Canvas canvas = new Canvas(bmp);
		// drawable_ladestation_foto.setBounds(0, 0, canvas.getWidth(),
		// canvas.getHeight());
		// drawable_ladestation_foto.draw(canvas);
		// bmp.writeToParcel(dest, 0);

		dest.writeStringArray(new String[] { string_kommentar,
				string_bezeichnung, string_verfuegbarkeit_kommentar,
				time_verfuegbarkeit_anfang.format3339(false),
				time_verfuegbarkeit_ende.format3339(false) });
		dest.writeInt(int_zugangstyp);
		dest.writeDouble(double_preis);
		dest.writeList(arraylist_stecker);
	}

	public static final Parcelable.Creator<Ladestation> CREATOR = new Parcelable.Creator<Ladestation>() {
		public Ladestation createFromParcel(Parcel in) {

			if (in != null) {

				return new Ladestation(in);
			} else {

				return null;
			}
		}

		public Ladestation[] newArray(int size) {
			return new Ladestation[size];
		}
	};

	private Ladestation(Parcel in) {

		long_id = in.readLong();

		int[] int_geopoint = new int[2];
		in.readIntArray(int_geopoint);
		geopoint_standort = new GeoPoint(int_geopoint[0], int_geopoint[1]);

		// drawable_ladestation_foto = new BitmapDrawable(
		// context_application.getResources(),
		// Bitmap.CREATOR.createFromParcel(in));

		String[] string_array = new String[5];
		in.readStringArray(string_array);
		string_kommentar = string_array[0];
		string_bezeichnung = string_array[1];
		string_verfuegbarkeit_kommentar = string_array[2];

		time_verfuegbarkeit_anfang = new Time();
		time_verfuegbarkeit_ende = new Time();
		time_verfuegbarkeit_anfang.parse3339(string_array[3]);
		time_verfuegbarkeit_ende.parse3339(string_array[4]);

		int_zugangstyp = in.readInt();
		double_preis = in.readDouble();
		in.readList(arraylist_stecker, Stecker.class.getClassLoader());
	}

}
