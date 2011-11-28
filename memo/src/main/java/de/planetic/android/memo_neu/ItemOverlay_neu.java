package de.planetic.android.memo_neu;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

// klasse fuer overlays einer karte, beinhaltet liste mit punkten, kuemmert
// sich um zeichnen, klicken, etc
public class ItemOverlay_neu extends ItemizedOverlay<OverlayItem> {

	// liste der punkte im overlay
	private ArrayList<OverlayItem> arraylist_Overlays = new ArrayList<OverlayItem>();

	// context um Toast auszugeben
	private Context context;

	// Konstruktor mit extra context fuer Toast
	// standardgrafik festlegen und marker auf mitte unten setzen
	public ItemOverlay_neu(Drawable defaultMarker, Context con) {
		super(boundCenterBottom(defaultMarker));
		context = con;
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		return arraylist_Overlays.get(i);
	}

	// gibt die Anzahl der hinterlegten Punkte zurueck
	@Override
	public int size() {

		return arraylist_Overlays.size();
	}

	// fuegt Punkte hinzu
	// setlastfocus um problem mit geloeschten elementen zu vermeiden
	public void addOverlay(OverlayItem overlay) {
		arraylist_Overlays.add(overlay);
	}

	public void initialisieren() {

		setLastFocusedIndex(-1);
		populate();
	}

	// listener fuer klicken eines punktes
	// lese koordinaten des punktes, gebe aus
	@Override
	protected boolean onTap(int index) {

		GeoPoint geopoint_geopkt = arraylist_Overlays.get(index).getPoint();

		Toast.makeText(
				context,
				"Lon: " + Integer.toString(geopoint_geopkt.getLongitudeE6())
						+ " Lat: "
						+ Integer.toString(geopoint_geopkt.getLatitudeE6()),
				Toast.LENGTH_LONG).show();
		return true;
	}
}
