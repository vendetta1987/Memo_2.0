package de.planetic.android.memo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import de.planetic.android.memo.db.Adresse;
import de.planetic.android.memo.db.DBLesenSchreiben;
import de.planetic.android.memo.db.Ladestation;

public class LadestationDetail_Fragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return inflater.inflate(R.layout.ladestationdetail_fragment_layout,
				container, false);
	}

	@Override
	public void onStart() {

		leseDatenAus();
		super.onStart();
	}

	public void leseDatenAus() {

		View v = this.getView();

		DBLesenSchreiben db = new DBLesenSchreiben(getActivity());

		Ladestation ladestation_saeule = db.leseLadestation(
				getArguments().getLong("id"), true, false).get(0);

		((TextView) v
				.findViewById(R.id.ladestationdetail_fragment_layout_textView_bezeichnung))
				.setText(ladestation_saeule.string_bezeichnung);

		((TextView) v
				.findViewById(R.id.ladestationdetail_fragment_layout_textView_kommentar))
				.setText(ladestation_saeule.string_kommentar);

		((TextView) v
				.findViewById(R.id.ladestationdetail_fragment_layout_textView_verfuegbarkeit))
				.setText(ladestation_saeule.leseVerfuegbarkeit());

		((TextView) v
				.findViewById(R.id.ladestationdetail_fragment_layout_textView_zugangstyp))
				.setText(String.valueOf(ladestation_saeule.int_zugangstyp));

		((TextView) v
				.findViewById(R.id.ladestationdetail_fragment_layout_textView_preis))
				.setText(String.valueOf(ladestation_saeule.double_preis));

		Adresse adresse_ort = db.leseAdresse(ladestation_saeule.long_adress_id)
				.get(0);

		((TextView) v
				.findViewById(R.id.ladestationdetail_fragment_layout_textView_land))
				.setText(adresse_ort.string_land);
		((TextView) v
				.findViewById(R.id.ladestationdetail_fragment_layout_textView_plz))
				.setText(adresse_ort.string_plz);
		((TextView) v
				.findViewById(R.id.ladestationdetail_fragment_layout_textView_ort))
				.setText(adresse_ort.string_ort);
		((TextView) v
				.findViewById(R.id.ladestationdetail_fragment_layout_textView_str))
				.setText(adresse_ort.string_str_nr);

		db.schliessen();

	}
}
