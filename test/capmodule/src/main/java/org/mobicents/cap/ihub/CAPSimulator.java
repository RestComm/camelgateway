/*
 * TeleStax, Open Source Cloud Communications  
 * Copyright 2012, Telestax Inc and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.mobicents.cap.ihub;

import org.mobicents.protocols.ss7.cap.CAPStackImpl;
import org.mobicents.protocols.ss7.cap.api.CAPProvider;
import org.mobicents.protocols.ss7.sccp.impl.SccpStackImpl;

/**
 * 
 * @author Amit Bhayani
 * 
 */
public class CAPSimulator {

	// MAP
	private CAPStackImpl capStack;
	private CAPProvider capProvider;

	// SCCP
	private SccpStackImpl sccpStack;

	// SSn
	private int ssn;

	private CAPListener capListener = null;

	public CAPSimulator() {

	}

	public SccpStackImpl getSccpStack() {
		return sccpStack;
	}

	public void setSccpStack(SccpStackImpl sccpStack) {
		this.sccpStack = sccpStack;
	}

	public int getSsn() {
		return ssn;
	}

	public void setSsn(int ssn) {
		this.ssn = ssn;
	}

	public void start() throws Exception {
		// Create MAP Stack and register listener
		this.capStack = new CAPStackImpl("Test", this.sccpStack.getSccpProvider(), this.getSsn());
		this.capProvider = this.capStack.getCAPProvider();

		this.capListener = new CAPListener(this);

		this.capProvider.addCAPDialogListener(this.capListener);

		this.capProvider.getCAPServiceCircuitSwitchedCall().addCAPServiceListener(this.capListener);

		this.capProvider.getCAPServiceCircuitSwitchedCall().acivate();

		this.capStack.start();

	}

	public void stop() {
		this.capStack.stop();
	}

	protected CAPProvider getCapProvider() {
		return capProvider;
	}

}
