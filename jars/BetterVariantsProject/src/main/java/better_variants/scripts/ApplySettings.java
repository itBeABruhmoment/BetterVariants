package better_variants.scripts;

import better_variants.data.CommonStrings;
import better_variants.data.SettingsData;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import variants_lib.data.FactionData;
import variants_lib.scripts.VariantsLibPostApplicationLoadScript;

public class ApplySettings implements VariantsLibPostApplicationLoadScript {
    private static final Logger log = Global.getLogger(better_variants.scripts.ApplySettings.class);
    static {
        log.setLevel(Level.ALL);
    }

    @Override
    public void runPostApplicationLoadScript() {
        log.info(CommonStrings.MOD_ID + ": loading settings");
        SettingsData.getInstance().loadSettings();
        SettingsData.getInstance().applySettings();
    }

    @Override
    public String getOriginMod() {
        return CommonStrings.MOD_ID;
    }

    @Override
    public boolean reloadWhenLunaSettingsForOriginModChanged() {
        return true;
    }
}
