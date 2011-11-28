package de.planetic.android.memo;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class GPS {

	public Context context;
	public int lat, lon;
	public LocationListener locationListener;

	public GPS(Context con) {
		context = con;
	}

	public void starteGPS() {

		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {

				Double d_lat = location.getLatitude() * 10e6;
				lat = d_lat.intValue();
				Double d_lon = location.getLongitude() * 10e6;
				lon = d_lon.intValue();
			}

			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}

			public void onProviderEnabled(String provider) {
			}

			public void onProviderDisabled(String provider) {
			}
		};

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				1000 * 5, 0, locationListener);
	}

	public void stoppeGPS() {
		LocationManager locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		locationManager.removeUpdates(locationListener);
	}

	public int[] aktuellePosition() {
		return new int[] { lat, lon };
	}

}
