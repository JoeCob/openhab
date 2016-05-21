/**
 *
 *
 * 
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.obd.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extension of the default OSGi bundle activator
 * 
 * @author Pauli Anttila
 * @since 1.4.0
 */
public final class OBDActivator implements BundleActivator {

	private static Logger logger = LoggerFactory
			.getLogger(OBDActivator.class);

	private static BundleContext context;

	/**
	 * Called whenever the OSGi framework starts our bundle
	 */
	public void start(BundleContext bc) throws Exception {
		context = bc;
		logger.info("OBD binding has been started. v1.4.1");
		//V1.1 - Changes to variables to try to reduce garbage generation. 
		//	   - Moved MAF calculation to setMaf instead of getMaf. 
		//v1.1.1 - Changed Connection method to use fullInit. 
		//	   - Should help handling reconnectios bettter.
		//v1.2 - Changed serial read method to improve speed. 
		// 	   - Increased read frequency to some commands (engine, speed and pressure). 
		//v1.3 - Added Battery Voltage Information
		//     - Changed logic when pooling fails to continue and update the values. 
		//	   - Added gear calculation to the binding instead of doing trough rules. 
		//v1.4 - Added Ignition Check using battery level and rpm as failback.
		//v1.4.1 - Changed expcetion handling to avoid stalling with NAN or other parsing error at Binding. 
		//		 - Changed the pooling string for (removed space and leading zero) pid rate increase.
		//		 - Removed sleeping at ObdCOmmand
		//v1.4.2 - Added fuelType
		//		 - Fix for NAN on getMaf (2nd try)
		//		 - Changed verbosity of some log messages. 
		
	}

	/**
	 * Called whenever the OSGi framework stops our bundle
	 */
	public void stop(BundleContext bc) throws Exception {
		context = null;
		logger.info("OBD binding has been stopped.");
	}

	/**
	 * Returns the bundle context of this bundle
	 * 
	 * @return the bundle context
	 */
	public static BundleContext getContext() {
		return context;
	}

}
