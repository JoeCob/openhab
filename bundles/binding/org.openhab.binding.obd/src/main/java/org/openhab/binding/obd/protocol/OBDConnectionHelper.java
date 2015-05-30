/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.obd.protocol;

import java.util.TimerTask;

import org.openhab.binding.obd.internal.OBDBinding;

/**
 * Connection helper which can be executed periodically to try to reconnect to a
 * broker if the connection was previously lost.
 * 
 * @author Davy Vanherbergen
 * @since 1.3.0
 */
public class OBDConnectionHelper extends TimerTask {

	private OBDBinding connection;

	/**
	 * Create new connection helper to help reconnect the given connection.
	 * 
	 * @param connection
	 *            to reconnect.
	 */
	public OBDConnectionHelper(OBDBinding connection) {
		this.connection = connection;
	}

	@Override
	public void run() {
		try {
			connection.activate();
		} catch (Exception e) {
			// reconnect failed,
			// maybe we will have more luck next time...
		}
	}

}
