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

import pt.lighthouselabs.obd.commands.ObdCommand;
import pt.lighthouselabs.obd.enums.AvailableCommandNames;

/**
 * Engine runtime.
 */
public class EngineRuntimeObdCommand extends ObdCommand {

  private int value = 0;

  /**
   * Default ctor.
   */
  public EngineRuntimeObdCommand() {
    super("011F");
  }

  /**
   * Copy ctor.
   * 
   * @param other
   */
  public EngineRuntimeObdCommand(EngineRuntimeObdCommand other) {
    super(other);
  }

  @Override
  protected void performCalculations() {
    // ignore first two bytes [01 0C] of the response
    value = buffer.get(2) * 256 + buffer.get(3);
  }

  @Override
  public String getFormattedResult() {
    // determine time
    final String hh = String.format("%02d", value / 3600);
    final String mm = String.format("%02d", (value % 3600) / 60);
    final String ss = String.format("%02d", value % 60);
    return String.format("%s:%s:%s", hh, mm, ss);
  }

  @Override
  public String getName() {
    return AvailableCommandNames.ENGINE_RUNTIME.getValue();
  }

}
