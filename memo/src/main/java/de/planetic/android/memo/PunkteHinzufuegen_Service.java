package de.planetic.android.memo;

import java.util.Calendar;
import java.util.List;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

/**
 * {@link IntentService} zur Georeferenzierung von neu eingetragenen Punkten.
 * Empfängt {@link Intent} und arbeitet diese der Reihe nach ab. Die
 * Verarbeitung wird beim Start der Anwendung automatisch angestoßen und nutzt
 * {@link Notification} zur Benachrichtigung des Nutzers.
 * 
 * @see PunkteHinzufuegen_Service_AsyncTask
 */
public class PunkteHinzufuegen_Service extends IntentService {

	public static final int VERARBEITET = 0;
	public static final int NICHT_VERARBEITET_GPS = 1;
	public static final int NICHT_VERARBEITET_ADRESSE = 2;

	private static final int NOTIFICATION_ID = 1;
	private MemoSingleton memosingleton_anwendung;

	public PunkteHinzufuegen_Service() {
		super("PunkteHinzufuegen_Service");
	}

	@Override
	public void onCreate() {
		memosingleton_anwendung = (MemoSingleton) getApplication();

		super.onCreate();
	}

	/**
	 * Empfängt {@link Intent} und liest mitgelieferte Daten aus. Startet
	 * Georeferenzierung/Adressauflösung und schreibt die Ergebnisse in die
	 * Datenbank zurück. Die Punkte bleiben in der Tabelle Synch und werden im
	 * Erfolgsfall um Geokoordinaten ergänzt oder andernfalls beim nächsten
	 * Start erneut verarbeitet.
	 * 
	 */
	@Override
	protected void onHandleIntent(Intent intent_service) {

		ContentValues contentvalues_werte = new ContentValues();

		GeoPunkt geopunkt_position = null;

		// intent-daten auslesen
		String string_name = intent_service.getStringExtra(getPackageName()
				+ "_" + "string_name");
		String string_beschreibung = intent_service
				.getStringExtra(getPackageName() + "_" + "string_beschreibung");
		String string_adresse = intent_service.getStringExtra(getPackageName()
				+ "_" + "string_adresse");
		String string_radio_eigenschaften = intent_service
				.getStringExtra(getPackageName() + "_"
						+ "string_radio_eigenschaften");
		String string_icon = intent_service.getStringExtra(getPackageName()
				+ "_" + "string_icon");
		Double double_preis = intent_service.getDoubleExtra(getPackageName()
				+ "_" + "double_preis", 0.0);
		// falls position aus gps ermittelt werden soll
		boolean boolean_pos_aus_gps = intent_service.getBooleanExtra(
				getPackageName() + "_" + "boolean_pos_aus_gps", true);
		// falls die verarbeitung erfolgreich war
		boolean boolean_verarbeitet;
		// falls ein punkt beim letzten mal nicht verarbeitet werden konnte
		boolean boolean_erneut_verarbeiten = intent_service.getBooleanExtra(
				getPackageName() + "_" + "boolean_erneut_verarbeiten", false);
		// id fuer erneute verarbeitung
		long long_id = intent_service.getLongExtra(getPackageName() + "_"
				+ "long_id", -1), long_zeit;
		int int_wartezeit;

		// bereitet meldung fuer notification-leiste vor
		Notification notification_nachricht = new Notification(R.drawable.icon,
				getResources().getString(R.string.app_name),
				System.currentTimeMillis());

		NotificationManager notificationmanager_verwaltung = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		if (boolean_pos_aus_gps
				&& memosingleton_anwendung.gps_verwaltung.gpsVerfuegbar(false)) {

			Intent intent_befehl = new Intent(MemoSingleton.INTENT_STARTE_GPS);
			intent_befehl.putExtra(getPackageName() + "_" + "int_listener",
					MemoSingleton.GPS_LISTENER_SERVICE);
			this.sendBroadcast(intent_befehl);
		}

		notification_nachricht
				.setLatestEventInfo(
						getApplicationContext(),
						getResources()
								.getString(
										R.string.punktehinzufuegen_service_notification_speichere)
								+ " " + string_name,
						getResources()
								.getString(
										R.string.punktehinzufuegen_service_notification_bitte_warten),
						PendingIntent.getBroadcast(getApplicationContext(), 0,
								new Intent(), Notification.FLAG_ONGOING_EVENT));

		notification_nachricht.number = intent_service.getIntExtra(
				getPackageName() + "_" + "int_notification_zaehler", 0);

		notificationmanager_verwaltung.cancel(NOTIFICATION_ID);

		notificationmanager_verwaltung.notify(NOTIFICATION_ID,
				notification_nachricht);

		if (boolean_pos_aus_gps) {

			if (memosingleton_anwendung.gps_verwaltung.gpsVerfuegbar(false)) {
				// position aus gps erfassen

				long_zeit = System.currentTimeMillis();

				int_wartezeit = Integer
						.parseInt(Memo_Einstellungen
								.leseEinstellungen(getApplicationContext())
								.getString(
										getApplicationContext()
												.getResources()
												.getString(
														R.string.memo_einstellungen_gps_wartezeit_schluessel),
										"1"));

				for (int i = 0; i < int_wartezeit * 12; i++) {
					// X x 12 x 5 sek = 60 sek x X
					// wartet je 5 sek und liest dann die letzte bekannte
					// position aus, falls diese nach dem neuer punkt-auftrag
					// erfasst wurde

					if (memosingleton_anwendung.gps_verwaltung.long_letzte_aktualisierung > long_zeit) {

						geopunkt_position = memosingleton_anwendung.gps_verwaltung
								.aktuellePosition();
						break;
					}

					if ((i % 12) == 0) {

						notification_nachricht
								.setLatestEventInfo(
										getApplicationContext(),
										getResources()
												.getString(
														R.string.punktehinzufuegen_service_notification_speichere)
												+ " " + string_name,
										getResources()
												.getString(
														R.string.punktehinzufuegen_service_notification_warte_auf_gps)
												+ " "
												+ String.valueOf((i / 12) + 1)
												+ " "
												+ getResources()
														.getString(
																R.string.punktehinzufuegen_service_notification_von)
												+ " "
												+ Integer
														.toString(int_wartezeit)
												+ " "
												+ getResources()
														.getString(
																R.string.punktehinzufuegen_service_notification_minuten),
										PendingIntent
												.getBroadcast(
														getApplicationContext(),
														0,
														new Intent(),
														Notification.FLAG_ONGOING_EVENT));
						notificationmanager_verwaltung.notify(NOTIFICATION_ID,
								notification_nachricht);
					}

					SystemClock.sleep(5 * 1000);
				}
			} else {

				geopunkt_position = null;
			}
		} else {
			// adresse in position umwandeln

			geopunkt_position = adresseAufloesen(string_adresse);
		}

		contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_2, string_name);

		if (geopunkt_position != null) {
			// fuer den punkt wurden geokoordinaten ermittelt

			contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_3,
					geopunkt_position.getLatitudeE6());
			contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_4,
					geopunkt_position.getLongitudeE6());

			boolean_verarbeitet = true;
		} else {
			// falls keine koordinaten ermittelt werden konnten

			contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_3, -1);
			contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_4, -1);

			boolean_verarbeitet = false;
		}

		contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_5, string_icon);

		contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_6, Calendar
				.getInstance().getTimeInMillis());

		contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_7,
				string_beschreibung);
		contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_8,
				string_radio_eigenschaften);
		contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_9, double_preis);
		contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_10,
				string_adresse);

		if (boolean_verarbeitet) {
			// hinterlege in der tabelle, dass der punkt verarbeitet wurde

			contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_11,
					VERARBEITET);

			notification_nachricht
					.setLatestEventInfo(
							getApplicationContext(),
							string_name
									+ " "
									+ getResources()
											.getString(
													R.string.punktehinzufuegen_service_notification_gespeichert),
							getResources().getString(R.string.lat_kurz)
									+ String.valueOf(geopunkt_position
											.getLatitudeE6())
									+ " "
									+ getResources().getString(
											R.string.lon_kurz)
									+ String.valueOf(geopunkt_position
											.getLongitudeE6()), PendingIntent
									.getBroadcast(getApplicationContext(), 0,
											new Intent(),
											Notification.FLAG_AUTO_CANCEL));
			notificationmanager_verwaltung.notify(NOTIFICATION_ID,
					notification_nachricht);
		} else {
			// hinterlege das der punkt mit folgender methode erneut verarbeitet
			// werden soll

			if (boolean_pos_aus_gps) {

				contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_11,
						NICHT_VERARBEITET_GPS);
			} else {

				contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_11,
						NICHT_VERARBEITET_ADRESSE);
			}

			notification_nachricht
					.setLatestEventInfo(
							getApplicationContext(),
							string_name
									+ " "
									+ getResources()
											.getString(
													R.string.punktehinzufuegen_service_notification_vorgemerkt),
							getResources()
									.getString(
											R.string.punktehinzufuegen_service_notification_wird_demnaechst_gespeichert),
							PendingIntent.getBroadcast(getApplicationContext(),
									0, new Intent(),
									Notification.FLAG_AUTO_CANCEL));
			notificationmanager_verwaltung.notify(NOTIFICATION_ID,
					notification_nachricht);
		}

		if (boolean_erneut_verarbeiten && (long_id >= 0)) {
			// aktualisiere den erneut verarbeiteten punkt

			memosingleton_anwendung.sqldatabase_writeable.update(
					SQL_DB_Verwaltung.TABELLEN_NAME_SYNCH, contentvalues_werte,
					SQL_DB_Verwaltung.NAME_SPALTE_1 + "=?",
					new String[] { String.valueOf(long_id) });
		} else {

			memosingleton_anwendung.sqldatabase_writeable.insert(
					SQL_DB_Verwaltung.TABELLEN_NAME_SYNCH, null,
					contentvalues_werte);
		}

		if (boolean_pos_aus_gps
				&& intent_service.getBooleanExtra(getPackageName() + "_"
						+ "boolean_letzte_zeile", true)) {

			Intent intent_befehl = new Intent(MemoSingleton.INTENT_STOPPE_GPS);
			intent_befehl.putExtra(getPackageName() + "_" + "int_listener",
					MemoSingleton.GPS_LISTENER_SERVICE);
			this.sendBroadcast(intent_befehl);
		}
	}

	/**
	 * {@code public {@link GeoPunkt} adresseAufloesen(String string_adresse)}
	 * <p/>
	 * Nutzt die übergebene Adresse als Suchanfrage bei Google und ermittelt
	 * deren Geokoordinaten. Nur ein Ergebnis wird ausgewertet.
	 * 
	 * @param string_adresse
	 *            {@link String} mit den Adressdaten
	 * @return {@link GeoPunkt} mit den empfangenen Geokoordinaten
	 */
	public GeoPunkt adresseAufloesen(String string_adresse) {
		Geocoder geocoder_geokodierung = new Geocoder(getApplicationContext());

		List<Address> list_ergebnis = null;

		NetworkInfo networkinfo_internet = ((ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();

		if ((networkinfo_internet != null)
				&& (networkinfo_internet.isAvailable())) {

			try {

				if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO) {

					list_ergebnis = geocoder_geokodierung.getFromLocationName(
							string_adresse, 1);
				} else {

					if (Geocoder.isPresent()) {

						list_ergebnis = geocoder_geokodierung
								.getFromLocationName(string_adresse, 1);
					}

				}
			} catch (Exception e) {

				Log.d("memo_debug", e.toString());
			}
		}

		if ((list_ergebnis != null) && (!list_ergebnis.isEmpty())) {

			for (Address address_adresse : list_ergebnis) {

				if (address_adresse.hasLatitude()
						&& address_adresse.hasLongitude()) {

					return new GeoPunkt(
							((Double) (address_adresse.getLatitude() * 1e6))
									.intValue(),
							((Double) (address_adresse.getLongitude() * 1e6))
									.intValue());
				}
			}
		}

		return null;
	}
}
