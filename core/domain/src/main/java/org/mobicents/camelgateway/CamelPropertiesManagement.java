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

import javolution.text.TextBuilder;
import javolution.xml.XMLBinding;
import javolution.xml.XMLObjectReader;
import javolution.xml.XMLObjectWriter;
import javolution.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

/**
 * @author amit bhayani
 * 
 */
public class CamelPropertiesManagement implements CamelPropertiesManagementMBean {

	private static final Logger logger = Logger.getLogger(CamelPropertiesManagement.class);

	protected static final String ROUTE = "route";
	protected static final String NO_ACTIVITY_TIMEOUT_SEC = "noactivitytimeoutsec";
	protected static final String UPDATE_ASSIGN_INVOKE_IDS = "updateassignedinvokeids";

	private static final String TAB_INDENT = "\t";
	private static final String CLASS_ATTRIBUTE = "type";
	private static final XMLBinding binding = new XMLBinding();
	private static final String PERSIST_FILE_NAME = "camelproperties.xml";

	private static CamelPropertiesManagement instance;

	private final String name;

	private String persistDir = null;

	private final TextBuilder persistFile = TextBuilder.newInstance();

	private String route;
	
	//Default we keep 1 hr?
	private int noActivityTimeoutSec = 3600;
	
	//Default is false
	private boolean updateAssignedInvokeIds = false;

	private CamelPropertiesManagement(String name) {
		this.name = name;
		binding.setClassAttribute(CLASS_ATTRIBUTE);
	}

	protected static CamelPropertiesManagement getInstance(String name) {
		if (instance == null) {
			instance = new CamelPropertiesManagement(name);
		}
		return instance;
	}

	public static CamelPropertiesManagement getInstance() {
		return instance;
	}

	public String getName() {
		return name;
	}

	@Override
	public String getRoute() {
		return this.route;
	}

	@Override
	public void setRoute(String route) {
		this.route = route;
		this.store();
	}

	public String getPersistDir() {
		return persistDir;
	}

	public void setPersistDir(String persistDir) {
		this.persistDir = persistDir;
	}

	@Override
	public void setNoActivityTimeoutSec(int notifyAfter) {
		if(notifyAfter < 3){
			logger.warn("NoActivityTimeoutSec value is less than 3 secs, setting to default  which is 3 sec");
			notifyAfter = 3;
		}
		this.noActivityTimeoutSec = notifyAfter;
		this.store();
	}

	@Override
	public int getNoActivityTimeoutSec() {
		return noActivityTimeoutSec;
	}
	
	@Override
	public void setUpdateAssignedInvokeIds(boolean updateAssignedInvokeIds) {
		this.updateAssignedInvokeIds = updateAssignedInvokeIds;
		this.store();
	}

	@Override
	public boolean getUpdateAssignedInvokeIds() {
		return this.updateAssignedInvokeIds;
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

		logger.info(String.format("Loading CAMEL Properties from %s", persistFile.toString()));

		try {
			this.load();
		} catch (FileNotFoundException e) {
			logger.warn(String.format("Failed to load the CAMEL configuration file. \n%s", e.getMessage()));
		}

	}

	public void stop() throws Exception {
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
			writer.write(this.noActivityTimeoutSec, NO_ACTIVITY_TIMEOUT_SEC, Integer.class);
			writer.write(this.updateAssignedInvokeIds, UPDATE_ASSIGN_INVOKE_IDS, Boolean.class);
			writer.write(this.route, ROUTE, String.class);
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
			try{
				this.noActivityTimeoutSec = reader.read(NO_ACTIVITY_TIMEOUT_SEC, Integer.class);
			}catch(NullPointerException npe){
				//ignore
			}
			
			try{
				this.updateAssignedInvokeIds = reader.read(UPDATE_ASSIGN_INVOKE_IDS, Boolean.class);
			}catch(NullPointerException npe){
				//ignore
			}
			this.route = reader.read(ROUTE, String.class);
			reader.close();
		} catch (XMLStreamException ex) {
			// this.logger.info(
			// "Error while re-creating Linksets from persisted file", ex);
		}
	}


}
