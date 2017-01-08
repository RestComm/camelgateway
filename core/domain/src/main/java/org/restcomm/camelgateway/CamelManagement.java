/**
 * TeleStax, Open Source Cloud Communications  Copyright 2012. 
 * and individual contributors
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
package org.restcomm.camelgateway;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.apache.log4j.Logger;
import org.jboss.mx.util.MBeanServerLocator;

/**
 * @author amit bhayani
 * 
 */
public class CamelManagement {
	private static final Logger logger = Logger.getLogger(CamelManagement.class);

	public static final String JMX_DOMAIN = "org.restcomm.camelgateway";

	protected static final String CAMEL_PERSIST_DIR_KEY = "camel.persist.dir";
	protected static final String USER_DIR_KEY = "user.dir";

	private String persistDir = null;
	private final String name;

	private CamelPropertiesManagement camelPropertiesManagement = null;
	private NetworkRoutingRuleManagement networkRoutingRuleManagement = null;

	private MBeanServer mbeanServer = null;

	public CamelManagement(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getPersistDir() {
		return persistDir;
	}

	public void setPersistDir(String persistDir) {
		this.persistDir = persistDir;
	}

	public void start() throws Exception {
        CamelStatAggregator.getInstance().clearDialogsInProcess();

        this.camelPropertiesManagement = CamelPropertiesManagement.getInstance(this.name);
		this.camelPropertiesManagement.setPersistDir(this.persistDir);
		this.camelPropertiesManagement.start();

		this.networkRoutingRuleManagement = NetworkRoutingRuleManagement.getInstance(this.name);
		this.networkRoutingRuleManagement.setPersistDir(this.persistDir);
		this.networkRoutingRuleManagement.start();

		// Register the MBeans
		this.mbeanServer = MBeanServerLocator.locateJBoss();

		ObjectName camelPropObjNname = new ObjectName(CamelManagement.JMX_DOMAIN + ":name=CamelPropertiesManagement");
		StandardMBean camelPropMxBean = new StandardMBean(this.camelPropertiesManagement,
				CamelPropertiesManagementMBean.class, true);
		this.mbeanServer.registerMBean(camelPropMxBean, camelPropObjNname);

		ObjectName ussdScRuleObjNname = new ObjectName(CamelManagement.JMX_DOMAIN
				+ ":name=NetworkRoutingRuleManagement");
		StandardMBean ussdScRuleMxBean = new StandardMBean(this.networkRoutingRuleManagement,
				NetworkRoutingRuleManagementMBean.class, true);
		this.mbeanServer.registerMBean(ussdScRuleMxBean, ussdScRuleObjNname);

		logger.info("Started CamelGatewayManagement");
	}

	public void stop() throws Exception {
		this.camelPropertiesManagement.stop();
		this.networkRoutingRuleManagement.stop();

		if (this.mbeanServer != null) {

			ObjectName camelPropObjNname = new ObjectName(CamelManagement.JMX_DOMAIN
					+ ":name=CamelPropertiesManagement");
			this.mbeanServer.unregisterMBean(camelPropObjNname);

			ObjectName ussdScRuleObjNname = new ObjectName(CamelManagement.JMX_DOMAIN
					+ ":name=NetworkRoutingRuleManagement");
			this.mbeanServer.unregisterMBean(ussdScRuleObjNname);
		}
	}
}
