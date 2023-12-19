package better_variants.bar_events;

import better_variants.data.CommonStrings;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.HasMemory;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Voices;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBDeserter;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBStats;
import com.fs.starfarer.api.impl.campaign.missions.cb.CustomBountyCreator;
import com.fs.starfarer.api.impl.campaign.missions.cb.MilitaryCustomBounty;
import com.fs.starfarer.api.impl.campaign.missions.hub.*;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;
import variants_lib.data.FleetBuildData;
import variants_lib.data.VariantsLibFleetFactory;
import variants_lib.data.VariantsLibFleetParams;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// TODO: read callaction of base custom bounty
public class BetterVariantsBounty extends MilitaryCustomBounty {
    private static final Logger log = Global.getLogger(better_variants.bar_events.BetterVariantsBounty.class);
    static {
        log.setLevel(Level.ALL);
    }

    public static final ArrayList<CustomBountyCreator> CREATORS = new ArrayList<CustomBountyCreator>() {{add(new BetterVariantsDeserterBountyCreator());}};

    @Override
    public List<CustomBountyCreator> getCreators() {
        log.info("BetterVariantsBountyCreate creator #############################");
        return CREATORS;
    }

    /*
    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
        log.info("BetterVariantsBountyCreate ####################################");
        if (barEvent) {
            setGiverRank(Ranks.AGENT);
            setGiverPost(Ranks.POST_EXECUTIVE);
            setGiverImportance(PersonImportance.HIGH);
            setGiverFaction(Factions.TRITACHYON);
            setGiverTags(Tags.CONTACT_MILITARY);
            setGiverVoice(Voices.BUSINESS);
            findOrCreateGiver(createdAt, false, false);
        }
        // return super.create(createdAt, barEvent);
        boolean result = super.create(createdAt, barEvent);
        log.info(result);
        log.info("BetterVariantsBountyCreate end");
        return result;
    }
     */

    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
        log.info("BetterVariantsBountyCreate ####################################");
        if (barEvent) {
            createBarGiver(createdAt);
        }

        PersonAPI person = getPerson();
        if (person == null) {
            log.info("BetterVariantsBountyCreate r1");
            return false;
        }

        String id = getMissionId();
        if (!setPersonMissionRef(person, "$" + id + "_ref")) {
            log.info("BetterVariantsBountyCreate r2");
            return false;
        }

        setStartingStage(Stage.BOUNTY);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);
        addNoPenaltyFailureStages(new Object[] { Stage.FAILED_NO_PENALTY });

        connectWithMemoryFlag(Stage.BOUNTY, Stage.COMPLETED, (HasMemory)person, "$" + id + "_completed");
        connectWithMemoryFlag(Stage.BOUNTY, Stage.FAILED, (HasMemory)person, "$" + id + "_failed");

        addTag("Bounties");

        int dLow = pickDifficulty(DifficultyChoice.LOW);
        this.creatorLow = pickCreator(dLow, DifficultyChoice.LOW);

        if (this.creatorLow != null) {
            log.info("creator not null");
            this.dataLow = this.creatorLow.createBounty(createdAt, this, dLow, Stage.BOUNTY);
        } else {
            log.info("creator null"); // creator appears to be null in current iteration
        }
        if (this.dataLow == null || this.dataLow.fleet == null) {
            // the issue
            log.info("BetterVariantsBountyCreate r3");
            return false;
        }

        int dNormal = pickDifficulty(DifficultyChoice.NORMAL);
        this.creatorNormal = pickCreator(dNormal, DifficultyChoice.NORMAL);
        if (this.creatorNormal != null) {
            this.dataNormal = this.creatorNormal.createBounty(createdAt, this, dNormal, Stage.BOUNTY);
        }
        if (this.dataNormal == null || this.dataNormal.fleet == null) {
            log.info("BetterVariantsBountyCreate r4");
            return false;
        }

        int dHigh = pickDifficulty(DifficultyChoice.HIGH);
        this.creatorHigh = pickCreator(dHigh, DifficultyChoice.HIGH);
        if (this.creatorHigh != null) {
            this.dataHigh = this.creatorHigh.createBounty(createdAt, this, dHigh, Stage.BOUNTY);
        }

        if (this.dataHigh == null || this.dataHigh.fleet == null) {
            log.info("BetterVariantsBountyCreate r5");
            return false;
        }

        return true;
    }

    @Override
    protected CustomBountyCreator pickCreator(int difficulty, DifficultyChoice choice) {
        log.info("pick creator");
        log.info(CREATORS.size());
        return CREATORS.get(0);
    }



    public static class BetterVariantsDeserterBountyCreator extends CBDeserter {
        // mostly copied from decompiled code
        @Override
        public CustomBountyCreator.CustomBountyData createBounty(MarketAPI createdAt, HubMissionWithBarEvent mission, int difficulty, Object bountyStage) {
            log.info("createBounty");
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
            if (data.fleet == null) return null;
            setRepChangesBasedOnDifficulty(data, difficulty);
            data.baseReward = CBStats.getBaseBounty(difficulty, CBStats.DESERTER_MULT, (BaseHubMission)mission);
            log.info("createBounty end");


            // stuff I added
            final CampaignFleetAPI bountyFleet = data.fleet;
            final PersonAPI commander = bountyFleet.getCommander();
            final VariantsLibFleetParams params = new VariantsLibFleetParams();
            params.faction = Factions.PIRATES;
            params.numOfficers  = 5;
            params.fleetPoints = 200;
            final VariantsLibFleetFactory fleetFactory = FleetBuildData.FLEET_DATA.get("bv_hegemony_xiv");
            fleetFactory.editFleet(bountyFleet, params);
            for(FleetMemberAPI memberAPI : bountyFleet.getMembersWithFightersCopy()) {
                if(memberAPI.isFlagship()) {
                    memberAPI.setCaptain(commander);
                    break;
                }
            }
            bountyFleet.setCommander(commander);

            return data;
        }

        @Override
        public void addIntelAssessment(TextPanelAPI text, HubMissionWithBarEvent mission, CustomBountyCreator.CustomBountyData data) {
            log.info("add intel assessment start");
            float opad = 10.0F;
            List<FleetMemberAPI> list = new ArrayList<FleetMemberAPI>();
            List<FleetMemberAPI> members = data.fleet.getFleetData().getMembersListCopy();
            int max = 7;
            int cols = 7;
            float iconSize = (440 / cols);
            Color h = Misc.getHighlightColor();

            for (FleetMemberAPI member : members) {
                if (member.isFighterWing())
                    continue;
                FleetMemberAPI copy = Global.getFactory().createFleetMember(FleetMemberType.SHIP, member.getVariant());
                copy.setCaptain(member.getCaptain());
//                if (member.isFlagship()) {
//                    copy.setCaptain(data.fleet.getCommander());
//                }
                list.add(copy);
            }

            if (!list.isEmpty()) {
                TooltipMakerAPI info = text.beginTooltip();
                info.setParaSmallInsignia();
                info.addPara(String.valueOf(Misc.ucFirst(mission.getPerson().getHeOrShe())) + " taps a data pad, and " +
                        "an intel assessment shows up on your tripad.", 0.0F);
                int rows = list.size() / cols;
                if(list.size() % cols != 0) {
                    rows++;
                }
                info.addShipList(cols, rows, iconSize, data.fleet.getFaction().getBaseUIColor(), list, opad);

                int num = members.size() - list.size();

                if (num < 5) {
                    num = 0;
                } else if (num < 10) {
                    num = 5;
                } else if (num < 20) {
                    num = 10;
                } else {
                    num = 20;
                }

//                if (num > 1) {
//                    info.addPara("The assessment notes the fleet may contain upwards of %s other ships of lesser significance.",
//                            opad, h, new String[]{num});
//                } else if (num > 0) {
//                    info.addPara("The assessment notes the fleet may contain several other ships of lesser significance.",
//                            opad);
//                } else {
//                    info.addPara("It appears to contain complete information about the scope of the assignment.", opad);
//                }
                text.addTooltip();
            }
            log.info("add intel assessment end");
        }

        @Override
        public String getId() {
            return "BetterVariantsDeserterBountyCreator";
        }
    }

    // TODO: figure out if hyperspace location is important
    public static class BetterVariantsCreateFleetAction implements MissionTrigger.TriggerAction {
        private static final Logger log = Global.getLogger(better_variants.bar_events.BetterVariantsBounty.BetterVariantsCreateFleetAction.class);
        static {
            log.setLevel(Level.ALL);
        }

        public String fleetId = "";
        public VariantsLibFleetParams params = new VariantsLibFleetParams();
        public Vector2f position = new Vector2f(0, 0);

        BetterVariantsCreateFleetAction(String fleetId, VariantsLibFleetParams params, StarSystemAPI location) {
            this.fleetId = fleetId;
            this.params = params;
            this.position = location.getLocation();
        }

        @Override
        public void doAction(MissionTrigger.TriggerActionContext context) {
            log.info("do action start");
            final Random rand = new Random(params.seed);
            final VariantsLibFleetFactory fleetFactory = FleetBuildData.FLEET_DATA.get(fleetId);
            if(fleetFactory == null) {
                log.info(String.format("%s:no fleet factory with the id \"%s\" could be found", CommonStrings.MOD_ID, fleetId));
                return;
            }

            final CampaignFleetAPI bountyFleet = fleetFactory.createFleet(params);
            bountyFleet.setLocation(position.x, position.y);
            context.fleet = bountyFleet;
            context.fleet.setFacing(rand.nextFloat() * 360.0f);
            context.fleet.getMemoryWithoutUpdate().set("$core_fleetBusy", Boolean.TRUE);
            context.allFleets.add(bountyFleet);
            if (!context.fleet.hasScriptOfClass(MissionFleetAutoDespawn.class)) {
                context.fleet.addScript(new MissionFleetAutoDespawn(context.mission, context.fleet));
            }
            log.info("do action end");
        }
    }
}
