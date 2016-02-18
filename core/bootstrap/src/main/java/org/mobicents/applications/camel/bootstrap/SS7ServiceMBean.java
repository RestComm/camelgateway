/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.restcomm.applications.camel.bootstrap;

/**
 *
 * @author kulikov
 */
public interface SS7ServiceMBean {
	public static final String ONAME = "org.mobicents.ss7:service=SS7Service";

	public void start() throws Exception;
	public void stop();
     /**
      * Returns SCCP Provider jndi name.
      */
	 public String getJndiName();
	 
	 /**
	  * Returns SS7 Name for this release
	  * @return
	  */
	 public String getSS7Name();
	 
	 /**
	  * Get Vendor supporting this SS7
	  * @return
	  */
	 public String getSS7Vendor();
	 
	 /**
	  * Return SS7 version of this release
	  * @return
	  */
	 public String getSS7Version();
	 
	 String getSS7ServiceName();

		boolean isStarted();
}
