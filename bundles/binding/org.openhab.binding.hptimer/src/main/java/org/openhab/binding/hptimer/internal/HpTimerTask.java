package org.openhab.binding.hptimer.internal;

import java.util.TimerTask;

public class HpTimerTask extends TimerTask {
	HpTimerDevice device;

	public HpTimerTask(HpTimerDevice hpTimerDevice) {
		// TODO Auto-generated constructor stub
		this.device = device;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		this.device.update();
	}

}
