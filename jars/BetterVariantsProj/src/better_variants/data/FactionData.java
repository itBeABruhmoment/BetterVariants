package better_variants.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
/*
data.BetterVariants_FactionData.FactionConfig da = (data.BetterVariants_FactionData.FactionConfig) data.BetterVariants_FactionData.FACTION_DATA.get("hegemony");
String s = da.toString();
Console.showMessage(s);
*/
public class FactionData {
    private static final Logger log = Global.getLogger(better_variants.data.FactionData.class);
    static {
        log.setLevel(Level.ALL);
    }
    
    public static final HashMap<String, FactionConfig> FACTION_DATA = new HashMap<String, FactionConfig>();
    private static final String CSV_FIRST_COLUMN_NAME = "factionID";
    private static final String CSV_SECOND_COLUMN_NAME = "fleets";
    private static final String CSV_THIRD_COLUMN_NAME = "specialFleetSpawnRate";
    private static final String CSV_FOURTH_COLUMN_NAME = "tags";

    private static boolean hasDuplicate(String original, Vector<String> strings)
    {
        int duplicateCount = 0;
        for(String str : strings) {
            if(original.equals(str)) {
                duplicateCount++;
            }

            if(duplicateCount == 2) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasDuplicateTags(Vector<String> strings)
    {
        for(String str : strings) {
            if(hasDuplicate(str, strings)) {
                return true;
            }
        }
        return false;
    }

    private static Vector<String> processTags(String tagsRaw)
    {
        String[] tagsMediumRare = tagsRaw.split(",");
        Vector<String> tagsDone = new Vector<String>();
        for(String tag : tagsMediumRare) {
            String trimmed = tag.trim();
            if(!trimmed.equals("")) {
                tagsDone.add(trimmed);
            }
        }
        tagsDone.trimToSize();
        return tagsDone;
    }

    public static void loadData() throws IOException, JSONException, Exception
    {
        final JSONArray data = Global.getSettings().loadCSV(CommonStrings.FACTION_TAGS_CSV_PATH,
            CommonStrings.MOD_ID);
        
        for(int i = 0; i < data.length(); i++) {
            final JSONObject row = data.getJSONObject(i);

            // read faction id, ignore rows without this field
            String factionId = row.optString(CSV_FIRST_COLUMN_NAME);
            if(factionId.equals("")) {
                continue;
            }

            // read custom fleets the faction can spawn
            String fleetRaw = row.optString(CSV_SECOND_COLUMN_NAME);
            Vector<String> fleetIds = processTags(fleetRaw);

            // read spawnrate of special fleets
            String specialFleetSpawnRateRaw = row.optString(CSV_THIRD_COLUMN_NAME);
            double specialFleetSpawnRate = 0;
            try {
                specialFleetSpawnRate = Double.parseDouble(specialFleetSpawnRateRaw);
            } catch(NumberFormatException e) {
                throw new Exception(CommonStrings.MOD_ID + ": the faction " + factionId + " has invalid \"specialFleetSpawnRateRaw\" field");
            }
            if(specialFleetSpawnRate < 0 || specialFleetSpawnRate > 1) {
                throw new Exception(CommonStrings.MOD_ID + ": the faction " + factionId + " has invalid number in \"specialFleetSpawnRateRaw\" field");
            }

            // read tags
            String tagsRaw = row.optString(CSV_FOURTH_COLUMN_NAME);
            Vector<String> tags = processTags(tagsRaw);
            HashSet<String> tagsHash = new HashSet<String>();
            for(String tag : tags) {
                tagsHash.add(tag);
            }

            if(hasDuplicateTags(tags)) {
                throw new Exception(CommonStrings.MOD_ID + ": the faction " + factionId + " has duplicate tags. Remove them");
            }

            FACTION_DATA.put(factionId, new FactionConfig(tagsHash, fleetIds, specialFleetSpawnRate));
        }
        
    }

    public static class FactionConfig
    {
        public final HashSet<String> tags;
        public final Vector<String> customFleetIds;
        public final double specialFleetSpawnRate;

        public boolean hasTag(String tag)
        {
            return tags.contains(tag);
        }

        FactionConfig(HashSet<String> Tags, Vector<String> CustomFleetIds, double SpecialFleetSpawnRate)
        {
            tags = Tags;
            customFleetIds = CustomFleetIds;
            specialFleetSpawnRate = SpecialFleetSpawnRate;
        }

        @Override
        public String toString() {
            String str = "";
            for(String tag : tags) {
                str += tag + " ";
            }
            str += "\n";

            for(String id : customFleetIds) {
                str += id + " ";
            }
            str += "\n";

            str += specialFleetSpawnRate + "\n";

            return str;
        }
    }

    private FactionData() {} // do nothing
}