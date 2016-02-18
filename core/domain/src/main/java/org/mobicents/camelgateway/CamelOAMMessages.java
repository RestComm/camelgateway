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

/**
 * 
 * @author amit bhayani
 * 
 */
public interface CamelOAMMessages {
	
	/**
	 * Generic constants
	 */
	public static final String TAB = "        ";
	
	public static final String SPACE = " ";
	
	public static final String NEW_LINE = "\n";

	public static final String COMMA = ",";
	
	
	public static final String CREATE_NETWORK_RULE_FAIL_ALREADY_EXIST = "Creation of network routing rule failed. Rule already exist";
	
	public static final String NULL_RULE_TYPE = "Rule type cannot be null";
	
	public static final String DELETE_RULE_FAILED_NO_RULE_FOUND = "No network routing rule found for %d";
	
	public static final String INVALID_ROUTING_RULE_URL = "Invalid routing rule URL";
	
	public static final String CREATE_NETWORK_RULE_SUCCESSFULL = "Successfully created routing rule for network id=%d";
	
	public static final String DELETE_NETWORK_RULE_SUCCESSFUL = "Successfully deleted routing rule for network id=%d";
	
	public static final String NO_NETWORK_RULE_DEFINED_YET = "No network routing rule defined yet";

	public static final String INVALID_COMMAND = "Invalid Command";

	public static final String PARAMETER_SUCCESSFULLY_SET = "Parameter has been successfully set";
	
	
	public static final String SHOW_NETWORK_ID = "Network id=";
	
	public static final String SHOW_URL = " URL=";
	
	public static final String SHOW_EXACT_MATCH = " Exact Match=";

}
