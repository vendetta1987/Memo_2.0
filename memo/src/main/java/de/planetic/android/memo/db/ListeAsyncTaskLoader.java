//package de.planetic.android.memo.db;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import android.content.Context;
//import android.support.v4.content.AsyncTaskLoader;
//import android.util.Log;
//
//public class ListeAsyncTaskLoader extends
//		AsyncTaskLoader<ArrayList<HashMap<String, String>>> {
//
//	private DBLesenSchreiben db_rw;
//	private ArrayList<HashMap<String, String>> arraylist_return;
//	private boolean boolean_inhalt_veraendert;
//
//	public long long_letzte_id;
//
//	public ListeAsyncTaskLoader(Context context) {
//		super(context);
//
//		db_rw = new DBLesenSchreiben(getContext());
//		boolean_inhalt_veraendert = false;
//		long_letzte_id = 0;
//		arraylist_return = new ArrayList<HashMap<String, String>>();
//	}
//
//	/**
//	 * {@code protected void onStartLoading()}
//	 * <p/>
//	 * Startet die Überwachung durch den {@link AsyncTaskLoader} und gibt
//	 * Ergebnisse direkt zurück, falls keine Veränderugen vorliegen und bereits
//	 * Daten vorhanden sind. Gemeldete Veränderungen bewirken das erneute
//	 * Einlesen der Daten.
//	 */
//	@Override
//	protected void onStartLoading() {
//
//		if (!boolean_inhalt_veraendert && (long_letzte_id > 0)) {
//
//			deliverResult(arraylist_return);
//		} else {
//
//			forceLoad();
//		}
//	}
//
//	/**
//	 * {@codeprotected void onReset()}
//	 * <p/>
//	 * Setzt den {@link AsyncTaskLoader} auf den Ausgangszustand zurück. Beim
//	 * nächsten Aufruf von {@code onStartLoading()} werden alle Daten neu
//	 * geladen.
//	 */
//	@Override
//	protected void onReset() {
//
//		long_letzte_id = 0;
//		arraylist_return = new ArrayList<HashMap<String, String>>();
//		super.onReset();
//	}
//
//	/**
//	 * {@code public void onContentChanged()}
//	 * <p/>
//	 * Setzt {@code boolean_inhalt_veraendert} auf {@code true} und ruft
//	 * anschließend {@code forceLoad()} auf.
//	 */
//	@Override
//	public void onContentChanged() {
//
//		boolean_inhalt_veraendert = true;
//
//		if (isReset()) {
//			Log.d("", "");
//		}
//
//		super.onContentChanged();
//	}
//
//	/**
//	 * {@code public void deliverResult(
//			ArrayList<HashMap<String, String>> arraylist_ergebnis)}
//	 * <p/>
//	 * Gibt die ausgelesenen Daten zurück falls die Überwachung aktiv ist
//	 * 
//	 * @param arraylist_ergebnis
//	 *            {@link ArrayList} mit den ausgelesenen Daten in
//	 *            {@link HashMap}
//	 */
//	@Override
//	public void deliverResult(
//			ArrayList<HashMap<String, String>> arraylist_ergebnis) {
//
//		if (!isReset()) {
//
//			super.deliverResult(arraylist_ergebnis);
//		}
//	}
//
//	/**
//	 * {@codepublic ArrayList<HashMap<String, String>> loadInBackground()}
//	 * <p/>
//	 * Liest die Daten in temporäre Variablen aus und fügt sie abschließend dem
//	 * gespeicherten Datensatz hinzu. Wird der Vorgang durch {@code reset()}
//	 * unterbrochen, bleiben die Daten auf dem vorherigen Stand.
//	 * 
//	 * @return {@link ArrayList} mit einer {@link HashMap} pro ausgelesener
//	 *         {@link Ladestation}
//	 */
//	@Override
//	public ArrayList<HashMap<String, String>> loadInBackground() {
//
//		long long_temp;
//		HashMap<String, String> hashmap_station;
//		ArrayList<Ladestation> arraylist_stationen = db_rw.leseLadestation(
//				long_letzte_id, false, true);
//		ArrayList<HashMap<String, String>> arraylist_temp = new ArrayList<HashMap<String, String>>();
//
//		long_temp = long_letzte_id;
//		arraylist_temp.addAll(arraylist_return);
//
//		for (Ladestation ladestation_saeule : arraylist_stationen) {
//
//			if (isReset()) {
//
//				break;
//			}
//
//			hashmap_station = new HashMap<String, String>();
//
//			hashmap_station.put("bezeichnung",
//					ladestation_saeule.string_bezeichnung);
//			hashmap_station.put("verfuegbarkeit",
//					ladestation_saeule.leseVerfuegbarkeit());
//			hashmap_station.put("id",
//					String.valueOf(ladestation_saeule.long_id));
//
//			long_temp++;
//			arraylist_temp.add(hashmap_station);
//		}
//
//		if (!isReset()) {
//
//			long_letzte_id = long_temp;
//			arraylist_return = arraylist_temp;
//			boolean_inhalt_veraendert = false;
//		}
//
//		return arraylist_return;
//	}
//
// }
