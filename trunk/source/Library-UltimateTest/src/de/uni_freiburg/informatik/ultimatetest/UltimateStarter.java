/*
 * Copyright (C) 2014-2015 Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * Copyright (C) 2015 University of Freiburg
 * 
 * This file is part of the ULTIMATE UnitTest Library.
 * 
 * The ULTIMATE UnitTest Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * The ULTIMATE UnitTest Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ULTIMATE UnitTest Library. If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify the ULTIMATE UnitTest Library, or any covered work, by linking
 * or combining it with Eclipse RCP (or a modified version of Eclipse RCP), 
 * containing parts covered by the terms of the Eclipse Public License, the 
 * licensors of the ULTIMATE UnitTest Library grant you additional permission 
 * to convey the resulting work.
 */
package de.uni_freiburg.informatik.ultimatetest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.core.runtime.IStatus;
import org.xml.sax.SAXException;

import de.uni_freiburg.informatik.ultimate.core.controllers.ExternalUltimateCore;
import de.uni_freiburg.informatik.ultimate.core.coreplugin.toolchain.ToolchainData;
import de.uni_freiburg.informatik.ultimate.core.preferences.UltimatePreferenceInitializer;
import de.uni_freiburg.informatik.ultimate.core.services.model.ILoggingService;
import de.uni_freiburg.informatik.ultimate.core.services.model.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.ep.interfaces.IController;
import de.uni_freiburg.informatik.ultimate.ep.interfaces.ICore;
import de.uni_freiburg.informatik.ultimate.ep.interfaces.ISource;
import de.uni_freiburg.informatik.ultimate.ep.interfaces.ITool;

/**
 * 
 * This class wraps the Ultimate application and allows to start it without
 * setting an IController object.
 * 
 * Call runUltimate() to execute it and complete after processing the results
 * (to release resources).
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * 
 */
public class UltimateStarter implements IController {

	private Logger mLogger;
	private FileAppender mAppender;

	private final UltimateRunDefinition mUltimateRunDefinition;
	private final long mDeadline;
	private final String mLogPattern;
	private final File mLogFile;
	private final ExternalUltimateCore mExternalUltimateCore;

	private IUltimateServiceProvider mCurrentSerivces;
	
	public UltimateStarter(UltimateRunDefinition ultimateRunDefinition, long deadline) {
		this(ultimateRunDefinition, deadline, null, null);
	}

	public UltimateStarter(UltimateRunDefinition ultimateRunDefintion, long deadline, File logFile, String logPattern) {
		mUltimateRunDefinition = ultimateRunDefintion;
		mLogger = Logger.getLogger(UltimateStarter.class);
		mExternalUltimateCore = new ExternalUltimateCoreTest(this);
		mDeadline = deadline;
		mLogFile = logFile;
		mLogPattern = logPattern;
		detachLogger();
	}

	public IStatus runUltimate() throws Throwable {
		return mExternalUltimateCore.runUltimate();
	}

	@Override
	public int init(ICore core, ILoggingService loggingService) {
		core.resetPreferences();
		return mExternalUltimateCore.init(core, loggingService, mUltimateRunDefinition.getSettings(), mDeadline,
				mUltimateRunDefinition.getInput(), null).getCode();
	}

	public void complete() {
		mExternalUltimateCore.complete();
	}

	private void attachLogger() {
		if (mLogFile == null) {
			return;
		}

		detachLogger();
		try {
			mAppender = new FileAppender(new PatternLayout(mLogPattern), mLogFile.getAbsolutePath());
			mLogger.addAppender(mAppender);
		} catch (IOException e1) {
			detachLogger();
			mLogger.fatal("Failed to create logfile " + mLogFile + ". Reason: " + e1);
		}
	}

	private void detachLogger() {
		if (mAppender == null) {
			return;
		}
		mLogger.removeAppender(mAppender);
	}

	@Override
	public String getPluginName() {
		return "UltimateStarter";
	}

	@Override
	public String getPluginID() {
		return "UltimateStarter";
	}

	@Override
	public UltimatePreferenceInitializer getPreferences() {
		return null;
	}

	@Override
	public ISource selectParser(Collection<ISource> parser) {
		mLogger.fatal("UltimateStarter does not support the selection of parsers by the user!");
		return null;
	}

	@Override
	public ToolchainData selectTools(List<ITool> tools) {
		try {
			ToolchainData tc = new ToolchainData(mUltimateRunDefinition.getToolchain().getAbsolutePath());
			mCurrentSerivces = tc.getServices();
			mLogger.info("Loaded toolchain from " + mUltimateRunDefinition.getToolchain().getAbsolutePath());
			return tc;
		} catch (FileNotFoundException | JAXBException | SAXException e) {
			mLogger.fatal("Toolchain could not be created from file " + mUltimateRunDefinition.getToolchain() + ": "
					+ e);
			return null;
		}
	}

	@Override
	public List<String> selectModel(List<String> modelNames) {
		mLogger.fatal("UltimateStarter does not support the selection of models by the user!");
		return null;
	}

	@Override
	public void displayToolchainResultProgramIncorrect() {

	}

	@Override
	public void displayToolchainResultProgramCorrect() {

	}

	@Override
	public void displayToolchainResultProgramUnknown(String description) {

	}

	@Override
	public void displayException(String description, Throwable ex) {

	}

	public IUltimateServiceProvider getServices() {
		return mCurrentSerivces;
	}

	private class ExternalUltimateCoreTest extends ExternalUltimateCore {

		public ExternalUltimateCoreTest(IController controller) {
			super(controller);
		}

		@Override
		protected Logger getLogger(ILoggingService loggingService) {
			mLogger = super.getLogger(loggingService);
			attachLogger();
			return mLogger;
		}

		@Override
		public void complete() {
			detachLogger();
			super.complete();
		}

	}
}
