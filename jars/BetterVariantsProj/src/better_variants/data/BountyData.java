package better_variants.data;

import better_variants.data.FactionData;
import better_variants.data.VariantData;
import better_variants.bar_events.BetterVariantsBarEventCreator;
import better_variants.bar_events.TestEvent;
import better_variants.data.CommonStrings;
import better_variants.data.FleetBuildData;
import better_variants.data.SettingsData;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetAssignment;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.impl.campaign.events.OfficerManagerEvent.SkillPickPreference;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;

import data.scripts.bounty.ActiveBounty;
import data.scripts.bounty.MagicBountyCoordinator;
import data.scripts.bounty.MagicBountyData;
import data.scripts.bounty.MagicBountyData.bountyData;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONException;

public class BountyData {
    
    public static void addBounty(String bountyId, float spawnWeight)
    {
        bountyData this_bounty = new bountyData(
            new ArrayList<String>(), //trigger_market_id, 
            new ArrayList<String>(), //trigger_marketFaction_any, 
            false,//trigger_marketFaction_alliedWith, 
            new ArrayList<String>(),//trigger_marketFaction_none, 
            false,//trigger_marketFaction_enemyWith, 
            0,//trigger_market_minSize, 
            0,//trigger_player_minLevel, 
            0,//trigger_min_days_elapsed, 
            0,//trigger_min_fleet_size, 
            spawnWeight,//trigger_weight_mult, 
            new HashMap<String, Boolean>(),//trigger_memKeys_all, 
            new HashMap<String, Boolean>(),//trigger_memKeys_any, 
            new HashMap<String, Boolean>(),//trigger_memKeys_none, 
            new HashMap<String, Float>(),//trigger_playerRelationship_atLeast, 
            new HashMap<String, Float>(),//trigger_playerRelationship_atMost, 
            -99f,//trigger_giverTargetRelationship_atLeast, 
            99f,//trigger_giverTargetRelationship_atMost, 
            "better variants bounty",//job_name, 
            "test",//job_description, 
            "hello there",//job_comm_reply, 
            "success",//job_intel_success, 
            "fail",//job_intel_failure, 
            "fail",//job_intel_expired, 
            "pirates",//job_forFaction, 
            "auto",//job_difficultyDescription, 
            360,//job_deadline, 
            69,//job_credit_reward, 
            1.0f,//job_credit_scaling, 
            5.0f,//job_reputation_reward, 
            new HashMap<String, Integer>(),//job_item_reward, 
            "assassination",//job_type, 
            true,//job_show_type, 
            false,//job_show_captain, 
            "vanilla",//job_show_fleet, 
            "vanilla",//job_show_distance, 
            true,//job_show_arrow, 
            "Accept the job",//job_pick_option, 
            null,//job_pick_script, 
            "$" + bountyId,//job_memKey, 
            null,//job_conclusion_script, 
            null,//existing_target_memkey, 
            null,//target_first_name, 
            null,//target_last_name, 
            null,//target_portrait, 
            FullName.Gender.MALE,//target_gender, 
            "citizen",//target_rank, 
            "spacer",//arget_post, 
            null,//target_personality, 
            null,//target_aiCoreId, 
            15,//target_level, 
            -1,//target_elite_skills, 
            SkillPickPreference.ANY,//target_skill_preference, 
            new HashMap<String, Integer>(),//target_skills, 
            "bounty fleet",//fleet_name, 
            "pirates",//fleet_faction, 
            "buffalo2_FS",//fleet_flagship_variant, 
            "shippy",//fleet_flagship_name, 
            true,//fleet_flagship_recoverable, 
            true,//fleet_flagship_autofit, 
            new HashMap<String, Integer>(),//fleet_preset_ships, 
            true,//fleet_preset_autofit, 
            1.0f,//fleet_scaling_multiplier, 
            100,//fleet_min_FP, 
            "pirates",//fleet_composition_faction, 
            1.0f,//fleet_composition_quality, 
            true,//fleet_transponder, 
            true,//fleet_no_retreat, 
            FleetAssignment.ORBIT_AGGRESSIVE,//fleet_behavior, 
            new ArrayList<String>(),//location_marketIDs, 
            new ArrayList<String>(),//location_marketFactions, 
            "CLOSE",//location_distance, 
            new ArrayList<String>(),//location_themes, 
            new ArrayList<String>(),//location_themes_blacklist, 
            new ArrayList<String>(),//location_entities, 
            false,//location_prioritizeUnexplored, 
            true//location_defaultToAnyEntity
        );

        if(((!MagicBountyData.BOUNTIES.containsKey(bountyId) && !Global.getSector().getMemoryWithoutUpdate().contains(this_bounty.job_memKey)))){
            MagicBountyData.BOUNTIES.put(bountyId, this_bounty);
        }
    }

    public static void test()
    {
        String key = "tart_organs";
        final data.scripts.bounty.MagicBountyData.bountyData bounty = data.scripts.bounty.MagicBountyData.getBountyData(key);
        final data.scripts.bounty.MagicBountyCoordinator bountyCoordinator = data.scripts.bounty.MagicBountyCoordinator.getInstance();
        data.scripts.bounty.ActiveBounty active = bountyCoordinator.getActiveBounty(key);
        if(active == null) {
            active = bountyCoordinator.createActiveBounty(key, bounty);
        }
        bountyCoordinator.configureBountyListeners();
        //active.acceptBounty(arg0, arg1, arg2, arg3);
    }
}
