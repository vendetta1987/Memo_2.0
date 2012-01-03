package de.planetic.android.memo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Klasse zur Verwaltung des GPS-Empfängers. Ermöglicht das Starten/Stoppen,
 * Ermittlung der letzten bekannten Position und Abfragen der Verfügbarkeit.
 * 
 */
public class GPS_Verwaltung {

	private Context context_con;
	private LocationManager locationmanager;
	private HashMap<Integer, LocationListener> hashmap_locationlistener;
	private int int_lat, int_lon;

	public long long_letzte_aktualisierung;

	/**
	 * Standardkonstruktor initialisiert Parameter.
	 * 
	 * @param con
	 *            Context für die Nutzung von Intents.
	 */
	public GPS_Verwaltung(Context con) {
		context_con = con;

		int_lat = 0;
		int_lon = 0;
		long_letzte_aktualisierung = 0;
		hashmap_locationlistener = new HashMap<Integer, LocationListener>();
		locationmanager = (LocationManager) context_con
				.getSystemService(Context.LOCATION_SERVICE);
	}

	/**
	 * {@code public boolean starteGPS(final Intent intent_befehl)}
	 * <p/>
	 * Registriert {@link LocationListener} für GPS-Empfänger und speichert
	 * diesen intern in einer {@link HashMap}. {@link LocationListener}
	 * {@code .onLocationChanged} sendet {@link Intent} bei
	 * Positionsaktualisierung abhängig von {@code intent_befehl}
	 * 
	 * @param intent_befehl
	 *            {@link Intent} zum Aufruf der Funktion. Kann Schlüssel
	 *            {@code int_listener} beinhalten, siehe {@link MemoSingleton}
	 *            {@code .GPS_LISTENER_*}
	 * @see MemoSingleton
	 * @return {@code true} Falls ein aktiver GPS-Empfänger gefunden wurde. <br/>
	 *         {@code false} Falls kein aktiver GPS-Empfänger gefunden wurde.
	 */
	public boolean starteGPS(final Intent intent_befehl) {

		List<String> list_anbieter = locationmanager.getProviders(true);

		if (list_anbieter.contains(LocationManager.GPS_PROVIDER)) {

			LocationListener locationlistener_listener = new LocationListener() {
				@Override
				public void onLocationChanged(Location location) {

					int_lat = new Double(location.getLatitude() * 1e6)
							.intValue();
					int_lon = new Double(location.getLongitude() * 1e6)
							.intValue();

					long_letzte_aktualisierung = Calendar.getInstance()
							.getTimeInMillis();

					Intent intent_temp;

					switch (intent_befehl
							.getIntExtra(context_con.getPackageName() + "_"
									+ "int_listener",
									MemoSingleton.GPS_LISTENER_NORMAL)) {
					case MemoSingleton.GPS_LISTENER_AKTUELL:
						intent_temp = new Intent(
								MemoSingleton.INTENT_KARTE_VERFOLGE_AKTUELLE_POS);
						intent_temp.putExtra(context_con.getPackageName() + "_"
								+ "boolean_aktivieren", true);
						intent_temp.putExtra(context_con.getPackageName() + "_"
								+ "int_lat", int_lat);
						intent_temp.putExtra(context_con.getPackageName() + "_"
								+ "int_lon", int_lon);
						context_con.sendBroadcast(intent_temp);
						break;
					case MemoSingleton.GPS_LISTENER_NAVIGATION:
						intent_temp = new Intent(
								MemoSingleton.INTENT_KARTE_NAVIGATIONSANWEISUNG);
						intent_temp.putExtra(context_con.getPackageName() + "_"
								+ "double_lat", location.getLatitude());
						intent_temp.putExtra(context_con.getPackageName() + "_"
								+ "double_lon", location.getLongitude());
						context_con.sendBroadcast(intent_temp);
						break;
					}
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
					LocationManager.GPS_PROVIDER, 1000 * 1, 0,
					locationlistener_listener);

			switch (intent_befehl.getIntExtra(context_con.getPackageName()
					+ "_" + "int_listener", MemoSingleton.GPS_LISTENER_NORMAL)) {
			case MemoSingleton.GPS_LISTENER_AKTUELL:
				((MemoSingleton) context_con.getApplicationContext()).boolean_aktuelle_position = true;
				break;
			}

			hashmap_locationlistener.put(intent_befehl.getIntExtra(
					context_con.getPackageName() + "_" + "int_listener",
					MemoSingleton.GPS_LISTENER_NORMAL),
					locationlistener_listener);
			return true;
		} else {

			return false;
		}
	}

	/**
	 * {@code public boolean gpsVerfuegbar()}
	 * <p/>
	 * Prüft die Verfügbarkeit eines GPS-Empfängers und gibt einen {@link Toast}
	 * aus, falls keiner gefunden wurde.
	 * 
	 * @return {@code true} Falls ein GPS-Empfänger vorhanden ist.<br/>
	 *         {@code false} Falls kein GPS-Empfänger vorhanden ist.
	 */
	public boolean gpsVerfuegbar() {
		List<String> list_anbieter = locationmanager.getProviders(true);

		boolean boolean_verfuegbar = list_anbieter
				.contains(LocationManager.GPS_PROVIDER);

		if (!boolean_verfuegbar) {
			Toast.makeText(
					context_con,
					context_con.getResources().getString(
							R.string.memosingleton_gps_nicht_verfuegbar),
					Toast.LENGTH_LONG).show();
		}

		return boolean_verfuegbar;
	}

	/**
	 * {@code public void stoppeGPS(Intent intent_befehl)}
	 * <p/>
	 * Entfernt vorher registrierten {@link LocationListener} aus der internen
	 * {@link HashMap}, falls vorhanden. Sendet {@link Intent} zur Deaktivierung
	 * der Positionsverfolgung, falls nötig.
	 * 
	 * @param intent_befehl
	 *            {@link Intent} zum Aufruf der Funktion. Kann Schlüssel
	 *            {@code int_listener} beinhalten, siehe {@link MemoSingleton}
	 *            {@code .GPS_LISTENER_*}
	 * @see MemoSingelton
	 */
	public void stoppeGPS(Intent intent_befehl) {

		int int_listener = intent_befehl.getIntExtra(
				context_con.getPackageName() + "_" + "int_listener",
				MemoSingleton.GPS_LISTENER_NORMAL);

		if (hashmap_locationlistener.containsKey(int_listener)) {

			if (int_listener == MemoSingleton.GPS_LISTENER_ALLE) {

				for (LocationListener locationlistener_listener : hashmap_locationlistener
						.values()) {

					locationmanager.removeUpdates(locationlistener_listener);
				}

				hashmap_locationlistener.clear();

			} else {

				locationmanager.removeUpdates(hashmap_locationlistener
						.get(int_listener));

				hashmap_locationlistener.remove(int_listener);
			}
		}

		if (int_listener == MemoSingleton.GPS_LISTENER_AKTUELL) {

			intent_befehl = new Intent(
					MemoSingleton.INTENT_KARTE_VERFOLGE_AKTUELLE_POS);
			intent_befehl.putExtra(context_con.getPackageName() + "_"
					+ "boolean_aktivieren", false);
			context_con.sendBroadcast(intent_befehl);
			((MemoSingleton) context_con.getApplicationContext()).boolean_aktuelle_position = false;
		}
	}

	/**
	 * {@code public GeoPunkt aktuellePosition()}
	 * <p/>
	 * Durchläuft alle Positionsanbieter und aktualisiert die in
	 * {@code long_letzte_aktualisierung} gespeicherte, letzte bekannte
	 * Position.
	 * 
	 * @return {@link GeoPunkt} mit den letzten erfassten Koordinaten.
	 */
	public GeoPunkt aktuellePosition() {

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

				// TODO 10e6 ?
				int_lat = ((Double) (location_ort.getLatitude() * 10e6))
						.intValue();
				int_lon = ((Double) (location_ort.getLongitude() * 10e6))
						.intValue();
			}

		}

		geopunkt_punkt = new GeoPunkt(int_lat, int_lon);

		return geopunkt_punkt;
	}
}