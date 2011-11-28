package de.planetic.android.memo_neu;

import java.util.Calendar;
import java.util.Random;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TabHost;
import android.widget.Toast;
import de.planetic.android.memo.SQLDatenbank;

public class PunkteZeigen_Tab extends TabActivity implements OnCancelListener {

	private static final int DIALOG_GEOPUNKT_HINZUFUEGEN = 0;

	private SQL_DB_Verwaltung sqldb_db_verwaltung;
	private MemoSingleton memosingleton_anwendung;
	private boolean boolean_gps_verfuegbar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.punktezeigen_tab_layout_neu);

		sqldb_db_verwaltung = new SQL_DB_Verwaltung(this);
		memosingleton_anwendung = (MemoSingleton) getApplication();
		boolean_gps_verfuegbar = memosingleton_anwendung.gps_verwaltung
				.starteGPS();

		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabhost = getTabHost(); // The activity TabHost
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

		tabhost.setCurrentTab(0);

		// http://developer.android.com/resources/tutorials/views/hello-tabwidget.html
	}

	@Override
	public void onDestroy() {
		memosingleton_anwendung.gps_verwaltung.stoppeGPS();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater menuinf_inflater = getMenuInflater();
		menuinf_inflater.inflate(R.menu.punktezeigen_tab_menu_neu, menu);
		// Menue mit gegebenem Layout erstellen
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Intent intent_befehl = new Intent();

		switch (item.getItemId()) {
		case R.id.punktezeigen_tab_menu_item1:
			dbFuellen();
			intent_befehl.setAction(MemoSingleton.INTENT_DB_FUELLEN);
			break;
		case R.id.punktezeigen_tab_menu_item2:
			dbLeeren();
			intent_befehl.setAction(MemoSingleton.INTENT_DB_LEEREN);
			break;
		case R.id.punktezeigen_tab_menu_item3:
			showDialog(DIALOG_GEOPUNKT_HINZUFUEGEN);
			break;
		default:
		}

		this.sendBroadcast(intent_befehl);

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int int_id) {
		final Dialog dialog = new Dialog(this);

		switch (int_id) {
		case DIALOG_GEOPUNKT_HINZUFUEGEN:
			dialog.setContentView(R.layout.punktezeigen_tab_dialog_pkthinzufuegen_neu);
			dialog.setTitle(R.string.punktezeigen_tab_dialog_pkthinzufuegen_neu_title);
			dialog.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			dialog.findViewById(
					R.id.punktezeigen_tab_dialog_pkthinzufuegen_neu_button1)
					.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							// OK

							if (werteDialogAus(dialog,
									DIALOG_GEOPUNKT_HINZUFUEGEN)) {
								removeDialog(DIALOG_GEOPUNKT_HINZUFUEGEN);
							}
						}
					});
			dialog.findViewById(
					R.id.punktezeigen_tab_dialog_pkthinzufuegen_neu_button2)
					.setOnClickListener(new OnClickListener() {
						public void onClick(View v) {
							// Abbrechen
							dialog.cancel();
						}
					});

			CheckBox checkbox_gps = (CheckBox) dialog
					.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_neu_checkBox1);

			checkbox_gps.setClickable(boolean_gps_verfuegbar);
			checkbox_gps.setChecked(boolean_gps_verfuegbar);
			checkbox_gps
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {

							EditText edittext_adresse = (EditText) dialog
									.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_neu_editText3);

							edittext_adresse.setFocusable(!isChecked);
							edittext_adresse
									.setFocusableInTouchMode(!isChecked);
							edittext_adresse.setEnabled(!isChecked);
						}
					});

			dialog.findViewById(
					R.id.punktezeigen_tab_dialog_pkthinzufuegen_neu_editText3)
					.setFocusable(!boolean_gps_verfuegbar);
			dialog.findViewById(
					R.id.punktezeigen_tab_dialog_pkthinzufuegen_neu_editText3)
					.setEnabled(!boolean_gps_verfuegbar);

			dialog.setOnCancelListener(this);

			break;
		default:
			return null;
		}

		return dialog;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		removeDialog(DIALOG_GEOPUNKT_HINZUFUEGEN);
	}

	private boolean werteDialogAus(Dialog dialog, int int_dialog_id) {

		String string_fehler = "", string_name, string_beschreibung, string_adresse, string_radio_eigenschaften;
		double double_preis;
		boolean boolean_ergebnis = true, boolean_pos_aus_gps = false;

		switch (int_dialog_id) {
		case DIALOG_GEOPUNKT_HINZUFUEGEN:
			string_name = ((EditText) dialog
					.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_neu_editText1))
					.getText().toString();
			if (string_name
					.equals(getResources()
							.getString(
									R.string.punktezeigen_tab_dialog_pkthinzufuegen_neu_editText1_text))) {
				boolean_ergebnis = false;
				string_fehler = string_fehler.concat("Name ");
			}

			string_beschreibung = ((EditText) dialog
					.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_neu_editText2))
					.getText().toString();
			if (string_beschreibung
					.equals(getResources()
							.getString(
									R.string.punktezeigen_tab_dialog_pkthinzufuegen_neu_editText2_text))) {
				boolean_ergebnis = false;
				string_fehler = string_fehler.concat("Beschreibung ");
			}

			if (((CheckBox) dialog
					.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_neu_checkBox1))
					.isChecked()) {

				boolean_pos_aus_gps = true;

				string_adresse = "";
			} else {

				string_adresse = ((EditText) dialog
						.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_neu_editText3))
						.getText().toString();

				if (string_adresse
						.equals(getResources()
								.getString(
										R.string.punktezeigen_tab_dialog_pkthinzufuegen_neu_editText3_text))) {
					boolean_ergebnis = false;
					string_fehler = string_fehler.concat("Adresse");
				}
			}

			string_radio_eigenschaften = ((RadioButton) dialog
					.findViewById(((RadioGroup) dialog
							.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_neu_radioGroup1))
							.getCheckedRadioButtonId())).getText().toString();

			double_preis = Double
					.valueOf(((EditText) dialog
							.findViewById(R.id.punktezeigen_tab_dialog_pkthinzufuegen_neu_editText4))
							.getText().toString());

			if (geoPunktHinzufuegen(string_name, string_beschreibung,
					string_adresse, string_radio_eigenschaften, double_preis,
					boolean_pos_aus_gps) < 0) {
				Toast.makeText(
						this,
						getResources()
								.getString(
										R.string.punktezeigen_tab_dialog_pkthinzufuegen_db_gescheitert),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(
						this,
						getResources()
								.getString(
										R.string.punktezeigen_tab_dialog_pkthinzufuegen_db_erfolgreich),
						Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			string_adresse = "";
			string_beschreibung = "";
			string_name = "";
			string_radio_eigenschaften = "";
			double_preis = -1;
		}

		if (!boolean_ergebnis) {
			Toast.makeText(
					this,
					getResources()
							.getString(
									R.string.punktezeigen_tab_dialog_pkthinzufuegen_unvollstaendig)
							+ " " + string_fehler, Toast.LENGTH_LONG).show();
		}

		return boolean_ergebnis;
	}

	private long geoPunktHinzufuegen(String string_name,
			String string_beschreibung, String string_adresse,
			String string_radio_eigenschaften, double double_preis,
			boolean boolean_pos_aus_gps) {

		SQLiteDatabase sqldb_zugriff = sqldb_db_verwaltung
				.getWritableDatabase();

		Intent intent_befehl = new Intent();

		ContentValues contentvalues_werte = new ContentValues(5);

		GeoPunkt geopunkt_position;

		long long_db_ergebnis;

		if (boolean_pos_aus_gps) {
			// position aus gps erfassen

			geopunkt_position = memosingleton_anwendung.gps_verwaltung
					.aktuellePosition();
		} else {
			// adresse in position umwandeln

			geopunkt_position = memosingleton_anwendung.gps_verwaltung
					.adresseAufloesen(string_adresse, this);
		}

		// TODO rueckgabe geopunkt pruefen
		if (geopunkt_position == null) {
			return -1;
		}

		contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_2, string_name);
		contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_3,
				geopunkt_position.getLatitudeE6());
		contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_4,
				geopunkt_position.getLongitudeE6());
		// TODO Symbol waehlbar machen?
		contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_5,
				R.drawable.icon);
		contentvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_6, Calendar
				.getInstance().getTimeInMillis());

		long_db_ergebnis = sqldb_zugriff.insert(
				SQL_DB_Verwaltung.TABELLEN_NAME, null, contentvalues_werte);

		intent_befehl.setAction(MemoSingleton.INTENT_DB_FUELLEN);

		this.sendBroadcast(intent_befehl);

		return long_db_ergebnis;
	}

	private void dbFuellen() {
		// Schwerin: lat:53600337; lon:11418141

		SQLiteDatabase sqldb_zugriff = sqldb_db_verwaltung
				.getWritableDatabase();

		Cursor cursor_db_anfrage;

		ContentValues cvalues_werte = new ContentValues(5);

		Random random_zufall = new Random();

		Random random_vier = new Random();

		Calendar calender_kalender = Calendar.getInstance();

		cursor_db_anfrage = sqldb_zugriff.rawQuery("select max(id) from "
				+ SQL_DB_Verwaltung.TABELLEN_NAME, null);
		cursor_db_anfrage.moveToFirst();

		for (int x = (cursor_db_anfrage.getInt(0) + 1); x < (cursor_db_anfrage
				.getInt(0) + 1 + 50); x++) {

			cvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_2, "GeoPunkt"
					+ Integer.toString(x));// name
			cvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_3,
					random_zufall.nextInt(80000000));// lat
			// beschraenkung von GeoPoint -> max +-80° lat
			cvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_4,
					random_zufall.nextInt(180000000));// lon
			// beschraenkung von GeoPoint -> max +-180° lon

			switch (random_vier.nextInt(4)) {
			case 0:
				cvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_5,
						R.drawable.icon);// icon
				break;
			case 1:
				cvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_5,
						R.drawable.kopfueber);// icon
				break;
			case 2:
				cvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_5,
						R.drawable.links);// icon
				break;
			case 3:
				cvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_5,
						R.drawable.rechts);// icon
				break;
			default:
				cvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_5,
						R.drawable.icon);// icon
			}

			cvalues_werte.put(SQL_DB_Verwaltung.NAME_SPALTE_6,
					calender_kalender.getTimeInMillis());// zeit

			sqldb_zugriff.insert(SQLDatenbank.TABELLEN_NAME, null,
					cvalues_werte);
		}
	}

	private void dbLeeren() {
		SQLiteDatabase sqldb_zugriff = sqldb_db_verwaltung
				.getWritableDatabase();

		sqldb_zugriff.delete(SQL_DB_Verwaltung.TABELLEN_NAME, null, null);

		MemoSingleton memosingleton_anwendung = (MemoSingleton) getApplication();
		memosingleton_anwendung.aktualisiereDBZugriff(
				MemoSingleton.ZURUECKSETZEN, 0);

	}

}
