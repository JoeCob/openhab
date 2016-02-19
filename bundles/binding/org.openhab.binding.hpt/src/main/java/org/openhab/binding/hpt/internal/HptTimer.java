package org.openhab.binding.hpt.internal;

import java.util.TimerTask;

public class HptTimer extends TimerTask {
	
	private HptBinding binding;



	public HptTimer(HptBinding binding) {
		// TODO Auto-generated constructor stub
		this.binding =  binding;
	}

	public HptTimer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		binding.updateValue(System.currentTimeMillis());

	}

}
