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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.List;

import javolution.text.TextBuilder;
import javolution.util.FastList;
import javolution.xml.XMLBinding;
import javolution.xml.XMLObjectReader;
import javolution.xml.XMLObjectWriter;
import javolution.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;
import org.restcomm.camelgateway.rules.NetworkRoutingRule;

/**
 * @author amit bhayani
 * 
 */
public class NetworkRoutingRuleManagement implements NetworkRoutingRuleManagementMBean {

	private static final Logger logger = Logger.getLogger(NetworkRoutingRuleManagement.class);

	private static final String SC_ROUTING_RULE_LIST = "networkroutingrulelist";

	private static final String TAB_INDENT = "\t";
	private static final String CLASS_ATTRIBUTE = "type";
	private static final XMLBinding binding = new XMLBinding();
	private static final String PERSIST_FILE_NAME = "networkroutingrule.xml";

	private String name;

	private String persistDir = null;

	protected FastList<NetworkRoutingRule> scRoutingRuleList = new FastList<NetworkRoutingRule>();

	private final TextBuilder persistFile = TextBuilder.newInstance();

	private static NetworkRoutingRuleManagement instance;

	private NetworkRoutingRuleManagement(String name) {
		this.name = name;
		binding.setClassAttribute(CLASS_ATTRIBUTE);
		binding.setAlias(NetworkRoutingRule.class, "networkroutingrule");
	}

	protected static NetworkRoutingRuleManagement getInstance(String name) {
		if (instance == null) {
			instance = new NetworkRoutingRuleManagement(name);
		}
		return instance;
	}

	public static NetworkRoutingRuleManagement getInstance() {
		return instance;
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

	@Override
	public List<NetworkRoutingRule> getNetworkRoutingRuleList() {
		return this.scRoutingRuleList.unmodifiable();
	}

	@Override
	public NetworkRoutingRule getNetworkRoutingRule(int networkId) {
		for (FastList.Node<NetworkRoutingRule> n = this.scRoutingRuleList.head(), end = this.scRoutingRuleList.tail(); (n = n
				.getNext()) != end;) {
			NetworkRoutingRule rule = n.getValue();

			if (rule.getNetworkId() == networkId) {
				return rule;
			}
		}
		return null;
	}

	@Override
	public NetworkRoutingRule createNetworkRoutingRule(int networkId, String urlOrsipProxy) throws Exception {

		NetworkRoutingRule rule = this.getNetworkRoutingRule(networkId);
		if (rule != null) {
			throw new Exception(CamelOAMMessages.CREATE_NETWORK_RULE_FAIL_ALREADY_EXIST);
		}

		if (urlOrsipProxy == null || urlOrsipProxy.equals("")) {
			throw new Exception(CamelOAMMessages.INVALID_ROUTING_RULE_URL);
		}

		rule = new NetworkRoutingRule(networkId);
		rule.setRuleUrl(urlOrsipProxy);

		this.scRoutingRuleList.add(rule);

		this.store();

		return rule;
	}

	@Override
	public NetworkRoutingRule deleteNetworkRoutingRule(int networkId) throws Exception {
		NetworkRoutingRule rule = this.getNetworkRoutingRule(networkId);
		if (rule == null) {
			throw new Exception(String.format(CamelOAMMessages.DELETE_RULE_FAILED_NO_RULE_FOUND, networkId));
		}

		this.scRoutingRuleList.remove(rule);

		this.store();

		return rule;
	}

	public void start() throws Exception {

		this.persistFile.clear();

		if (persistDir != null) {
			this.persistFile.append(persistDir).append(File.separator).append(this.name).append("_")
					.append(PERSIST_FILE_NAME);
		} else {
			persistFile
					.append(System.getProperty(CamelManagement.CAMEL_PERSIST_DIR_KEY,
							System.getProperty(CamelManagement.USER_DIR_KEY))).append(File.separator).append(this.name)
					.append("_").append(PERSIST_FILE_NAME);
		}

		logger.info(String.format("Loading network routig rule configuration from %s", persistFile.toString()));

		try {
			this.load();
		} catch (FileNotFoundException e) {
			logger.warn(String.format("Failed to load the network routig rule configuration file. \n%s", e.getMessage()));
		}

	}

	public void stop() throws Exception {
		this.store();
	}

	public void removeAllResourses() throws Exception {
		this.scRoutingRuleList.clear();
		this.store();
	}

	/**
	 * Persist
	 */
	public void store() {

		// TODO : Should we keep reference to Objects rather than recreating
		// everytime?
		try {
			XMLObjectWriter writer = XMLObjectWriter.newInstance(new FileOutputStream(persistFile.toString()));
			writer.setBinding(binding);
			// Enables cross-references.
			// writer.setReferenceResolver(new XMLReferenceResolver());
			writer.setIndentation(TAB_INDENT);
			writer.write(this.scRoutingRuleList, SC_ROUTING_RULE_LIST, FastList.class);

			writer.close();
		} catch (Exception e) {
			logger.error("Error while persisting the Rule state in file", e);
		}
	}

	/**
	 * Load and create LinkSets and Link from persisted file
	 * 
	 * @throws Exception
	 */
	public void load() throws FileNotFoundException {

		XMLObjectReader reader = null;
		try {
			reader = XMLObjectReader.newInstance(new FileInputStream(persistFile.toString()));

			reader.setBinding(binding);
			this.scRoutingRuleList = reader.read(SC_ROUTING_RULE_LIST, FastList.class);

			reader.close();
		} catch (XMLStreamException ex) {
			// this.logger.info(
			// "Error while re-creating Linksets from persisted file", ex);
		}
	}

}
