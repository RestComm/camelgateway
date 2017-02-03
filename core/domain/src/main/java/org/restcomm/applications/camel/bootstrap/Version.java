/*
 * JBoss, Home of Professional Open Source
 * Copyright XXXX, Red Hat Middleware LLC, and individual contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a full listing
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301, USA.
 */
package org.restcomm.applications.camel.bootstrap;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Version class reads the version.properties packaged with for
 * run time display of Version
 * 
 * @author amit.bhayani
 * 
 */
public final class Version {

	/**
	 * The single instance.
	 */
	public final static Version instance = new Version();

	/**
	 * The version properties.
	 */
	private Properties props;

	/**
	 * Do not allow direct public construction.
	 */
	private Version() {
		props = loadProperties();
	}

	/**
	 * Returns an unmodifiable map of version properties.
	 * 
	 * @return
	 */
	public Map getProperties() {
		return Collections.unmodifiableMap(props);
	}

	/**
	 * Returns the value for the given property name.
	 * 
	 * @param name -
	 *            The name of the property.
	 * @return The property value or null if the property is not set.
	 */
	public String getProperty(final String name) {
		return props.getProperty(name);
	}

    public String getProjectName() {
        String version = instance.getProperty("name");
        if (version != null) {
            return version;
        } else {
            return "???";
        }
    }

    public String getProjectType() {
        String version = instance.getProperty("project.type");
        if (version != null) {
            return version;
        } else {
            return "???";
        }
    }

    public String getProjectVersion() {
        String version = instance.getProperty("version");
        if (version != null) {
            return version;
        } else {
            return "???";
        }
    }

    public String getStatisticsServer() {
        String version = instance.getProperty("statistics.server");
        if (version != null) {
            return version;
        } else {
            return "https://statistics.restcomm.com/rest/";
        }
    }

    public String getShortName() {
        String version = instance.getProperty("short.name");
        if (version != null) {
            return version;
        } else {
            return "???";
        }
    }

	/**
	 * Returns the version information as a string.
	 * 
	 * @return Basic information as a string.
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder("RestComm Camel Server: ");
		boolean first = true;
		for (Object key : props.keySet()) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append(key).append('=').append(props.get(key));
		}
		return sb.toString();
	}

	/**
	 * Load the version properties from a resource.
	 */
	private Properties loadProperties() {

		props = new Properties();

		try {
			InputStream in = Version.class
					.getResourceAsStream("version.properties");
			props.load(in);
			in.close();
		} catch (Exception e) {
			throw new Error("Missing version.properties");
		}

		return props;
	}

}
