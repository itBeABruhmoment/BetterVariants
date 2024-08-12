package better_variants.data;

import com.fs.starfarer.api.impl.campaign.ids.Factions;
import lunalib.lunaSettings.LunaSettings;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;
import variants_lib.data.FactionData;
import variants_lib.data.FleetBuildData;

public class SettingsData {
    private static final Logger log = Global.getLogger(better_variants.data.SettingsData.class);
    static {
        log.setLevel(Level.ALL);
    }

    private static SettingsData instance = new SettingsData();

    private boolean harderRemnants = false;

    public static SettingsData getInstance() {
        return instance;
    }

    public void loadSettings()
    {
        JSONObject settingsJson = null;
        try {
            settingsJson = Global.getSettings().loadJSON(CommonStrings.SETTINGS_JSON_PATH, CommonStrings.MOD_ID);
        } catch(Exception e) {
            settingsJson = null;
        }

        // first load from JSON
        if(settingsJson == null) {
            log.debug(CommonStrings.MOD_ID + ": settings could not be opened setting them to default values");
            this.harderRemnants = false;
        } else {
            this.harderRemnants = getBool(settingsJson, CommonStrings.HARD_REMNANTS_SETTING, false);
        }

        // then override with settings from Luna Lib if enabled
        if (Global.getSettings().getModManager().isModEnabled("lunalib")) {
            Boolean harderRemnantsTemp = LunaSettings.getBoolean(CommonStrings.MOD_ID, CommonStrings.LUNA_HARD_REMNANTS_SETTING);
            if(harderRemnantsTemp == null) {
                log.info(CommonStrings.MOD_ID + ": \"" + CommonStrings.LUNA_HARD_REMNANTS_SETTING + "\" not found using default");
                this.harderRemnants = false;
            } else {
                this.harderRemnants = harderRemnantsTemp;
            }
        }
    }

    public void applySettings() {
        if(instance.harderRemnants) {
            log.info(CommonStrings.MOD_ID + "harder remnants enabled");
            FactionData.FactionConfig remnantsConfig = FactionData.FACTION_DATA.get(Factions.REMNANTS);
            if(remnantsConfig != null) {
                remnantsConfig.tags.add(variants_lib.data.CommonStrings.NO_AUTOFIT_TAG);
                if(FleetBuildData.FLEET_DATA.containsKey("bv_remnant_rangedBounty1")) {
                    remnantsConfig.customFleetIds.add("bv_remnant_rangedBounty1");
                } else {
                    log.error(CommonStrings.MOD_ID + "bv_remnant_rangedBounty1 fleet not found");
                }
                if(FleetBuildData.FLEET_DATA.containsKey("bv_remnant_strikeBounty")) {
                    remnantsConfig.customFleetIds.add("bv_remnant_strikeBounty");
                } else {
                    log.error(CommonStrings.MOD_ID + "bv_remnant_strikeBounty fleet not found");
                }
            } else {
                log.error(CommonStrings.MOD_ID + "remnants faction config not found");
            }
        }
    }


    private static boolean getBool(JSONObject json, String key, boolean defaultVal)
    {
        boolean returnVal = defaultVal;
        try {
            returnVal = json.getBoolean(key);
        } catch(Exception e) {
            returnVal = defaultVal;
            log.debug(CommonStrings.MOD_ID + ": " + key + " field could not be read defaulting to " + defaultVal);
        }
        return returnVal;
    }

    public boolean isHarderRemnants() {
        return harderRemnants;
    }

    private SettingsData(){}
}
