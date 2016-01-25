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
package de.uni_freiburg.informatik.ultimate.witnessprinter;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import de.uni_freiburg.informatik.ultimate.core.preferences.UltimatePreferenceStore;
import de.uni_freiburg.informatik.ultimate.core.services.model.IToolchainStorage;
import de.uni_freiburg.informatik.ultimate.core.services.model.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.core.util.CoreUtil;
import de.uni_freiburg.informatik.ultimate.core.util.MonitoredProcess;
import de.uni_freiburg.informatik.ultimate.core.util.MonitoredProcess.MonitoredProcessState;
import de.uni_freiburg.informatik.ultimate.result.WitnessResult;
import de.uni_freiburg.informatik.ultimate.result.WitnessResult.WitnessVerificationStatus;
import de.uni_freiburg.informatik.ultimate.result.model.IProgramExecution;
import de.uni_freiburg.informatik.ultimate.result.model.IResult;
import de.uni_freiburg.informatik.ultimate.util.relation.Triple;
import de.uni_freiburg.informatik.ultimate.witnessprinter.preferences.PreferenceInitializer;

/**
 * The poorly named WitnessManager "handles" witnesses as specified by the Ultimate Core settings.
 * 
 * E.g. it may create witness files from counter example results and may call another verifier with this witness as
 * input.
 * 
 * The actual witness generation is done by the corresponding plugin (i.e. via
 * {@link IProgramExecution#getSVCOMPWitnessString()}).
 * 
 * @author Daniel Dietsch (dietsch@informatik.uni-freiburg.de)
 * @see IProgramExecution
 * 
 */
public class WitnessManager {

	private final Logger mLogger;
	private final IUltimateServiceProvider mServices;
	private final IToolchainStorage mStorage;

	public WitnessManager(final Logger logger, final IUltimateServiceProvider services,
			final IToolchainStorage storage) {
		mLogger = logger;
		mServices = services;
		mStorage = storage;
	}

	/**
	 * 
	 * @param funsResultSupplier
	 *            A collection of functions were each provides a triple (IResult, filename, string that represents a
	 *            valid SVCOMP witness).
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void run(final Collection<Supplier<Triple<IResult, String, String>>> funsResultSupplier)
			throws IOException, InterruptedException {
		final UltimatePreferenceStore ups = new UltimatePreferenceStore(Activator.PLUGIN_ID);

		int cexNo = 0;
		String suffix = null;
		for (final Supplier<Triple<IResult, String, String>> funResultSupplier : funsResultSupplier) {
			final Triple<IResult, String, String> triple = funResultSupplier.get();
			final IResult cex = triple.getFirst();
			final String originalFile = triple.getSecond();
			final String svcompWitness = triple.getThird();

			final boolean writeInWorkingDir = ups.getBoolean(PreferenceInitializer.LABEL_WITNESS_WRITE_WORKINGDIR);
			final boolean writeBesideInputFile = ups.getBoolean(PreferenceInitializer.LABEL_WITNESS_WRITE);
			final List<String> filenamesToDelete = new ArrayList<>();

			if (!writeBesideInputFile && !writeInWorkingDir) {
				continue;
			}
			String filename = null;
			if (writeInWorkingDir && svcompWitness != null) {
				filename = writeWitness(svcompWitness, null, suffix);
				filenamesToDelete.add(filename);
			}

			if (writeBesideInputFile && svcompWitness != null) {
				filename = writeWitness(svcompWitness, originalFile, suffix);
				filenamesToDelete.add(filename);
			}

			if (ups.getBoolean(PreferenceInitializer.LABEL_WITNESS_VERIFY)) {
				if (svcompWitness == null) {
					reportWitnessResult(null, cex, WitnessVerificationStatus.INTERNAL_ERROR,
							WitnessVerificationStatus.VERIFIED);
				} else {
					checkWitness(filename, cex, originalFile, svcompWitness);
				}
			} else if (ups.getBoolean(PreferenceInitializer.LABEL_WITNESS_LOG)) {
				reportWitnessResult(svcompWitness, cex, WitnessVerificationStatus.UNVERIFIED,
						WitnessVerificationStatus.UNVERIFIED);
			}

			if (ups.getBoolean(PreferenceInitializer.LABEL_WITNESS_DELETE_GRAPHML)) {
				for (final String fi : filenamesToDelete) {
					deleteFile(fi);
				}
			}
			cexNo++;
			suffix = String.valueOf(cexNo);
		}
	}

	private void deleteFile(String filename) {
		if (filename == null) {
			return;
		}
		File file = new File(filename);
		if (!file.delete()) {
			mLogger.warn("File " + filename + " could not be deleted");
		}
	}

	private String writeWitness(String svcompWitness, String origInputFile, String additionalSuffix) {
		final String suffix = "witness";
		final String fileending = ".graphml";
		final String separator = "-";

		StringBuilder filename = new StringBuilder();
		if (origInputFile != null) {
			filename.append(origInputFile).append(separator).append(suffix);
		} else {
			filename.append(suffix);
		}

		if (additionalSuffix != null) {
			filename.append(separator).append(additionalSuffix);
		}
		filename.append(fileending);

		try {
			File file = CoreUtil.writeFile(filename.toString(), svcompWitness);
			mLogger.info("Wrote witness to " + file.getAbsolutePath());
		} catch (IOException e) {
			mLogger.fatal("Something went wrong during writing of a witness", e);
			return null;
		}
		return filename.toString();
	}

	private void reportWitnessResult(final String svcompWitness, final IResult cex,
			final WitnessVerificationStatus verificationStatus, final WitnessVerificationStatus expectedStatus) {
		mServices.getResultService().reportResult(cex.getPlugin(),
				new WitnessResult(Activator.PLUGIN_ID, cex, svcompWitness, verificationStatus, expectedStatus));
	}

	private boolean checkWitness(String svcompWitnessFile, IResult cex, String originalFile, String svcompWitness)
			throws IOException, InterruptedException {
		mLogger.info("Verifying witness for CEX: " + cex.getShortDescription());
		final UltimatePreferenceStore ups = new UltimatePreferenceStore(Activator.PLUGIN_ID);
		final PreferenceInitializer.WitnessVerifierType type = ups.getEnum(PreferenceInitializer.LABEL_WITNESS_VERIFIER,
				PreferenceInitializer.WitnessVerifierType.class);
		final String command = ups.getString(PreferenceInitializer.LABEL_WITNESS_VERIFIER_COMMAND);

		switch (type) {
		case CPACHECKER:
			return checkWitnessWithCPAChecker(svcompWitnessFile, cex, originalFile, command, svcompWitness);
		default:
			throw new UnsupportedOperationException("Witness verifier " + type + " unknown");
		}
	}

	private boolean checkWitnessWithCPAChecker(String svcompWitnessFile, IResult cex, String originalFile,
			String command, String svcompWitness) throws IOException, InterruptedException {
		final String cpaCheckerHome = System.getenv().get("CPACHECKER_HOME");
		if (cpaCheckerHome == null) {
			mLogger.error("CPACHECKER_HOME not set, cannot use CPACHECKER as witness verifier");
			reportWitnessResult(svcompWitness, cex, WitnessVerificationStatus.INTERNAL_ERROR,
					WitnessVerificationStatus.VERIFIED);
			return false;
		}
		final UltimatePreferenceStore ups = new UltimatePreferenceStore(Activator.PLUGIN_ID);
		int timeoutInS = ups.getInt(PreferenceInitializer.LABEL_WITNESS_VERIFIER_TIMEOUT);

		final String[] cmdArray = makeCPACheckerCommand(command, svcompWitnessFile,
				ups.getString(PreferenceInitializer.LABEL_WITNESS_CPACHECKER_PROPERTY), originalFile, cpaCheckerHome,
				timeoutInS);
		timeoutInS++;
		mLogger.info(StringUtils.join(cmdArray, " "));
		MonitoredProcess cpaCheckerProcess = MonitoredProcess.exec(cmdArray, cpaCheckerHome, null, mServices, mStorage);
		final BufferedInputStream errorStream = new BufferedInputStream(cpaCheckerProcess.getErrorStream());
		final BufferedInputStream outputStream = new BufferedInputStream(cpaCheckerProcess.getInputStream());

		final String error = convertStreamToString(errorStream);
		final String output = convertStreamToString(outputStream);

		// wait for timeoutInS seconds for the witness checker, then kill it
		// forcefully
		mLogger.info("Waiting for " + timeoutInS + "s for CPA Checker...");
		MonitoredProcessState cpaCheckerState = cpaCheckerProcess.impatientWaitUntilTime(timeoutInS * 1000L);
		mLogger.info("Return code was " + cpaCheckerState.getReturnCode());

		// TODO: interpret error and output

		if (checkOutputForSuccess(output)) {
			mLogger.info("Witness for CEX was verified successfully");
			reportWitnessResult(svcompWitness, cex, WitnessVerificationStatus.VERIFIED,
					WitnessVerificationStatus.VERIFIED);
			return true;
		} else {
			final StringBuilder logMessage = new StringBuilder().append("Witness for CEX did not verify");
			if (cpaCheckerState.isKilled()) {
				logMessage.append(" due to timeout of ").append(timeoutInS).append("s");
			}
			logMessage.append("! CPAChecker said:").append(CoreUtil.getPlatformLineSeparator()).append("STDERR:")
					.append(CoreUtil.getPlatformLineSeparator()).append(error)
					.append(CoreUtil.getPlatformLineSeparator()).append("STDOUT:")
					.append(CoreUtil.getPlatformLineSeparator()).append(output);
			mLogger.error(logMessage.toString());
			reportWitnessResult(svcompWitness, cex, WitnessVerificationStatus.VERIFICATION_FAILED,
					WitnessVerificationStatus.VERIFIED);
			return false;
		}
	}

	private boolean checkOutputForSuccess(String output) {
		for (String line : output.split(CoreUtil.getPlatformLineSeparator())) {
			if (line.startsWith("Verification result: FALSE.")) {
				return true;
			}
		}
		return false;
	}

	private String[] makeCPACheckerCommand(String command, String svcompWitnessFile, String cpaCheckerProp,
			String originalFile, String workingDir, int timeoutInS) {
		final List<String> cmdArgs = new ArrayList<>();
		final String[] args = command.split(" ");
		final File file = new File(workingDir + File.separator + args[0]);
		cmdArgs.add(file.getAbsolutePath());
		for (int i = 1; i < args.length; ++i) {
			cmdArgs.add(args[i]);
		}
		cmdArgs.add("-timelimit");
		cmdArgs.add(String.valueOf(timeoutInS));
		cmdArgs.add("-spec");
		cmdArgs.add(escape(svcompWitnessFile));
		cmdArgs.add("-spec");
		cmdArgs.add(escape(cpaCheckerProp));
		cmdArgs.add(escape(originalFile));
		return cmdArgs.toArray(new String[cmdArgs.size()]);
	}

	private String escape(String str) {
		return str;
	}

	private static String convertStreamToString(InputStream input) {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
		final StringBuilder out = new StringBuilder();
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				out.append(line).append(CoreUtil.getPlatformLineSeparator());
			}
			reader.close();
		} catch (IOException e) {
			// suppress all exceptions
		}
		return out.toString();
	}
}