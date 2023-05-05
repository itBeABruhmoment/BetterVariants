package better_variants.bar_events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.magiclib.util.MagicTxt;
import org.magiclib.bounty.MagicBountyData.bountyData;
import better_variants.data.CommonStrings;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent.SkillPickPreference;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FleetAssignment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// looks like I can't trust magic bounty data to not delete bounty data. Static class for storing data for bar event
public class BountyCreationData {
    // Console.showMessage(better_variants.bar_events.BountyCreationData.getTraitorBountyData().toString());
    private static final Logger log = Global.getLogger(better_variants.bar_events.BountyCreationData.class);
    static {
        log.setLevel(Level.ALL);
    }

    private static final String BETTER_VARIANTS_BOUNTY_PATH = "data/bettervariants/betterVariantsMagicBounty_data.json";
    private static bountyData ENEMY_BOUNTY = null;
    private static bountyData TRAITOR_BOUNTY = null;
    private static JSONObject BOUNTY_DATA;

    public static bountyData getEnemyBountyData()
    {
        return ENEMY_BOUNTY;
    }

    public static bountyData getTraitorBountyData()
    {
        return TRAITOR_BOUNTY;
    }

    public static void loadData() throws Exception{
        
        //load MagicBounty_data.json
        try {
            BOUNTY_DATA = Global.getSettings().loadJSON(BETTER_VARIANTS_BOUNTY_PATH, CommonStrings.MOD_ID);
        } catch(Exception e) {
            throw new Exception(CommonStrings.MOD_ID + ": failed to load " + BETTER_VARIANTS_BOUNTY_PATH);
        }
        
        try {
            TRAITOR_BOUNTY = loadBountyWithKey("better_variants_traitor");
        } catch(Exception e) {
            throw new Exception(CommonStrings.MOD_ID + ": error loading better_variants_traitor bounty. Error message \"" + e.getMessage() + "\"");
        }

        try {
            ENEMY_BOUNTY = loadBountyWithKey("better_variants_enemy");
        } catch(Exception e) {
            throw new Exception(CommonStrings.MOD_ID + ": error loading better_variants_enemy bounty. Error message \"" + e.getMessage() + "\"");
        }

        if(ENEMY_BOUNTY == null) {
            throw new Exception(CommonStrings.MOD_ID + ": error loading better_variants_enemy bounty");
        }
        if(TRAITOR_BOUNTY == null) {
            throw new Exception(CommonStrings.MOD_ID + ": error loading better_variants_traitor");
        }
        
        ENEMY_BOUNTY.job_reputation_reward = 0.03f;
        TRAITOR_BOUNTY.job_reputation_reward = 0.03f;

        BOUNTY_DATA = null;
    }

    private static bountyData loadBountyWithKey(String bountyId)
    {
        String genderString = getString(bountyId, "target_gender");
        FullName.Gender gender = null;
        if(genderString!=null){
            switch (genderString) {
                case "MALE":
                    gender = FullName.Gender.MALE;
                    break;
                case "FEMALE":
                    gender = FullName.Gender.FEMALE;
                    break;
                case "UNDEFINED":
                    gender = FullName.Gender.ANY;
                    break;
                default:
                    break;
            }
        }
        String fleet_behavior = getString(bountyId, "fleet_behavior");
        FleetAssignment order = FleetAssignment.ORBIT_AGGRESSIVE;
        if(fleet_behavior!=null){
            switch (fleet_behavior){
                case "PASSIVE": {
                    order=FleetAssignment.ORBIT_PASSIVE;
                    break;
                }
                case "AGGRESSIVE":{
                    order=FleetAssignment.DEFEND_LOCATION;
                    break;
                }
                case "ROAMING":{
                    order=FleetAssignment.PATROL_SYSTEM;
                    break;
                }
            }
        }
        String target_skill_pref = getString(bountyId, "target_skill_preference");
        SkillPickPreference skillPref = SkillPickPreference.GENERIC;
        if(target_skill_pref!=null && !target_skill_pref.isEmpty()){
            switch (target_skill_pref){
                case "CARRIER" :{
                    skillPref=SkillPickPreference.CARRIER;
                    break;
                }
                case "PHASE" :{
                    skillPref=SkillPickPreference.PHASE;
                    break;
                }
            }
        }
        String memKey = "$"+bountyId;
        if(getString(bountyId, "job_memKey")!=null && !getString(bountyId, "job_memKey").isEmpty()){
            memKey = getString(bountyId, "job_memKey");
        }
        float reputation = 0.05f;
        if(getInt(bountyId, "job_reputation_reward")!=null){
            reputation = (float)getInt(bountyId, "job_reputation_reward")/100f;
        }
        String reply = MagicTxt.getString("mb_comm_reply");
        if (getString(bountyId,"job_comm_reply")==null){
            reply = null;
        } else if (!getString(bountyId,"job_comm_reply").isEmpty()){
            reply = getString(bountyId,"job_comm_reply");
        }
        /*
        List<String> themes = new ArrayList<>();
        if(getStringList(bountyId, "location_themes")!=null && !getStringList(bountyId, "location_themes").isEmpty()){
            themes = getStringList(bountyId, "location_themes");
            if(themes.contains("procgen_no_theme") || themes.contains("procgen_no_theme_pulsar_blackhole")){
                themes.add("theme_misc_skip");
                themes.add("theme_misc");
                themes.add("theme_core_unpopulated");
            }
        }
        */
        String origin_faction = getString(bountyId, "fleet_composition_faction");
        if(origin_faction==null || origin_faction.isEmpty()){
            origin_faction = getString(bountyId, "fleet_faction");
        }
        //Random flagship variant:                
        String flagship = getString(bountyId, "fleet_flagship_variant");   
        List <String> flagshipList = getStringList(bountyId, "fleet_flagship_variant");
        if(flagshipList!=null && !flagshipList.isEmpty()){
            int i = 0;
            flagship=flagshipList.get(i);
        }   
        //fixes for my own mistakes
        List <String> locations = new ArrayList<>();
        if(getStringList(bountyId, "location_marketIDs")!=null){
            locations = getStringList(bountyId, "location_marketIDs");
        } else if(getStringList(bountyId, "location_entitiesID")!=null){
            locations = getStringList(bountyId, "location_entitiesID");
        }
        //fixes for my own mistakes
        Integer minSize = getInt(bountyId, "fleet_min_FP", getInt(bountyId, "fleet_min_DP"));                    
        bountyData this_bounty = new bountyData(
            getStringList(bountyId, "trigger_market_id"),
            getStringList(bountyId, "trigger_marketFaction_any"),
            getBoolean(bountyId, "trigger_marketFaction_alliedWith"),
            getStringList(bountyId, "trigger_marketFaction_none"),
            getBoolean(bountyId, "trigger_marketFaction_enemyWith"),
            getInt(bountyId, "trigger_market_minSize"),
            getInt(bountyId, "trigger_player_minLevel"),
            getInt(bountyId, "trigger_min_days_elapsed"),
            getInt(bountyId, "trigger_min_fleet_size", 0),
            getFloat(bountyId, "trigger_weight_mult", 1f),
            getBooleanMap(bountyId, "trigger_memKeys_all"),
            getBooleanMap(bountyId, "trigger_memKeys_any"),
            getBooleanMap(bountyId, "trigger_memKeys_none"),
            getFloatMap(bountyId, "trigger_playerRelationship_atLeast"),
            getFloatMap(bountyId, "trigger_playerRelationship_atMost"),
            getFloat(bountyId, "trigger_giverTargetRelationship_atLeast",-99f),
            getFloat(bountyId, "trigger_giverTargetRelationship_atMost",99f),
            getString(bountyId, "job_name", MagicTxt.getString("mb_unnamed")), //"Unnamed job"
            getString(bountyId, "job_description"),
            reply,
            getString(bountyId, "job_intel_success"),
            getString(bountyId, "job_intel_failure"),
            getString(bountyId, "job_intel_expired"),
            getString(bountyId, "job_forFaction"),
            getString(bountyId, "job_difficultyDescription"),
            getInt(bountyId, "job_deadline"),
            getInt(bountyId, "job_credit_reward"),
            getFloat(bountyId, "job_reward_scaling"),
            reputation,
            getIntMap(bountyId, "job_item_reward"),
            getString(bountyId, "job_type"),
            getBooleanDefaultTrue(bountyId, "job_show_type"),
            getBooleanDefaultTrue(bountyId, "job_show_captain"),
            getString(bountyId, "job_show_fleet"),
            getString(bountyId, "job_show_distance"),
            getBoolean(bountyId, "job_show_arrow"),
            getString(bountyId, "job_pick_option"),
            getString(bountyId, "job_pick_script"),
            memKey,
            getString(bountyId, "job_conclusion_script"),
            getString(bountyId, "existing_target_memkey", null),
            getString(bountyId, "target_first_name"),
            getString(bountyId, "target_last_name"),
            getString(bountyId, "target_portrait"),
            gender,
            getString(bountyId, "target_rank"),
            getString(bountyId, "target_post"),
            getString(bountyId, "target_personality"),
            getString(bountyId, "target_aiCoreId"),
            getInt(bountyId, "target_level"),
            getInt(bountyId, "target_elite_skills", -1),
            skillPref,
            getIntMap(bountyId, "target_skills"),
            getString(bountyId, "fleet_name"),
            getString(bountyId, "fleet_faction"),
            flagship,
            getString(bountyId, "fleet_flagship_name"),
            getBoolean(bountyId, "fleet_flagship_recoverable"),
            getBoolean(bountyId, "fleet_flagship_autofit"),
            getIntMap(bountyId, "fleet_preset_ships"),
            getBoolean(bountyId, "fleet_preset_autofit"),
            getFloat(bountyId, "fleet_scaling_multiplier"),
            minSize,
            origin_faction,
            getFloat(bountyId, "fleet_composition_quality", 1),
            getBoolean(bountyId, "fleet_transponder"),
            getBoolean(bountyId, "fleet_no_retreat"),                    
            order,
            locations,
            getStringList(bountyId, "location_marketFactions"),
            getString(bountyId, "location_distance"),
            getStringList(bountyId, "location_themes"),
            getStringList(bountyId, "location_themes_blacklist"),
            getStringList(bountyId, "location_entities"),
            getBoolean(bountyId, "location_prioritizeUnexplored"),
            getBoolean(bountyId, "location_defaultToAnyEntity")
        );
        return this_bounty;
    }

    private static String getString(String bountyId, String key){
        return getString(bountyId, key, null);
    }

    private static String getString(String bountyId, String key, String defaultValue){
        String value=defaultValue;

        try {
            JSONObject reqSettings = BOUNTY_DATA.getJSONObject(bountyId);
            if(reqSettings.has(key)){
                value = reqSettings.getString(key);
            }
        } catch (JSONException ex){}

        return value;
    }

    private static Integer getInt(String bountyId, String key){
        return getInt(bountyId, key, -1);
    }

    private static Integer getInt(String bountyId, String key, int defaultValue){
        int value = defaultValue;

        try {
            JSONObject reqSettings = BOUNTY_DATA.getJSONObject(bountyId);
            if(reqSettings.has(key)){
                value = reqSettings.getInt(key);
            }
        } catch (JSONException ex){}

        return value;
    }

    private static boolean getBoolean(String bountyId, String key){
        boolean value=false;

        try {
            JSONObject reqSettings = BOUNTY_DATA.getJSONObject(bountyId);
            if(reqSettings.has(key)){
                value = reqSettings.getBoolean(key);
            }
        } catch (JSONException ex){}

        return value;
    }

    private static boolean getBooleanDefaultTrue(String bountyId, String key){
        boolean value=true;

        try {
            JSONObject reqSettings = BOUNTY_DATA.getJSONObject(bountyId);
            if(reqSettings.has(key)){
                value = reqSettings.getBoolean(key);
            }
        } catch (JSONException ex){}

        return value;
    }

    private static Float getFloat(String bountyId, String key){
        return getFloat(bountyId, key, -1);
    }

    private static Float getFloat(String bountyId, String key, float defaultValue){
        float value= defaultValue;

        try {
            JSONObject reqSettings = BOUNTY_DATA.getJSONObject(bountyId);
            if(reqSettings.has(key)){
                value = (float)reqSettings.getDouble(key);
            }
        } catch (JSONException ex){}

        return value;
    }

    private static List<String> getStringList(String bountyId, String key){
        List<String> value=new ArrayList<>();

        try {
            JSONObject reqSettings = BOUNTY_DATA.getJSONObject(bountyId);
            if(reqSettings.has(key)){
                JSONArray list = reqSettings.getJSONArray(key);
                if(list.length()>0){
                    for (int i = 0; i < list.length(); i++) {
                        value.add(list.getString(i));
                    }
                }
            }
        } catch (JSONException ex){}

        return value;
    }

    private static Map<String,Boolean> getBooleanMap(String bountyId, String key){
        Map<String,Boolean> value = new HashMap<>();

        try {
            JSONObject reqSettings = BOUNTY_DATA.getJSONObject(bountyId);
            if(reqSettings.has(key)){
                JSONObject list = reqSettings.getJSONObject(key);
                if(list.length()>0){
                    for(Iterator<?> iter = list.keys(); iter.hasNext();){
                        String this_key = (String)iter.next();
                        boolean this_data = list.getBoolean(this_key);
                        value.put(this_key,this_data);
                    }
                }
            }
        } catch (JSONException ex){}

        return value;
    }

    private static Map<String,Float> getFloatMap(String bountyId, String key){
        Map<String,Float> value = new HashMap<>();

        try {
            JSONObject reqSettings = BOUNTY_DATA.getJSONObject(bountyId);
            if(reqSettings.has(key)){
                JSONObject list = reqSettings.getJSONObject(key);
                if(list.length()>0){
                    for(Iterator<?> iter = list.keys(); iter.hasNext();){
                        String this_key = (String)iter.next();
                        float this_data = (float)list.getDouble(this_key);
                        value.put(this_key,this_data);
                    }
                }
            }
        } catch (JSONException ex){}

        return value;
    }

    private static Map<String,Integer> getIntMap(String bountyId, String key){
        Map<String,Integer> value = new HashMap<>();

        try {
            JSONObject reqSettings = BOUNTY_DATA.getJSONObject(bountyId);
            if(reqSettings.has(key)){
                JSONObject list = reqSettings.getJSONObject(key);
                if(list.length()>0){
                    for(Iterator<?> iter = list.keys(); iter.hasNext();){
                        String this_key = (String)iter.next();
                        int this_data = (int)list.getDouble(this_key);
                        value.put(this_key,this_data);
                    }
                }
            }
        } catch (JSONException ex){}

        return value;
    }
    
    private static Map<String,List<String>> getListMap(String bountyId, String key){
        Map<String,List<String>> value = new HashMap<>();
        try {
            JSONObject reqSettings = BOUNTY_DATA.getJSONObject(bountyId);
            //get object
            if(reqSettings.has(key)){
                //get key list
                JSONObject keyList = reqSettings.getJSONObject(key);
                if(keyList.length()>0){
                    for(Iterator<?> iter = keyList.keys(); iter.hasNext();){
                        String this_key = (String)iter.next();
                        //get list of values for each key
                        JSONArray data_list = keyList.getJSONArray(key);
                        List<String>parsed_list = new ArrayList<>();
                        if(data_list.length()>0){
                            for(int i=0; i<data_list.length();i++){
                                //turn json list into array list
                                parsed_list.add(data_list.getString(i));
                            }
                        }
                        value.put(this_key,parsed_list);
                    }
                }
            }
        } catch (JSONException ex){}

        return value;
    }
    
}