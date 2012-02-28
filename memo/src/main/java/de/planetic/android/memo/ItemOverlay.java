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

import de.planetic.android.memo.db.DBLesenSchreiben;

// klasse fuer overlays einer karte, beinhaltet liste mit punkten, kuemmert
// sich um zeichnen, klicken, etc
/**
 * Eigene Implementierung von {@link ItemizedOverlay}. Beinhaltet
 * {@link ArrayList} der enthaltenen Punkte und erlaubt das Übergeben von
 * {@link Path}-Variablen zur Darstellung auf der Karte. In diesem Fall
 * reagieren die Punkte nicht auf Klicken. {@code populate()} muss explizit
 * durch {@code initialisieren()} aufgerufen werden!
 * 
 */
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
	/**
	 * {@code public ItemOverlay(Drawable defaultMarker, Context con)}
	 * <p/>
	 * Konstruktor für die Verwendung ohne Pfade. Erstellt leere Instanz und
	 * legt {@code defaultMarker} als Symbol fest.
	 * 
	 * @param defaultMarker
	 *            {@link Drawable} das als Symbol verwendet wird.
	 * @param con
	 *            {@link Context} für den Zugriff auf {@link MemoSingleton}
	 */
	public ItemOverlay(Drawable defaultMarker, Context con) {
		super(boundCenterBottom(defaultMarker));
		context = con;
		boolean_pfad = false;

		populate();
	}

	/**
	 * {@code public ItemOverlay(Drawable defaultMarker, Context con, Path path_pfad_grob, Path path_pfad_fein, RectF rectf_vergleich)}
	 * <p/>
	 * Konstruktor für die Verwendung mit Pfaden. Erstellt leere Instanz und
	 * legt {@code defaultMarker} als Symbol fest. Bereitet zusätzlich
	 * {@link Paint} für das Zeichnen des Pfades vor.
	 * 
	 * @param defaultMarker
	 *            {@link Drawable} das als Symbol verwendet wird.
	 * @param con
	 *            {@link Context} für den Zugriff auf {@link MemoSingleton}
	 * 
	 * @param path_pfad_grob
	 *            {@link Path} mit den groben Pfadangaben aus der Google-Antwort
	 * @param path_pfad_fein
	 *            {@link Path} mit den feinen Pfadangaben aus der Google-Antwort
	 * @param rectf_vergleich
	 *            {@link RectF} Vergleichsrechteck für die Anpassung des Pfades
	 *            per Matrixmultiplikation
	 * @see Navigation_AsyncTask
	 * @see Matrix
	 */
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

	/**
	 * {@code public void draw(Canvas canvas, MapView mapview, boolean shadow)}
	 * <p/>
	 * Eigene Implementierung von {@code super.draw(...)}. Zeichnet Pfad während
	 * des Durchlaufs mit {@code shadow=false} und berechnet die
	 * Translation/Skalierung entsprechend {@code rectf_vergleich}.
	 * {@link Matrix} wird aus den unterschiedlichen {@link RectF} mit den
	 * Eckpunkten 45°, 45° und -45°, -45° bei maximaler und aktueller
	 * Vergrößerung der {@link MapView} berechnet.
	 * 
	 */
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
	/**
	 * Fügt neues {@link OverlayItem} der {@link ArrayList} hinzu.
	 * 
	 * @param overlay
	 *            {@link OverlayItem} das hinzugefügt werden soll
	 */
	public void addOverlay(OverlayItem overlay) {
		arraylist_overlays.add(overlay);
	}

	/**
	 * Entfernt {@link OverlayItem} aus der {@link ArrayList}
	 * 
	 * @param overlay
	 *            {@link OverlayItem} das entfernt werden soll
	 */
	public void removeOverlay(OverlayItem overlay) {
		arraylist_overlays.remove(overlay);
	}

	/**
	 * Löscht alle {@link OverlayItem} dieses {@link ItemOverlay} und ruft
	 * {@code initialisieren()} auf.
	 */
	public void clearOverlay() {
		arraylist_overlays.clear();
		initialisieren();
	}

	/**
	 * Ruft {@code populate()} auf um die {@link OverlayItem} auf das Zeichnen
	 * vorzubereiten. {@code setLastFocusedIndex(-1)} beugt Fehler beim Klicken
	 * auf ein leeres {@link ItemOverlay} vor.
	 */
	public void initialisieren() {
		setLastFocusedIndex(-1);
		populate();
	}

	// listener fuer klicken eines punktes
	// lese koordinaten des punktes, gebe aus
	/**
	 * Falls kein Pfad übergeben wurde und das {@link ItemOverlay} auch nicht
	 * für die aktuelle Position verwendet wird, bewirkt ein Klicken den Aufruf
	 * von {@code MemoSingleton.dbAbfragen()}.
	 * 
	 * @see MemoSingleton
	 */
	@Override
	protected boolean onTap(int index) {

		if (!boolean_pfad
				&& !((MemoSingleton) context.getApplicationContext()).boolean_aktuelle_position) {

			DBLesenSchreiben db_rw = new DBLesenSchreiben(context);

			((MemoSingleton) context.getApplicationContext()).dbAbfragen(
					db_rw.leseLadestation(
							Long.parseLong(arraylist_overlays.get(index)
									.getSnippet()), true, false).get(0), false);

			db_rw.schliessen();
		}

		return true;
	}
}
