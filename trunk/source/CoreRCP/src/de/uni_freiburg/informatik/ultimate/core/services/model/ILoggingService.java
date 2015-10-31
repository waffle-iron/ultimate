/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE Core.
 * 
 * The ULTIMATE Core is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE Core is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE Core. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE Core, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE Core grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimate.core.services.model;

import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.ep.interfaces.ITool;

public interface ILoggingService {

	/**
	 * Gets the correct logger for an asking plug-in This method should be
	 * called by all plug-ins that extend {@link ITool} and by the Core Plugin
	 * and want to access their {@link Logger}. Appenders are set as defaults
	 * and log levels are set using preferences <br/>
	 * <br/>
	 * <b>Usage:</b> Most tools can use their Activator.s_PLUGIN_ID as argument<br/>
	 * <br/>
	 * Not to be used for external tools or the controller plug-in
	 * 
	 * @param pluginId
	 *            The PluginId of the plug-in that asks for its {@link Logger}
	 * @return the log4j {@link Logger} for the plug-in
	 */
	public abstract Logger getLogger(String pluginId);

	/**
	 * Gets the correct logger for an asking external tool This method should be
	 * called by all external tools that want to access their {@link Logger}.
	 * Appenders are set as defaults and log levels are set using the
	 * preferences <br/>
	 * 
	 * Not to be used for plug-ins!
	 * 
	 * @param id
	 *            An identifier for the external tool that asks for its
	 *            {@link Logger}
	 * @return the log4j {@link Logger} for the external tool
	 */
	public abstract Logger getLoggerForExternalTool(String id);

	/**
	 * Gets the correct logger for the controller plug-in. This method should be
	 * called by all controller plug-ins that want to access their
	 * {@link Logger}. Appenders are set as defaults and log levels are set
	 * using the Eclipse IPreferenceStore <br/>
	 * 
	 * @return Logger for the current controller.
	 */
	public abstract Logger getControllerLogger();

}