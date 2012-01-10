package de.planetic.android.memo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MemoStart extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.memostart_layout);
	}

	public void punkteZeigen(View v_view) {

		Intent int_intent = new Intent(this, PunkteZeigen_Tab.class);
		this.startActivity(int_intent);
	}

	public void serverSynchronisieren(View v_view) {

		Toast.makeText(this, "serverSynchronisieren", Toast.LENGTH_SHORT)
				.show();
	}

	public void einstellungenZeigen(View v_view) {

		// Toast.makeText(this, "einstellungenZeigen",
		// Toast.LENGTH_SHORT).show();
		startActivity(new Intent(this, Memo_Einstellungen.class));
	}
}
