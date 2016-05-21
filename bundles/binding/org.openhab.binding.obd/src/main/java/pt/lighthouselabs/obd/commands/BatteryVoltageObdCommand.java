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

import java.math.BigDecimal;

import pt.lighthouselabs.obd.enums.AvailableCommandNames;
import pt.lighthouselabs.obd.exceptions.NonNumericResponseException;

/**
 * Current speed.
 */
public class BatteryVoltageObdCommand extends ObdCommand  {

  private String voltageString = "";
  //private int voltage;

  /**
   * Default ctor.
   */
  public BatteryVoltageObdCommand() {
    super("AT RV");
    this.setCheckFrequency(100);
  }

  /**
   * Copy ctor.
   * 
   * @param other
   */
  public BatteryVoltageObdCommand(BatteryVoltageObdCommand other) {
    super(other);
  }
  
  
  /**
   * 
   */
  @Override
  protected void fillBuffer() {
    rawData = rawData.replaceAll("\\s", "");
    
    logger.trace("OBD Buffer is {}", buffer.toString() );
  }
  

  @Override
  protected void performCalculations() {
    // Ignore first two bytes [hh hh] of the response.
	  if (this.isValid()){ 
		  voltageString = rawData.replace('V', ' ');
		  //voltageString = rawData.substring(rawData.lastIndexOf("V"));
		  //voltageString.replace("V", "");
		 // voltage = Integer.parseInt(voltageString);
	  } else {
		 voltageString = "0";
	  }
  }

  public String getFormattedResult() {
	  if (isValid()) {
		  return voltageString;}
	  else { 
		  return "0";
	  }
  }
  
  public double getVoltage() { 
	  /*if (voltageString.contains("V")) { 
		  voltageString = voltageString
	  }*/
	  return new BigDecimal(Double.parseDouble(voltageString))
	    .setScale(2, BigDecimal.ROUND_HALF_DOWN)
	    .doubleValue(); 
  }

 

  @Override
  public String getName() {
    return AvailableCommandNames.VOLTAGE.getValue();
  }
  
  @Override
  public boolean validityCheck() {
    // ignore first two bytes [41 0C] of the response

	return false;
  }

}
