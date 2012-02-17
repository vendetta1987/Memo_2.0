package de.planetic.android.memo;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class DBLesenSchreiben {

	private SQLiteDatabase sqldb_writeable;
	private ContentValues cv_werte;

	public DBLesenSchreiben(SQLiteDatabase writeable_db) {

		sqldb_writeable = writeable_db;
		cv_werte = new ContentValues();
	}

	public long schreibeAdresse(Adresse adresse_ort) {

		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_LAND, adresse_ort.string_land);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_PLZ, adresse_ort.string_plz);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_ORT, adresse_ort.string_ort);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_STR_NR,
				adresse_ort.string_str_nr);

		return schreibeDaten(SQLDB_Verwaltung_neu.TABELLE_ADRESSE);

	}

	public ArrayList<Adresse> leseAdresse(int int_id) {

		Adresse adresse_ort;
		ArrayList<Adresse> arraylist_adresse_return = new ArrayList<Adresse>();
		Cursor cursor_anfrage;

		cursor_anfrage = leseDaten(int_id, SQLDB_Verwaltung_neu.TABELLE_ADRESSE);

		if (cursor_anfrage.moveToFirst()) {

			do {

				adresse_ort = new Adresse();

				adresse_ort.string_land = cursor_anfrage
						.getString(cursor_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_LAND));
				adresse_ort.string_plz = cursor_anfrage
						.getString(cursor_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_PLZ));
				adresse_ort.string_ort = cursor_anfrage
						.getString(cursor_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_ORT));
				adresse_ort.string_str_nr = cursor_anfrage
						.getString(cursor_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_STR_NR));

				arraylist_adresse_return.add(adresse_ort);
			} while (cursor_anfrage.moveToNext());
		}

		return arraylist_adresse_return;
	}

	public long schreibeLadestation(Ladestation ladestation_saeule) {

		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_ADRESS_ID,
				ladestation_saeule.int_adress_id);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_STANDORT_LAENGE,
				ladestation_saeule.geopoint_standort.getLongitudeE6() / 1e6);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_STANDORT_BREITE,
				ladestation_saeule.geopoint_standort.getLatitudeE6() / 1e6);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_KOMMENTAR,
				ladestation_saeule.string_kommentar);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_BEZEICHNUNG,
				ladestation_saeule.string_bezeichnung);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_LADESTATION_FOTO,
				leseBlob(ladestation_saeule.drawable_ladestation_foto));
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_VERFUEGBARKEIT_ANFANG,
				ladestation_saeule.int_verfuegbarkeit_anfang);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_VERFUEGBARKEIT_ENDE,
				ladestation_saeule.int_verfuegbarkeit_ende);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_VERFUEGBARKEIT_KOMMENTAR,
				ladestation_saeule.string_kommentar);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_ZUGANGSTYP,
				ladestation_saeule.int_zugangstyp);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_BETREIBER_ID,
				ladestation_saeule.int_betreiber_id);
		cv_werte.put(SQLDB_Verwaltung_neu.SPALTE_PREIS,
				ladestation_saeule.double_preis);

		return schreibeDaten(SQLDB_Verwaltung_neu.TABELLE_LADESTATION);

	}

	private byte[] leseBlob(Drawable drawable_bild) {

		ByteArrayOutputStream baos_ausgabe = new ByteArrayOutputStream();
		((BitmapDrawable) drawable_bild).getBitmap().compress(
				Bitmap.CompressFormat.PNG, 100, baos_ausgabe);

		return baos_ausgabe.toByteArray();
	}

	public ArrayList<Ladestation> leseLadestation(int int_id,
			Context context_application) {

		Ladestation ladestation_saeule;
		Cursor cursor_anfrage;
		ArrayList<Ladestation> arraylist_ladestation_return = new ArrayList<Ladestation>();
		context_application = context_application.getApplicationContext();

		cursor_anfrage = leseDaten(int_id,
				SQLDB_Verwaltung_neu.TABELLE_LADESTATION);

		if (cursor_anfrage.moveToFirst()) {

			do {

				ladestation_saeule = new Ladestation(context_application);

				ladestation_saeule.int_adress_id = cursor_anfrage
						.getInt(cursor_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_ADRESS_ID));
				ladestation_saeule
						.setzeStandort(
								((Double) (cursor_anfrage
										.getDouble(cursor_anfrage
												.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_STANDORT_BREITE)) * 1e6))
										.intValue(),
								((Double) (cursor_anfrage.getDouble(cursor_anfrage
										.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_STANDORT_LAENGE)) * 1e6))
										.intValue());
				ladestation_saeule.string_kommentar = cursor_anfrage
						.getString(cursor_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_KOMMENTAR));
				ladestation_saeule.string_bezeichnung = cursor_anfrage
						.getString(cursor_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_BEZEICHNUNG));

				byte[] byte_blob = cursor_anfrage
						.getBlob(cursor_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_LADESTATION_FOTO));

				ladestation_saeule.drawable_ladestation_foto = new BitmapDrawable(
						BitmapFactory.decodeByteArray(byte_blob, 0,
								byte_blob.length));

				ladestation_saeule.int_verfuegbarkeit_anfang = cursor_anfrage
						.getInt(cursor_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_VERFUEGBARKEIT_ANFANG));
				ladestation_saeule.int_verfuegbarkeit_ende = cursor_anfrage
						.getInt(cursor_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_VERFUEGBARKEIT_ENDE));
				ladestation_saeule.string_verfuegbarkeit_kommentar = cursor_anfrage
						.getString(cursor_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_VERFUEGBARKEIT_KOMMENTAR));
				ladestation_saeule.int_zugangstyp = cursor_anfrage
						.getInt(cursor_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_ZUGANGSTYP));
				ladestation_saeule.int_betreiber_id = cursor_anfrage
						.getInt(cursor_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_BETREIBER_ID));
				ladestation_saeule.double_preis = cursor_anfrage
						.getDouble(cursor_anfrage
								.getColumnIndex(SQLDB_Verwaltung_neu.SPALTE_PREIS));

				arraylist_ladestation_return.add(ladestation_saeule);
			} while (cursor_anfrage.moveToNext());
		}

		return arraylist_ladestation_return;
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
		return long_return;
	}

	private Cursor leseDaten(int int_id, String string_tabelle) {
		Cursor cursor_anfrage;

		if (int_id > 0) {

			cursor_anfrage = sqldb_writeable.query(string_tabelle, null,
					SQLDB_Verwaltung_neu.SPALTE_ID + "=?",
					new String[] { String.valueOf(int_id) }, null, null, null);
		} else {

			cursor_anfrage = sqldb_writeable.query(string_tabelle, null, null,
					null, null, null, SQLDB_Verwaltung_neu.SPALTE_ID);
		}

		return cursor_anfrage;
	}
}
