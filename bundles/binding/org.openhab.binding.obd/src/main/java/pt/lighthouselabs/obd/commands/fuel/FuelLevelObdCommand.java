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

/**
 * Get fuel level in percentage
 */
public class FuelLevelObdCommand extends ObdCommand {

  private float fuelLevel = 0f;

  public FuelLevelObdCommand() {
    super("012F1");
    this.setCheckFrequency(60000);
  }

  @Override
  protected void performCalculations() {
    // ignore first two bytes [hh hh] of the response
    fuelLevel = 100.0f * buffer.get(2) / 255.0f;
  }

  @Override
  public String getFormattedResult() {
    return String.format("%.1f%s", fuelLevel, "%");
  }

  @Override
  public String getName() {
    return AvailableCommandNames.FUEL_LEVEL.getValue();
  }

  public float getFuelLevel() {
    return fuelLevel;
  }

}