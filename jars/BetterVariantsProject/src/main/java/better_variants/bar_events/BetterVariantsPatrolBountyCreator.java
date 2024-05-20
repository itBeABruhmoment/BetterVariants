package better_variants.bar_events;

import better_variants.data.BetterVariantsBountyData;
import better_variants.data.BetterVariantsBountyDataMember;
import better_variants.data.CommonStrings;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBPatrol;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBStats;
import com.fs.starfarer.api.impl.campaign.missions.cb.CustomBountyCreator;
import com.fs.starfarer.api.impl.campaign.missions.hub.BaseHubMission;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithTriggers;
import com.fs.starfarer.api.impl.campaign.missions.hub.ReqMode;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.AICores;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import variants_lib.data.FleetBuildData;
import variants_lib.data.SettingsData;
import variants_lib.data.VariantsLibFleetFactory;
import variants_lib.data.VariantsLibFleetParams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.function.Predicate;

public class BetterVariantsPatrolBountyCreator extends CBPatrol implements BountyCreator {
    private static final Logger log = Global.getLogger(better_variants.bar_events.BetterVariantsPatrolBountyCreator.class);
    static {
        log.setLevel(Level.ALL);
    }

    final static ArrayList<String> TARGET_FACTIONS = new ArrayList<String>() {{
        add(Factions.HEGEMONY); add(Factions.LUDDIC_CHURCH); add(Factions.DIKTAT); add(Factions.LUDDIC_PATH);
        add(Factions.PERSEAN); add(Factions.INDEPENDENT); add(Factions.TRITACHYON);
    }};

    protected long seed = 0;

    public CustomBountyCreator.CustomBountyData createBounty(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage) {
        CustomBountyCreator.CustomBountyData data = new CustomBountyCreator.CustomBountyData();
        data.difficulty = difficulty;

        // stuff I added start

        // find target factions for bounties
        final ArrayList<String> targetFactions = BountyUtil.getFactionsWithRelation(createdAt.getFactionId(),
                RepLevel.VENGEFUL, RepLevel.INHOSPITABLE);
        final String[] targetFactionsArr = new String[targetFactions.size()];
        for(int i = 0; i < targetFactions.size(); i++) {
            targetFactionsArr[i] = targetFactions.get(i);
        }
        log.info(String.format("%s: %s", CommonStrings.MOD_ID, Arrays.toString(targetFactionsArr)));

        // stuff I added end

        mission.requireMarketSizeAtLeast(4);
        mission.requireMarketNotHidden();
        mission.requireMarketHasSpaceport();
        mission.requireMarketNotInHyperspace();
//        mission.requireMarketFactionCustom(ReqMode.NOT_ANY, new String[] { "decentralized" });
        // mission.requireMarketFactionNot(new String[] { "pirates" });
        mission.requireMarketFaction(targetFactionsArr);
        mission.requireMarketFactionNotPlayer();
        mission.requireMarketLocationNot(new LocationAPI[] { createdAt.getContainingLocation() });
        MarketAPI target = mission.pickMarket();

        if (target == null || target.getStarSystem() == null) {
            log.info(String.format("%s: could not find location to spawn patrol", CommonStrings.MOD_ID));
            return null;
        }

        StarSystemAPI system = target.getStarSystem();
        data.system = system;
        data.market = target;

        int num = 1;
        if (difficulty > 6) num = 2;
        if (difficulty > 8) num = 3;
        float protectorDiff = (difficulty - 3);

        HubMissionWithTriggers.FleetSize size = HubMissionWithTriggers.FleetSize.MEDIUM;
        HubMissionWithTriggers.FleetQuality quality = HubMissionWithTriggers.FleetQuality.DEFAULT;
        String type = "patrolMedium";
        HubMissionWithTriggers.OfficerQuality oQuality = HubMissionWithTriggers.OfficerQuality.DEFAULT;
        HubMissionWithTriggers.OfficerNum oNum = HubMissionWithTriggers.OfficerNum.DEFAULT;

        for (int i = 0; i < num; i++) {
            float diff = difficulty;
            if (i > 0) diff = protectorDiff;

            if (diff <= 2.0F) {
                size = HubMissionWithTriggers.FleetSize.TINY;
                type = "patrolSmall";
            } else if (diff <= 3.0F) {
                size = HubMissionWithTriggers.FleetSize.VERY_SMALL;
                type = "patrolSmall";
            } else if (diff <= 4.0F) {
                size = HubMissionWithTriggers.FleetSize.SMALL;
                type = "patrolSmall";
            } else if (difficulty <= 5) {
                size = HubMissionWithTriggers.FleetSize.MEDIUM;
                type = "patrolMedium";
            } else {
                size = HubMissionWithTriggers.FleetSize.LARGE;
                type = "patrolLarge";
            }

            beginFleet(mission, data);
            mission.triggerCreateFleet(size, quality, target.getFactionId(), type, data.system);
            mission.triggerSetFleetOfficers(oNum, oQuality);
            mission.triggerAutoAdjustFleetSize(size, size.next());
            mission.triggerFleetAllowLongPursuit();
            mission.triggerFleetSetAllWeapons();

            mission.triggerSetPatrol();

            if (i == 0) {
                mission.triggerSpawnFleetNear(target.getPrimaryEntity(), null, null);
                mission.triggerFleetSetPatrolActionText("patrolling");
                mission.triggerOrderFleetPatrol(data.system, true, new String[] { "jump_point", "objective" });
                mission.triggerOrderExtraPatrolPoints(new SectorEntityToken[] { target.getPrimaryEntity() });
            } else {
                mission.triggerSpawnFleetNear((SectorEntityToken)data.fleet, null, null);
                mission.triggerFleetSetPatrolActionText("guarding " + data.fleet.getName().toLowerCase());
                mission.triggerFleetSetPatrolLeashRange(100.0F);
                mission.triggerOrderFleetPatrol(data.system, true, new SectorEntityToken[] { (SectorEntityToken)data.fleet });
            }

            CampaignFleetAPI fleet = createFleet(mission, data);
            if (i == 0) {
                data.fleet = fleet;
            }
        }

        if (data.fleet == null) {
            log.info(String.format("%s: could not create fleet", CommonStrings.MOD_ID));
            return null;
        }

        // stuff I added

        // pick bounty
        final ArrayList<String> factions = new ArrayList<>();
        factions.add(target.getFactionId());
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
        params.faction = target.getFactionId();
        params.seed = seed;
        params.fleetPoints = BountyUtil.fpByDifficulty(difficulty);
        params.quality = BountyUtil.qualityByDifficulty(difficulty);
        params.averageOfficerLevel = BountyUtil.avgOfficerLevelByDifficulty(difficulty);
        params.numOfficers = BountyUtil.maxOfficersByDifficulty(difficulty);

        factory.editFleet(data.fleet, params);
        data.fleet.getMemoryWithoutUpdate().set(CommonStrings.DO_NOT_MODIFY_FLEET, true);
        data.fleet.setInflated(true);

        setRepChangesBasedOnDifficulty(data, difficulty);
        data.baseReward = CBStats.getBaseBounty(difficulty, CBStats.PATROL_MULT, (BaseHubMission)mission);

        return data;
    }

    @Override
    public CustomBountyData createBounty(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage, int makeDifferent) {
        this.seed = BountyUtil.createSeedForBounty(createdAt, makeDifferent);
        return this.createBounty(createdAt, mission, difficulty, bountyStage);
    }

    @Override
    public float getWeight(int difficulty) {
        return 5.0f;
    }
}
