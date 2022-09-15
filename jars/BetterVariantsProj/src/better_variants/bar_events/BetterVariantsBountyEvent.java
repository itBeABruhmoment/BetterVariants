package better_variants.bar_events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lazywizard.console.Console;

import data.scripts.util.MagicCampaign;
import data.scripts.bounty.ActiveBounty;
import data.scripts.bounty.MagicBountyCoordinator;
import data.scripts.bounty.MagicBountyData;
import data.scripts.bounty.MagicBountyData.bountyData;

import java.awt.Color;
import java.security.Key;

import org.lwjgl.util.vector.Vector2f;

import variants_lib.data.FactionData;
import variants_lib.data.SettingsData;
import variants_lib.scripts.fleetedit.OfficerEditing;
import variants_lib.scripts.fleetedit.FleetBuilding;
//import better_variants.data.FactionData;
//import better_variants.data.FleetBuildData;
//import better_variants.data.SettingsData;
//import better_variants.scripts.fleetedit.FleetCompEditing;
//import better_variants.scripts.fleetedit.OfficerEditing;

import com.fs.starfarer.api.characters.FullName;
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
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.RepairTrackerAPI;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.PersonBountyIntel.BountyType;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson;
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.BreadcrumbSpecial;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

import better_variants.data.CommonStrings;

import static data.scripts.util.MagicTxt.getString;

// bar event for "talk to the officer offering exotic bounties"
public class BetterVariantsBountyEvent extends BaseBarEventWithPerson{
    private static final Logger log = Global.getLogger(better_variants.bar_events.BetterVariantsBountyEvent.class);
    static {
        log.setLevel(Level.ALL);
    }
    

    private static final Random RAND = new Random();
    private static final float BOUNTY_NO_CHANGE_DURATION = 30.0f;
    private static final String EASY_BOUNTY_MEMKEY = "$bv_existingbounty_easy";
    private static final String MEDIUM_BOUNTY_MEMKEY = "$bv_existingbounty_med";
    private static final String HARD_BOUNTY_MEMKEY = "$bv_existingbounty_hard";
    private static final String LAST_VIEWED_BOUNTY_MEMKEY = "$bv_last_viewed_bounty";
    private static final String TRAITOR_BOUNTY_DATA_KEY = "better_variants_traitor";
    private static final String ENEMY_BOUNTY_DATA_KEY = "better_variants_enemy";
    private static final String OFFERED_AS_BOUNTY_TAG = "bounty";

    private static String generateUniqueBountyKey()
    {
        MagicBountyCoordinator coordinator = MagicBountyCoordinator.getInstance();
        int count = 0;
        String key = "better_variants_bounty" + count;
        while(coordinator.getActiveBounty(key) != null) {
            count++;
            key = "better_variants_bounty" + count;
        }
        return key;
    }

    @Override
    public boolean shouldShowAtMarket(MarketAPI market)
    {
        String marketFaction = market.getFaction().getId();
        return FactionData.FACTION_DATA.containsKey(marketFaction) && FactionData.FACTION_DATA.get(marketFaction).hasTag(OFFERED_AS_BOUNTY_TAG);
    }

    @Override
    public void addPromptAndOption(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        // Calling super does nothing in this case, but is good practice because a subclass should
        // implement all functionality of the superclass (and usually more)
        super.addPromptAndOption(dialog, memoryMap);
        regen(dialog.getInteractionTarget().getMarket()); // Sets field variables and creates a random person

        // Display the text that will appear when the player first enters the bar and looks around
        dialog.getTextPanel().addPara("A military officer is holding court at one of the nicer tables; you watch a merc captain approach for a perfunctory exchange of words and mutual tap of datapads. They appear to be acting as some kind of system patrol liaison contracting out exotic bounties.");

        // Display the option that lets the player choose to investigate our bar event
        dialog.getOptionPanel().addOption("Talk to the officer offering exotic military bounties", this);
    }

    /**
     * Called when the player chooses this event from the list of options shown when they enter the bar.
     */
    @Override
    public void init(InteractionDialogAPI dialog, Map<String, MemoryAPI> memoryMap) {
        super.init(dialog, memoryMap);
        // Choose where the player has to travel to
        //TestEvent.initQuest();

        // set the bounty giver to person appropriate to market
        PersonAPI bountyGiver = getPerson();
        FactionAPI marketFaction = getMarket().getFaction();

        PersonAPI temp = marketFaction.createRandomPerson();
        bountyGiver.setFaction(marketFaction.getId());
        bountyGiver.setName(temp.getName());
        bountyGiver.setPortraitSprite(temp.getPortraitSprite());

        // If player starts our event, then backs out of it, `done` will be set to true.
        // If they then start the event again without leaving the bar, we should reset `done` to false.
        done = false;

        // The boolean is for whether to show only minimal person information. True == minimal
        dialog.getVisualPanel().showPersonInfo(person, true);

        // Launch into our event by triggering the "INIT" option, which will call `optionSelected()`
        this.optionSelected(null, OptionId.INIT);
    }

    /**
     * This method is called when the player has selected some option for our bar event.
     *
     * @param optionText the actual text that was displayed on the selected option
     * @param optionData the value used to uniquely identify the option
     */
    @Override
    public void optionSelected(String optionText, Object optionData) {
        if (optionData instanceof OptionId) {
            // Clear shown options before we show new ones
            dialog.getOptionPanel().clearOptions();

            // Handle all possible options the player can choose
            switch ((OptionId) optionData) {
                case INIT: {
                    dialog.getTextPanel().addPara("You approach and the officer gives you a prefunctory nod. \"Captian,\" he says, all business.");
                    dialog.getTextPanel().addPara("\"I have several assignments on the docket,\" he looks down, scrolling his datapad. \"What sort of job can you handle?\"");

                    // make bounty giving person display properly
                    dialog.getVisualPanel().showPersonInfo(getPerson());


                    // And give them some options on what to do next
                    dialog.getOptionPanel().addOption("A managable target", OptionId.EASY_BOUNTY);
                    dialog.getOptionPanel().addOption("A standard target", OptionId.MEDIUM_BOUNTY);
                    dialog.getOptionPanel().addOption("A challenging target", OptionId.HARD_BOUNTY);
                    dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                    break;
                }
                case EASY_BOUNTY: {
                    final MagicBountyCoordinator bountyCoordinator = MagicBountyCoordinator.getInstance();
                    MemoryAPI bountyGiverMemory = dialog.getInteractionTarget().getMemoryWithoutUpdate();

                    // get a key for the bounty coordinator
                    final String activeBountyKey;
                    final TargetType bountyType;
                    if(bountyGiverMemory.contains(EASY_BOUNTY_MEMKEY)) {
                        BountyInfo info = (BountyInfo) bountyGiverMemory.get(EASY_BOUNTY_MEMKEY);
                        activeBountyKey = info.activeBountyKey;
                        bountyType = info.type;
                    } else {
                        activeBountyKey = generateUniqueBountyKey();
                        bountyType = pickRandomTargetType(getPerson().getFaction());
                        bountyGiverMemory.set(EASY_BOUNTY_MEMKEY, new BountyInfo(bountyType, activeBountyKey), BOUNTY_NO_CHANGE_DURATION);
                    }

                    if(bountyType == TargetType.TRAITOR) {
                        log.debug("traitor");
                    } else {
                        log.debug("enemy");
                    }

                    // store the last viewed bounty for the accept case
                    bountyGiverMemory.set(LAST_VIEWED_BOUNTY_MEMKEY, EASY_BOUNTY_MEMKEY, 1.0f);

                    int bountyFleetDP = (int)(Global.getSector().getPlayerFleet().getFleetPoints() * 0.7) + 10;
                    if(bountyFleetDP < 70) {
                        bountyFleetDP = 70;
                    } else if(bountyFleetDP > 500) {
                        bountyFleetDP = 500;
                    }

                    // get active bounty for the job
                    final MagicBountyData.bountyData bounty;
                    if(bountyType == TargetType.ENEMY) {
                        bounty = BountyCreationData.getEnemyBountyData();
                    } else {
                        bounty = BountyCreationData.getTraitorBountyData();
                    }
                    ActiveBounty active = bountyCoordinator.getActiveBounty(activeBountyKey);
                    if(active == null) {
                        active = createActiveBounty(getPerson(), activeBountyKey, bountyFleetDP, bountyType);
                    }

                    // if active bounty fails to generate
                    if(active == null) {
                        dialog.getOptionPanel().addOption("you have found a bug", OptionId.BUG);
                        break;
                    }

                    displayBountyInfo(dialog, active, bounty, bountyType);

                    dialog.getOptionPanel().addOption("Accept bounty", OptionId.ACCEPT);
                    dialog.getOptionPanel().addOption("View other bounties", OptionId.INIT);
                    dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                    break;
                }
                case MEDIUM_BOUNTY: {
                    final MagicBountyCoordinator bountyCoordinator = MagicBountyCoordinator.getInstance();
                    MemoryAPI bountyGiverMemory = dialog.getInteractionTarget().getMemoryWithoutUpdate();

                    // get a key for the bounty coordinator
                    final String activeBountyKey;
                    final TargetType bountyType;
                    if(bountyGiverMemory.contains(MEDIUM_BOUNTY_MEMKEY)) {
                        BountyInfo info = (BountyInfo) bountyGiverMemory.get(MEDIUM_BOUNTY_MEMKEY);
                        activeBountyKey = info.activeBountyKey;
                        bountyType = info.type;
                    } else {
                        activeBountyKey = generateUniqueBountyKey();
                        bountyType = pickRandomTargetType(getPerson().getFaction());
                        bountyGiverMemory.set(MEDIUM_BOUNTY_MEMKEY, new BountyInfo(bountyType, activeBountyKey), BOUNTY_NO_CHANGE_DURATION);
                    }

                    if(bountyType == TargetType.TRAITOR) {
                        log.debug("traitor");
                    } else {
                        log.debug("enemy");
                    }

                    // store the last viewed bounty for the accept case
                    bountyGiverMemory.set(LAST_VIEWED_BOUNTY_MEMKEY, MEDIUM_BOUNTY_MEMKEY, 1.0f);

                    int bountyFleetDP = (int)(Global.getSector().getPlayerFleet().getFleetPoints() * 1.0) + 10;
                    if(bountyFleetDP < 100) {
                        bountyFleetDP = 100;
                    } else if(bountyFleetDP > 500) {
                        bountyFleetDP = 500;
                    }

                    // get active bounty for the job
                    final MagicBountyData.bountyData bounty;
                    if(bountyType == TargetType.ENEMY) {
                        bounty = BountyCreationData.getEnemyBountyData();
                    } else {
                        bounty = BountyCreationData.getTraitorBountyData();
                    }
                    ActiveBounty active = bountyCoordinator.getActiveBounty(activeBountyKey);
                    if(active == null) {
                        active = createActiveBounty(getPerson(), activeBountyKey, bountyFleetDP, bountyType);
                    }

                    // if active bounty fails to generate
                    if(active == null) {
                        dialog.getOptionPanel().addOption("you have found a bug", OptionId.BUG);
                        break;
                    }

                    displayBountyInfo(dialog, active, bounty, bountyType);

                    dialog.getOptionPanel().addOption("Accept bounty", OptionId.ACCEPT);
                    dialog.getOptionPanel().addOption("View other bounties", OptionId.INIT);
                    dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                    break;
                }
                case HARD_BOUNTY: {
                    final MagicBountyCoordinator bountyCoordinator = MagicBountyCoordinator.getInstance();
                    MemoryAPI bountyGiverMemory = dialog.getInteractionTarget().getMemoryWithoutUpdate();

                    // get a key for the bounty coordinator
                    final String activeBountyKey;
                    final TargetType bountyType;
                    if(bountyGiverMemory.contains(HARD_BOUNTY_MEMKEY)) {
                        BountyInfo info = (BountyInfo) bountyGiverMemory.get(HARD_BOUNTY_MEMKEY);
                        activeBountyKey = info.activeBountyKey;
                        bountyType = info.type;
                    } else {
                        activeBountyKey = generateUniqueBountyKey();
                        bountyType = pickRandomTargetType(getPerson().getFaction());
                        bountyGiverMemory.set(HARD_BOUNTY_MEMKEY, new BountyInfo(bountyType, activeBountyKey), BOUNTY_NO_CHANGE_DURATION);
                    }

                    if(bountyType == TargetType.TRAITOR) {
                        log.debug("traitor");
                    } else {
                        log.debug("enemy");
                    }

                    // store the last viewed bounty for the accept case
                    bountyGiverMemory.set(LAST_VIEWED_BOUNTY_MEMKEY, HARD_BOUNTY_MEMKEY, 1.0f);

                    int bountyFleetDP = (int)(Global.getSector().getPlayerFleet().getFleetPoints() * 1.3) + 10;
                    if(bountyFleetDP < 130) {
                        bountyFleetDP = 130;
                    } else if(bountyFleetDP > 500) {
                        bountyFleetDP = 500;
                    }

                    // get active bounty for the job
                    final MagicBountyData.bountyData bounty;
                    if(bountyType == TargetType.ENEMY) {
                        bounty = BountyCreationData.getEnemyBountyData();
                    } else {
                        bounty = BountyCreationData.getTraitorBountyData();
                    }
                    ActiveBounty active = bountyCoordinator.getActiveBounty(activeBountyKey);
                    if(active == null) {
                        active = createActiveBounty(getPerson(), activeBountyKey, bountyFleetDP, bountyType);
                    }

                    // if active bounty fails to generate
                    if(active == null) {
                        dialog.getOptionPanel().addOption("you have found a bug", OptionId.BUG);
                        break;
                    }

                    displayBountyInfo(dialog, active, bounty, bountyType);

                    dialog.getOptionPanel().addOption("Accept bounty", OptionId.ACCEPT);
                    dialog.getOptionPanel().addOption("View other bounties", OptionId.INIT);
                    dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                    break;
                }
                case ACCEPT: {
                    final MagicBountyCoordinator bountyCoordinator = MagicBountyCoordinator.getInstance();
                    MemoryAPI bountyGiverMemory = dialog.getInteractionTarget().getMemoryWithoutUpdate();

                    String bountyToAccept = bountyGiverMemory.getString(LAST_VIEWED_BOUNTY_MEMKEY);
                    BountyInfo bountyInfo = (BountyInfo) bountyGiverMemory.get(bountyToAccept);
                    String activeBountyKey = bountyInfo.activeBountyKey;
                    TargetType bountyType = bountyInfo.type;

                    final MagicBountyData.bountyData bounty;
                    if(bountyType == TargetType.ENEMY) {
                        bounty = BountyCreationData.getEnemyBountyData();
                    } else {
                        bounty = BountyCreationData.getTraitorBountyData();
                    }

                    // get active bounty for the job
                    ActiveBounty active = bountyCoordinator.getActiveBounty(activeBountyKey);

                    // incase active is missing for some reason
                    if(active == null) {
                        dialog.getOptionPanel().addOption("you have found a bug", OptionId.BUG);
                        break;
                    }

                    //bountyCoordinator.configureBountyListeners();
                    log.debug("faction: " + getPerson().getFaction().getId());
                    active.acceptBounty(dialog.getInteractionTarget(), (float)calculateReward(active), 0.03f, getPerson().getFaction().getId());

                    dialog.getTextPanel().addPara("You accept the bounty");
                    dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                    
                    // remove event from bar
                    BarEventManager.getInstance().notifyWasInteractedWith(this);

                    break;
                }
                case LEAVE: {
                    // They've chosen to leave, so end our interaction. This will send them back to the bar.
                    // If noContinue is false, then there will be an additional "Continue" option shown
                    // before they are returned to the bar. We don't need that.
                    noContinue = true;
                    done = true;

                    break;
                }
                case BUG: {
                    dialog.getTextPanel().addPara("the fleet generation probably bugged out. Contact the creator of Better Variants about this");
                    dialog.getOptionPanel().addOption("Leave", OptionId.LEAVE);
                    break;
                }
            }
        }
    }

    // creates an active bounty
    // returns true if an active bounty is sucessfully created, false otherwise
    private static ActiveBounty createActiveBounty(PersonAPI bountyGiver, String activeBountyKey, int minFleetPoints, TargetType type)
    {
        final MagicBountyCoordinator bountyCoordinator = MagicBountyCoordinator.getInstance();

        // determine faction of person
        FactionAPI giverFaction = bountyGiver.getFaction();
        FactionAPI targetFleetTypeFaction = null;
        if(type == TargetType.ENEMY) {
            targetFleetTypeFaction = getRandomEnemyFaction(giverFaction);
        } else {
            targetFleetTypeFaction = bountyGiver.getFaction();
        }

        // try creating ActiveBounty
        bountyData bountyGen = null;
        PersonAPI target = null;
        if(type == TargetType.ENEMY) {
            bountyGen = BountyUtils.createCopy(BountyCreationData.getEnemyBountyData());
            target = targetFleetTypeFaction.createRandomPerson();
            bountyGen.fleet_faction = targetFleetTypeFaction.getId();
            bountyGen.location_marketFactions = new ArrayList<>();
            bountyGen.location_marketFactions.add(targetFleetTypeFaction.getId());
        } else {
            bountyGen = BountyUtils.createCopy(BountyCreationData.getTraitorBountyData());
            target = giverFaction.createRandomPerson();
        }
        bountyGen.job_name = "Bounty - " + target.getName().getFirst() + " " + target.getName().getLast();
        bountyGen.fleet_min_FP = minFleetPoints;
        bountyGen.target_first_name = target.getName().getFirst();
        bountyGen.target_last_name = target.getName().getLast();
        bountyGen.target_gender = target.getGender();
        bountyGen.target_portrait = target.getPortraitSprite();
        bountyGen.job_forFaction =giverFaction.getId();

        ActiveBounty active = bountyCoordinator.createActiveBounty(activeBountyKey, bountyGen);
        if(active == null) {
            return null;
        }

        MemoryAPI fleetMemory = active.getFleet().getMemoryWithoutUpdate();
        fleetMemory.set(MemFlags.MEMORY_KEY_FLEET_TYPE, CommonStrings.EXOTIC_BOUNTY_FLEET_TYPE);
        fleetMemory.set(variants_lib.data.CommonStrings.FLEET_EDITED_MEMKEY, true);

        // edit fleet of active bounty
        String fleetCompId = FleetBuilding.editFleet(active.getFleet(), new Random());
        if(fleetCompId != null) {
            active.getFleet().getMemoryWithoutUpdate().set(variants_lib.data.CommonStrings.FLEET_VARIANT_KEY, fleetCompId);
        }
        if(SettingsData.OfficerEditingEnabled()) {
            OfficerEditing.editAllOfficers(active.getFleet(), fleetCompId);
        }

        active.getFleet().getCommander().setName(target.getName());
        active.getFleet().getCommander().setGender(target.getGender());
        active.getFleet().getCommander().setPortraitSprite(target.getPortraitSprite());

        return active;
    }

    private static TargetType pickRandomTargetType(FactionAPI faction)
    {
        Vector<FactionAPI> enemies = getEnemyFactions(faction);
        if(enemies.size() == 0) {
            return TargetType.TRAITOR;
        }

        int random = RAND.nextInt();
        if(random % 2 == 0) {
            return TargetType.TRAITOR;
        } else {
            return TargetType.ENEMY;
        }
    }

    private void displayBountyInfo(InteractionDialogAPI dialog, ActiveBounty active, MagicBountyData.bountyData bounty, TargetType type)
    {
        PersonAPI target = active.getFleet().getCommander();

        if(type == TargetType.TRAITOR) {
            dialog.getTextPanel().addPara("\"I'd take this one on myself if I had a warfleet to command,\" " + getHeOrShe() + 
                " makes an expression like " + getHeOrShe() + " bit into something sour, \"" + target.getNameString() + 
                "  is a betrayer and deserter. An example has to be made.");
        } else {
            dialog.getTextPanel().addPara("\"" + target.getNameString() + "has been involved in numerous raids and must be eliminated\"");
        }

        text.addPara("\"" + createLocationEstimateText(active) + " " + getString("mb_distance") + ".\"",
            Misc.getTextColor(),
            Misc.getHighlightColor(),
            Math.round(Misc.getDistanceLY(market.getPrimaryEntity(), active.getFleetSpawnLocation())) + ""
        );

        text.addPara(
            "\"The reward for completing this task is %s credits.\"",
            Misc.getHighlightColor(),
            calculateReward(active) + ""
        );

        // Show fleet
        if (bounty.job_show_fleet != MagicBountyData.ShowFleet.None) {
            showFleet(
                    text,
                    dialog.getTextWidth(),
                    active.getFleet().getFaction().getBaseUIColor(),
                    MagicBountyData.ShowFleet.All,
                    active.getFleet().getFleetData().getMembersInPriorityOrder(),
                    active.getFlagshipInFleet(),
                    active.getPresetShipsInFleet()
            );
        }

        // Show target
        dialog.getVisualPanel().showPersonInfo(active.getFleet().getCommander());

        // Show map
        dialog.getVisualPanel().showMapMarker(
            active.getFleetSpawnLocation().getStarSystem().getHyperspaceAnchor(), 
		    null,
            null, 
			false,
            null,
            null,
            null
        );

    }

    private int calculateReward(ActiveBounty active)
    {
        float travelCostEstimate = Math.round(Misc.getDistanceLY(market.getPrimaryEntity(), active.getFleetSpawnLocation())) * 0.50f;
        float fleetValueEstimate = active.getFleet().getFleetPoints() * 0.20f;
        return Math.round(travelCostEstimate + fleetValueEstimate) * 10000 + 10000;
    }

    private static FactionAPI getRandomEnemyFaction(FactionAPI faction)
    {
        Vector<FactionAPI> enemies = getEnemyFactions(faction);
        int index = RAND.nextInt() % enemies.size();
        if(index < 0 || index >= enemies.size()) { // index evaluated to -1 somehow
            index = 1;
        }
        return enemies.get(index);
    }

    private static Vector<FactionAPI> getEnemyFactions(FactionAPI faction)
    {
        Vector<FactionAPI> enemies = new Vector<>();
        List<FactionAPI> allFactions = Global.getSector().getAllFactions();

        for(FactionAPI otherFaction : allFactions) {
            String otherFactionId = otherFaction.getId();
            if(faction != otherFaction
            && FactionData.FACTION_DATA.containsKey(otherFactionId)
            && FactionData.FACTION_DATA.get(otherFactionId).hasTag(OFFERED_AS_BOUNTY_TAG)
            && faction.getRelationship(otherFactionId) < -0.4f) {
                enemies.add(otherFaction);
            }
        }

        return enemies;
    }

    private static Vector<FactionAPI> getAllyFactions(FactionAPI faction)
    {
        Vector<FactionAPI> allies = new Vector<>();
        List<FactionAPI> allFactions = Global.getSector().getAllFactions();

        for(FactionAPI otherFaction : allFactions) {
            String otherFactionId = otherFaction.getId();
            if(faction != otherFaction
            && FactionData.FACTION_DATA.containsKey(otherFactionId)
            && FactionData.FACTION_DATA.get(otherFactionId).hasTag(OFFERED_AS_BOUNTY_TAG)
            && faction.getRelationship(otherFactionId) > 0.6f) {
                allies.add(otherFaction);
            }
        }

        return allies;
    }

    private class BountyInfo {
        public TargetType type;
        public String activeBountyKey;

        public BountyInfo(TargetType Type, String ActiveBountyKey) {
            type = Type;
            activeBountyKey = ActiveBountyKey;
        }
    }

    private void showFleet(
            TextPanelAPI info,
            float width,
            Color factionBaseUIColor,
            MagicBountyData.ShowFleet setting,
            List<FleetMemberAPI> ships,
            List<FleetMemberAPI> flagship,
            List<FleetMemberAPI> preset
    ) {

        int columns = 10;
        switch (setting) {
            case Text:
                //write the number of ships
                int num = ships.size();
                if (num < 5) {
                    num = 5;
                    info.addPara(getString("mb_fleet6"),
                            Misc.getTextColor(),
                            Misc.getHighlightColor(),
                            "" + num
                    );
                    break;
                } else if (num < 10) num = 5;
                else if (num < 20) num = 10;
                else if (num < 30) num = 20;
                else num = 30;
                info.addPara(getString("mb_fleet5"),
                        Misc.getTextColor(),
                        Misc.getHighlightColor(),
                        "" + num
                );
            case Flagship:
                //show the flagship
                info.addPara(getString("mb_fleet0") + getString("mb_fleet"));
                info.beginTooltip().addShipList(
                        columns,
                        1,
                        (width - 10) / columns,
                        factionBaseUIColor,
                        flagship,
                        10f
                );
                info.addTooltip();
                break;
            case FlagshipText:
                //show the flagship
                info.addPara(getString("mb_fleet0") + getString("mb_fleet"));
                info.beginTooltip().addShipList(
                        columns,
                        1,
                        (width - 10) / columns,
                        factionBaseUIColor,
                        flagship,
                        10f
                );
                info.addTooltip();

                //write the number of other ships
                num = ships.size() - 1;
                num = Math.round((float) num * (1f + random.nextFloat() * 0.5f));
                if (num < 5) {
                    info.addPara(getString("mb_fleet4"),
                            Misc.getTextColor(),
                            Misc.getHighlightColor()
                    );
                    break;
                } else if (num < 10) num = 5;
                else if (num < 20) num = 10;
                else if (num < 30) num = 20;
                else num = 30;
                info.addPara(getString("mb_fleet3"),
                        Misc.getTextColor(),
                        Misc.getHighlightColor(),
                        "" + num
                );
                break;

            case Preset:
                //show the preset fleet
                info.addPara(getString("mb_fleet1") + getString("mb_fleet"));

                info.beginTooltip().addShipList(
                        columns,
                        (int) Math.round(Math.ceil((double) preset.size() / columns)),
                        (width - 10) / columns,
                        factionBaseUIColor,
                        preset,
                        10f
                );
                info.addTooltip();
                break;

            case PresetText:
                //show the preset fleet
                info.addPara(getString("mb_fleet1") + getString("mb_fleet"));
                List<FleetMemberAPI> toShow = preset;
                info.beginTooltip().addShipList(
                        columns,
                        (int) Math.round(Math.ceil((double) preset.size() / columns)),
                        (width - 10) / columns,
                        factionBaseUIColor,
                        toShow,
                        10f
                );
                info.addTooltip();
                //write the number of other ships
                num = ships.size() - toShow.size();
                num = Math.round((float) num * (1f + random.nextFloat() * 0.5f));
                if (num < 5) {
                    info.addPara(getString("mb_fleet4"),
                            Misc.getTextColor(),
                            Misc.getHighlightColor()
                    );
                    break;
                } else if (num < 10) num = 5;
                else if (num < 20) num = 10;
                else if (num < 30) num = 20;
                else num = 30;
                info.addPara(getString("mb_fleet3"),
                        Misc.getTextColor(),
                        Misc.getHighlightColor(),
                        "" + num
                );
                break;

            case Vanilla:
                //show the Flagship and the 6 biggest ships in the fleet
                info.addPara(getString("mb_fleet1") + getString("mb_fleet"));

                //there are less than 7 ships total, all will be shown
                if (ships.size() <= columns) {
                    toShow = new ArrayList<>();
                    //add flagship first
                    for (FleetMemberAPI m : ships) {
                        if (m.isFlagship()) {
                            toShow.add(m);
                            break;
                        }
                    }
                    //then all the rest
                    for (FleetMemberAPI m : ships) {
                        if (!m.isFlagship()) {
                            toShow.add(m);
                        }
                    }
                    //display the ships
                    info.beginTooltip().addShipList(
                            columns,
                            1,
                            (width - 10) / columns,
                            factionBaseUIColor,
                            toShow,
                            10f
                    );
                    info.addTooltip();
                    info.addPara(getString("mb_fleet4"),
                            Misc.getTextColor(),
                            Misc.getHighlightColor()
                    );
                    break;
                }
                //If there are more than 7 ships, pick the largest 7
                toShow = new ArrayList<>();
                //add flagship first
                for (FleetMemberAPI m : ships) {
                    if (m.isFlagship()) {
                        toShow.add(m);
                        break;
                    }
                }
                //then complete the list
                for (FleetMemberAPI m : ships) {
                    if (toShow.size() >= columns) break;
                    if (!m.isFlagship()) toShow.add(m);
                }
                //make the ship list
                info.beginTooltip().addShipList(
                        columns,
                        1,
                        (width - 10) / columns,
                        factionBaseUIColor,
                        toShow,
                        10f
                );
                info.addTooltip();

                //write the number of other ships
                num = ships.size() - columns;
                num = Math.round((float) num * (1f + random.nextFloat() * 0.5f));
                if (num < 5) {
                    info.addPara(getString("mb_fleet4"),
                            Misc.getTextColor(),
                            Misc.getHighlightColor()
                    );
                    break;
                } else if (num < 10) num = 5;
                else if (num < 20) num = 10;
                else if (num < 30) num = 20;
                else num = 30;
                info.addPara(getString("mb_fleet3"),
                        Misc.getTextColor(),
                        Misc.getHighlightColor(),
                        "" + num
                );
                break;
            case All:
                //show the full fleet
                info.addPara(getString("mb_fleet2") + getString("mb_fleet"));
                toShow = new ArrayList<>();
                //add flagship first
                for (FleetMemberAPI m : ships) {
                    if (m.isFlagship()) {
                        toShow.add(m);
                        break;
                    }
                }
                //then all the rest
                for (FleetMemberAPI m : ships) {
                    if (!m.isFlagship()) {
                        toShow.add(m);
                    }
                }
                //display the ships
                info.beginTooltip().addShipList(
                        columns,
                        (int) Math.round(Math.ceil((double) ships.size() / columns)),
                        (width - 10) / columns,
                        factionBaseUIColor,
                        toShow,
                        10f
                );
                info.addTooltip();
            default:
                break;
        }
    }

    static String createLocationEstimateText(final ActiveBounty bounty) {
        String loc = BreadcrumbSpecial.getLocatedString(bounty.getFleetSpawnLocation());
        String sheIs = getString("mb_distance_she");
        if (bounty.getCaptain().getGender() == FullName.Gender.MALE) sheIs = getString("mb_distance_he");
        if (bounty.getCaptain().getGender() == FullName.Gender.ANY) sheIs = getString("mb_distance_they");
        if (bounty.getCaptain().isAICore()) sheIs = getString("mb_distance_it");
        loc = sheIs + getString("mb_distance_rumor") + loc + getString(".");

        return loc;
    }

    enum TargetType {
        TRAITOR,
        ENEMY
    }

    enum OptionId {
        INIT,
        EASY_BOUNTY,
        MEDIUM_BOUNTY,
        HARD_BOUNTY,
        ACCEPT,
        LEAVE,
        BUG
    }
}

/*
case INIT: { // scope because everything in a switch is the same scope apparently
                    final MagicBountyCoordinator bountyCoordinator = MagicBountyCoordinator.getInstance();
                    MemoryAPI bountyGiverMemory = dialog.getInteractionTarget().getMemoryWithoutUpdate();

                    String key = null;
                    if(bountyGiverMemory.contains(EXISTING_BOUNTY_MEMKEY)) {
                        key = (String) bountyGiverMemory.get(EXISTING_BOUNTY_MEMKEY);
                    } else {
                        key = generateUniqueBountyKey();
                        bountyGiverMemory.set(EXISTING_BOUNTY_MEMKEY, key);
                    }

                    // get active bounty for the job
                    final MagicBountyData.bountyData bounty = MagicBountyData.getBountyData(BOUNTY_DATA_KEY);
                    ActiveBounty active = bountyCoordinator.getActiveBounty(key);
                    if(active == null) {
                        active = createActiveBounty(dialog.getInteractionTarget(), key);
                    }

                    // if active fails to generate
                    if(active == null) {
                        dialog.getOptionPanel().addOption("you have found a bug", OptionId.BUG);
                        break;
                    }

                    PersonAPI target = active.getFleet().getCommander();
                    dialog.getTextPanel().addPara("\"I'd take this one on myself if I had a warfleet to command,\" " + getHeOrShe() + 
                    " makes an expression like " + getHeOrShe() + " bit into something sour, \"" + target.getNameString() + 
                    "  is a betrayer and deserter. An example has to be made.");

                    text.addPara(createLocationEstimateText(active));

                    //SHOW FLEET
                    if (bounty.job_show_fleet != MagicBountyData.ShowFleet.None) {
                        showFleet(
                                text,
                                dialog.getTextWidth(),
                                active.getFleet().getFaction().getBaseUIColor(),
                                MagicBountyData.ShowFleet.All,
                                active.getFleet().getFleetData().getMembersInPriorityOrder(),
                                active.getFlagshipInFleet(),
                                active.getPresetShipsInFleet()
                        );
                    }

                    // And give them some options on what to do next
                    dialog.getOptionPanel().addOption("Accept Bounty", OptionId.ACCEPT_BOUNTY);
                    dialog.getOptionPanel().addOption("Decline", OptionId.LEAVE);
                    break;
                }

*/