package de.planetic.android.memo;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

// klasse fuer overlays einer karte, beinhaltet liste mit punkten, kuemmert
// sich um zeichnen, klicken, etc
public class ItemOverlay extends ItemizedOverlay<OverlayItem> {

	// liste der punkte im overlay
	private ArrayList<OverlayItem> arraylist_overlays = new ArrayList<OverlayItem>();
	// context um Toast auszugeben
	private Context context;
	private Paint paint_farbe;
	private Path path_pfad_grob, path_pfad_fein;
	private RectF rectf_vergleich;
	private boolean boolean_pfad;

	// Konstruktor mit extra context fuer Toast
	// standardgrafik festlegen und marker auf mitte unten setzen
	public ItemOverlay(Drawable defaultMarker, Context con) {
		super(boundCenterBottom(defaultMarker));
		context = con;
		boolean_pfad = false;

		populate();
	}

	public ItemOverlay(Drawable defaultMarker, Context con,
			Path path_pfad_grob, Path path_pfad_fein, RectF rectf_vergleich) {
		super(boundCenterBottom(defaultMarker));
		context = con;

		boolean_pfad = true;

		paint_farbe = new Paint();
		paint_farbe.setColor(Color.BLUE);
		paint_farbe.setStyle(Style.STROKE);
		paint_farbe.setStrokeJoin(Join.ROUND);
		paint_farbe.setStrokeCap(Cap.ROUND);
		paint_farbe.setStrokeWidth(7.5f);

		this.path_pfad_grob = path_pfad_grob;
		this.path_pfad_fein = path_pfad_fein;
		this.rectf_vergleich = rectf_vergleich;

		populate();
	}

	@Override
	public void draw(Canvas canvas, MapView mapview, boolean shadow) {

		Projection projection_umrechnung;
		Point point_or_temp, point_ul_temp;
		Matrix matrix_skaltrans;
		Path path_temp;

		if (!shadow && boolean_pfad) {

			projection_umrechnung = mapview.getProjection();
			point_or_temp = projection_umrechnung.toPixels(new GeoPoint(
					45000000, 45000000), null);
			point_ul_temp = projection_umrechnung.toPixels(new GeoPoint(
					-45000000, -45000000), null);

			matrix_skaltrans = new Matrix();
			matrix_skaltrans.setRectToRect(rectf_vergleich, new RectF(
					(float) point_ul_temp.x, (float) point_or_temp.y,
					(float) point_or_temp.x, (float) point_ul_temp.y),
					Matrix.ScaleToFit.CENTER);

			if (mapview.getZoomLevel() < 13) {

				path_temp = new Path(path_pfad_grob);
			} else {

				path_temp = new Path(path_pfad_fein);
			}

			path_temp.transform(matrix_skaltrans);

			canvas.drawPath(path_temp, paint_farbe);
		}

		super.draw(canvas, mapview, shadow);
	}

	@Override
	protected OverlayItem createItem(int i) {
		return arraylist_overlays.get(i);
	}

	// gibt die Anzahl der hinterlegten Punkte zurueck
	@Override
	public int size() {
		return arraylist_overlays.size();
	}

	// fuegt Punkte hinzu
	// setlastfocus um problem mit geloeschten elementen zu vermeiden
	public void addOverlay(OverlayItem overlay) {
		arraylist_overlays.add(overlay);
	}

	public void removeOverlay(OverlayItem overlay) {
		arraylist_overlays.remove(overlay);
	}

	public void clearOverlay() {
		arraylist_overlays.clear();
		initialisieren();
	}

	public void initialisieren() {
		setLastFocusedIndex(-1);
		populate();
	}

	// listener fuer klicken eines punktes
	// lese koordinaten des punktes, gebe aus
	@Override
	protected boolean onTap(int index) {

		if (!boolean_pfad
				&& !((MemoSingleton) context.getApplicationContext()).boolean_aktuelle_position) {

			((MemoSingleton) context.getApplicationContext()).dbAbfragen(
					(GeoPunkt) arraylist_overlays.get(index).getPoint(), false);
		}

		return true;
	}
}
