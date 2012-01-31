package de.planetic.android.memo;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Memo_Einstellungen extends PreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);
		
		this.getSharedPreferences(this.getPackageName() + "_preferences",
				Context.MODE_PRIVATE).edit()
				.putBoolean("boolean_google_lizenz_beachten", false).commit();
	}

	public static final SharedPreferences leseEinstellungen(
			final Context context_con) {

		return context_con.getSharedPreferences(context_con.getPackageName()
				+ "_preferences", Context.MODE_PRIVATE);
	}
}
