package org.avario.engine.sounds.wav;

import org.avario.AVarioActivity;
import org.avario.R;
import org.avario.engine.prefs.Preferences;
import org.avario.engine.sounds.AsyncTone;
import org.avario.utils.Logger;

import android.util.SparseIntArray;

public class WavPrenotifyTone extends AsyncTone {
	private long prenotifyLast = 0;
	private SparseIntArray listSounds = new SparseIntArray(5);

	public WavPrenotifyTone() {
		listSounds.put(0, player.load(AVarioActivity.CONTEXT, R.raw.prenotify, 1));
	}

	@Override
	public void beep() {
		int iSoundId = listSounds.get(0);
		try {
			long now = System.currentTimeMillis();
			if (!isPlaying && (now - prenotifyLast > Preferences.prenotify_interval)) {
				isPlaying = true;
				prenotifyLast = now;
				player.play(iSoundId, 1f, 1f, 0, 0, 1);
				Thread.sleep(100);
			}
		} catch (InterruptedException e) {
			Logger.get().log("Fail lift prenotify ", e);
		} finally {
			player.stop(iSoundId);
			isPlaying = false;
		}
	}

	@Override
	public void stop() {
		player.release();
	}

}
