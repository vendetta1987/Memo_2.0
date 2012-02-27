package de.planetic.android.memo.db;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import de.planetic.android.memo.R;

public class Betreiber {

	public long long_id;
	public String string_name;
	public Drawable drawable_logo;
	public long long_abrechnung_id;
	public String string_website;

	private Context context_application;

	public Betreiber(Context context) {

		context_application = context.getApplicationContext();
		long_id = 1;
		string_name = "Betreiber";
		setzeBetreiberLogo(R.drawable.icon);
		long_abrechnung_id = 1;
		string_website = "www.betreiber.de";
	}

	public boolean setzeBetreiberLogo(int int_id) {

		try {
			drawable_logo = context_application.getResources().getDrawable(
					int_id);

			return true;
		} catch (Exception e) {

			drawable_logo = context_application.getResources().getDrawable(
					R.drawable.icon);

			Log.d("memo_debug", "setzeLadestationFoto Fehler");

			return false;
		}
	}
}
