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

import java.util.List;

import org.restcomm.camelgateway.rules.NetworkRoutingRule;

/**
 * @author amit bhayani
 * 
 */
public interface NetworkRoutingRuleManagementMBean {
	List<NetworkRoutingRule> getNetworkRoutingRuleList();

	NetworkRoutingRule getNetworkRoutingRule(int networkId);

	/**
	 * Creates new HTTP based routing rule
	 * 
	 * @param shortCode
	 *            the ussd short code for this rule
	 * @param url
	 *            the HTTP URL where HTTP POST request should be forwarded if
	 *            rule match's
	 * @return the {@link NetworkRoutingRule} instance
	 * 
	 * @throws Exception
	 */
	NetworkRoutingRule createNetworkRoutingRule(int networkId, String url)
			throws Exception;

	NetworkRoutingRule deleteNetworkRoutingRule(int networkId) throws Exception;
}
