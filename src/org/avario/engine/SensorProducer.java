package org.avario.engine;

import java.util.ArrayList;
import java.util.List;

import org.avario.R;
import org.avario.engine.consumerdef.AccelerometerConsumer;
import org.avario.engine.consumerdef.BarometerConsumer;
import org.avario.engine.consumerdef.CompasConsumer;
import org.avario.engine.consumerdef.LocationConsumer;
import org.avario.engine.consumerdef.VarioConsumer;
import org.avario.engine.sensors.BaroSensorThread;
import org.avario.engine.sensors.CompasSensorThread;
import org.avario.engine.sensors.LocationThread;
import org.avario.utils.Logger;

import android.app.Activity;
import android.location.Location;
import android.widget.Toast;

public class SensorProducer {
	private static SensorProducer THIS;

	private final List<LocationConsumer> gspConsumers = new ArrayList<LocationConsumer>();
	private final List<BarometerConsumer> baroConsumers = new ArrayList<BarometerConsumer>();
	private final List<CompasConsumer> compasConsumers = new ArrayList<CompasConsumer>();
	private final List<AccelerometerConsumer> accelerometerConsumers = new ArrayList<AccelerometerConsumer>();

	protected CompasSensorThread compasThread;
	protected BaroSensorThread baroThread;
	protected LocationThread locationThread;

	protected SensorProducer() {

	}

	public static void init(Activity activity, boolean useInternalSensors) {
		THIS = new SensorProducer();
		if (useInternalSensors) {
			THIS.initSensorsListners(activity);
		}

	}

	public static SensorProducer get() {
		return THIS;
	}

	public static void clear() {
		THIS.close();
	}

	protected void close() {
		gspConsumers.clear();
		baroConsumers.clear();
		gspConsumers.clear();

		if (compasThread != null) {
			compasThread.stop();
		}
		if (baroThread != null) {
			baroThread.stop();
		}
		if (locationThread != null) {
			locationThread.stop();
		}
	}

	public void registerConsumer(VarioConsumer consumer) {
		Logger.get().log("Register consumer " + consumer);
		if (consumer instanceof LocationConsumer) {
			gspConsumers.add((LocationConsumer) consumer);
			Logger.get().log(
					"Register GPS consumer " + consumer.getClass().getName());
		}

		if (consumer instanceof BarometerConsumer) {
			baroConsumers.add((BarometerConsumer) consumer);
			Logger.get().log(
					"Register baro consumer " + consumer.getClass().getName());
		}

		if (consumer instanceof CompasConsumer) {
			compasConsumers.add((CompasConsumer) consumer);
			Logger.get()
					.log("Register compas consumer "
							+ consumer.getClass().getName());
		}

		if (consumer instanceof AccelerometerConsumer) {
			accelerometerConsumers.add((AccelerometerConsumer) consumer);
			Logger.get().log(
					"Register accelerometer consumer "
							+ consumer.getClass().getName());
		}
	}

	private void initSensorsListners(Activity activity) {
		compasThread = new CompasSensorThread();
		baroThread = new BaroSensorThread();
		locationThread = new LocationThread();

		compasThread.startSensor();
		baroThread.startSensor();
		locationThread.startSensor();

		Toast.makeText(activity, R.string.initalizing_sensors,
				Toast.LENGTH_LONG).show();
	}

	public void notifyGpsConsumers(Location location) {
		synchronized (gspConsumers) {
			for (LocationConsumer consumer : gspConsumers) {
				consumer.notifyWithLocation(location);
			}
		}
	}

	public void notifyBaroConsumers(float altitude) {
		synchronized (baroConsumers) {
			for (BarometerConsumer consumer : baroConsumers) {
				consumer.notifyWithAltFromPreasure(altitude);
			}
		}
	}

	public void notifyCompasConsumers(float bearing) {
		synchronized (compasConsumers) {
			for (CompasConsumer consumer : compasConsumers) {
				consumer.notifyNorth(bearing);
			}
		}
	}

	public void notifyAccelerometerConsumers(float x, float y, float z) {
		synchronized (accelerometerConsumers) {
			for (AccelerometerConsumer consumer : accelerometerConsumers) {
				consumer.notifyAcceleration(x, y, z);
			}
		}
	}
}
