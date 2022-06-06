package better_variants.data;

import java.util.HashMap;
import java.io.IOException;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

// loads data of "data/bettervariants/variant_tags.csv"
public class VariantData {
    private static final Logger log = Global.getLogger(better_variants.data.VariantData.class);
    static {
        log.setLevel(Level.ALL);
    }

    public static final HashMap<String, Vector<String>> VARIANT_DATA = new HashMap<String, Vector<String>>();
    private static final String CSV_FIRST_COLUMN_NAME = "variantID";
    private static final String CSV_SECOND_COLUMN_NAME = "tags";

    // if the variant is registered return the variantId, if not return null
    public static String isRegisteredVariant(FleetMemberAPI ship)
    {
        if(ship.isFighterWing()) {
            return null;
        }

        if(VARIANT_DATA.containsKey(ship.getVariant().getHullVariantId())) {
            return ship.getVariant().getHullVariantId();
        }

        if(VARIANT_DATA.containsKey(ship.getVariant().getOriginalVariant())) {
            return ship.getVariant().getOriginalVariant();
        }
        
        return null;
    }

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
        final JSONArray data = Global.getSettings().loadCSV(CommonStrings.VARIANT_TAGS_CSV_PATH,
            CommonStrings.MOD_ID);
        
        for(int i = 0; i < data.length(); i++) {
            final JSONObject row = data.getJSONObject(i);
            String variantId = row.optString(CSV_FIRST_COLUMN_NAME);

            if(variantId.equals("")) {
                continue;
            }

            String tagsRaw = row.optString(CSV_SECOND_COLUMN_NAME);
            Vector<String> tags = processTags(tagsRaw);

            if(hasDuplicateTags(tags)) {
                throw new Exception(CommonStrings.MOD_ID + ": the variant " + variantId + " has duplicate tags. Remove them");
            }

            VARIANT_DATA.put(variantId, tags);
        }
        
    }

    private VariantData() {}
}
