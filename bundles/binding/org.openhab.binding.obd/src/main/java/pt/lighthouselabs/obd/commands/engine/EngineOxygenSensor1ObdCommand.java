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
package pt.lighthouselabs.obd.commands.engine;

import org.openhab.binding.obd.protocol.OBDJavaConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.lighthouselabs.obd.commands.PercentageObdCommand;
import pt.lighthouselabs.obd.enums.AvailableCommandNames;

/**
 * Calculated Engine Load value.
 */
public class EngineOxygenSensor1ObdCommand extends PercentageObdCommand {

  float voltage;
	
  double oxygen1pct;
  private int b3;
  private int b4;
  
	private static final Logger logger = LoggerFactory
			.getLogger(OBDJavaConnector.class);
	
  
  public EngineOxygenSensor1ObdCommand() {
    super("0114 01");
    this.setCheckFrequency(2000);
  }
  
  @Override
  protected void performCalculations() {
    // ignore first two bytes [hh hh] of the response
	  //(A/200 - (B-128) * 100/128 
	  b3 = buffer.get(2);
	  b4 = buffer.get(3);
	  logger.debug( "OxyGenSensor Debug: Bytes 3 and 4 are: {} and {} ", b3, b4 );
	  if ( isValidPct() ) {
		  setPercentage((b4-128) * (100f/128f)); 
		  }
	  if ( isValidVoltage(b3) ) {
		  setVoltage(((float)b3*0.005f));
		  }
  }
  
  public float getVoltage() {
	  	logger.debug("getVoltage returning {}", voltage);
		return voltage;
	  }

	public void setVoltage(float voltage) {
		logger.debug("Voltage se to {}", voltage);
		this.voltage = voltage;
	}

  /**
   * @param other
   */
  public EngineOxygenSensor1ObdCommand(EngineOxygenSensor1ObdCommand other) {
    super(other);
  }

  /*
   * (non-Javadoc)
   * 
   * @see pt.lighthouselabs.obd.commands.ObdCommand#getName()
   */
  @Override
  public String getName() {
    return AvailableCommandNames.ENGINE_OXYG1.getValue();
  }
  
 
  public boolean isValidPct () {
	  
	  if ( buffer.get(3) == 0xFF ) {
		  return false;
	  } else {
		  return valid;
	  }   
  }
  
  public boolean isValidVoltage (int voltagePart) {
	  
	  if ( (voltagePart < 0) || (voltagePart  > 255 ) ) {
		  return false;
	  } else {
		  return valid;
	  }   
  }
  
  @Override
  public boolean validityCheck() {
    // ignore first two bytes [41 0C] of the response

	return false;
  }
  

}