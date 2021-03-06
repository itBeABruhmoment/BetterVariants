package better_variants.data;

import java.util.HashMap;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;

// loads data on fleet types
public class FleetBuildData {
    private static final Logger log = Global.getLogger(better_variants.data.FleetBuildData.class);
    static {
        log.setLevel(Level.ALL);
    }

    public static final HashMap<String, FleetComposition> FLEET_DATA = new HashMap<String, FleetComposition>();
    private static final String CSV_FIRST_COLUMN_NAME = "fileName";
    private static final String CSV_SECOND_COLUMN_NAME = "modId";

    /*public static void testJson() throws JSONException, IOException
    {
        org.json.JSONObject data = Global.getSettings().loadJSON("data/bettervariants/fleets/why.json", "better_variants");
        org.json.JSONObject data = Global.getSettings().loadJSON("mod_info.json", "better_variants");
        Global.getSettings().loadJSON("data/config/settings.json");
        Console.showMessage(data);
        org.json.JSONArray partitions = data.getJSONArray("fleetPartitions");
        org.json.JSONObject partition1 = partitions.getJSONObject(0);
        org.json.JSONArray variants = partition1.getJSONArray("variants");
        Console.showMessage(variants.getJSONObject(0).getInt("weight"));
    }*/

    public static void loadFleetJson(String fileName, String modId) throws Exception, IOException
    {
        // for error messages
        String loadedFileInfo = CommonStrings.MOD_ID + ":the file \"" + fileName + "\" from the mod \"" + modId + "\"";

        // load the json
        log.debug("trying to read " + fileName + " from " + modId);
        JSONObject fleetDataJson = null;
        try {
            fleetDataJson = Global.getSettings().loadJSON(fileName, modId);
        } catch(Exception e) {
            throw new Exception(loadedFileInfo + " could not be opened. Ensure everything is formated correctly (check your spelling, file structure, etc)");
        }
        
        String fleetDataId = fleetDataJson.optString("fleetDataId");
        if(fleetDataId.equals("")) {
            throw new Exception(loadedFileInfo + " has no \"fleetDataId\" field, check spelling and formatting");
        }

        FleetComposition comp = new FleetComposition(fleetDataJson, fleetDataId, loadedFileInfo);
        FLEET_DATA.put(fleetDataId, comp);
    }

    public static void loadData() throws Exception, IOException
    {
        // load "fleets.csv"
        final JSONArray fleetDataRegister = Global.getSettings().loadCSV(CommonStrings.FLEETS_CSV_PATH,
        CommonStrings.MOD_ID);
        
        for(int i = 0; i < fleetDataRegister.length(); i++) {
            // get info for loading the fleet data json
            final JSONObject row = fleetDataRegister.getJSONObject(i);
            String fileName = row.optString(CSV_FIRST_COLUMN_NAME);
            if(fileName.equals("")) {
                continue;
            }
            fileName = CommonStrings.FLEETS_FOLDER_PATH + fileName;
            String modId = row.optString(CSV_SECOND_COLUMN_NAME);

            loadFleetJson(fileName, modId);
        }
    }
    
    private FleetBuildData() {} // do nothing
}
