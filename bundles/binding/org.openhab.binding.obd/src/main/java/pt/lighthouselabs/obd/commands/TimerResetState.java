package pt.lighthouselabs.obd.commands;

import java.util.TimerTask;
import static java.util.concurrent.TimeUnit.*;

import org.openhab.binding.obd.internal.OBDBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimerResetState extends TimerTask {

	
	private static final Logger logger = LoggerFactory.getLogger(OBDBinding.class);
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	public TimerResetState ( ObdCommand cmd ) {
		try {
			logger.debug("Reseting valid state for command " + cmd.getName() );
			cmd.valid  = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("Error reseting OBD to valid for OBD command {}", cmd.getName());
			logger.error(e.getMessage());
		}
	}

}
