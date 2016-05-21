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
package pt.lighthouselabs.obd.commands.fuel;

import pt.lighthouselabs.obd.commands.ObdCommand;
import pt.lighthouselabs.obd.enums.AvailableCommandNames;
import pt.lighthouselabs.obd.enums.FuelStatus;

/**
 * Fuel Consumption Rate per hour.
 */
public class FuelStatusObdCommand extends ObdCommand {

  private int status = 0;

  public FuelStatusObdCommand() {
    super("01031");
    this.setCheckFrequency(100);
  }

  public FuelStatusObdCommand(FuelStatusObdCommand other) {
    super(other);
  }

  @Override
  protected void performCalculations() {
    // ignore first two bytes [hh hh] of the response
	 if (isValid()) {
		 status = buffer.get(2);
	 } 
  }

  @Override
  public String getFormattedResult() {
    return String.format(FuelStatus.formatted(status));
  }

  public int getStatus() { 
	  return status;
  }

  @Override
  public String getName() {
    return AvailableCommandNames.FUEL_CONSUMPTION.getValue();
  }

}
