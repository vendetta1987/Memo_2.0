package de.planetic.android.memo.db;

public class Adresse {

	public long long_id;
	public String string_land;
	public String string_plz;
	public String string_ort;
	public String string_str_nr;

	public Adresse() {

		long_id = 1;
		string_land = "DE";
		string_plz = "19061";
		string_ort = "Schwerin";
		string_str_nr = "Hagenower Straße 73";
	}
}
