package de.planetic.android.memo_neu;

import java.io.IOException;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

public class GPS_Verwaltung_AsyncTask extends
		AsyncTask<String, Integer, List<Address>> {

	private Context context_con;
	private ProgressDialog progressdialog_dialog;

	public GPS_Verwaltung_AsyncTask(Context con) {
		context_con = con;

	}

	@Override
	protected void onPreExecute() {
		progressdialog_dialog = new ProgressDialog(context_con);

		progressdialog_dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressdialog_dialog.setTitle("");
		progressdialog_dialog.setCancelable(false);

		progressdialog_dialog.show();
	}

	@Override
	protected List<Address> doInBackground(String... string_adresse) {
		Geocoder geocoder_geokodierung = new Geocoder(context_con);

		try {
			return geocoder_geokodierung.getFromLocationName(string_adresse[0],
					10);
		} catch (IOException e) {
			Log.d("memo_debug", e.toString());
		}

		return null;
	}

	@Override
	protected void onPostExecute(List<Address> list_adressen) {
		progressdialog_dialog.dismiss();
	}

}
