package org.avario.engine.sounds;

import org.avario.engine.datastore.DataAccessObject;
import org.avario.engine.prefs.Preferences;
import org.avario.engine.sounds.tones.wav.WavLiftTone;
import org.avario.engine.sounds.tones.wav.WavPrenotifyTone;
import org.avario.engine.sounds.tones.wav.WavSinkTone;
import org.avario.utils.Logger;
import org.avario.utils.Speaker;
import org.avario.utils.StringFormatter;
import org.avario.utils.UnitsConverter;

import android.app.Activity;

public class BeepBeeper implements Runnable {

	private Thread thr;
	private static BeepBeeper THIS;

	private final AsyncTone liftBeep = new WavLiftTone();// new LiftTone();
	private final AsyncTone sinkBeep = new WavSinkTone();// new SinkTone();
	private final AsyncTone prenotifyBeep = new WavPrenotifyTone();// new
																	// PrenotifyTone();

	protected BeepBeeper() {
		thr = new Thread(this);
	}

	public static void init(Activity context) {
		THIS = new BeepBeeper();
		THIS.start();
	}

	public static void clear() {
		THIS.stop();
	}

	@Override
	public void run() {
		while (thr.isAlive()) {
			try {
				float beepSpeed = DataAccessObject.get().getLastVSpeed();
				beepSpeed = beepSpeed > 5 ? 5 : beepSpeed;
				beepSpeed = beepSpeed < -5 ? -5 : beepSpeed;
				if (!validateThisSpeed(beepSpeed)) {
					Thread.sleep(200);
				} else {
					if (beepSpeed > 0) {
						liftBeep.setSpeed(beepSpeed);
						liftBeep.beep();
					} else if (beepSpeed < 0) {
						sinkBeep.setSpeed(beepSpeed);
						sinkBeep.beep();
					}
					if (Preferences.use_speach) {
						float saySpeed = UnitsConverter.toPreferredVSpeed(beepSpeed);
						Speaker.get().say(
								Preferences.units_system == 2 ? StringFormatter.noDecimals(saySpeed) : StringFormatter
										.oneDecimal(saySpeed));
					}
					Thread.sleep(Math.round(200 - 40 * beepSpeed));
				}
			} catch (InterruptedException e) {
				break;
			} catch (Exception ex) {
				Logger.get().log("Fail in beep: ", ex);
			}
		}
	}

	private boolean validateThisSpeed(float beepSpeed) {
		if (beepSpeed == Float.NaN) {
			return false;
		}

		if ((beepSpeed > -Preferences.lift_start) && (beepSpeed < Preferences.lift_start)) {
			prenotifyThermal();
		}

		if (Math.abs(beepSpeed) < Preferences.lift_start) {
			return false;
		}

		if (beepSpeed <= 0f && beepSpeed > Preferences.sink_start) {
			return false;
		}

		if (Math.abs(beepSpeed) > 15) {
			return false;
		}

		return true;
	}

	private void prenotifyThermal() {
		if (Preferences.prenotify_interval > 0 /*&& DataAccessObject.get().isGPSFix()*/) {
			prenotifyBeep.beep();
		}
	}

	private void start() {
		thr.start();
	}

	public void stop() {
		liftBeep.stop();
		sinkBeep.stop();
		prenotifyBeep.stop();
		thr.interrupt();
	}
}
