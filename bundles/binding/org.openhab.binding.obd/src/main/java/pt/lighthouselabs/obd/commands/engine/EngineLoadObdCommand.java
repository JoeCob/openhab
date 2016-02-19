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

import pt.lighthouselabs.obd.commands.PercentageObdCommand;
import pt.lighthouselabs.obd.enums.AvailableCommandNames;

/**
 * Calculated Engine Load value.
 */
public class EngineLoadObdCommand extends PercentageObdCommand {

  public EngineLoadObdCommand() {
    super("0104 01");
    this.setCheckFrequency(1000);
  }

  /**
   * @param other
   */
  public EngineLoadObdCommand(EngineLoadObdCommand other) {
    super(other);
  }

  @Override
  protected void performCalculations() {
    // ignore first two bytes [hh hh] of the response
	  if ( isValid() ) {
		  setPercentage((buffer.get(2) * 100.0f) / 255.0f); }
	  else {
		  setPercentage(-1);
	  }
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see pt.lighthouselabs.obd.commands.ObdCommand#getName()
   */
  @Override
  public String getName() {
    return AvailableCommandNames.ENGINE_LOAD.getValue();
  }
  
  @Override
  public boolean validityCheck() {
    // ignore first two bytes [41 0C] of the response

	return false;
  }
  

}