package de.uni_freiburg.informatik.ultimate.plugins.generator.treeautomizer.script;

import de.uni_freiburg.informatik.ultimate.core.model.preferences.IPreferenceProvider;
import de.uni_freiburg.informatik.ultimate.core.model.services.IToolchainStorage;
import de.uni_freiburg.informatik.ultimate.core.model.services.IUltimateServiceProvider;
import de.uni_freiburg.informatik.ultimate.logic.Script;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SolverBuilder;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SolverBuilder.Settings;
import de.uni_freiburg.informatik.ultimate.modelcheckerutils.smt.SolverBuilder.SolverMode;
import de.uni_freiburg.informatik.ultimate.plugins.generator.treeautomizer.Activator;
import de.uni_freiburg.informatik.ultimate.plugins.generator.treeautomizer.preferences.TreeAutomizerPreferenceInitializer;

public class HCGBuilderHelper {

	public static class ConstructAndInitializeBackendSmtSolver {

		private Settings mSolverSettings;
		private String mLogicForExternalSolver;
		private Script mScript;

		public ConstructAndInitializeBackendSmtSolver(IUltimateServiceProvider services,
				IToolchainStorage storage,
				String filename) {
			constructAndInitializeBackendSmtSolver(services, storage, filename);
		}

		void constructAndInitializeBackendSmtSolver(
				IUltimateServiceProvider services,
				IToolchainStorage storage,
				String filename) {
			final IPreferenceProvider prefs = services.getPreferenceProvider(Activator.PLUGIN_ID);
			final SolverMode solverMode = prefs
					.getEnum(TreeAutomizerPreferenceInitializer.LABEL_Solver, SolverMode.class);

			final String commandExternalSolver = prefs
					.getString(TreeAutomizerPreferenceInitializer.LABEL_ExtSolverCommand);

			mLogicForExternalSolver = prefs
					.getString(TreeAutomizerPreferenceInitializer.LABEL_ExtSolverLogic);

			mSolverSettings = SolverBuilder.constructSolverSettings(
					filename, solverMode, commandExternalSolver, false, null);

			mScript = SolverBuilder.buildAndInitializeSolver(services,
					storage,
					solverMode,
					mSolverSettings,
					// dumpUsatCoreTrackBenchmark,
					false,
					// dumpMainTrackBenchmark,
					false,
					mLogicForExternalSolver,
					"HornClauseSolverBackendSolverScript");
		}

		public Settings getSolverSettings() {
			return mSolverSettings;
		}

		public String getLogicForExternalSolver() {
			return mLogicForExternalSolver;
		}

		public Script getScript() {
			return mScript;
		}
	}
}
