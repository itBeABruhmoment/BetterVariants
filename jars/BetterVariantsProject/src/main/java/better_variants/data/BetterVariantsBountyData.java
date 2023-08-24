package better_variants.data;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import variants_lib.data.JsonUtils;
import variants_lib.data.Util;

import java.io.IOException;
import java.util.HashMap;

public class BetterVariantsBountyData {
    private static final Logger log = Global.getLogger(BetterVariantsBountyData.class);
    static {
        log.setLevel(Level.ALL);
    }
    private HashMap<String, BetterVariantsBounty> bounties = new HashMap<>();
    private static BetterVariantsBountyData instance = new BetterVariantsBountyData();
    private BetterVariantsBountyData() {}

    public static BetterVariantsBountyData getInstance() {
        return instance;
    }

    /**
     * Initializes bounties with data from bounty_fleets.csv
     * @throws Exception
     */
    public void loadData() throws Exception {
        log.info("loading " + CommonStrings.BOUNTY_FLEETS_PATH);
        final JSONArray csv = Global.getSettings().loadCSV(CommonStrings.BOUNTY_FLEETS_PATH);
        for(int i = 0; i < csv.length(); i++) {
            final JSONObject row = csv.getJSONObject(i);

            final String fleetId = JsonUtils.getString(CommonStrings.FLEET_ID_COLUMN_NAME, "", row);
            if(fleetId.equals("")) {
                throw new Exception(String.format("the %s of row %d of %s is blank or could not be read",
                        CommonStrings.FLEET_ID_COLUMN_NAME, i, CommonStrings.BOUNTY_FLEETS_PATH));
            }

            final float weight = JsonUtils.getFloat(row, CommonStrings.WEIGHT_COLUMN_NAME, 10.0f);
            final int minFP = JsonUtils.getInt(CommonStrings.MIN_FP_COLUMN_NAME, 10, row);
            final int maxFP = JsonUtils.getInt(CommonStrings.MAX_FP_COLUMN_NAME, 100, row);
            bounties.put(fleetId, new BetterVariantsBounty(fleetId, weight, minFP, maxFP));
        }
        log.info("finished loading " + CommonStrings.BOUNTY_FLEETS_PATH);
    }

    public HashMap<String, BetterVariantsBounty> getBounties() {
        return bounties;
    }

    public static class BetterVariantsBounty {
        private String fleetId = "";
        private float weight = 1.0f;
        private int minFleetPoints = 10;
        private int maxFleetPoints = 100;

        public BetterVariantsBounty(String fleetId, float weight, int minFleetPoints, int maxFleetPoints) {
            this.fleetId = fleetId;
            this.weight = weight;
            this.minFleetPoints = minFleetPoints;
            this.maxFleetPoints = maxFleetPoints;
        }

        public String getFleetId() {
            return fleetId;
        }

        public float getWeight() {
            return weight;
        }

        public int getMinFleetPoints() {
            return minFleetPoints;
        }

        public int getMaxFleetPoints() {
            return maxFleetPoints;
        }
    }
}
