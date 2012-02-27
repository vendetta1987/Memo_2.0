package de.planetic.android.memo.db;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import de.planetic.android.memo.MemoSingleton;

public class DBLesenSchreiben {

	// Reihenfolge des Einfuegens
	// ((((Adresse), (Abrechung -> Betreiber)) -> (Ladestation)), Stecker) ->
	// Stecker Anzahl

	private SQLiteDatabase sqldb_writeable;
	private ContentValues cv_werte;
	private Context context_application;

	public DBLesenSchreiben(Context context) {

		context_application = context.getApplicationContext();
		sqldb_writeable = new SQLDB_Verwaltung_neu(context_application)
				.getWritableDatabase();
		cv_werte = new ContentValues();
	}

	public void schliessen() {

		sqldb_writeable.close();
	}

	public long schreibeAdresse(Adresse adresse_ort) {

		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_LAND, adresse_ort.string_land);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_PLZ, adresse_ort.string_plz);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_ORT, adresse_ort.string_ort);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_STR_NR,
				adresse_ort.string_str_nr);

		return schreibeDaten(SQLDB_Verwaltung_neu.TABELLE_ADRESSE);

	}

	public ArrayList<Adresse> leseAdresse(long long_id) {

		Adresse adresse_ort;
		ArrayList<Adresse> arraylist_adresse_return = new ArrayList<Adresse>();
		Cursor cursor_adresse_anfrage;

		cursor_adresse_anfrage = leseDaten(long_id, false,
				SQLDB_Verwaltung_neu.SPALTE_ID,
				SQLDB_Verwaltung_neu.TABELLE_ADRESSE);

		if (cursor_adresse_anfrage.moveToFirst()) {

			do {

				adresse_ort = new Adresse();

				adresse_ort.string_land = cursor_adresse_anfrage
						.getString(cursor_adresse_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_LAND));
				adresse_ort.string_plz = cursor_adresse_anfrage
						.getString(cursor_adresse_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_PLZ));
				adresse_ort.string_ort = cursor_adresse_anfrage
						.getString(cursor_adresse_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_ORT));
				adresse_ort.string_str_nr = cursor_adresse_anfrage
						.getString(cursor_adresse_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_STR_NR));

				arraylist_adresse_return.add(adresse_ort);
			} while (cursor_adresse_anfrage.moveToNext());
		}

		cursor_adresse_anfrage.close();

		return arraylist_adresse_return;
	}

	public long schreibeAbrechnung(Abrechnung abrechnung_rechnung) {

		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_BEZEICHNUNG,
				abrechnung_rechnung.string_bezeichnung);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_PREIS,
				abrechnung_rechnung.double_preis);

		return schreibeDaten(SQLDB_Verwaltung_neu.TABELLE_ABRECHNUNG);
	}

	public long schreibeBetreiber(Betreiber betreiber_firma) {

		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_NAME,
				betreiber_firma.string_name);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_LOGO,
				erzeugeBlob(betreiber_firma.drawable_logo));
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_ABRECHNUNG_ID,
				betreiber_firma.long_abrechnung_id);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_WEBSITE,
				betreiber_firma.string_website);

		return schreibeDaten(SQLDB_Verwaltung_neu.TABELLE_BETREIBER);
	}

	public long schreibeLadestation(Ladestation ladestation_saeule) {

		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_ADRESS_ID,
				ladestation_saeule.adresse_ort.long_id);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_STANDORT_LAENGE,
				ladestation_saeule.geopoint_standort.getLongitudeE6() / 1e6);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_STANDORT_BREITE,
				ladestation_saeule.geopoint_standort.getLatitudeE6() / 1e6);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_KOMMENTAR,
				ladestation_saeule.string_kommentar);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_BEZEICHNUNG,
				ladestation_saeule.string_bezeichnung);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_LADESTATION_FOTO,
				erzeugeBlob(ladestation_saeule.drawable_ladestation_foto));
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_VERFUEGBARKEIT_ANFANG,
				ladestation_saeule.time_verfuegbarkeit_anfang.format3339(false));
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_VERFUEGBARKEIT_ENDE,
				ladestation_saeule.time_verfuegbarkeit_ende.format3339(false));
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_VERFUEGBARKEIT_KOMMENTAR,
				ladestation_saeule.string_kommentar);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_ZUGANGSTYP,
				ladestation_saeule.int_zugangstyp);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_BETREIBER_ID,
				ladestation_saeule.betreiber_anbieter.long_id);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_PREIS,
				ladestation_saeule.double_preis);

		long long_return = schreibeDaten(SQLDB_Verwaltung_neu.TABELLE_LADESTATION);

		if (long_return > 0) {

			schreibeSteckerAnzahl(long_return,
					ladestation_saeule.arraylist_stecker);
		}

		return long_return;

	}

	public ArrayList<Ladestation> leseLadestation(long long_id,
			boolean boolean_bild, boolean boolean_lese_alles) {

		Ladestation ladestation_saeule;
		Cursor cursor_ladestation_anfrage;
		ArrayList<Ladestation> arraylist_ladestation_return = new ArrayList<Ladestation>();

		cursor_ladestation_anfrage = leseDaten(long_id, boolean_lese_alles,
				SQLDB_Verwaltung_neu.SPALTE_ID,
				SQLDB_Verwaltung_neu.TABELLE_LADESTATION);

		if (cursor_ladestation_anfrage.moveToFirst()) {

			do {

				ladestation_saeule = new Ladestation(context_application);

				ladestation_saeule.long_id = cursor_ladestation_anfrage
						.getLong(cursor_ladestation_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_ID));
				ladestation_saeule
						.setzeStandort(
								((Double) (cursor_ladestation_anfrage
										.getDouble(cursor_ladestation_anfrage
												.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_STANDORT_BREITE)) * 1e6))
										.intValue(),
								((Double) (cursor_ladestation_anfrage.getDouble(cursor_ladestation_anfrage
										.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_STANDORT_LAENGE)) * 1e6))
										.intValue());
				ladestation_saeule.string_kommentar = cursor_ladestation_anfrage
						.getString(cursor_ladestation_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_KOMMENTAR));
				ladestation_saeule.string_bezeichnung = cursor_ladestation_anfrage
						.getString(cursor_ladestation_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_BEZEICHNUNG));

				if (boolean_bild) {

					ladestation_saeule.drawable_ladestation_foto = erzeugeDrawable(cursor_ladestation_anfrage
							.getBlob(cursor_ladestation_anfrage
									.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_LADESTATION_FOTO)));
				}

				ladestation_saeule.time_verfuegbarkeit_anfang
						.parse3339(cursor_ladestation_anfrage.getString(cursor_ladestation_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_VERFUEGBARKEIT_ANFANG)));
				ladestation_saeule.time_verfuegbarkeit_ende
						.parse3339(cursor_ladestation_anfrage.getString(cursor_ladestation_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_VERFUEGBARKEIT_ENDE)));
				ladestation_saeule.string_verfuegbarkeit_kommentar = cursor_ladestation_anfrage
						.getString(cursor_ladestation_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_VERFUEGBARKEIT_KOMMENTAR));
				ladestation_saeule.int_zugangstyp = cursor_ladestation_anfrage
						.getInt(cursor_ladestation_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_ZUGANGSTYP));
				ladestation_saeule.double_preis = cursor_ladestation_anfrage
						.getDouble(cursor_ladestation_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_PREIS));
				ladestation_saeule.arraylist_stecker = leseSteckerAnzahl(cursor_ladestation_anfrage
						.getLong(cursor_ladestation_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_ID)));
				ladestation_saeule.adresse_ort = leseAdresse(
						cursor_ladestation_anfrage.getLong(cursor_ladestation_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_ADRESS_ID)))
						.get(0);
				ladestation_saeule.betreiber_anbieter = leseBetreiber(
						cursor_ladestation_anfrage
								.getLong(cursor_ladestation_anfrage
										.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_BETREIBER_ID)),
						boolean_bild, false).get(0);

				arraylist_ladestation_return.add(ladestation_saeule);
			} while (cursor_ladestation_anfrage.moveToNext());
		}

		cursor_ladestation_anfrage.close();

		return arraylist_ladestation_return;
	}

	public ArrayList<Betreiber> leseBetreiber(long long_id,
			boolean boolean_bild, boolean boolean_lese_alles) {

		Betreiber betreiber_anbieter;
		ArrayList<Betreiber> arraylist_betreiber_return = new ArrayList<Betreiber>();
		Cursor cursor_betreiber_anfrage = leseDaten(long_id,
				boolean_lese_alles, SQLDB_Verwaltung_neu.SPALTE_ID,
				SQLDB_Verwaltung_neu.TABELLE_BETREIBER);

		if (cursor_betreiber_anfrage.moveToFirst()) {

			do {

				betreiber_anbieter = new Betreiber(context_application);

				betreiber_anbieter.long_id = cursor_betreiber_anfrage
						.getLong(cursor_betreiber_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_ID));
				betreiber_anbieter.string_name = cursor_betreiber_anfrage
						.getString(cursor_betreiber_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_NAME));

				if (boolean_bild) {

					betreiber_anbieter.drawable_logo = erzeugeDrawable(cursor_betreiber_anfrage
							.getBlob(cursor_betreiber_anfrage
									.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_LOGO)));
				}

				betreiber_anbieter.long_abrechnung_id = cursor_betreiber_anfrage
						.getLong(cursor_betreiber_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_ABRECHNUNG_ID));
				betreiber_anbieter.string_website = cursor_betreiber_anfrage
						.getString(cursor_betreiber_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_WEBSITE));

				arraylist_betreiber_return.add(betreiber_anbieter);

			} while (cursor_betreiber_anfrage.moveToNext());
		}

		return arraylist_betreiber_return;
	}

	public long schreibeStecker(Stecker stecker_typ) {

		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_NAME, stecker_typ.string_name);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_BEZEICHNUNG,
				stecker_typ.string_bezeichnung);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_STECKER_FOTO,
				erzeugeBlob(stecker_typ.drawable_stecker_foto));

		return schreibeDaten(SQLDB_Verwaltung_neu.TABELLE_STECKER);
	}

	private Stecker leseStecker(long long_id, boolean bool_bild) {

		Cursor cursor_stecker_anfrage;
		Stecker stecker_typ = null;

		cursor_stecker_anfrage = leseDaten(long_id, false,
				SQLDB_Verwaltung_neu.SPALTE_ID,
				SQLDB_Verwaltung_neu.TABELLE_STECKER);

		if (cursor_stecker_anfrage.moveToFirst()) {

			do {

				stecker_typ = new Stecker(context_application);

				stecker_typ.string_name = cursor_stecker_anfrage
						.getString(cursor_stecker_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_NAME));
				stecker_typ.string_bezeichnung = cursor_stecker_anfrage
						.getString(cursor_stecker_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_BEZEICHNUNG));
				if (bool_bild) {

					stecker_typ.drawable_stecker_foto = erzeugeDrawable(cursor_stecker_anfrage
							.getBlob(cursor_stecker_anfrage
									.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_STECKER_FOTO)));
				}
				stecker_typ.long_id = cursor_stecker_anfrage
						.getLong(cursor_stecker_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_ID));

			} while (cursor_stecker_anfrage.moveToNext());
		}

		cursor_stecker_anfrage.close();

		return stecker_typ;
	}

	private void schreibeSteckerAnzahl(long long_id,
			ArrayList<Stecker> arraylist_stecker) {

		if (arraylist_stecker.isEmpty()) {

			arraylist_stecker.add(new Stecker(context_application));
		}

		for (Stecker stecker_typ : arraylist_stecker) {

			cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_STECKER_ID,
					stecker_typ.long_id);
			cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_ANZAHL,
					stecker_typ.int_anzahl);
			cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_LADESTATION_ID, long_id);

			if (schreibeDaten(SQLDB_Verwaltung_neu.TABELLE_STECKER_ANZAHL) < 0) {

				Log.d("memo_debug", "schreibeSteckerAnzahl Fehler");
			}
		}
	}

	private ArrayList<Stecker> leseSteckerAnzahl(long long_id) {

		Cursor cursor_steckeranzahl_anfrage;
		Stecker stecker_typ;
		ArrayList<Stecker> arraylist_stecker = new ArrayList<Stecker>();

		cursor_steckeranzahl_anfrage = leseDaten(long_id, false,
				SQLDB_Verwaltung_neu.SPALTE_LADESTATION_ID,
				SQLDB_Verwaltung_neu.TABELLE_STECKER_ANZAHL);

		if (cursor_steckeranzahl_anfrage.moveToFirst()) {

			do {

				stecker_typ = leseStecker(
						cursor_steckeranzahl_anfrage
								.getLong(cursor_steckeranzahl_anfrage
										.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_STECKER_ID)),
						false);

				stecker_typ.int_anzahl = cursor_steckeranzahl_anfrage
						.getInt(cursor_steckeranzahl_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_ANZAHL));

				arraylist_stecker.add(stecker_typ);
			} while (cursor_steckeranzahl_anfrage.moveToNext());
		}

		cursor_steckeranzahl_anfrage.close();

		return arraylist_stecker;
	}

	private byte[] erzeugeBlob(Drawable drawable_bild) {

		ByteArrayOutputStream baos_ausgabe = new ByteArrayOutputStream();
		((BitmapDrawable) drawable_bild).getBitmap().compress(
				Bitmap.CompressFormat.PNG, 100, baos_ausgabe);

		return baos_ausgabe.toByteArray();
	}

	private Drawable erzeugeDrawable(byte[] byte_blob) {

		return new BitmapDrawable(BitmapFactory.decodeByteArray(byte_blob, 0,
				byte_blob.length));
	}

	private long schreibeDaten(String string_tabelle) {

		long long_return = -1;

		try {

			long_return = sqldb_writeable.insertOrThrow(string_tabelle, null,
					cv_werte);
		} catch (SQLiteConstraintException e) {

			Log.d("memo_debug", string_tabelle + " ForeignKey Fehler");

			return -7;
		} catch (Exception e) {

			e.printStackTrace();

			return long_return;
		} finally {

			cv_werte.clear();
		}

		if (long_return > 0) {

			context_application.sendBroadcast(new Intent(
					MemoSingleton.INTENT_DB_FUELLEN));
		}

		return long_return;
	}

	private Cursor leseDaten(long long_id, boolean boolean_lese_alles,
			String string_spalte, String string_tabelle) {

		Cursor cursor_anfrage;
		String string_vergleich;

		if (boolean_lese_alles) {

			string_vergleich = ">";
		} else {

			string_vergleich = "=";
		}

		cursor_anfrage = sqldb_writeable.query(string_tabelle, null,
				string_spalte + string_vergleich + "?",
				new String[] { String.valueOf(long_id) }, null, null, null);

		return cursor_anfrage;
	}

	public void loescheDaten() {

		sqldb_writeable.delete(SQLDB_Verwaltung_neu.TABELLE_STECKER_ANZAHL,
				null, null);
		sqldb_writeable.delete(SQLDB_Verwaltung_neu.TABELLE_LADESTATION, null,
				null);
		sqldb_writeable
				.delete(SQLDB_Verwaltung_neu.TABELLE_ADRESSE, null, null);
		sqldb_writeable.delete("sqlite_sequence", null, null);

		context_application.sendBroadcast(new Intent(
				MemoSingleton.INTENT_DB_LEEREN));
	}
}
