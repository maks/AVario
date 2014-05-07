package org.avario.ui;

import org.avario.AVarioActivity;
import org.avario.R;
import org.avario.engine.SensorProducer;
import org.avario.engine.consumerdef.LocationConsumer;
import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.prefs.Preferences;
import org.avario.utils.Logger;
import org.avario.utils.StringFormatter;
import org.avario.utils.UnitsConverter;

import android.app.Activity;
import android.graphics.Typeface;
import android.location.Location;
import android.os.AsyncTask;
import android.widget.TextView;

public class NumericViewUpdater extends AsyncTask<Integer, Integer, Integer> implements LocationConsumer {
	private Activity context = null;
	private static NumericViewUpdater THIS;
	protected long startTime;
	protected TextView altitudeView = null;
	protected TextView altitudeMeasure = null;
	protected TextView groundSpeedView = null;
	protected TextView groundSpeedMeasure = null;
	protected TextView qfeView = null;
	protected TextView hGainView = null;
	protected TextView timeSpanView = null;
	protected TextView recView = null;
	protected TextView glideRatio = null;

	protected NumericViewUpdater(Activity context) {
		this.context = context;
		final Typeface font = StringFormatter.getLargeFont(context.getApplicationContext());
		altitudeView = (TextView) context.findViewById(R.id.altValue);
		altitudeView.setTypeface(font);
		altitudeMeasure = (TextView) context.findViewById(R.id.altMeasure);
		groundSpeedView = (TextView) context.findViewById(R.id.gspeedValue);
		groundSpeedView.setTypeface(font);
		groundSpeedMeasure = (TextView) context.findViewById(R.id.gspeedMeasure);
		groundSpeedMeasure.setText(UnitsConverter.preferredDistLong() + "/h");
		recView = (TextView) context.findViewById(R.id.rec_status);
		recView.setText(Preferences.units_system == 1 ? R.string.ms : R.string.fs);
		hGainView = (TextView) context.findViewById(R.id.hgain);
		hGainView.setTypeface(font);
		qfeView = (TextView) context.findViewById(R.id.qfe);
		qfeView.setTypeface(font);
		timeSpanView = (TextView) context.findViewById(R.id.ftimeValue);
		glideRatio = (TextView) context.findViewById(R.id.ratio);
	}

	public static void init(Activity context) {
		THIS = new NumericViewUpdater(context);
		THIS.execute();
		SensorProducer.get().registerConsumer(THIS);
	}

	@Override
	public void notifyWithLocation(final Location location) {
		AVarioActivity.CONTEXT.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (location.hasSpeed()) {
					groundSpeedView.setText(StringFormatter.noDecimals(UnitsConverter.toPreferredLong(UnitsConverter
							.msTokmh(location.getSpeed()))));
					groundSpeedMeasure.setText(UnitsConverter.preferredDistLong() + "/h");
					if (startTime == 0 && location.getSpeed() > 3f) {
						startTime = System.currentTimeMillis();
					}
				}
				double lastGain = UnitsConverter.toPreferredShort(Math.round(DataAccessObject.get()
						.getHistoryAltimeterGain()));
				hGainView.setText(context.getApplicationContext().getString(R.string.lastgainvalue,
						StringFormatter.noDecimals(lastGain)));

				float vSpeed = DataAccessObject.get().getLastVSpeed();
				if (vSpeed != 0f) {
					float ratio = location.getSpeed() / vSpeed;
					ratio = ratio > 99 ? 99 : ratio;
					ratio = ratio < -99 ? -99 : ratio;
					glideRatio.setText(StringFormatter.noDecimals(location.getSpeed() / vSpeed) + " : 1");
				}
			}
		});
	}

	@Override
	protected Integer doInBackground(Integer... params) {
		while (true) {
			try {
				publishProgress();
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				break;
			} catch (Exception e) {
				Logger.get().log("Fail to refresh UI", e);
				break;
			}
		}

		return null;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	@Override
	protected void onProgressUpdate(final Integer... progress) {
		try {
			if (startTime > 0) {
				timeSpanView.setText(UnitsConverter.timeSpan(startTime));
			}
			groundSpeedMeasure.setText(UnitsConverter.preferredDistLong() + "/h");
			altitudeView.setText(StringFormatter.noDecimals(UnitsConverter.toPreferredShort(DataAccessObject.get()
					.getLastAltitude())));
			altitudeMeasure.setText(context.getApplicationContext().getString(
					Preferences.units_system == 2 ? R.string.feet : R.string.meters));

			double lastGain = UnitsConverter.toPreferredShort(Math.round(DataAccessObject.get()
					.getHistoryAltimeterGain()));
			hGainView.setText(context.getApplicationContext().getString(R.string.lastgainvalue,
					StringFormatter.noDecimals(lastGain)));
			qfeView.setText(StringFormatter.noDecimals(UnitsConverter.toPreferredShort(DataAccessObject.get().getQFE())));
		} catch (Exception e) {
			Logger.get().log("Fail to refresh UI progress", e);
		}
	}

	public static void clear() {
		THIS.cancel(true);
	}
}
