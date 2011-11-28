package de.planetic.android.memo;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

// klasse fuer overlays einer karte, beinhaltet liste mit punkten, kuemmert
// sich um zeichnen, klicken, etc
public class ItemOverlay extends ItemizedOverlay<OverlayItem> {

	// liste der punkte im overlay
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	// context um Toast auszugeben
	private Context context;

	// Konstruktor mit extra context fuer Toast
	// standardgrafik festlegen und marker auf mitte unten setzen
	public ItemOverlay(Drawable defaultMarker, Context con) {
		super(boundCenterBottom(defaultMarker));
		context = con;
	}

	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	// gibt die Anzahl der hinterlegten Punkte zurueck
	@Override
	public int size() {

		return mOverlays.size();
	}

	// fuegt Punkte hinzu
	public void addOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
	}

	// listener fuer klicken eines punktes
	// lese koordinaten des punktes, gebe aus
	@Override
	protected boolean onTap(int index) {

		GeoPoint pnt = mOverlays.get(index).getPoint();

		Toast.makeText(
				context,
				"Lon: " + Integer.toString(pnt.getLongitudeE6()) + " Lat: "
						+ Integer.toString(pnt.getLatitudeE6()),
				Toast.LENGTH_LONG).show();
		return true;
	}
}
