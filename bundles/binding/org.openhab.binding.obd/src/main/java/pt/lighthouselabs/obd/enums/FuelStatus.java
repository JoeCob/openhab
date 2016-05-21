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
package pt.lighthouselabs.obd.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Select one of the Fuel Trim percentage banks to access.
 */
public enum FuelStatus {

	/*Value	Description
1	Open loop due to insufficient engine temperature
2	Closed loop, using oxygen sensor feedback to determine fuel mix
4	Open loop due to engine load OR fuel cut due to deceleration
8	Open loop due to system failure
16	Closed loop, using at least one oxygen sensor but there is a fault in the feedback system */
	
  OPEN_LOOP(0x01, "Open loop due to insufficient engine temperature"),
  CLOSED_LOOP(0x02, "Closed loop, using oxygen sensor feedback to determine fuel mix"),
  OPEN_LOOP_LOAD_DECEL(0x04, "Open loop due to engine load OR fuel cut due to deceleration"),
  OPEN_LOOP_FAILURE(0x08, "Open loop due to system failure"),
  CLOSED_LOOP_FAULT(0x16, "Closed loop, using at least one oxygen sensor but there is a fault in the feedback system");
  
  private final int value;
  private final String status;

  private static Map<Integer, FuelStatus> map = new HashMap<Integer, FuelStatus>();

  static {
    for (FuelStatus error : FuelStatus.values())
      map.put(error.getValue(), error);
  }

  private FuelStatus(final int value, final String status) {
    this.value = value;
    this.status = status;
  }

  public int getValue() {
    return value;
  }

  public String getStatus() {
    return status;
  }

  public static FuelStatus fromValue(final int value) {
    return map.get(value);
  }

  public static String formatted (final int value) {
	    return map.get(value).status;
	  }

}