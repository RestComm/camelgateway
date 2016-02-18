/**
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
package org.restcomm.camelgateway;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.restcomm.camelgateway.rules.NetworkRoutingRule;
import org.mobicents.ss7.management.console.ShellExecutor;

/**
 * @author amit bhayani
 * 
 */
public class CamelShellExecutor implements ShellExecutor {

	private static final Logger logger = Logger.getLogger(CamelShellExecutor.class);

	private CamelManagement camelManagement;
	private CamelPropertiesManagement camelPropertiesManagement = CamelPropertiesManagement.getInstance();
	private NetworkRoutingRuleManagement networkRoutingRuleManagement = NetworkRoutingRuleManagement.getInstance();

	/**
	 * 
	 */
	public CamelShellExecutor() {
		// TODO Auto-generated constructor stub
	}

	public CamelManagement getCamelManagement() {
		return camelManagement;
	}

	public void setCamelManagement(CamelManagement camelManagement) {
		this.camelManagement = camelManagement;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.ss7.management.console.ShellExecutor#execute(java.lang.
	 * String[])
	 */
	@Override
	public String execute(String[] commands) {

		try {
			if (commands.length < 2) {
				return CamelOAMMessages.INVALID_COMMAND;
			}
			String command = commands[1];

			if (command.equals("networkrule")) {
				return this.manageNetworkRule(commands);
			} else if (command.equals("set")) {
				return this.manageSet(commands);
			} else if (command.equals("get")) {
				return this.manageGet(commands);
			}
			return CamelOAMMessages.INVALID_COMMAND;
		} catch (Exception e) {
			logger.error(String.format("Error while executing comand %s", Arrays.toString(commands)), e);
			return e.getMessage();
		}
	}

	private String manageNetworkRule(String[] commands) throws Exception {
		String command = commands[2];
		if (command.equals("create")) {
			return this.createNetworkRule(commands);
		} else if (command.equals("delete")) {
			return this.deleteNetworkRule(commands);
		} else if (command.equals("show")) {
			return this.showScRule();
		} 
		return CamelOAMMessages.INVALID_COMMAND;
	}

	/**
	 * <p>
	 * Command is camel networkrule create <network-d> <url>
	 * </p>
	 * <p>
	 * 
	 * @param commands
	 * @return
	 * @throws Exception
	 */
	private String createNetworkRule(String[] commands) throws Exception {
		if (commands.length < 5 || commands.length > 7) {
			return CamelOAMMessages.INVALID_COMMAND;
		}

		int networkId = Integer.parseInt(commands[3]);
		String url = commands[4];

		networkRoutingRuleManagement.createNetworkRoutingRule(networkId, url);

		return String.format(CamelOAMMessages.CREATE_NETWORK_RULE_SUCCESSFULL, networkId);
	}

	/**
	 * Command is camel networkrule delete <network-id>
	 * 
	 * @param commands
	 * @return
	 * @throws Exception
	 */
	private String deleteNetworkRule(String[] commands) throws Exception {
		if (commands.length != 4) {
			return CamelOAMMessages.INVALID_COMMAND;
		}

		int shortCode = Integer.parseInt(commands[3]);

		networkRoutingRuleManagement.deleteNetworkRoutingRule(shortCode);

		return String.format(CamelOAMMessages.DELETE_NETWORK_RULE_SUCCESSFUL, shortCode);
	}

	/**
	 * Command is ussd scrule show
	 * 
	 * @return
	 */
	private String showScRule() {
		List<NetworkRoutingRule> esmes = this.networkRoutingRuleManagement.getNetworkRoutingRuleList();
		if (esmes.size() == 0) {
			return CamelOAMMessages.NO_NETWORK_RULE_DEFINED_YET;
		}
		StringBuffer sb = new StringBuffer();
		for (NetworkRoutingRule scRule : esmes) {
			sb.append(CamelOAMMessages.NEW_LINE);
			scRule.show(sb);
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.ss7.management.console.ShellExecutor#handles(java.lang.
	 * String)
	 */
	@Override
	public boolean handles(String command) {
		return "camel".equals(command);
	}

	private String manageSet(String[] options) throws Exception {
		if (options.length < 4) {
			return CamelOAMMessages.INVALID_COMMAND;
		}

		String parName = options[2].toLowerCase();
		if (parName.equals(CamelPropertiesManagement.ROUTE)) {
			camelPropertiesManagement.setRoute(options[3]);
		} else if (parName.equals(CamelPropertiesManagement.NO_ACTIVITY_TIMEOUT_SEC)) {
			camelPropertiesManagement.setNoActivityTimeoutSec(Integer.parseInt(options[3]));
		} else if (parName.equals(CamelPropertiesManagement.UPDATE_ASSIGN_INVOKE_IDS)) {
			camelPropertiesManagement.setUpdateAssignedInvokeIds(Boolean.parseBoolean(options[3]));
		} else {
			return CamelOAMMessages.INVALID_COMMAND;
		}

		return CamelOAMMessages.PARAMETER_SUCCESSFULLY_SET;
	}

	private String manageGet(String[] options) throws Exception {
		if (options.length == 3) {
			String parName = options[2].toLowerCase();

			StringBuilder sb = new StringBuilder();
			sb.append(options[2]);
			sb.append(" = ");
			if (parName.equals(CamelPropertiesManagement.ROUTE)) {
				sb.append(camelPropertiesManagement.getRoute());
			} else if (parName.equals(CamelPropertiesManagement.NO_ACTIVITY_TIMEOUT_SEC)) {
				sb.append(camelPropertiesManagement.getNoActivityTimeoutSec());
			} else if (parName.equals(CamelPropertiesManagement.UPDATE_ASSIGN_INVOKE_IDS)) {
				sb.append(camelPropertiesManagement.getUpdateAssignedInvokeIds());
			} else {
				return CamelOAMMessages.INVALID_COMMAND;
			}

			return sb.toString();
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(CamelPropertiesManagement.ROUTE + " = ");
			sb.append(camelPropertiesManagement.getRoute());
			sb.append("\n");
			sb.append(CamelPropertiesManagement.NO_ACTIVITY_TIMEOUT_SEC + " = ");
			sb.append(camelPropertiesManagement.getNoActivityTimeoutSec());
			sb.append("\n");
			sb.append(CamelPropertiesManagement.UPDATE_ASSIGN_INVOKE_IDS + " = ");
			sb.append(camelPropertiesManagement.getUpdateAssignedInvokeIds());
			sb.append("\n");

			return sb.toString();
		}
	}
}
