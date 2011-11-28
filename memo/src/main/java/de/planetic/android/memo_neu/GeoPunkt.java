package de.planetic.android.memo_neu;

import com.google.android.maps.GeoPoint;

public class GeoPunkt extends GeoPoint {
	public String string_name;
	public int int_icon;
	public long long_id;
	public long long_zeit;

	public GeoPunkt() {
		super(0, 0);
		string_name = "GeoPunkt";
		int_icon = R.drawable.icon;
		long_id = 0;
		long_zeit = 0;
	}

	public GeoPunkt(int int_lat, int int_lon) {
		super(int_lat, int_lon);
		string_name = "GeoPunkt";
		int_icon = R.drawable.icon;
		long_id = 0;
		long_zeit = 0;
	}

	private GeoPunkt(int int_lat, int int_lon, long long_id, int int_icon,
			String string_name, long long_zeit) {
		super(int_lat, int_lon);
		this.long_id = long_id;
		this.int_icon = int_icon;
		this.long_zeit = long_zeit;
		this.string_name = string_name;
	}

	public GeoPunkt setLatitude(int int_lat) {
		return new GeoPunkt(int_lat, this.getLongitudeE6(), long_id, int_icon,
				string_name, long_zeit);
	}

	public GeoPunkt setLongitude(int int_lon) {
		return new GeoPunkt(this.getLatitudeE6(), int_lon, long_id, int_icon,
				string_name, long_zeit);
	}
}
