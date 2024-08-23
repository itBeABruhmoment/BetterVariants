package better_variants.scripts;

import better_variants.data.CommonStrings;
import better_variants.data.SettingsData;
import com.fs.starfarer.api.Global;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import variants_lib.scripts.VariantsLibPostApplicationLoadScript;

// See the VariantsLibModPlugin to see where this code is actually ran from
public class ApplySettings implements VariantsLibPostApplicationLoadScript {
    private static final Logger log = Global.getLogger(better_variants.scripts.ApplySettings.class);
    static {
        log.setLevel(Level.ALL);
    }

    @Override
    public void runPostApplicationLoadScript() {
        log.info(CommonStrings.MOD_ID + ": loading settings");
        SettingsData.getInstance().loadSettings();
        log.info(CommonStrings.MOD_ID + ": settings loaded");
        log.info(SettingsData.getInstance().toString());
        log.info(CommonStrings.MOD_ID + ": applying settings");
        SettingsData.getInstance().applySettings();
        log.info(CommonStrings.MOD_ID + ": settings applied");
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
