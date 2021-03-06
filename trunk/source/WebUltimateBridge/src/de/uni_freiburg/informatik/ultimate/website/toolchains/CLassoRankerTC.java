package de.uni_freiburg.informatik.ultimate.website.toolchains;

import java.util.ArrayList;
import java.util.List;

import de.uni_freiburg.informatik.ultimate.website.Setting;
import de.uni_freiburg.informatik.ultimate.website.Tasks.TaskNames;
import de.uni_freiburg.informatik.ultimate.website.Tool;
import de.uni_freiburg.informatik.ultimate.website.WebToolchain;

public class CLassoRankerTC extends WebToolchain {

	@Override
	protected String defineDescription() {
		return NameStrings.s_TOOL_LassoRanker;
	}

	@Override
	protected String defineName() {
		return NameStrings.s_TOOL_LassoRanker;
	}

	@Override
	protected String defineId() {
		return "cLassoRanker";
	}

	@Override
	protected TaskNames[] defineTaskName() {
		return new TaskNames[] { TaskNames.RANK_SYNTHESIS_C };
	}

    @Override
    protected String defineLanguage() {
        return "c";
    }
    

	@Override
	protected String defineUserInfo() {
		return null;
	}
	
	@Override
	protected String defineInterfaceLayoutFontsize() {
		return PrefStrings.s_InterfaceLayoutFontsizeDefault;
	}

	@Override
	protected String defineInterfaceLayoutOrientation() {
		return PrefStrings.s_InterfaceLayoutOrientationDefault;
	}

	@Override
	protected String defineInterfaceLayoutTransitions() {
		return PrefStrings.s_InterfaceLayoutTransitionDefault;
	}

	@Override
	protected List<Tool> defineTools() {
		final List<Tool> tools = new ArrayList<Tool>();
		
		tools.add(new Tool(PrefStrings.s_syntaxchecker));
		tools.add(new Tool(PrefStrings.s_cacsl2boogietranslator));
		tools.addAll(BoogieLassoRankerTC.boogieLassoRankerToolchain());

		return tools;
	}
	
	@Override
	protected List<Setting> defineAdditionalSettings() {
		final List<Setting> rtr = BoogieLassoRankerTC.boogieLassoRankerAdditionalSettings();
//		rtr.add(new Setting(PrefStrings.s_CACSL_LABEL_StartFunction, SettingType.STRING, "Starting procedure: ",
//				"main", true));
		return rtr;
	}
	
	@Override
	protected String defineToolchainSettingsFile() {
		return "LassoRanker.epf";
	}

}
