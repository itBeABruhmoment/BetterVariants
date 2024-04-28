package better_variants.bar_events;

import better_variants.data.BetterVariantsBountyData;
import better_variants.data.BetterVariantsBountyDataMember;
import better_variants.data.CommonStrings;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBRemnant;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBStats;
import com.fs.starfarer.api.impl.campaign.missions.cb.CustomBountyCreator;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import variants_lib.data.FleetBuildData;
import variants_lib.data.VariantsLibFleetFactory;
import variants_lib.data.VariantsLibFleetParams;

import java.util.ArrayList;

public class BetterVariantsRemnantBountyCreator extends CBRemnant implements BountyCreator{
    private static final Logger log = Global.getLogger(better_variants.bar_events.BetterVariantsRemnantBountyCreator.class);
    static {
        log.setLevel(Level.ALL);
    }

    protected long seed = 0;

    @Override
    public CustomBountyCreator.CustomBountyData createBounty(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage) {
        CustomBountyCreator.CustomBountyData data = new CustomBountyCreator.CustomBountyData();
        data.difficulty = difficulty;

        mission.requireSystemTags(ReqMode.NOT_ANY, new String[] { "theme_core" });

        mission.preferSystemInteresting();
        mission.preferSystemUnexplored();
        mission.requireSystemNotHasPulsar();
        if (difficulty >= 9 && mission.rollProbability(PROB_IN_SYSTEM_WITH_BASE)) {
            mission.preferSystemTags(ReqMode.ANY, new String[] { "theme_remnant_main" });
            mission.requireSystemTags(ReqMode.NOT_ANY, new String[] { "theme_remnant_destroyed" });
        } else if (mission.rollProbability(PROB_IN_SYSTEM_WITH_TAP)) {
            mission.preferSystemTags(ReqMode.ANY, new String[] { "has_coronal_tap" });
        } else {
            mission.preferSystemBlackHoleOrNebula();
            mission.preferSystemOnFringeOfSector();
        }

        StarSystemAPI system = mission.pickSystem();
        data.system = system;

        HubMissionWithTriggers.FleetSize size = HubMissionWithTriggers.FleetSize.MEDIUM;
        HubMissionWithTriggers.FleetQuality quality = HubMissionWithTriggers.FleetQuality.VERY_HIGH;
        HubMissionWithTriggers.OfficerQuality oQuality = HubMissionWithTriggers.OfficerQuality.AI_MIXED;
        HubMissionWithTriggers.OfficerNum oNum = HubMissionWithTriggers.OfficerNum.ALL_SHIPS;
        String type = "patrolSmall";

        if (difficulty == 7) {
            size = HubMissionWithTriggers.FleetSize.LARGE;
            type = "patrolMedium";
            oQuality = HubMissionWithTriggers.OfficerQuality.AI_BETA_OR_GAMMA;
        } else if (difficulty == 8) {
            size = HubMissionWithTriggers.FleetSize.VERY_LARGE;
            type = "patrolLarge";
            oQuality = HubMissionWithTriggers.OfficerQuality.AI_MIXED;
        } else if (difficulty == 9) {
            size = HubMissionWithTriggers.FleetSize.HUGE;
            type = "patrolLarge";
            oQuality = HubMissionWithTriggers.OfficerQuality.AI_ALPHA;
        } else if (difficulty >= 10) {
            size = HubMissionWithTriggers.FleetSize.MAXIMUM;
            type = "patrolLarge";
            oQuality = HubMissionWithTriggers.OfficerQuality.AI_ALPHA;
        }

        beginFleet(mission, data);

        mission.triggerCreateFleet(size, quality, "remnant", type, data.system);
        mission.triggerSetFleetDoctrineQuality(5, 3, 5);
        mission.triggerSetFleetOfficers(oNum, oQuality);
        mission.triggerAutoAdjustFleetSize(size, size.next());
        mission.triggerSetRemnantConfigActive();
        mission.triggerSetFleetNoCommanderSkills();
        mission.triggerFleetAddCommanderSkill("flux_regulation", 1);
        mission.triggerFleetAddCommanderSkill("electronic_warfare", 1);
        mission.triggerFleetAddCommanderSkill("coordinated_maneuvers", 1);

        mission.triggerFleetSetAllWeapons();

        mission.triggerPickLocationAtInSystemJumpPoint(data.system);
        mission.triggerSpawnFleetAtPickedLocation(null, null);

        mission.triggerOrderFleetPatrol(data.system, true, new String[] { "jump_point", "neutrino", "neutrino_high", "station",
                "salvageable", "gas_giant" });

        data.fleet = createFleet(mission, data);

        if (data.fleet == null) {
            log.info(String.format("%s: could not create fleet", CommonStrings.MOD_ID));
            return null;
        }

        // stuff I added

        // get seed
        long seed = 0;
        try {
            seed = BountyUtil.createSeedForBounty(createdAt, difficulty);
        } catch (Exception e) {
            log.info(String.format("%s: error when getting salvage seed \n %s", CommonStrings.MOD_ID, e));
        }

        // pick bounty
        final ArrayList<String> factions = new ArrayList<>();
        factions.add(Factions.REMNANTS);
        final BetterVariantsBountyDataMember bounty = BetterVariantsBountyData.getInstance().pickBounty(factions, difficulty, seed);
        if(bounty == null) {
            log.info(String.format("%s: no bounty for \"%s\" with difficulty %d could be found", CommonStrings.MOD_ID, factions, difficulty));
            return null;
        }

        // create fleet
        final VariantsLibFleetFactory factory = FleetBuildData.FLEET_DATA.get(bounty.getFleetId());
        if(factory == null) {
            log.error(String.format("%s: no fleet factory with id \"%s\" could be found", CommonStrings.MOD_ID, bounty.getFleetId()));
            return null;
        }
        log.info(String.format("%s: use factory \"%s\"", CommonStrings.MOD_ID, factory.id));

        final VariantsLibFleetParams params = new VariantsLibFleetParams();
        params.faction = Factions.REMNANTS;
        params.seed = seed;
        params.fleetPoints = BountyUtil.fpByDifficulty(difficulty);
        params.quality = BountyUtil.qualityByDifficulty(difficulty);
        params.averageOfficerLevel = BountyUtil.avgOfficerLevelByDifficulty(difficulty);
        params.numOfficers = BountyUtil.maxOfficersByDifficulty(difficulty);

        factory.editFleet(data.fleet, params);
        data.fleet.getMemoryWithoutUpdate().set(CommonStrings.DO_NOT_MODIFY_FLEET, true);
        data.fleet.setInflated(true);

        setRepChangesBasedOnDifficulty(data, difficulty);
        data.baseReward = CBStats.getBaseBounty(difficulty, CBStats.REMNANT_MULT, (BaseHubMission)mission);

        return data;
    }

    @Override
    public CustomBountyData createBounty(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage, int makeDifferent) {
        this.seed = BountyUtil.createSeedForBounty(createdAt, makeDifferent);
        return this.createBounty(createdAt, mission, difficulty, bountyStage);
    }

    @Override
    public int getMinDifficulty() {
        return 8;
    }

    @Override
    public int getMaxDifficulty() {
        return 10;
    }

    @Override
    public float getFrequency(HubMissionWithBarEvent mission, int difficulty) {
        return 10.f;
    }
}
