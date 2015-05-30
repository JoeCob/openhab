package pt.lighthouselabs.obd.commands;

import java.util.TimerTask;

public class TimerResetState extends TimerTask {

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	public TimerResetState ( ObdCommand cmd ) {
		cmd.valid  = true;
	}

}
