package de.planetic.android.memo_neu;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import android.app.Application;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.maps.Overlay;

public class MemoSingleton extends Application {

	// werden zur unterscheidung bei aktualisiereDBZugrif genutzt
	public static final int LISTE = 0;
	public static final int KARTE = 1;
	public static final int ZURUECKSETZEN = 2;
	public static final String INTENT_DB_FUELLEN = "db_fuellen";
	public static final String INTENT_DB_LEEREN = "db_leeren";

	// speichert listeneintraege
	// speichert karteneintraege
	public List<HashMap<String, Object>> list_liste_daten;
	public List<Overlay> list_karte_overlays;

	// speichern letzten lesezugriff auf db in ms seit 1.1.1970
	private long long_letzter_db_zugriff_liste;
	private long long_letzter_db_zugriff_karte;

	public GPS_Verwaltung gps_verwaltung;

	@Override
	public void onCreate() {
		super.onCreate();
		long_letzter_db_zugriff_liste = 0;
		long_letzter_db_zugriff_karte = 0;

		list_liste_daten = new ArrayList<HashMap<String, Object>>();
		list_karte_overlays = new ArrayList<Overlay>();

		gps_verwaltung = new GPS_Verwaltung(getApplicationContext());
	}

	// aktualisiert zeitstempel bei letzten zugriff der activities um nur neue
	// eintraege zu beruecksichtigen
	public void aktualisiereDBZugriff(int int_klasse, long int_id) {
		switch (int_klasse) {
		case LISTE:
			long_letzter_db_zugriff_liste = int_id;
			break;
		case KARTE:
			long_letzter_db_zugriff_karte = int_id;
			break;
		case ZURUECKSETZEN:
			long_letzter_db_zugriff_liste = 0;
			long_letzter_db_zugriff_karte = 0;
			list_liste_daten.clear();
			list_karte_overlays.clear();
			break;
		default:
		}
	}

	// gibt letzten zugriff zeitstempel zurueck
	public long letzterDBZugriff(int int_klasse) {
		switch (int_klasse) {
		case LISTE:
			return long_letzter_db_zugriff_liste;
		case KARTE:
			return long_letzter_db_zugriff_karte;
		default:
			return 0;
		}
	}
}
