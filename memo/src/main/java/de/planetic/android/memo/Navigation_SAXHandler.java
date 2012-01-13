package de.planetic.android.memo;

import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

/**
 * Eigene Implementierung eines SAX-Handlers. Durchsucht die Antwort des Google
 * Directions API nach den benötigten Daten.
 * <p/>
 * {@code string_grob_kodiert} beinhalter die Koordinaten des groben
 * Routenpfades in kodierter Form<br/>
 * {@code string_status} beinhaltet die Statusangabe der Antwort<br/>
 * {@code string_urheberrecht} beinhaltet die Angaben zum Urheberrecht der
 * Karte/Route<br/>
 * {@code arraylist_html_anweisungen} beinhaltet die Navigationsanweisungen<br/>
 * 
 * @see Navigation_AsyncTask
 */
public class Navigation_SAXHandler extends DefaultHandler {

	public String string_grob_kodiert, string_status, string_urheberrecht;
	public ArrayList<String> arraylist_fein_kodiert;
	public HashMap<String, ArrayList<HashMap<String, String>>> hashmap_html_anweisungen;

	private boolean boolean_overview_polyline, boolean_points, boolean_status,
			boolean_urheberrecht, boolean_step, boolean_html,
			boolean_start_location, boolean_lat, boolean_lon, boolean_distanz,
			boolean_value;
	private StringBuilder stringbuilder_temp;
	private HashMap<String, String> hashmap_temp;

	/**
	 * Speichert die Naviagtionsanweisungen in der {@link HashMap}
	 * {@code hashmap_html_anweisungen}.<br/>
	 * Jede Anweisung wird als {@link HashMap} übergeben. Daraus wird
	 * {@code string_schluessel} in der Form "G_ggb_ggl_M_mmb_mml" erzeugt (gg -
	 * Grad, mm - Minuten, b - Breite, l - Länge). Unter diesem Schlüssel wird
	 * in {@code hashmap_html_anweisungen} eine {@link ArrayList} eingetragen
	 * die {@code hashmap_daten} beinhaltet.
	 * 
	 * @param hashmap_daten
	 *            {@link Hashmap} mit den Einträgen {@code string_lat},
	 *            {@code string_lon}, {@code string_html} und
	 *            {@code string_distanz}
	 * @see GPS_Verwaltung
	 */
	private void erzeugeHTMLAnweisungen(HashMap<String, String> hashmap_daten) {

		int int_lat = ((Double) (Double.parseDouble(hashmap_daten
				.get("string_lat")) * 1e6)).intValue();
		int int_lon = ((Double) (Double.parseDouble(hashmap_daten
				.get("string_lon")) * 1e6)).intValue();
		int int_lat_temp, int_lon_temp;
		String string_schluessel;

		int_lat_temp = int_lat / 1000000;
		int_lon_temp = int_lon / 1000000;

		string_schluessel = "G_" + String.valueOf(int_lat_temp) + "_"
				+ String.valueOf(int_lon_temp) + "_M_"
				+ String.valueOf((int_lat - (int_lat_temp * 1000000)) / 10000)
				+ "_"
				+ String.valueOf((int_lon - (int_lon_temp * 1000000)) / 10000);

		if (!hashmap_html_anweisungen.containsKey(string_schluessel)) {

			hashmap_html_anweisungen.put(string_schluessel,
					new ArrayList<HashMap<String, String>>());
			hashmap_html_anweisungen.get(string_schluessel).add(hashmap_daten);
		} else {

			if (!hashmap_html_anweisungen.get(string_schluessel).contains(
					hashmap_daten)) {

				hashmap_html_anweisungen.get(string_schluessel).add(
						hashmap_daten);
			}
		}
	}

	/**
	 * Wird beim Start des Dokumentes aufgerufen und initialisiert die
	 * Variablen.
	 */
	@Override
	public void startDocument() {

		boolean_step = false;
		boolean_overview_polyline = false;
		boolean_points = false;
		boolean_status = false;
		boolean_urheberrecht = false;
		boolean_html = false;
		boolean_start_location = false;
		boolean_lat = false;
		boolean_lon = false;
		boolean_distanz = false;
		boolean_value = false;
		string_status = new String();
		string_grob_kodiert = new String();
		string_urheberrecht = new String();
		arraylist_fein_kodiert = new ArrayList<String>();
		hashmap_html_anweisungen = new HashMap<String, ArrayList<HashMap<String, String>>>();

		Log.d("memo_debug_naviagtion_saxhandler", "startdocument");
	}

	/**
	 * Wird zu Beginn eines XML-Tags aufgerufen und prüft den Namen des Tags.
	 * Die {@link Boolean}-Variablen signalisieren geöffnete Tags und
	 * ermöglichen die Erfassung der Daten in der Methode
	 * {@code characters(...)}. Zusätzlich werden temporär genutzte Variablen
	 * initialisiert.
	 */
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) {

		if (localName.equalsIgnoreCase("status")) {

			boolean_status = true;
		} else if (localName.equalsIgnoreCase("step")) {

			boolean_step = true;
		} else if (localName.equalsIgnoreCase("start_location")) {

			boolean_start_location = true;
			hashmap_temp = new HashMap<String, String>();
		} else if (localName.equalsIgnoreCase("lat")) {

			boolean_lat = true;
		} else if (localName.equalsIgnoreCase("lng")) {

			boolean_lon = true;
		} else if (localName.equalsIgnoreCase("points")) {

			boolean_points = true;
			stringbuilder_temp = new StringBuilder();
		} else if (localName.equalsIgnoreCase("html_instructions")) {

			boolean_html = true;
			stringbuilder_temp = new StringBuilder();
		} else if (localName.equalsIgnoreCase("distance")) {

			boolean_distanz = true;
		} else if (localName.equalsIgnoreCase("value")) {

			boolean_value = true;
		} else if (localName.equalsIgnoreCase("copyrights")) {

			boolean_urheberrecht = true;
		} else if (localName.equalsIgnoreCase("overview_polyline")) {

			boolean_overview_polyline = true;
		}
	}

	/**
	 * Wird für die Daten innerhalb eines XML-Tags aufgerufen und ermöglicht das
	 * Auslesen dieser. Die {@link Boolean}-Variablen dienen der Zuordnung der
	 * Inhalte zu Tags und bestimmen die Verarbeitung der Daten. Umfangreiche
	 * Inhalte können zu mehrfachem Aufruf dieser Methode führen, wobei die
	 * Daten schrittweise ausgelesen werden.
	 */
	@Override
	public void characters(char[] ch, int start, int length) {

		if (boolean_status) {

			string_status = string_status.concat(new String(ch, start, length));
		} else if (boolean_step && boolean_start_location && boolean_lat) {

			hashmap_temp.put("string_lat", new String(ch, start, length));
			// hashmap_temp.put("string_lat", Integer.toString(new
			// Double((Double
			// .parseDouble(new String(ch, start, length)) * 10e5))
			// .intValue()));
		} else if (boolean_step && boolean_start_location && boolean_lon) {

			hashmap_temp.put("string_lon", new String(ch, start, length));
			// hashmap_temp.put("string_lon", Integer.toString(new
			// Double((Double
			// .parseDouble(new String(ch, start, length)) * 10e5))
			// .intValue()));
		} else if (boolean_step && boolean_points) {

			stringbuilder_temp.append(new String(ch, start, length));
		} else if (boolean_step && boolean_html) {

			stringbuilder_temp.append(new String(ch, start, length));
		} else if (boolean_step && boolean_distanz && boolean_value) {

			hashmap_temp.put("string_distanz", new String(ch, start, length));
		} else if (boolean_urheberrecht) {

			string_urheberrecht = string_urheberrecht.concat(new String(ch,
					start, length));
		} else if (boolean_overview_polyline && boolean_points) {

			string_grob_kodiert = string_grob_kodiert.concat(new String(ch,
					start, length));
		}
	}

	/**
	 * Wird mit jedem schließenden XML-Tag aufgerufen und setzt die
	 * {@link Boolean}-Flags entsprechend zurück. Die in {@code characters(...)}
	 * verarbeiteten Daten werden gespeichert.
	 */
	@Override
	public void endElement(String uri, String localName, String qName) {

		if (localName.equalsIgnoreCase("status")) {

			boolean_status = false;
		} else if (localName.equalsIgnoreCase("step")) {

			boolean_step = false;
		} else if (localName.equalsIgnoreCase("start_location")) {

			boolean_start_location = false;
		} else if (localName.equalsIgnoreCase("lat")) {

			boolean_lat = false;
		} else if (localName.equalsIgnoreCase("lng")) {

			boolean_lon = false;
		} else if (localName.equalsIgnoreCase("points")) {

			boolean_points = false;
			if (!boolean_overview_polyline) {

				arraylist_fein_kodiert.add(stringbuilder_temp.toString());
			}
		} else if (localName.equalsIgnoreCase("html_instructions")) {

			boolean_html = false;

			hashmap_temp.put("string_html", stringbuilder_temp.toString()
					.replaceAll("<(.|\n)*?>", ""));
		} else if (localName.equalsIgnoreCase("distance")) {

			boolean_distanz = false;

			erzeugeHTMLAnweisungen(hashmap_temp);
		} else if (localName.equalsIgnoreCase("value")) {

			boolean_value = false;
		} else if (localName.equalsIgnoreCase("copyrights")) {

			boolean_urheberrecht = false;
		} else if (localName.equalsIgnoreCase("overview_polyline")) {

			boolean_overview_polyline = false;
		}
	}

	@Override
	public void endDocument() {

		Log.d("memo_debug_navigation_saxhandler", "enddocument");
	}
}
