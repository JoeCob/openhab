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

import pt.lighthouselabs.obd.enums.AvailableCommandNames;

/**
 * Current speed.
 */
public class IgnitionObdCommand extends ObdCommand  {

  private String ignition = "";
  

  /**
   * Default ctor.
   */
  public IgnitionObdCommand() {
    super("AT IGN");
    this.setCheckFrequency(100);
  }

  
  
  /**
   * Copy ctor.
   * 
   * @param other
   */
  public IgnitionObdCommand(IgnitionObdCommand other) {
    super(other);
  }

  @Override
  protected void fillBuffer() {
    rawData = rawData.replaceAll("\\s", "");
    
    logger.trace("OBD Buffer is {}", buffer.toString() );
  }
  

  @Override
  protected void performCalculations() {
    // Ignore first two bytes [hh hh] of the response.
	  if (this.isValid()){ 
		  ignition = rawData;
	  } else {
		 ignition = "NO DATA";
	  }
  }


  @Override
  public String getName() {
    return AvailableCommandNames.SPEED.getValue();
  }
  
  @Override
  public boolean validityCheck() {
    // ignore first two bytes [41 0C] of the response

	return true;
  }

 public boolean getStatus() { 
	 if (this.isValid()) { 
		 if (rawData == "ON") { 
			 return true;
		 } else {
			 return false;
		 }
	 } 
	 return false;
 }

@Override
public String getFormattedResult() {
	// TODO Auto-generated method stub
	return ignition;
}

}
