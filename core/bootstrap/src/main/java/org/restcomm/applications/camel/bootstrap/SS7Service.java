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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.jboss.system.ServiceMBeanSupport;

/**
 * @author amit bhayani
 */
public class SS7Service extends ServiceMBeanSupport implements SS7ServiceMBean {

	private static final Logger logger = Logger.getLogger(SS7Service.class);

	private Object stack;

	private String jndiName;
	
	private final String serviceName;

	private static final String rLogo = " ]]]]]]]]] ";
	private static final String lLogo = " [[[[[[[[[ ";

    public SS7Service(String serviceName) {
        this.serviceName = serviceName;
    }
    
	@Override
	public void startService() throws Exception {
		// starting
		rebind(stack);
		logger.info(generateMessageWithLogo("service started"));
	}
	
    public String getSS7ServiceName() {
        return this.serviceName;
    }

    private String generateMessageWithLogo(String message) {
		return lLogo + getSS7Name() + " " + getSS7Version() + " " + message + rLogo;
	}

	public String getSS7Name() {
		String name = Version.instance.getProperty("name");
		if (name != null) {
			return name;
		} else {
			return "Restcomm Camel";
		}
	}

	public String getSS7Vendor() {
		String vendor = Version.instance.getProperty("vendor");
		if (vendor != null) {
			return vendor;
		} else {
			return "TeleStax Inc";
		}
	}

	public String getSS7Version() {
		String version = Version.instance.getProperty("version");
		if (version != null) {
			return version;
		} else {
			return "2.0";
		}
	}

	public void setJndiName(String jndiName) {
		this.jndiName = jndiName;
	}

	public String getJndiName() {
		return jndiName;
	}

	public Object getStack() {
		return stack;
	}

	public void setStack(Object stack) {
		this.stack = stack;
	}

	@Override
	public void stopService() {
		try {
			unbind(jndiName);
		} catch (Exception e) {

		}

		logger.info(generateMessageWithLogo("service stopped"));
	}

	/**
	 * Binds trunk object to the JNDI under the jndiName.
	 */
	private void rebind(Object stack) throws NamingException {
		Context ctx = new InitialContext();
		String tokens[] = jndiName.split("/");

		for (int i = 0; i < tokens.length - 1; i++) {
			if (tokens[i].trim().length() > 0) {
				try {
					ctx = (Context) ctx.lookup(tokens[i]);
				} catch (NamingException e) {
					ctx = ctx.createSubcontext(tokens[i]);
				}
			}
		}

		ctx.bind(tokens[tokens.length - 1], stack);
	}

	/**
	 * Unbounds object under specified name.
	 * 
	 * @param jndiName
	 *            the JNDI name of the object to be unbound.
	 */
	private void unbind(String jndiName) throws NamingException {
		InitialContext initialContext = new InitialContext();
		initialContext.unbind(jndiName);
	}
	
    @Override
    public boolean isStarted() {
        return (this.getState() == STARTED);
    }

}