package de.planetic.android.memo;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import de.planetic.android.memo.db.ListeAsyncTaskLoader;

/**
 * {@link Activity} zur Anzeige der hinterlegten Punkte in einer Liste.
 */
public class PunkteZeigen_Tab_Liste extends FragmentActivity implements
		LoaderCallbacks<ArrayList<HashMap<String, String>>> {

	public static final String GEOPUNKT_NAME = "geopunkt_name";
	public static final String GEOPUNKT_LAT_LON = "geopunkt_lat_lon";
	public static final String GEOPUNKT_ICON = "geopunkt_icon";

	private static final int LISTE = 0;
	private static final int KARTE = 1;

	private BroadcastReceiver bcreceiver_receiver;
	private PunkteZeigen_Tab_AsyncTask asynctask_dbabfrage;
	private MemoSingleton memosingleton_anwendung;
	private boolean boolean_details_karte;

	/**
	 * Initialisiert die Anwendung und konfiguriert {@link OnItemClickListener}
	 * für die Listeneinträge.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.punktezeigen_liste_layout);

		memosingleton_anwendung = (MemoSingleton) getApplication();
		boolean_details_karte = false;

		erzeugeListe();
	}

	@Override
	public void onResume() {

		if (getSupportFragmentManager().findFragmentByTag(
				"punktezeigen_liste_layout_listfragment1") == null) {

			erzeugeListe();
		}

		super.onResume();
	}

	/**
	 * Registriert {@link BroadcastReceiver} und {@link IntentFilter}.
	 */
	@Override
	public void onStart() {

		bcreceiver_receiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				String string_intent_action = intent.getAction();

				if (string_intent_action
						.equals(MemoSingleton.INTENT_ZEIGE_LISTE)) {
					// listeAnzeigen(intent.getIntExtra("int_anzahl", 0), true);
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_DB_FUELLEN)) {
					// dbAbfrageStarten(intent);
					getSupportLoaderManager().getLoader(0).onContentChanged();
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_DB_LEEREN)) {
					// listeListeLoeschen();
					getSupportLoaderManager().getLoader(0).reset();
					getSupportLoaderManager().getLoader(0).startLoading();
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_PUNKTE_FILTERN)) {
					// dbAbfrageStarten(intent);
				} else if (string_intent_action
						.equals(MemoSingleton.INTENT_ZEIGE_DETAILS)) {
					zeigeDetails(KARTE, intent.getLongExtra("id", 1));
				}
			}
		};

		IntentFilter ifilter_filter = new IntentFilter();
		ifilter_filter.addAction(MemoSingleton.INTENT_DB_FUELLEN);
		ifilter_filter.addAction(MemoSingleton.INTENT_DB_LEEREN);
		ifilter_filter.addAction(MemoSingleton.INTENT_PUNKTE_FILTERN);
		ifilter_filter.addAction(MemoSingleton.INTENT_ZEIGE_LISTE);
		ifilter_filter.addAction(MemoSingleton.INTENT_ZEIGE_DETAILS);

		this.registerReceiver(bcreceiver_receiver, ifilter_filter);

		Log.d("memo_debug_punktezeigen_tab_liste", "onstart");

		super.onStart();
	}

	/**
	 * Löscht die registrierten {@link BroadcastReceiver} beim Beenden der
	 * {@link Activity}.
	 */
	@Override
	public void onStop() {

		this.unregisterReceiver(bcreceiver_receiver);

		Log.d("memo_debug_punktezeigen_tab_liste", "onstop");

		super.onStop();
	}

	/**
	 * Verwaltet die Hintergrundthreads bei Drehung des Gerätes.
	 * 
	 * @see PunkteZeigen_Tab_AsyncTask
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {

		if ((asynctask_dbabfrage != null)
				&& (asynctask_dbabfrage.getStatus() == Status.RUNNING)) {

			outState.putBoolean("boolean_liste_asynctask", true);
		}

		getSupportFragmentManager().popBackStackImmediate(null,
				FragmentManager.POP_BACK_STACK_INCLUSIVE);
		loescheFragment("punktezeigen_liste_layout_listfragment1");

		Log.d("memo_debug_punktezeigen_tab_liste", "onsaveinstancestate");

		super.onSaveInstanceState(outState);
	}

	/**
	 * Stellt die Listenansicht nach Drehung des Gerätes wiederher.
	 */
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {

		Log.d("memo_debug_punktezeigen_tab_liste", "onrestoreinstancestate");

		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	public void onBackPressed() {

		if (boolean_details_karte) {

			getSupportFragmentManager().popBackStack();
			memosingleton_anwendung.context_punktezeigen_tab.tabhost
					.setCurrentTab(PunkteZeigen_Tab.TAB_KARTE);
			boolean_details_karte = false;
		} else {

			super.onBackPressed();
		}
	}

	// /**
	// * {@code private void listeListeLoeschen()}
	// * <p/>
	// * Löscht die gesamte Liste.
	// */
	// private void listeListeLoeschen() {
	// ListView listview_liste = (ListView)
	// findViewById(R.id.punktezeigen_liste_layout_listview1);
	//
	// listview_liste.setAdapter(null);
	// listview_liste.invalidate();
	//
	// }

	public Loader<ArrayList<HashMap<String, String>>> onCreateLoader(int id,
			Bundle args) {

		return new ListeAsyncTaskLoader(this);
	}

	public void onLoadFinished(Loader<ArrayList<HashMap<String, String>>> arg0,
			ArrayList<HashMap<String, String>> arg1) {

		ListFragment lf_fragment = (ListFragment) getSupportFragmentManager()
				.findFragmentByTag("punktezeigen_liste_layout_listfragment1");

		if (lf_fragment != null) {

			if (arg1.size() == 0) {

				lf_fragment.setListShown(false);
			} else {

				lf_fragment
						.setListAdapter(new SimpleAdapter(
								this,
								arg1,
								R.layout.punktezeigen_liste_listview_item_layout,
								new String[] { "bezeichnung", "verfuegbarkeit",
										"id" },
								new int[] {
										R.id.punktezeigen_liste_listview_item_layout_textview1,
										R.id.punktezeigen_liste_listview_item_layout_textview2,
										R.id.punktezeigen_liste_listview_item_layout_textview3 }));
				lf_fragment.setListShown(true);
			}
		}
	}

	public void onLoaderReset(Loader<ArrayList<HashMap<String, String>>> arg0) {
	}

	private void zeigeDetails(int int_modus, long long_id) {

		Bundle bun = new Bundle();
		bun.putLong("id", long_id);
		Fragment frag = new LadestationDetail_Fragment();
		frag.setArguments(bun);

		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

		ft.replace(R.id.punktezeigen_liste_layout_framelayout1, frag,
				"punktezeigen_liste_layout_detailfragment");
		ft.addToBackStack(null);

		ft.commit();

		if (int_modus == KARTE) {

			boolean_details_karte = true;
		}
	}

	private void erzeugeListe() {

		ListFragment listfragment_liste = new ListFragment() {
			@Override
			public void onListItemClick(ListView l, View v, int position,
					long id) {

				zeigeDetails(
						LISTE,
						Long.decode(((TextView) v
								.findViewById(R.id.punktezeigen_liste_listview_item_layout_textview3))
								.getText().toString()));
			}
		};

		FragmentManager fm = getSupportFragmentManager();

		fm.beginTransaction()
				.replace(R.id.punktezeigen_liste_layout_framelayout1,
						listfragment_liste,
						"punktezeigen_liste_layout_listfragment1").commit();

		fm.executePendingTransactions();

		getSupportLoaderManager().initLoader(0, null, this);
	}

	private void loescheFragment(String string_tag) {

		FragmentManager fm = getSupportFragmentManager();

		Fragment frag = fm.findFragmentByTag(string_tag);

		if (frag != null) {

			fm.beginTransaction().remove(frag).commit();

			fm.executePendingTransactions();
		}
	}
}
