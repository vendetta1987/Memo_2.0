//package de.planetic.android.memo;
//
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.EditText;
//import android.widget.ImageView;
//import de.planetic.android.memo.db.DBLesenSchreiben;
//import de.planetic.android.memo.db.Ladestation;
//
//public class LadestationDetail_Fragment extends Fragment {
//
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//
//		return inflater.inflate(R.layout.ladestationdetail_fragment_layout,
//				container, false);
//	}
//
//	@Override
//	public void onStart() {
//
//		leseDatenAus();
//		super.onStart();
//	}
//
//	public void leseDatenAus() {
//
//		View v = this.getView();
//
//		DBLesenSchreiben db = new DBLesenSchreiben(getActivity());
//
//		Ladestation ladestation_saeule = db.leseLadestation(
//				getArguments().getLong("id"), true, false).get(0);
//
//		((EditText) v
//				.findViewById(R.id.ladestationdetail_fragment_layout_textView_bezeichnung))
//				.setText(ladestation_saeule.string_bezeichnung);
//
//		((EditText) v
//				.findViewById(R.id.ladestationdetail_fragment_layout_textView_kommentar))
//				.setText(ladestation_saeule.string_kommentar);
//
//		((ImageView) v
//				.findViewById(R.id.ladestationdetail_fragment_layout_imageView_foto))
//				.setImageDrawable(ladestation_saeule.drawable_ladestation_foto);
//
//		((EditText) v
//				.findViewById(R.id.ladestationdetail_fragment_layout_textView_verfuegbarkeit))
//				.setText(ladestation_saeule.leseVerfuegbarkeit());
//
//		((EditText) v
//				.findViewById(R.id.ladestationdetail_fragment_layout_textView_zugangstyp))
//				.setText(String.valueOf(ladestation_saeule.int_zugangstyp));
//
//		((ImageView) v
//				.findViewById(R.id.ladestationdetail_fragment_layout_imageView_logo))
//				.setImageDrawable(ladestation_saeule.betreiber_anbieter.drawable_logo);
//
//		((EditText) v
//				.findViewById(R.id.ladestationdetail_fragment_layout_textView_preis))
//				.setText(String.valueOf(ladestation_saeule.double_preis));
//
//		((EditText) v
//				.findViewById(R.id.ladestationdetail_fragment_layout_textView_land))
//				.setText(ladestation_saeule.adresse_ort.string_land);
//		((EditText) v
//				.findViewById(R.id.ladestationdetail_fragment_layout_textView_plz))
//				.setText(ladestation_saeule.adresse_ort.string_plz);
//		((EditText) v
//				.findViewById(R.id.ladestationdetail_fragment_layout_textView_ort))
//				.setText(ladestation_saeule.adresse_ort.string_ort);
//		((EditText) v
//				.findViewById(R.id.ladestationdetail_fragment_layout_textView_str))
//				.setText(ladestation_saeule.adresse_ort.string_str_nr);
//
//		db.schliessen();
//
//	}
// }
