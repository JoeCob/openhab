/**
 * Copyright (c) 2010-2015, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.obd.internal;

/**
 * Exception for Open Energy Monitor errors.
 * 
 * @author Pauli Anttila
 * @since 1.4.0
 */
public class OBDException extends Exception {

	private static final long serialVersionUID = 572419226403043307L;

	public OBDException() {
		super();
	}

	public OBDException(String message) {
		super(message);
	}

	public OBDException(String message, Throwable cause) {
		super(message, cause);
	}

	public OBDException(Throwable cause) {
		super(cause);
	}

}
