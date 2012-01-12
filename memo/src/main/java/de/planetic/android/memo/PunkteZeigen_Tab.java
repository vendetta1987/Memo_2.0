package de.planetic.android.memo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Random;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.Toast;

/**
 * {@link TabActivity} zur Darstellung der Punkte in der Datenbank. Angezeigt
 * werden eine Liste und eine Karte.
 * <p/>
 * {@code TAB_LISTE} und {@code TAB_KARTE} ermöglichen den Zugriff auf die Tabs
 * über {@link TabHost}.
 * 
 */
public class PunkteZeigen_Tab extends TabActivity implements OnCancelListener {

	private static final int DIALOG_GEOPUNKT_HINZUFUEGEN = 0;
	private static final int DIALOG_PUNKTE_FILTERN = 1;
	private static final String ICON_NAME = "icon_name";
	private static final String ICON_DATEI = "icon_datei";

	public static final int TAB_LISTE = 0;
	public static final int TAB_KARTE = 1;

	public TabHost tabhost;

	private MemoSingleton memosingleton_anwendung;

	/**
	 * Erzeugt die {@link TabActivity} und startet
	 * {@link PunkteZeigen_Tab_Liste} sowie {@link PunkteZeigen_Tab_Karte}
	 * darin. Außerdem wird {@link PunkteHinzufuegen_Service_AsyncTask} beim
	 * ersten Start der Anwendung ausgeführt.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.punktezeigen_tab_layout);

		memosingleton_anwendung = (MemoSingleton) getApplication();

		// speichere context im singleton um spaeter dialog fuer geopunkte
		// anzeigen zu koennen
		memosingleton_anwendung.context_punktezeigen_tab = this;

		Resources res = getResources(); // Resource object to get Drawables
		tabhost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, PunkteZeigen_Tab_Liste.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabhost.newTabSpec("PunkteZeigenListe")
				.setIndicator("Liste", res.getDrawable(R.drawable.icon))
				.setContent(intent);

		tabhost.addTab(spec);

		// Do the same for the other tabs
		intent = new Intent().setClass(this, PunkteZeigen_Tab_Karte.class);

		spec = tabhost.newTabSpec("PunkteZeigenKarte")
				.setIndicator("Karte", res.getDrawable(R.drawable.icon))
				.setContent(intent);
		tabhost.addTab(spec);

		tabhost.setCurrentTab(TAB_LISTE);

		// TODO aufruf bei drehung <-> start
		if (savedInstanceState == null) {

			new PunkteHinzufuegen_Service_AsyncTask(this).execute();
		}

		// http://developer.android.com/resources/tutorials/views/hello-tabwidget.html
	}

	/**
	 * Setzt {@code boolean_gedreht} in {@link MemoSingleton} um Drehung des
	 * Gerätes zu signalisieren.
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {

		memosingleton_anwendung.boolean_gedreht = true;

		super.onSaveInstanceState(outState);
	}

	/**
	 * Stoppt beim Beenden der {@link TabActivity} alle {@link LocationListener}
	 * in {@link GPS_Verwaltung}.
	 */
	@Override
	public void onDestroy() {

		Intent intent_befehl = new Intent(MemoSingleton.INTENT_STOPPE_GPS);
		intent_befehl.putExtra(getPackageName() + "_" + "int_listener",
				MemoSingleton.GPS_LISTENER_ALLE);
		sendBroadcast(intent_befehl);

		super.onDestroy();
	}

	/**
	 * Erzeugt das Menü aus den Resourcen.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.punktezeigen_tab_menu, menu);

		// Menue mit gegebenem Layout erstellen
		return true;
	}

	/**
	 * Ändert das angezeigte Menü entsprechend des aktuellen Tab und abhängig
	 * von den aktivierten GPS-{@link LocationListener}.
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		if ((tabhost.getCurrentTab() == 0)
				|| !memosingleton_anwendung.gps_verwaltung.gpsVerfuegbar(false)) {

			menu.removeItem(R.id.punktezeigen_tab_menu_item5);
		}

		if (memosingleton_anwendung.boolean_aktuelle_position) {

			menu.findItem(R.id.punktezeigen_tab_menu_item5).setTitle(
					getResources().getString(
							R.string.punktezeigen_tab_menu_item5_aus_title));
		} else {

			try {

				menu.findItem(R.id.punktezeigen_tab_menu_item5).setTitle(
						getResources().getString(
								R.string.punktezeigen_tab_menu_item5_an_title));
			} catch (Exception e) {

				e.printStackTrace();
			}
		}

		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Wertet den gewählten Menüpunkt aus und ruft die entsprechenden Funktionen
	 * auf.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent intent_befehl = new Intent();

		switch (item.getItemId()) {
		case R.id.punktezeigen_tab_menu_item1:
			// db fuellen
			dbFuellen();
			sendBroadcast(new Intent(MemoSingleton.INTENT_DB_FUELLEN));
			break;
		case R.id.punktezeigen_tab_menu_item2:
			// db leeren
			dbLeeren();
			intent_befehl.setAction(MemoSingleton.INTENT_DB_LEEREN);
			sendBroadcast(intent_befehl);
			break;
		case R.id.punktezeigen_tab_menu_item3:
			// punkt hinzufuegen
			showDialog(DIALOG_GEOPUNKT_HINZUFUEGEN);
			break;
		case R.id.punktezeigen_tab_menu_item4:
			// punkte filtern
			showDialog(DIALOG_PUNKTE_FILTERN);
			break;
		case R.id.punktezeigen_tab_menu_item5:
			// aktuelle Position
			aktuellePositionVerwalten();
			break;
		default:
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Automatisch aufgerufen, sobald {@code showDialog()} benutzt wird, um
	 * einen Dialog für das Hinzufügen von Punkten zur Datenbank oder das
	 * Filtern der DB zu erzeugen.
	 */
	@Override
	protected Dialog onCreateDialog(int int_id) {

		Dialog dialog = new Dialog(this);

		switch (int_id) {
		case DIALOG_GEOPUNKT_HINZUFUEGEN:
			return erzeugeDialogGeoPktHinzufuegen(dialog);
		case DIALOG_PUNKTE_FILTERN:
			return erzeugeDialogPunkteFiltern(dialog);
		default:
			return null;
		}
	}

	/**
	 * Automatisch aufgerufen sobald ein Dialog beendet wird.
	 */
	public void onCancel(DialogInterface dialog) {

		try {

			removeDialog(DIALOG_GEOPUNKT_HINZUFUEGEN);
			removeDialog(DIALOG_PUNKTE_FILTERN);
		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * {@code public void aktuellePositionVerwalten()}
	 * <p/>
	 * Startet oder stoppt den {@link LocationListener} für die aktuelle
	 * Position in {@link GPS_Verwaltung}. Die Funktion wird über das Menü oder
	 * über den Knopf "Position verfolgen", eingeblendet während der Navigation,
	 * per {@link Intent} aufgerufen.
	 */
	public void aktuellePositionVerwalten() {
		Intent intent_befehl = new Intent();

		if (memosingleton_anwendung.gps_verwaltung.gpsVerfuegbar(false)) {

			Button button_position_verfolgen = ((Button) tabhost
					.getCurrentView().findViewById(
							R.id.punktezeigen_karte_layout_button2));

			if (!memosingleton_anwendung.boolean_aktuelle_position) {

				intent_befehl.setAction(MemoSingleton.INTENT_STARTE_GPS);
				button_position_verfolgen
						.setText(R.string.navigation_text_aktuelle_position_aus);
			} else {

				intent_befehl.setAction(MemoSingleton.INTENT_STOPPE_GPS);
				button_position_verfolgen
						.setText(R.string.navigation_text_aktuelle_position_an);
			}

			intent_befehl.putExtra(getPackageName() + "_" + "int_listener",
					MemoSingleton.GPS_LISTENER_AKTUELL);
			sendBroadcast(intent_befehl);
		}
	}

	/**
	 * {@code private {@link Dialog} erzeugeDialogGeoPktHinzufuegen(final
	 * Dialog dialog)}
	 * <p/>
	 * Erzeugt einen Dialog für das Hinzufügen eines Punktes in die Datenbank.
	 * 
	 * @param dialog
	 *            {@link Dialog}-Objekt das zuvor erstellt wurde
	 * @return verarbeiteter {@link Dialog}
	 */
	private Dialog erzeugeDialogGeoPktHinzufuegen(final Dialog dialog) {

		boolean boolean_gps_verfuegbar = memosingleton_anwendung.gps_verwaltung
				.gpsVerfuegbar(true);

		dialog.setContentView(R.layout.punktezeigen_tab_dialog_pkthinzufuegen);
		dialog.setTitle(R.string.punktezeigen_tab_dialog_pkthinzufuegen_title);
		dialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		dialog.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_button1)
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						// OK

						if (werteDialogAus(dialog, DIALOG_GEOPUNKT_HINZUFUEGEN)) {
							removeDialog(DIALOG_GEOPUNKT_HINZUFUEGEN);
						}
					}
				});
		dialog.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_button2)
				.setOnClickListener(new OnClickListener() {
					public void onClick(View v) {
						// Abbrechen

						dialog.cancel();
					}
				});

		CheckBox checkbox_gps = (CheckBox) dialog
				.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_checkBox1);

		checkbox_gps.setClickable(boolean_gps_verfuegbar);
		checkbox_gps.setChecked(boolean_gps_verfuegbar);
		checkbox_gps.setEnabled(boolean_gps_verfuegbar);
		checkbox_gps.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {

				EditText edittext_adresse = (EditText) dialog
						.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_editText3);

				edittext_adresse.setFocusable(!isChecked);
				edittext_adresse.setFocusableInTouchMode(!isChecked);
				edittext_adresse.setEnabled(!isChecked);
			}
		});

		dialog.findViewById(
				R.id.punktezeigen_tab_dialog_pkthinzufuegen_editText3)
				.setFocusable(!boolean_gps_verfuegbar);
		dialog.findViewById(
				R.id.punktezeigen_tab_dialog_pkthinzufuegen_editText3)
				.setEnabled(!boolean_gps_verfuegbar);

		Spinner spinner_icon = (Spinner) dialog
				.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_spinner1);

		Cursor cursor_abfrage = memosingleton_anwendung.sqldatabase_readable
				.query(SQL_DB_Verwaltung.TABELLEN_NAME_HAUPT,
						new String[] { SQL_DB_Verwaltung.NAME_SPALTE_5 }, null,
						null, SQL_DB_Verwaltung.NAME_SPALTE_5, null, null);

		ArrayList<HashMap<String, Object>> arraylist_spinnerdaten = new ArrayList<HashMap<String, Object>>(
				cursor_abfrage.getCount());
		HashMap<String, Object> hashmap_spinnereintrag;
		String string_icon;

		if (cursor_abfrage.moveToFirst()) {

			do {

				hashmap_spinnereintrag = new HashMap<String, Object>(2);

				string_icon = cursor_abfrage.getString(cursor_abfrage
						.getColumnIndex(SQL_DB_Verwaltung.NAME_SPALTE_5));

				hashmap_spinnereintrag.put(ICON_NAME, string_icon);
				hashmap_spinnereintrag.put(ICON_DATEI,
						memosingleton_anwendung.getSymbol(string_icon, true));

				arraylist_spinnerdaten.add(hashmap_spinnereintrag);

			} while (cursor_abfrage.moveToNext());
		}

		cursor_abfrage.close();

		// TODO mindestens ein symbol mitliefern
		if (arraylist_spinnerdaten.isEmpty()) {

			hashmap_spinnereintrag = new HashMap<String, Object>(2);
			hashmap_spinnereintrag.put(ICON_NAME, "icon");
			hashmap_spinnereintrag.put(ICON_DATEI,
					memosingleton_anwendung.getSymbol("icon", true));
			arraylist_spinnerdaten.add(hashmap_spinnereintrag);
		}

		spinner_icon.setAdapter(new PunkteHinzufuegen_Dialog_SpinnerAdapter(
				this, arraylist_spinnerdaten));

		dialog.setOnCancelListener(this);

		return dialog;
	}

	/**
	 * {@code private {@link Dialog} erzeugeDialogPunkteFiltern(final Dialog
	 * dialog)}
	 * <p/>
	 * Erzeugt Dialog zur Einstellung des anzuwendenden Filters.
	 * 
	 * @param dialog
	 *            {@link Dialog}-Objekt das zuvor erzeugt wurde
	 * @return verarbeiteter {@link Dialog}
	 */
	private Dialog erzeugeDialogPunkteFiltern(final Dialog dialog) {

		dialog.setContentView(R.layout.punktezeigen_tab_dialog_pktfiltern);
		dialog.setTitle(R.string.punktezeigen_tab_dialog_pktfiltern_title);
		dialog.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

		((CheckBox) dialog
				.findViewById(R.id.punktezeigen_tab_dialog_pktfiltern_checkBox1))
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {

						EditText edittext_adresse = (EditText) dialog
								.findViewById(R.id.punktezeigen_tab_dialog_pktfiltern_editText1);
						if (isChecked) {

							edittext_adresse.setVisibility(View.VISIBLE);
						} else {

							edittext_adresse.setVisibility(View.GONE);
						}
					}
				});

		((CheckBox) dialog
				.findViewById(R.id.punktezeigen_tab_dialog_pktfiltern_checkBox2))
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {

						EditText edittext_name = (EditText) dialog
								.findViewById(R.id.punktezeigen_tab_dialog_pktfiltern_editText2);
						if (isChecked) {

							edittext_name.setVisibility(View.VISIBLE);
						} else {

							edittext_name.setVisibility(View.GONE);
						}
					}
				});

		((CheckBox) dialog
				.findViewById(R.id.punktezeigen_tab_dialog_pktfiltern_checkBox3))
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {

						LinearLayout linearlayout_preis = (LinearLayout) dialog
								.findViewById(R.id.punktezeigen_tab_dialog_pktfiltern_linearLayout1);
						if (isChecked) {

							linearlayout_preis.setVisibility(View.VISIBLE);
						} else {

							linearlayout_preis.setVisibility(View.GONE);
						}
					}
				});

		ArrayAdapter<CharSequence> arrayadapter_spinner = ArrayAdapter
				.createFromResource(
						this,
						R.array.punktezeigen_tab_dialog_pktfiltern_spinner1_stringarray,
						android.R.layout.simple_spinner_item);
		arrayadapter_spinner
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		((Spinner) dialog
				.findViewById(R.id.punktezeigen_tab_dialog_pktfiltern_spinner1))
				.setAdapter(arrayadapter_spinner);

		((Button) dialog
				.findViewById(R.id.punktezeigen_tab_dialog_pktfiltern_button1))
				.setOnClickListener(new OnClickListener() {
					// OK

					public void onClick(View v) {

						if (werteDialogAus(dialog, DIALOG_PUNKTE_FILTERN)) {

							removeDialog(DIALOG_PUNKTE_FILTERN);
						}
					}
				});

		((Button) dialog
				.findViewById(R.id.punktezeigen_tab_dialog_pktfiltern_button2))
				.setOnClickListener(new OnClickListener() {
					// Abbrechen

					public void onClick(View v) {

						dialog.cancel();
					}
				});

		return dialog;
	}

	/**
	 * {@code private boolean werteDialogAus(Dialog dialog, int int_dialog_id)}
	 * <p/>
	 * Wertet den übergebenen {@link Dialog} je nach {@code int_dialog_id} aus.
	 * Alle Felder werden geprüft und nur bei korrektem Inhalt wird die
	 * Verarbeitung per {@link Intent} fortgesetzt. Entweder wird
	 * {@link PunkteHinzufuegen_Service_AsyncTask} aufgerufen oder ein Filter
	 * auf die aktuelle Anzeige angewand.
	 * 
	 * @param dialog
	 *            auszuwertender {@link Dialog}
	 * @param int_dialog_id
	 *            {@code DIALOG_GEOPUNKT_HINZUFUEGEN} oder
	 *            {@code DIALOG_PUNKTE_FILTERN}
	 * @return {@code boolean} entsprechend der erfolgreichen Verarbeitung
	 */
	private boolean werteDialogAus(Dialog dialog, int int_dialog_id) {

		String string_fehler = "", string_name, string_beschreibung, string_adresse, string_radio_eigenschaften, string_groesserkleiner, string_icon;
		double double_preis;
		boolean boolean_ergebnis = true, boolean_pos_aus_gps;
		int int_filter_checkbox = 0;

		switch (int_dialog_id) {
		case DIALOG_GEOPUNKT_HINZUFUEGEN:

			string_name = ((EditText) dialog
					.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_editText1))
					.getText().toString();
			if (string_name.equals(getResources().getString(
					R.string.punktezeigen_tab_dialog_text_name))) {

				boolean_ergebnis = false;
				string_fehler = string_fehler.concat(getResources().getString(
						R.string.punktezeigen_tab_dialog_text_name)
						+ " ");
			}

			string_beschreibung = ((EditText) dialog
					.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_editText2))
					.getText().toString();
			if (string_beschreibung.equals(getResources().getString(
					R.string.punktezeigen_tab_dialog_text_beschreibung))) {

				boolean_ergebnis = false;
				string_fehler = string_fehler.concat(getResources().getString(
						R.string.punktezeigen_tab_dialog_text_beschreibung)
						+ " ");
			}

			if (((CheckBox) dialog
					.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_checkBox1))
					.isChecked()) {

				boolean_pos_aus_gps = true;

				string_adresse = "";
			} else {

				string_adresse = ((EditText) dialog
						.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_editText3))
						.getText().toString();

				if (string_adresse.equals(getResources().getString(
						R.string.punktezeigen_tab_dialog_text_adresse))) {

					boolean_ergebnis = false;
					string_fehler = string_fehler
							.concat(getResources()
									.getString(
											R.string.punktezeigen_tab_dialog_text_adresse));
				}

				boolean_pos_aus_gps = false;
			}

			string_radio_eigenschaften = ((RadioButton) dialog
					.findViewById(((RadioGroup) dialog
							.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_radioGroup1))
							.getCheckedRadioButtonId())).getText().toString();

			double_preis = Double
					.valueOf(((EditText) dialog
							.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_editText4))
							.getText().toString());

			string_icon = ((HashMap<String, Object>) ((Spinner) dialog
					.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_spinner1))
					.getSelectedItem()).get(ICON_NAME).toString();

			if (boolean_ergebnis) {
				Intent intent_service = new Intent(this,
						PunkteHinzufuegen_Service.class);

				intent_service.putExtra(getPackageName() + "_" + "string_name",
						string_name);
				intent_service.putExtra(getPackageName() + "_"
						+ "string_beschreibung", string_beschreibung);
				intent_service.putExtra(getPackageName() + "_"
						+ "string_adresse", string_adresse);
				intent_service.putExtra(getPackageName() + "_"
						+ "string_radio_eigenschaften",
						string_radio_eigenschaften);
				intent_service.putExtra(
						getPackageName() + "_" + "double_preis", double_preis);
				intent_service.putExtra(getPackageName() + "_"
						+ "boolean_pos_aus_gps", boolean_pos_aus_gps);
				intent_service.putExtra(getPackageName() + "_" + "string_icon",
						string_icon);

				startService(intent_service);
			}
			break;
		case DIALOG_PUNKTE_FILTERN:

			Intent intent_befehl = new Intent(
					MemoSingleton.INTENT_PUNKTE_FILTERN);

			if (((CheckBox) dialog
					.findViewById(R.id.punktezeigen_tab_dialog_pktfiltern_checkBox1))
					.isChecked()) {

				string_adresse = ((EditText) dialog
						.findViewById(R.id.punktezeigen_tab_dialog_pktfiltern_editText1))
						.getText().toString();

				intent_befehl.putExtra(getPackageName() + "_"
						+ "string_adresse", string_adresse);

				int_filter_checkbox++;

				if (string_adresse.equals(getResources().getString(
						R.string.punktezeigen_tab_dialog_text_adresse))) {
					boolean_ergebnis = false;
					string_fehler = string_fehler
							.concat(getResources()
									.getString(
											R.string.punktezeigen_tab_dialog_text_adresse)
									+ " ");
				}
			}

			if (((CheckBox) dialog
					.findViewById(R.id.punktezeigen_tab_dialog_pktfiltern_checkBox2))
					.isChecked()) {

				string_name = ((EditText) dialog
						.findViewById(R.id.punktezeigen_tab_dialog_pktfiltern_editText2))
						.getText().toString();

				intent_befehl.putExtra(getPackageName() + "_" + "string_name",
						string_name);

				int_filter_checkbox++;

				if (string_name.equals(getResources().getString(
						R.string.punktezeigen_tab_dialog_text_name))) {
					boolean_ergebnis = false;
					string_fehler = string_fehler.concat(getResources()
							.getString(
									R.string.punktezeigen_tab_dialog_text_name)
							+ " ");
				}
			}

			if (((CheckBox) dialog
					.findViewById(R.id.punktezeigen_tab_dialog_pktfiltern_checkBox3))
					.isChecked()) {

				switch (((Spinner) dialog
						.findViewById(R.id.punktezeigen_tab_dialog_pktfiltern_spinner1))
						.getSelectedItemPosition()) {
				case 0:
					string_groesserkleiner = "?";
					boolean_ergebnis = false;
					string_fehler = string_fehler
							.concat(getResources()
									.getString(
											R.string.punktezeigen_tab_dialog_text_preis));
					break;
				case 1:
					string_groesserkleiner = ">";
					break;
				case 2:
					string_groesserkleiner = "=";
					break;
				case 3:
					string_groesserkleiner = "<";
					break;
				default:
					string_groesserkleiner = "?";
				}

				double_preis = Double
						.valueOf(((EditText) dialog
								.findViewById(R.id.punktezeigen_tab_dialog_pktfiltern_editText3))
								.getText().toString());

				intent_befehl.putExtra(getPackageName() + "_"
						+ "string_groesserkleiner", string_groesserkleiner);
				intent_befehl.putExtra(getPackageName() + "_" + "double_preis",
						double_preis);

				int_filter_checkbox++;
			}

			if (boolean_ergebnis) {

				if (int_filter_checkbox > 0) {
					memosingleton_anwendung.boolean_gefiltert = true;
				} else {
					memosingleton_anwendung.boolean_gefiltert = false;
				}

				intent_befehl.putExtra(getPackageName() + "_"
						+ "boolean_filter", true);
				sendBroadcast(intent_befehl);
			}

			break;
		default:
		}

		if (!boolean_ergebnis) {
			Toast.makeText(
					this,
					getResources()
							.getString(
									R.string.punktezeigen_tab_dialog_text_unvollstaendig)
							+ " " + string_fehler, Toast.LENGTH_LONG).show();
		}

		return boolean_ergebnis;
	}

	/**
	 * Befüllt die Datenbank zu Testzwecken mit zufällig erzeugten Punkten.
	 */
	private void dbFuellen() {
		// Schwerin: lat:53600337; lon:11418141

		Cursor cursor_db_anfrage;

		ContentValues contentvalues_werte = new ContentValues(8);

		Random random_zufall = new Random();

		Calendar calender_kalender = Calendar.getInstance();

		cursor_db_anfrage = memosingleton_anwendung.sqldatabase_writeable
				.rawQuery("select max(" + SQL_DB_Verwaltung.NAME_SPALTE_1
						+ ") from " + SQL_DB_Verwaltung.TABELLEN_NAME_HAUPT,
						null);

		cursor_db_anfrage.moveToFirst();

		for (int x = (cursor_db_anfrage.getInt(0) + 1); x < (cursor_db_anfrage
				.getInt(0) + 1 + 50); x++) {

			contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_2, "GeoPunkt"
					+ Integer.toString(x));// name
			contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_3,
					random_zufall.nextInt(80000000));// lat
			// beschraenkung von GeoPoint -> max +-80° lat
			contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_4,
					random_zufall.nextInt(180000000));// lon
			// beschraenkung von GeoPoint -> max +-180° lon

			switch (random_zufall.nextInt(5)) {
			case 0:
				contentvalues_werte
						.put(SQL_DB_Verwaltung.NAME_SPALTE_5, "icon");// icon
				break;
			case 1:
				contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_5,
						"kopfueber");// icon
				break;
			case 2:
				contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_5,
						"links");// icon
				break;
			case 3:
				contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_5,
						"rechts");// icon
				break;
			case 4:
				contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_5, "dot");// icon
				break;
			default:
				contentvalues_werte
						.put(SQL_DB_Verwaltung.NAME_SPALTE_5, "icon");// icon
			}

			contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_6,
					calender_kalender.getTimeInMillis());// zeit
			contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_7,
					"Beschreibung");// beschreibung
			contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_8,
					"Eigenschaften");// eigenschaften
			contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_9,
					random_zufall.nextDouble() * 50);// preis
			contentvalues_werte
					.put(SQL_DB_Verwaltung.NAME_SPALTE_10, "Adresse");// adresse

			memosingleton_anwendung.sqldatabase_writeable.insert(
					SQL_DB_Verwaltung.TABELLEN_NAME_HAUPT, null,
					contentvalues_werte);
		}

		cursor_db_anfrage.close();
	}

	/**
	 * Leert die Datenbank und alle in {@link MemoSingleton} gespeicherten
	 * Variablen mit DB-Einträgen.
	 */
	private void dbLeeren() {

		memosingleton_anwendung.sqldatabase_writeable.delete(
				SQL_DB_Verwaltung.TABELLEN_NAME_HAUPT, null, null);
		memosingleton_anwendung.sqldatabase_writeable.delete(
				SQL_DB_Verwaltung.TABELLEN_NAME_SYNCH, null, null);

		MemoSingleton memosingleton_anwendung = (MemoSingleton) getApplication();
		memosingleton_anwendung.aktualisiereDBZugriff(
				MemoSingleton.ZURUECKSETZEN, 0);
	}

}
