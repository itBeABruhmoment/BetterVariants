package better_variants.bar_events;

import better_variants.data.BetterVariantsBountyData;
import better_variants.data.BetterVariantsBountyDataMember;
import better_variants.data.CommonStrings;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBDeserter;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBStats;
import com.fs.starfarer.api.impl.campaign.missions.cb.CustomBountyCreator;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import variants_lib.data.FleetBuildData;
import variants_lib.data.VariantsLibFleetFactory;
import variants_lib.data.VariantsLibFleetParams;

import java.util.ArrayList;

public class BetterVariantsDeserterBountyCreator extends CBDeserter {
    private static final Logger log = Global.getLogger(better_variants.bar_events.BetterVariantsDeserterBountyCreator.class);
    static {
        log.setLevel(Level.ALL);
    }

    @Override
    public CustomBountyCreator.CustomBountyData createBounty(
            MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage
    ) {
        CustomBountyCreator.CustomBountyData data = new CustomBountyCreator.CustomBountyData();
        data.difficulty = difficulty;

        mission.requireSystemInterestingAndNotUnsafeOrCore();
        mission.requireSystemNotHasPulsar();
        StarSystemAPI system = mission.pickSystem();
        data.system = system;

        HubMissionWithTriggers.FleetSize size = HubMissionWithTriggers.FleetSize.MEDIUM;
        HubMissionWithTriggers.FleetQuality quality = HubMissionWithTriggers.FleetQuality.DEFAULT;
        String type = "patrolMedium";
        HubMissionWithTriggers.OfficerQuality oQuality = HubMissionWithTriggers.OfficerQuality.DEFAULT;
        HubMissionWithTriggers.OfficerNum oNum = HubMissionWithTriggers.OfficerNum.DEFAULT;

        FactionAPI faction = mission.getPerson().getFaction();

        if (difficulty <= 4) {
            size = HubMissionWithTriggers.FleetSize.SMALL;
            quality = HubMissionWithTriggers.FleetQuality.DEFAULT;
            oQuality = HubMissionWithTriggers.OfficerQuality.DEFAULT;
            oNum = HubMissionWithTriggers.OfficerNum.DEFAULT;
            type = "patrolSmall";
        } else if (difficulty <= 5) {
            size = HubMissionWithTriggers.FleetSize.MEDIUM;
            quality = HubMissionWithTriggers.FleetQuality.DEFAULT;
            oQuality = HubMissionWithTriggers.OfficerQuality.DEFAULT;
            oNum = HubMissionWithTriggers.OfficerNum.DEFAULT;
            type = "patrolMedium";
        } else if (difficulty == 6) {
            size = HubMissionWithTriggers.FleetSize.LARGE;
            quality = HubMissionWithTriggers.FleetQuality.DEFAULT;
            oQuality = HubMissionWithTriggers.OfficerQuality.DEFAULT;
            oNum = HubMissionWithTriggers.OfficerNum.DEFAULT;
            type = "patrolLarge";
        } else if (difficulty == 7) {
            size = HubMissionWithTriggers.FleetSize.LARGE;
            quality = HubMissionWithTriggers.FleetQuality.HIGHER;
            oQuality = HubMissionWithTriggers.OfficerQuality.DEFAULT;
            oNum = HubMissionWithTriggers.OfficerNum.MORE;
            type = "patrolLarge";
        } else if (difficulty == 8) {
            size = HubMissionWithTriggers.FleetSize.VERY_LARGE;
            quality = HubMissionWithTriggers.FleetQuality.HIGHER;
            oQuality = HubMissionWithTriggers.OfficerQuality.DEFAULT;
            oNum = HubMissionWithTriggers.OfficerNum.MORE;
            type = "patrolLarge";
        } else if (difficulty == 9) {
            size = HubMissionWithTriggers.FleetSize.HUGE;
            quality = HubMissionWithTriggers.FleetQuality.HIGHER;
            oQuality = HubMissionWithTriggers.OfficerQuality.HIGHER;
            oNum = HubMissionWithTriggers.OfficerNum.MORE;
            type = "patrolLarge";
        } else {
            size = HubMissionWithTriggers.FleetSize.MAXIMUM;
            quality = HubMissionWithTriggers.FleetQuality.HIGHER;
            oQuality = HubMissionWithTriggers.OfficerQuality.HIGHER;
            oNum = HubMissionWithTriggers.OfficerNum.MORE;
            type = "patrolLarge";
        }

        beginFleet(mission, data);
        mission.triggerCreateFleet(size, quality, faction.getId(), type, data.system);
        mission.triggerSetFleetOfficers(oNum, oQuality);
        mission.triggerAutoAdjustFleetSize(size, size.next());
        mission.triggerSetFleetFaction("pirates");
        mission.triggerFleetSetShipPickMode(FactionAPI.ShipPickMode.PRIORITY_THEN_ALL);

        mission.triggerFleetSetNoFactionInName();
        if (faction.getEntityNamePrefix() == null || faction.getEntityNamePrefix().isEmpty()) {
            mission.triggerFleetSetName("Deserter");
        } else {
            mission.triggerFleetSetName(String.valueOf(faction.getEntityNamePrefix()) + " Deserter");
        }

        mission.triggerSetStandardAggroPirateFlags();
        mission.triggerPickLocationAtInSystemJumpPoint(data.system);
        mission.triggerSpawnFleetAtPickedLocation(null, null);

        mission.triggerOrderFleetPatrol(data.system, true, new String[] { "jump_point", "salvageable", "planet" });
        data.fleet = createFleet(mission, data);
        if (data.fleet == null) {
            log.info(String.format("%s: could not create fleet", CommonStrings.MOD_ID));
            return null;
        }

        // stuff I added
        final ArrayList<String> factions = new ArrayList<>();
        factions.add(mission.getPerson().getFaction().getId());
        final BetterVariantsBountyDataMember bounty = BetterVariantsBountyData.getInstance().pickBounty(factions, 1, 0);
        if(bounty == null) {
            log.info(String.format("%s: no bounty for \"%s\" with difficulty %d could be found", CommonStrings.MOD_ID, factions.toString(), 1));
            return null;
        }

        final VariantsLibFleetFactory factory = FleetBuildData.FLEET_DATA.get(bounty.getFleetId());
        if(factory == null) {
            log.error(String.format("%s: no fleet factory with id \"%s\" could be found", CommonStrings.MOD_ID, bounty.getFleetId()));
            return null;
        }
        log.info(String.format("%s: use factory \"%s\"", CommonStrings.MOD_ID, factory.id));

        final VariantsLibFleetParams params = new VariantsLibFleetParams();
        params.faction = Factions.PIRATES;
        params.fleetPoints = 200;
        factory.editFleet(data.fleet);

        setRepChangesBasedOnDifficulty(data, difficulty);
        data.baseReward = CBStats.getBaseBounty(difficulty, CBStats.DESERTER_MULT, (BaseHubMission)mission);
        return data;
    }
}
