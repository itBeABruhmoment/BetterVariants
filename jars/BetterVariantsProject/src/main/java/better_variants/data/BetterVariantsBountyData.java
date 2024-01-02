package better_variants.data;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;
import variants_lib.data.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class BetterVariantsBountyData {
    private static final Logger log = Global.getLogger(BetterVariantsBountyData.class);
    static {
        log.setLevel(Level.ALL);
    }
    private HashMap<String, BetterVariantsBountyDataMember> bounties = new HashMap<>();
    private HashSet<String> factionsWithBounties = new HashSet<>();
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
            final int minDifficulty = JsonUtils.getInt(CommonStrings.MIN_DIFFICULTY_COLUMN_NAME, 1, row);

            final String faction = JsonUtils.getString(CommonStrings.FACTION_COLUMN_NAME, "", row);
            if(fleetId.equals("")) {
                throw new Exception(String.format("the %s of row %d of %s is blank or could not be read",
                        CommonStrings.FACTION_COLUMN_NAME, i, CommonStrings.BOUNTY_FLEETS_PATH));
            }

            bounties.put(fleetId, new BetterVariantsBountyDataMember(fleetId, weight, minFP, maxFP, faction, minDifficulty));
        }

        for(final BetterVariantsBountyDataMember bounty : bounties.values()) {
            factionsWithBounties.add(bounty.getFaction());
        }

        log.info(String.format("%s: finished loading %s", CommonStrings.MOD_ID, CommonStrings.BOUNTY_FLEETS_PATH));
        log.info(String.format("%s: \n %s", CommonStrings.MOD_ID, toString()));
    }

    public HashMap<String, BetterVariantsBountyDataMember> getBounties() {
        return bounties;
    }
    public boolean bountiesExistForFaction(String faction) {
        return factionsWithBounties.contains(faction);
    }


    @Nullable
    public BetterVariantsBountyDataMember pickBounty(final ArrayList<String> factions, final int difficulty, final long seed) {
        log.info(String.format("%s: %s", CommonStrings.MOD_ID, factions));
        // get valid bounties, sum weights
        float totalWeightSum = 0.0f;
        final ArrayList<BetterVariantsBountyDataMember> validBounties = new ArrayList<>(32);
        for(final BetterVariantsBountyDataMember bounty : bounties.values()) {
            log.info(String.format("%s: %s", CommonStrings.MOD_ID, bounty));
            log.info(String.format("%s: %s", CommonStrings.MOD_ID, factions.contains(bounty.getFaction())));
            if(factions.contains(bounty.getFaction()) && bounty.getMinDifficulty() <= difficulty) {
                validBounties.add(bounty);
                totalWeightSum += bounty.getWeight();
            }
        }
        log.info(String.format("%s: %s", CommonStrings.MOD_ID, validBounties));

        // choose one
        float runningWeightSum = 0.0f;
        final float random = new Random(seed).nextFloat();
        for(final BetterVariantsBountyDataMember bounty : validBounties) {
            runningWeightSum += bounty.getWeight();
            if(runningWeightSum / totalWeightSum < random) {
                return bounty;
            }
        }

        if(validBounties.isEmpty()) {
            log.info(String.format("%s: no bounties found", CommonStrings.MOD_ID));
            return null;
        } else {
            log.info(String.format("%s: to last", CommonStrings.MOD_ID));
            return validBounties.get(validBounties.size() - 1);
        }
    }

    public boolean contains(final ArrayList<String> arr, final String find) {
        for(final String s : arr) {
            if(s.equals(find)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "BetterVariantsBountyData{" +
                "bounties=" + bounties +
                '}';
    }
}
