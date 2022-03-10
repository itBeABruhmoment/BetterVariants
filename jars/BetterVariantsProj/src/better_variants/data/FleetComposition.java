package better_variants.data;

import java.util.HashMap;
import java.util.HashSet;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lazywizard.console.Console;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;

public class FleetComposition {
    private static final Logger log = Global.getLogger(better_variants.data.FleetComposition.class);
    static {
        log.setLevel(Level.ALL);
    }

    public static final HashSet<String> DEFAULT_TARGET_FLEET_TYPES = new HashSet<String>() {{
        add(FleetTypes.MERC_ARMADA);    add(FleetTypes.MERC_BOUNTY_HUNTER); add(FleetTypes.MERC_PATROL);
        add(FleetTypes.MERC_PRIVATEER); add(FleetTypes.MERC_SCOUT);         add(FleetTypes.PATROL_LARGE);
        add(FleetTypes.PATROL_MEDIUM);  add(FleetTypes.PATROL_SMALL);       add(FleetTypes.TASK_FORCE);
    }};

    public HashSet<String> targetFleetTypes;
    public AlwaysBuildMember[] alwaysInclude;
    public FleetPartition[] partitions;
    public String id;
    public double spawnWeight;
    public int minDP;
    public int maxDP;
    public String defaultFleetWidePersonality;
    
    // runcode Console.showMessage(data.BetterVariants_FleetBuildData.FleetData.get("bv_tritachyon_doomhyperion").toString());
    @Override
    public String toString() 
    {
        String str = "";
        for(int i = 0; i < partitions.length; i++) {
            str += "part " + i + " weight: " + partitions[i].partitionWeight + " variance: " +
            partitions[i].partitionVariance + " maxDP: " + maxDP + " minDP: " + minDP + "\n";

            for(int j = 0; j < partitions[i].members.length; j++) {
                str += partitions[i].members[j].id + " " + partitions[i].members[j].weight + "\n";
            }
        }
        
        if(alwaysInclude == null) {
            return str;
        }
        for(AlwaysBuildMember mem : alwaysInclude) {
            str += "id: " + mem.id + " amount: " + mem.amount + "\n";
        }
        return str;
    }

    // constuct from json
    public FleetComposition(JSONObject fleetDataJson, String dataId, String loadedFileInfo) throws Exception, IOException
    {
        id = dataId;
        
        // read spawn weight
        try {
            spawnWeight = fleetDataJson.getDouble("spawnWeight");
        } catch(Exception e) {
            throw new Exception(loadedFileInfo + " could not have its \"spawnWeight\" field read. Check spelling/formatting");
        }
        if(spawnWeight < 0) {
            throw new Exception(loadedFileInfo + " has \"spawnWeight\" field less than zero");
        }

        // read "maxDP" and "minDp" fields
        try {
            minDP = fleetDataJson.getInt("minDP");
        } catch(Exception e) {
            throw new Exception(loadedFileInfo + " could not have its \"minDP\" field read. Check formatting. Field should be integer");
        }
        if(minDP < 0) {
            throw new Exception(loadedFileInfo + " has negative \"minDP\" field");
        }

        try {
            maxDP = fleetDataJson.getInt("maxDP");
        } catch(Exception e) {
            throw new Exception(loadedFileInfo + " could not have its \"maxDP\" field read. Check formatting. Field should be integer");
        }
        if(minDP < 0) {
            throw new Exception(loadedFileInfo + " has negative \"maxDP\" field");
        }
        if(minDP > maxDP) {
            throw new Exception(loadedFileInfo + " has \"maxDP\" field less than \"minDP\" field");
        }

        // read defaultFleetWidePersonality field
        try {
            defaultFleetWidePersonality = fleetDataJson.getString("defaultFleetWidePersonality");
        } catch(Exception e) {
            defaultFleetWidePersonality = Personalities.STEADY;
        }
        if(defaultFleetWidePersonality != null && !CommonStrings.PERSONALITIES.contains(defaultFleetWidePersonality)) {
            throw new Exception(loadedFileInfo + " has invalid personality in \"defaultFleetWidePersonality\" field");
        }

        // read "targetFleetTypes" field
        JSONArray targetFleetTypesJson = null;
        try {
            targetFleetTypesJson  = fleetDataJson.getJSONArray("targetFleetTypes");
        } catch(Exception e) {
            log.debug(loadedFileInfo + " has no \"targetFleetTypes\" field, setting to some default value");
            targetFleetTypesJson = null;
        }

        if(targetFleetTypesJson != null) {
            targetFleetTypes = new HashSet<String>();
            for(int i = 0; i < targetFleetTypesJson.length(); i++) {
                try {
                    targetFleetTypes.add(targetFleetTypesJson.getString(i));
                } catch(Exception e) {
                    throw new Exception(loadedFileInfo + " could not have element in \"targetFleetTypes\" field read");
                }
            }
        } else {
            targetFleetTypes = DEFAULT_TARGET_FLEET_TYPES;
        }

        // read partitions data field
        final JSONArray fleetPartitionsData;
        try {
            fleetPartitionsData = fleetDataJson.getJSONArray("fleetPartitions");
        } catch(Exception e) {
            throw new Exception(loadedFileInfo + " could not have its \"fleetPartitions\" field read. Check spelling/formatting");
        }
        if(fleetPartitionsData.length() == 0) {
            throw new Exception(loadedFileInfo + " has empty \"fleetPartitions\" field");
        }

        // read the individual partitions
        double partitionWeightSum = 0;
        partitions = new FleetPartition[fleetPartitionsData.length()];
        for(int i = 0; i < fleetPartitionsData.length(); i++) {
            final JSONObject partitionData = fleetPartitionsData.getJSONObject(i);
            partitions[i] = new FleetPartition(partitionData, loadedFileInfo, i);
            partitionWeightSum += partitions[i].partitionWeight;
        }

        // make partiton weights a percentage (number between 0 and 1)
        for(FleetPartition part : partitions) {
            part.makePartitionWeightPercentage(partitionWeightSum);
            part.makePartitionVariancePercentage(partitionWeightSum);
        }

        // load always include variants
        JSONArray alwaysIncludeData = null;
        try {
            alwaysIncludeData = fleetDataJson.getJSONArray("alwaysSpawn");
        } catch(Exception e) {
            alwaysIncludeData = null;
        }

        if(alwaysIncludeData == null) {
            alwaysIncludeData = null;
        } else {
            alwaysInclude = new AlwaysBuildMember[alwaysIncludeData.length()];
            for(int i = 0; i < alwaysIncludeData.length(); i++) {
                JSONObject alwaysIncludeMemberData = alwaysIncludeData.getJSONObject(i);

                String variantId = null;
                try {
                    variantId = alwaysIncludeMemberData.optString("id");
                } catch(Exception e) {
                    throw new Exception(loadedFileInfo + " always include " + i + " failed to read \"id\"");
                }
                if(!Global.getSettings().doesVariantExist(variantId)) {
                    throw new Exception(loadedFileInfo + " always include " + i + " \""+  id + "\" is not a recognized variant");
                }

                int amount = -1;
                try {
                    amount = alwaysIncludeMemberData.getInt("amount");
                } catch(Exception e) {
                    throw new Exception(loadedFileInfo + " always include " + i + " failed to read \"amount\"");
                }
                if(amount < 1) {
                    throw new Exception(loadedFileInfo + " always include " + i + " \"amount\" is invalid int");
                }

                alwaysInclude[i] = new AlwaysBuildMember(variantId, amount);
            }
        }
    }
}
