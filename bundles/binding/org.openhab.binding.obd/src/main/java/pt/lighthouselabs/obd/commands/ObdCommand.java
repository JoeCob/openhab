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

import pt.lighthouselabs.obd.exceptions.*;

import org.openhab.binding.obd.internal.OBDBinding;
import org.slf4j.*;

/**
 * Base OBD command.
 */
public abstract class ObdCommand {

  protected ArrayList<Integer> buffer = null;
  protected String cmd = null;
  protected boolean useImperialUnits = false;
  protected String rawData = null;
  protected boolean valid = true;
  
  
  
  Timer timer = new Timer();
  
  TimerTask taskResetState = new TimerResetState( this );
 
  
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
    timer.scheduleAtFixedRate(taskResetState, 60000, 300000 );
    //System.out.println("Scheduling Persistence for every  " + persistFrequency + " milliseconds") ;
  }

  /**
   * Prevent empty instantiation
   */
  private ObdCommand() {
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
    if (valid || !this.validityCheck() ) { 
    	if (!this.validityCheck() ) { logger.debug("Executing command since validity check is off");}
    	sendCommand(out);
    	readResult(in); 
    	} else
    	{ 
    		logger.debug("Not executing command {} since state is {}. Validity check is {}.", this.getName() , valid, this.validityCheck());    		
    	}
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
	 
	//String outCmd = cmd + "\r";
    // add the carriage return char

	String outCmd = cmd ;
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
    Thread.sleep(50);
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
		logger.error("Exception when reading data from serial.");
		e.printStackTrace();
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
    int begin = 0;
    int end = 2;
    while (end <= rawData.length()) {
      if (valid || !this.validityCheck() ) { buffer.add(Integer.decode("0x" + rawData.substring(begin, end)));} else {buffer.add(-1);}
      begin = end;
      end += 2;
    }
    
    logger.debug("OBD Buffer is {}", buffer.toString() );
  }
  
  public boolean isValid () {
	  
	  return valid;
	  
  }

  protected void readRawData(InputStream in) throws IOException, Exception {
    byte b = 0;
    StringBuilder res = new StringBuilder();

   
    // read until '>' arrives
    while ((char) (b = (byte) in.read()) != '>') {
    	logger.trace("<< {} ", b);
        res.append((char) b);
        
        if (res.toString().length() > 512) //Watch dog for invalid communication
        {
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
    
    if ( rawData.startsWith("7F 01", 0)  || rawData.startsWith("NO DATA", 0) || rawData.startsWith("?", 0))
    {
    	valid = false;
    	logger.debug("OBD readraw :{}, isValid: {}", rawData.toString(), valid);
    } else if ( rawData.startsWith("UNABLE TO CONNECT", 0)) {
    	valid = false;
    	logger.debug("OBD readraw :{}, isValid: {}", rawData.toString(), valid);
    	logger.debug("Sleeping 10 sec since ECU seems to be off");
    	Thread.sleep (10*1000);
    	
    }
    
    logger.debug("OBD readraw :{}, isValid: {}", rawData.toString(), valid);
    

  }

  void checkForErrors() {
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

}
