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
	private ArrayList<GeoPunkt> arraylist_geopunkte, arraylist_geopunkte_fein;
	private Paint paint_farbe;
	private Point point_temp;
	private Path path_pfad, path_pfad_fein;
	private boolean boolean_startziel, boolean_pfad;

	private Point point_or, point_ul;
	private boolean boolean_einmal;

	// Konstruktor mit extra context fuer Toast
	// standardgrafik festlegen und marker auf mitte unten setzen
	public ItemOverlay(Drawable defaultMarker, Context con) {
		super(boundCenterBottom(defaultMarker));
		context = con;
		boolean_pfad = false;

		populate();
	}

	public ItemOverlay(Drawable defaultMarker, Context con,
			ArrayList<GeoPunkt> arraylist_geopunkte,
			ArrayList<GeoPunkt> arraylist_geopunkte_fein) {
		super(boundCenterBottom(defaultMarker));
		context = con;

		boolean_pfad = true;
		boolean_einmal = true;

		this.arraylist_geopunkte = arraylist_geopunkte;
		this.arraylist_geopunkte_fein = arraylist_geopunkte_fein;

		paint_farbe = new Paint();
		paint_farbe.setColor(Color.BLUE);
		paint_farbe.setStyle(Style.STROKE);
		paint_farbe.setStrokeJoin(Join.ROUND);
		paint_farbe.setStrokeCap(Cap.ROUND);
		paint_farbe.setStrokeWidth(7.5f);

		point_temp = new Point();

		path_pfad = new Path();
		path_pfad_fein = new Path();

		populate();
	}

	@Override
	public void draw(Canvas canvas, MapView mapview, boolean shadow) {

		Projection projection_umrechnung;
		int int_zoom;
		Point point_or_temp, point_ul_temp;
		Matrix matrix_skaltrans;
		Path path_temp;

		if (boolean_pfad && boolean_einmal) {

			int_zoom = mapview.getZoomLevel();
			mapview.getController().setZoom(21);
			projection_umrechnung = mapview.getProjection();

			point_or = projection_umrechnung.toPixels(new GeoPoint(45000000,
					45000000), null);
			point_ul = projection_umrechnung.toPixels(new GeoPoint(-45000000,
					-45000000), null);

			boolean_startziel = true;

			for (GeoPunkt punkt : arraylist_geopunkte) {

				if (boolean_startziel) {

					projection_umrechnung.toPixels(punkt, point_temp);
					path_pfad.moveTo(point_temp.x, point_temp.y);

					boolean_startziel = false;
				}

				projection_umrechnung.toPixels(punkt, point_temp);
				path_pfad.lineTo(point_temp.x, point_temp.y);
				path_pfad.moveTo(point_temp.x, point_temp.y);
			}

			boolean_startziel = true;

			for (GeoPunkt punkt : arraylist_geopunkte_fein) {

				if (boolean_startziel) {

					projection_umrechnung.toPixels(punkt, point_temp);
					path_pfad_fein.moveTo(point_temp.x, point_temp.y);

					boolean_startziel = false;
				}

				projection_umrechnung.toPixels(punkt, point_temp);
				path_pfad_fein.lineTo(point_temp.x, point_temp.y);
				path_pfad_fein.moveTo(point_temp.x, point_temp.y);
			}

			mapview.getController().setZoom(int_zoom);

			boolean_einmal = false;
		}

		if (!shadow && boolean_pfad) {

			projection_umrechnung = mapview.getProjection();
			point_or_temp = projection_umrechnung.toPixels(new GeoPoint(
					45000000, 45000000), null);
			point_ul_temp = projection_umrechnung.toPixels(new GeoPoint(
					-45000000, -45000000), null);

			matrix_skaltrans = new Matrix();
			matrix_skaltrans.setRectToRect(
					new RectF((float) point_ul.x, (float) point_or.y,
							(float) point_or.x, (float) point_ul.y), new RectF(
							(float) point_ul_temp.x, (float) point_or_temp.y,
							(float) point_or_temp.x, (float) point_ul_temp.y),
					Matrix.ScaleToFit.CENTER);

			if (mapview.getZoomLevel() < 13) {

				path_temp = new Path(path_pfad);
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

		if (!boolean_pfad) {

			((MemoSingleton) context.getApplicationContext()).dbAbfragen(
					(GeoPunkt) arraylist_overlays.get(index).getPoint(), false);
		}

		return true;
	}
}
