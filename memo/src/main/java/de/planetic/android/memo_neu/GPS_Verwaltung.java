package de.planetic.android.memo_neu;

import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

public class GPS_Verwaltung {

	private Context context_con;
	private int int_lat, int_lon;
	private long long_letzte_aktualisierung;
	private LocationListener locationlistener_listener;

	public GPS_Verwaltung(Context con) {
		context_con = con;

		int_lat = 0;
		int_lon = 0;
		long_letzte_aktualisierung = 0;
	}

	public boolean starteGPS() {

		LocationManager locationmanager = (LocationManager) context_con
				.getSystemService(Context.LOCATION_SERVICE);

		List<String> list_anbieter = locationmanager.getProviders(true);

		if (list_anbieter.contains(LocationManager.GPS_PROVIDER)) {

			locationlistener_listener = new LocationListener() {
				@Override
				public void onLocationChanged(Location location) {

					Double d_lat = location.getLatitude() * 1e6;
					int_lat = d_lat.intValue();
					Double d_lon = location.getLongitude() * 1e6;
					int_lon = d_lon.intValue();
					long_letzte_aktualisierung = Calendar.getInstance()
							.getTimeInMillis();
				}

				public void onStatusChanged(String provider, int status,
						Bundle extras) {
				}

				public void onProviderEnabled(String provider) {
				}

				public void onProviderDisabled(String provider) {
				}
			};

			locationmanager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 1000 * 5, 0,
					locationlistener_listener);
		} else {

			Toast.makeText(
					context_con,
					context_con.getResources().getString(
							R.string.memosingleton_gps_nicht_verfuegbar),
					Toast.LENGTH_LONG).show();

			return false;
		}

		return true;
	}

	public void stoppeGPS() {
		if (locationlistener_listener != null) {
			LocationManager locationmanager = (LocationManager) context_con
					.getSystemService(Context.LOCATION_SERVICE);

			locationmanager.removeUpdates(locationlistener_listener);
		}
	}

	public GeoPunkt aktuellePosition() {

		LocationManager locationmanager = (LocationManager) context_con
				.getSystemService(Context.LOCATION_SERVICE);

		Iterator<String> iterator_provider = locationmanager.getProviders(true)
				.iterator();

		Location location_ort;

		GeoPunkt geopunkt_punkt;

		while (iterator_provider.hasNext()) {
			location_ort = locationmanager
					.getLastKnownLocation(iterator_provider.next());

			if ((location_ort != null)
					&& (location_ort.getTime() > long_letzte_aktualisierung)) {
				long_letzte_aktualisierung = location_ort.getTime();
				int_lat = ((Double) (location_ort.getLatitude() * 10e6))
						.intValue();
				int_lon = ((Double) (location_ort.getLongitude() * 10e6))
						.intValue();
			}

		}

		geopunkt_punkt = new GeoPunkt(int_lat, int_lon);

		return geopunkt_punkt;
	}

	public GeoPunkt adresseAufloesen(String string_adresse, Context context) {
		Geocoder geocoder_geokodierung = new Geocoder(context_con);

		List<Address> list_ergebnis = null;

		// try {
		// if (Geocoder.isPresent()) {
		//
		// list_ergebnis = geocoder_geokodierung.getFromLocationName(
		// string_adresse, 10);
		// }
		// } catch (Exception e) {
		// Log.d("memo_debug", e.toString());
		// }

		GPS_Verwaltung_AsyncTask gps_verwaltung_asynctask = new GPS_Verwaltung_AsyncTask(
				context);

		gps_verwaltung_asynctask.execute(string_adresse);

		if (list_ergebnis != null) {
			for (Address test : list_ergebnis) {
				if (test.hasLatitude() && test.hasLongitude()) {
					return new GeoPunkt(
							((Double) (test.getLatitude() * 1e6)).intValue(),
							((Double) (test.getLongitude() * 1e6)).intValue());
				}
			}
		}

		return null;

	}
}