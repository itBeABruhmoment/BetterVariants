package better_variants.data;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;

public class SettingsData {
    private static final Logger log = Global.getLogger(better_variants.data.SettingsData.class);
    static {
        log.setLevel(Level.ALL);
    }

    public static boolean enableBuffedTradeFleetEscorts;

    public static void loadSettings()
    {
        JSONObject settingsJson = null;
        try {
            settingsJson = Global.getSettings().loadJSON(CommonStrings.SETTINGS_JSON_PATH, CommonStrings.MOD_ID);
        } catch(Exception e) {
            settingsJson = null;
        }

        if(settingsJson == null) {
            log.debug(CommonStrings.MOD_ID + ": settings could not be opened setting them to default values");
            enableBuffedTradeFleetEscorts = true;
        } else {
            enableBuffedTradeFleetEscorts = getBool(settingsJson, "enableBuffedTradeFleetEscorts", true);
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

    private SettingsData(){}
}
