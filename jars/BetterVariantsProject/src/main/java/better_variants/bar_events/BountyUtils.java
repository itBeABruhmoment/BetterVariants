package better_variants.bar_events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import org.magiclib.util.MagicCampaign;
import org.magiclib.bounty.ActiveBounty;
import org.magiclib.bounty.MagicBountyCoordinator;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FactionDoctrineAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.RepairTrackerAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class BountyUtils {

//    public static bountyData createCopy(bountyData other)
//    {
//        bountyData data = new bountyData(
//            other.trigger_market_id,
//            other.trigger_marketFaction_any,
//            other.trigger_marketFaction_alliedWith,
//            other.trigger_marketFaction_none,
//            other.trigger_marketFaction_enemyWith,
//            other.trigger_market_minSize,
//            other.trigger_player_minLevel,
//            other.trigger_min_days_elapsed,
//            other.trigger_min_fleet_size,
//            other.trigger_weight_mult,
//            other.trigger_memKeys_all,
//            other.trigger_memKeys_any,
//            other.trigger_memKeys_none,
//            other.trigger_playerRelationship_atLeast,
//            other.trigger_playerRelationship_atMost,
//            other.trigger_giverTargetRelationship_atLeast,
//            other.trigger_giverTargetRelationship_atMost,
//            other.job_name,
//            other.job_description,
//            other.job_comm_reply,
//            other.job_intel_success,
//            other.job_intel_failure,
//            other.job_intel_expired,
//            other.job_forFaction,
//            other.job_difficultyDescription,
//            other.job_deadline,
//            other.job_credit_reward,
//            other.job_credit_scaling,
//            other.job_reputation_reward,
//            other.job_item_reward,
//            null,
//            other.job_show_type,
//            other.job_show_captain,
//            null,
//            null,
//            other.job_show_arrow,
//            other.job_pick_option,
//            other.job_pick_script,
//            other.job_memKey,
//            other.job_conclusion_script,
//            other.existing_target_memkey,
//            other.target_first_name,
//            other.target_last_name,
//            other.target_portrait,
//            other.target_gender,
//            other.target_rank,
//            other.target_post,
//            other.target_personality,
//            other.target_aiCoreId,
//            other.target_level,
//            other.target_elite_skills,
//            other.target_skill_preference,
//            other.target_skills,
//            other.fleet_name,
//            other.fleet_faction,
//            other.fleet_flagship_variant,
//            other.fleet_flagship_name,
//            other.fleet_flagship_recoverable,
//            other.fleet_flagship_autofit,
//            other.fleet_preset_ships,
//            other.fleet_preset_autofit,
//            other.fleet_scaling_multiplier,
//            other.fleet_min_FP,
//            other.fleet_composition_faction,
//            other.fleet_composition_quality,
//            other.fleet_transponder,
//            other.fleet_no_retreat,
//            other.fleet_behavior,
//            other.location_marketIDs,
//            other.location_marketFactions,
//            other.location_distance,
//            other.location_themes,
//            other.location_themes_blacklist,
//            other.location_entities,
//            other.location_prioritizeUnexplored,
//            other.location_defaultToAnyEntity
//            );
//            data.job_type = other.job_type;
//            data.job_show_fleet = other.job_show_fleet;
//            data.job_show_distance = other.job_show_distance;
//            return data;
//    }
}
