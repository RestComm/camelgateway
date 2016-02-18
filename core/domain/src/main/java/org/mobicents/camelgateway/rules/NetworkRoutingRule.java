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
package org.restcomm.camelgateway.rules;

import javolution.xml.XMLFormat;
import javolution.xml.XMLSerializable;
import javolution.xml.stream.XMLStreamException;

import org.restcomm.camelgateway.CamelOAMMessages;

/**
 * Acts as Fact for Rules
 * 
 * @author amit bhayani
 * 
 */
public class NetworkRoutingRule implements XMLSerializable {
	private static final String NETWORK_ID = "networkid";
	private static final String RULE_URL = "ruleurl";

	// networkId
	private int networkId;

	// to be used with other protocols
	private String ruleUrl;

	public NetworkRoutingRule() {

	}

	public NetworkRoutingRule(int networkId) {
		this.networkId = networkId;
	}

	public int getNetworkId() {
		return networkId;
	}

	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	public String getRuleUrl() {
		return ruleUrl;
	}

	public void setRuleUrl(String ruleUrl) {
		this.ruleUrl = ruleUrl;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		this.show(sb);
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + networkId;
		result = prime * result + ((ruleUrl == null) ? 0 : ruleUrl.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NetworkRoutingRule other = (NetworkRoutingRule) obj;
		if (networkId != other.networkId)
			return false;
		if (ruleUrl == null) {
			if (other.ruleUrl != null)
				return false;
		} else if (!ruleUrl.equals(other.ruleUrl))
			return false;
		return true;
	}

	/**
	 * XML Serialization/Deserialization
	 */
	protected static final XMLFormat<NetworkRoutingRule> ESME_XML = new XMLFormat<NetworkRoutingRule>(
			NetworkRoutingRule.class) {

		@Override
		public void read(javolution.xml.XMLFormat.InputElement xml,
				NetworkRoutingRule esme) throws XMLStreamException {
			esme.networkId = xml.getAttribute(NETWORK_ID, -1);
			esme.ruleUrl = xml.getAttribute(RULE_URL, null);
		}

		@Override
		public void write(NetworkRoutingRule esme,
				javolution.xml.XMLFormat.OutputElement xml)
				throws XMLStreamException {
			xml.setAttribute(NETWORK_ID, esme.networkId);
			xml.setAttribute(RULE_URL, esme.ruleUrl);
		}
	};

	public void show(StringBuffer sb) {
		sb.append(CamelOAMMessages.SHOW_NETWORK_ID).append(this.networkId)
				.append(CamelOAMMessages.SHOW_URL).append(this.ruleUrl);
		sb.append(CamelOAMMessages.NEW_LINE);
	}

}
