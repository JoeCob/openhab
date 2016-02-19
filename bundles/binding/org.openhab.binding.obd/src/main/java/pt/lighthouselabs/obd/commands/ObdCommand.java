/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package pt.lighthouselabs.obd.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;


import pt.lighthouselabs.obd.commands.protocol.FastInitObdCommand;
import pt.lighthouselabs.obd.exceptions.*;

import org.openhab.binding.obd.internal.OBDBinding;
import org.slf4j.*;

/**
 * Base OBD command.
 */
public abstract class ObdCommand {

  protected ArrayList<Integer> buffer = null;
  protected String cmd = null;
  protected String reInitCmd = "AT WS";
  protected boolean useImperialUnits = false;
  protected String rawData = null;
  protected boolean valid = true;
  private int reInitCount = 0;
  private double lastReset;
  private double lastCheck = 0;
  private double checkFrequency= 1000;
  private boolean permanentDisabled = false;
  private double now;
  private String outCmd;
  
  //Variables for fillBuffer
  private int begin = 0;
  private int end = 2;
  
  //Variables for readrawdata
  byte b = 0;
  StringBuilder res = new StringBuilder();
  
  
  private int commDelay = 10;
  
  
  
  Timer timer = new Timer();
  
  //TimerTask taskResetState = new TimerResetState( this );
  //ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

  //private final ScheduledExecutorService scheduler =
	     //  Executors.newScheduledThreadPool(1);
  
  private static final Logger logger = LoggerFactory.getLogger(OBDBinding.class);

  /**
   * Error classes to be tested in order
   */
  private Class[] ERROR_CLASSES = {
          UnableToConnectException.class,
          BusInitException.class,
          MisunderstoodCommandException.class,
          NoDataException.class,
          StoppedException.class,
          UnknownObdErrorException.class
  };

  /**
   * Default ctor to use
   * 
   * @param command
   *          the command to send
   */
  public  ObdCommand(String command) {
    this.cmd = command;
    this.buffer = new ArrayList<Integer>();
    
        
        
   /* try {
		timer.scheduleAtFixedRate(taskResetState, 60000, 60000);
		logger.debug("Scheduling State Reset for every  " + 60000 + " milliseconds") ;
	} catch (Exception e) {
		// TODO Auto-generated catch block
		logger.error("Error initializing OBD Command {}", this.getName());
		logger.error(e.getMessage());
	}*/
  }

  /**
   * Prevent empty instantiation
   */
  private ObdCommand() {
  }
  
  /**
   * Initialize specifiy a commDelay
 * @return 
   */
  public void setCommDelay (int delay) {
	  logger.trace("Setting commDelay to {}", delay);
	  this.commDelay = delay;
  }

  /**
   * Copy ctor.
   * 
   * @param other
   *          the ObdCommand to copy.
   */
  public ObdCommand(ObdCommand other) {
    this(other.cmd);
  }

  /**
   * Sends the OBD-II request and deals with the response.
   * 
   * This method CAN be overriden in fake commands.
   */
  public synchronized void run(InputStream in, OutputStream out) throws IOException,
      InterruptedException {

	logger.debug ("Start reading data for {}", this.getName());
	now = System.currentTimeMillis();
	
	
  	/*if (reInitCount > 50 ) {
    	//logger.debug("Performing a fastReinit at the OBD adapter due to too many NO DATA errors" ) ;
    	//this.sendFastReinit(in, out);
    	reInitCount = 0;
    	logger.debug("Performing a fastReinit at the OBD adapter due to too many NO DATA errors on mandatory commands" ) ;
    	throw 
    	
  	}*/
	
	// We do this to recheck commands that could be visable due to a change like Ignition on. 
	// Could be replaced by a code thrigerred when a change is detected (like ignition on) and 
	// enable all previously disabled commands in the past. 
	
	if ((now - lastCheck) < checkFrequency ) { 
		logger.debug("Not executing command {}. checkFrequency is {}, last checked at {}", this.getName() , checkFrequency, lastCheck); 
		return;
	} 
  	
	if ( !valid && (now -  lastReset > (10 * checkFrequency))  && !this.permanentDisabled   ) { // We do this to recheck commands that could be visable due ti a change like Ignition on. Could be replaced
		valid = true;
		lastReset = now;
		logger.debug("Reseting valid state for command " + this.getName() );
	}
	
	
    if (valid || !this.validityCheck() ) { 
    	if (!this.validityCheck() ) { logger.debug("Executing command since validity check is off");}
    	sendCommand(out);
    	readResult(in); 
    } else { 
    		logger.debug("Not executing command {} since state is {}. Validity check is {}.", this.getName() , valid, this.validityCheck());    		
    }
    lastCheck = now;
  }

  /**
   * Sends the OBD-II request.
   * 
   * This method may be overriden in subclasses, such as ObMultiCommand or
   * TroubleCodesObdCommand.
   * 
   * @param out
   *          The output stream.
   */
  protected void sendCommand(OutputStream out) throws IOException,
      InterruptedException {
	 
	// need to change to suport both multiline and single line. 
	// next line defaults to single line only. can lead to problems on multiline commands. 
	outCmd = cmd + " \r";
    // add the carriage return char

	//String outCmd = cmd ;
    // write to OutputStream (i.e.: a BluetoothSocket)
    logger.trace("Writing {}", outCmd.getBytes()  );
    out.write(outCmd.getBytes());
    logger.trace("Flushing" );
    out.flush();

    /*
     * HACK GOLDEN HAMMER ahead!!
     * 
     * Due to the time that some systems may take to respond, let's give it
     * 200ms.
     */
    Thread.sleep(commDelay);
  }

  /**
   * Resends this command.
   */
  protected void resendCommand(OutputStream out) throws IOException,
      InterruptedException {
    out.write("\r".getBytes());
    out.flush();
  }

  /**
   * Reads the OBD-II response.
   * <p>
   * This method may be overriden in subclasses, such as ObdMultiCommand.
   */
  protected void readResult(InputStream in) throws IOException {
	  
    try {
		readRawData(in);
	} catch (Exception e) {
		logger.debug("Exception when reading data from serial.");
		//e.printStackTrace();
	}
    checkForErrors();
    fillBuffer();
    performCalculations();
  }

  /**
   * This method exists so that for each command, there must be a method that is
   * called only once to perform calculations.
   */
  protected abstract void performCalculations();

  /**
   * 
   */
  protected void fillBuffer() {
    rawData = rawData.replaceAll("\\s", "");

    if (!rawData.matches("([0-9A-F]{2})+")) {
      throw new NonNumericResponseException(rawData);
    }

    // read string each two chars
    buffer.clear();
    begin = 0;
    end = 2;
    while (end <= rawData.length()) {
      if (valid || !this.validityCheck() ) { buffer.add(Integer.decode("0x" + rawData.substring(begin, end)));} else {buffer.add(-1);}
      begin = end;
      end += 2;
    }
    
    logger.debug("OBD Buffer is {}", buffer.toString() );
  }
  
  public boolean isValid () {
	  
	  //logger.debug("{} is {}", this.getName(), valid );
	  return valid;
	  
  }
  
  public void isValid (boolean isValid) {
	  
	  //logger.debug("{} is {}", this.getName(), valid );
	  this.valid =  isValid;
	  
  }

  protected void readRawData(InputStream in) throws IOException, UnableToConnectException, NoDataException, Exception {
    b = 0;
    res = new StringBuilder();

    logger.debug("Reading results");
    // read until '>' arrives
    while ((char) (b = (byte) in.read()) != '>') {
    	logger.trace("<< {} ", b);
        res.append((char) b);
        
        if (res.toString().length() > 128) //Watch dog for invalid communication
        {
        	logger.info("Watchdog breaking at serial reading");
        	break;
        }
    }
    /*
     * Imagine the following response 41 0c 00 0d.
     * 
     * ELM sends strings!! So, ELM puts spaces between each "byte". And pay
     * attention to the fact that I've put the word byte in quotes, because 41
     * is actually TWO bytes (two chars) in the socket. So, we must do some more
     * processing..
     */
    rawData = res.toString().trim();


    /*
     * Data may have echo or informative text like "INIT BUS..." or similar.
     * The response ends with two carriage return characters. So we need to take
     * everything from the last carriage return before those two (trimmed above).
     */
    rawData = rawData.substring(rawData.lastIndexOf(13) + 1);
    
    logger.debug("ReadRaw is {}", rawData );
    
    
  }

  void checkForErrors() {
	  
	  if (rawData.startsWith("7F 01", 0)  || rawData.startsWith("?", 0) || rawData.startsWith("127 01", 0) )
	  { 
		  logger.debug("Permanently disabling: {} reason {}. ", this.getName(), rawData.toString());
	    	
		  this.permanentDisabled = true;
		  this.valid = false;
		  return;
	  }
	  
	  if ( (rawData.startsWith("NO DATA", 0) && this.validityCheck()))
	    {
	    	valid = false;
	    	logger.debug("Command: {} - OBD readraw :{}, isValid: {}. This is not mandatory command. ", this.getName(), rawData.toString(), valid);
	    	return;
	    } else if ( rawData.startsWith("UNABLE TO CONNECT", 0) || rawData.startsWith("SEARCHING", 0)) {
	    	valid = false;
	    	logger.debug("Command: {} - OBD readraw :{}, isValid: {}", this.getName(), rawData.toString(), valid);
	    	logger.debug("ECU seems to be off");
	    	throw new UnableToConnectException();
	    	//Thread.sleep (60*1000); // Need to improve this. Maybe have a general setting at the connection level with the ignition state????
	    } else if  (rawData.startsWith("NO DATA", 0) && !this.validityCheck()) {
	    	logger.debug("Command: {} - OBD readraw :{}, isValid: {}. This was a mandatory command.", this.getName(), rawData.toString(), valid, this.validityCheck());
	    	valid = false;
	    	/*Thread.sleep(30000);
	    	if (reInitCount++ > 25 ) {
	    		reInitCount = 0;
	    		try {
					Thread.sleep(30000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		logger.debug("Throwing no data exception"); 
	    		throw new NoDataException();
	    	} else  { 
	    		logger.debug("No Data on Mandatory Object. This happaned {} times in sequence. ", reInitCount ) ;
	    		return;
	    	}*/
	    } 
	    
	    valid = true;

	    
	    
    for (Class<? extends ObdResponseException> errorClass : ERROR_CLASSES) {
      ObdResponseException messageError;

      try {
        messageError = errorClass.newInstance();
        messageError.setCommand(this.cmd);
      } catch (InstantiationException e) {
        throw new RuntimeException(e);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }

      if (messageError.isError(rawData)) {
        throw messageError;
      }
    }
  }

  /**
   * @return the raw command response in string representation.
   */
  public String getResult() {
    return rawData;
  }

  /**
   * @return a formatted command response in string representation.
   */
  public abstract String getFormattedResult();

  /**
   * @return a list of integers
   */
  protected ArrayList<Integer> getBuffer() {
    return buffer;
  }

  /**
   * @return true if imperial units are used, or false otherwise
   */
  public boolean useImperialUnits() {
    return useImperialUnits;
  }

  /**
   * Set to 'true' if you want to use imperial units, false otherwise. By
   * default this value is set to 'false'.
   * 
   * @param isImperial
   */
  public void useImperialUnits(boolean isImperial) {
    this.useImperialUnits = isImperial;
  }

  /**
   * @return the OBD command name.
   */
  public abstract String getName();

  public boolean validityCheck() {
	// TODO Auto-generated method stub
  return true;
}
  
  protected void sendFastReinit (InputStream in, OutputStream out) throws IOException,
  InterruptedException {
 
	  logger.debug ("Re-Inititlizing connection. Fast reinit. ");
	  new FastInitObdCommand().run(in, out);

	  Thread.sleep(30000);
	  logger.debug ("Fast reinit done.");
  }

public double getCheckFrequency() {
	return checkFrequency;
}

public void setCheckFrequency(double checkFrequency) {
	this.checkFrequency = checkFrequency;
}
  

}
