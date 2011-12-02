package de.planetic.android.memo;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

// klasse fuer overlays einer karte, beinhaltet liste mit punkten, kuemmert
// sich um zeichnen, klicken, etc
public class ItemOverlay extends ItemizedOverlay<OverlayItem> {

	// liste der punkte im overlay
	private ArrayList<OverlayItem> arraylist_Overlays = new ArrayList<OverlayItem>();

	// context um Toast auszugeben
	private Context context;

	// Konstruktor mit extra context fuer Toast
	// standardgrafik festlegen und marker auf mitte unten setzen
	public ItemOverlay(Drawable defaultMarker, Context con) {
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

		((MemoSingleton) context.getApplicationContext())
				.dbAbfragen((GeoPunkt) arraylist_Overlays.get(index).getPoint(), false);

		return true;
	}
}
