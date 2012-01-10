package de.planetic.android.memo;

import com.google.android.maps.GeoPoint;

/**
 * Eigene Implementierung von {@link GeoPoint}. Fügt weitere Eigenschaften
 * hinzu: Name des Punktes, zugehöriges Symbol, DB-ID und Zeitpunkt des
 * Hinzufügens
 * 
 */
public class GeoPunkt extends GeoPoint {

	public String string_name;
	public String string_icon;
	public long long_id;
	public long long_zeit;

	/**
	 * Konstruktor für leeren GeoPunkt mit Koordinaten 0,0; Name="GeoPunkt";
	 * Symbol="icon"; DB-ID=0; Zeitpunkt=0
	 */
	public GeoPunkt() {
		super(0, 0);
		string_name = "GeoPunkt";
		string_icon = "icon";
		long_id = 0;
		long_zeit = 0;
	}

	/**
	 * Konstruktor für Übergabe von Geokoordinaten.
	 * Name="GeoPunkt";Symbol="icon"; DB-ID=0; Zeitpunkt=0
	 * 
	 * @param int_lat
	 *            Breitengrad in Mikrograd.
	 * @param int_lon
	 *            Längengrad in Mikrograd.
	 */
	public GeoPunkt(int int_lat, int int_lon) {
		super(int_lat, int_lon);
		string_name = "GeoPunkt";
		string_icon = "icon";
		long_id = 0;
		long_zeit = 0;
	}

	/**
	 * Konstruktor mit allen Parametern.
	 * 
	 * @param int_lat
	 *            Breitengrad in Mikrograd.
	 * @param int_lon
	 *            Längengrad in Mikrograd.
	 * @param long_id
	 *            Datenbank-ID.
	 * @param string_icon
	 *            Name des Symbols in Ressourcen oder Symbolordner.
	 * @param string_name
	 *            Bezeichnung des Punktes.
	 * @param long_zeit
	 *            Zeitpunkt des DB-Eintrags.
	 */
	private GeoPunkt(int int_lat, int int_lon, long long_id,
			String string_icon, String string_name, long long_zeit) {
		super(int_lat, int_lon);
		this.long_id = long_id;
		this.string_icon = string_icon;
		this.long_zeit = long_zeit;
		this.string_name = string_name;
	}

	/**
	 * Funktion zum Setzen des Breitengrades.
	 * 
	 * @param int_lat
	 *            Breitengrad in Mikrograd
	 * @return Ein neues Objekt mit den gleichen Attributen und aktualisiertem
	 *         Breitengrad.
	 */
	public GeoPunkt setLatitude(int int_lat) {
		return new GeoPunkt(int_lat, this.getLongitudeE6(), long_id,
				string_icon, string_name, long_zeit);
	}

	/**
	 * Funktion zum Setzen des Längengrades.
	 * 
	 * @param int_lon
	 *            Längengrad in Mikrograd
	 * @return Ein neues Objekt mit den gleichen Attributen und aktualisiertem
	 *         Längengrad.
	 */
	public GeoPunkt setLongitude(int int_lon) {
		return new GeoPunkt(this.getLatitudeE6(), int_lon, long_id,
				string_icon, string_name, long_zeit);
	}
}
