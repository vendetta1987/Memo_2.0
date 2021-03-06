package de.planetic.android.memo.db;

import de.planetic.android.memo.R;
import de.planetic.android.memo.R.drawable;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class Stecker {

	public String string_name;
	public String string_bezeichnung;
	public Drawable drawable_stecker_foto;

	public long long_id;
	public int int_anzahl;

	private Context context_application;

	public Stecker(Context context) {

		context_application = context.getApplicationContext();
		long_id = 1;
		string_name = "Standard";
		string_bezeichnung = "Bezeichnung";
		int_anzahl = 1;
		setzeSteckerFoto(R.drawable.icon);
	}

	public boolean setzeSteckerFoto(int int_id) {

		try {
			drawable_stecker_foto = context_application.getResources()
					.getDrawable(int_id);

			return true;
		} catch (Exception e) {

			drawable_stecker_foto = context_application.getResources()
					.getDrawable(R.drawable.icon);

			Log.d("memo_debug", "setzeSteckerFoto Fehler");

			return false;
		}
	}
}
