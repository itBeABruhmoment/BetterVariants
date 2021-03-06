package better_variants.scripts.fleetedit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Collections;

import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.fleet.RepairTrackerAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.EconomyAPI.EconomyUpdateListener;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.ShipQuality;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;

import better_variants.data.BetterVariantsTags;
import better_variants.data.AlwaysBuildMember;
import better_variants.data.CommonStrings;
import better_variants.data.FactionData;
import better_variants.data.FleetBuildData;
import better_variants.data.VariantData;
import better_variants.data.FactionData.FactionConfig;
import better_variants.data.FleetComposition;
import better_variants.data.FleetPartition;
import better_variants.data.FleetPartitionMember;

import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.plugins.impl.CoreAutofitPlugin;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class FleetCompEditing {
    public static final String ALWAYS_EDIT = "ALWAYS_EDIT";
    private static final int MAX_OVERBUDGET = 3;
    private static final Random rand = new Random();
    private static final String[] FALLBACK_HULLMODS = {"hardenedshieldemitter", "fluxdistributor", 
    "fluxbreakers", "reinforcedhull", "targetingunit", "solar_shielding"};

    private static final Logger log = Global.getLogger(better_variants.scripts.fleetedit.FleetCompEditing.class);
    static {
        log.setLevel(Level.ALL);
    }
    
    private static double getDPDouble(String variantId)
    {
        return Global.getSettings().getVariant(variantId).getHullSpec().getSuppliesToRecover();
    }

    private static int getDPInt(String variantId)
    {
        return Math.round(Global.getSettings().getVariant(variantId).getHullSpec().getSuppliesToRecover());
    }

    private static Vector<FleetPartitionMember> getPickableVariants(FleetPartition partition, int DPLimit)
    {
        Vector<FleetPartitionMember> members = new Vector<FleetPartitionMember>(10);
        for(FleetPartitionMember member : partition.members) {
            if(getDPInt(member.id) <= DPLimit) {
                members.add(member);
            }
        }
        return members;
    }

    private static double sumWeights(Vector<FleetPartitionMember> members)
    {
        double sum = 0;
        for(FleetPartitionMember member : members) {
            sum += member.weight;
        }
        return sum;
    }

    private static String pickVariant(FleetPartition partition, int DPLimit)
    {
        Vector<FleetPartitionMember> pickableVariants = getPickableVariants(partition, DPLimit);
        if(pickableVariants.size() == 0) { // no elligible varaints because not enough dp to spawn them
            return null;
        }

        double random = rand.nextDouble();
        double totalWeightsSum = sumWeights(pickableVariants);
        double runningWeightsSum = 0;
        for(FleetPartitionMember member : pickableVariants) {
            runningWeightsSum += member.weight / totalWeightsSum;
            if(runningWeightsSum >= random) {
                return member.id;
            }
        }
        return pickableVariants.get(pickableVariants.size() - 1).id; // handle possible edge case
    }

    private static FleetMemberAPI createVariant(String variantId, float quality)
    {
        FleetMemberAPI ship = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variantId);
        return ship;
    }

    private static double sumPartitionWeights(int start, FleetComposition fleetCompData)
    {
        double sum = 0;
        for(int i = start; i < fleetCompData.partitions.length; i++) {
            sum += fleetCompData.partitions[i].partitionWeight;
        }
        return sum;
    }

    private static void createFleet(CampaignFleetAPI fleetAPI, FleetInfo info, FleetComposition fleetCompData)
    {
        float quality = 1.0f;
        FleetInflater inflater = fleetAPI.getInflater();
        if(inflater != null) {
            quality = inflater.getQuality();
        }

        Vector<FleetMemberAPI> combatShips = new Vector<FleetMemberAPI>(30);
        Vector<FleetMemberAPI> civilianShips = new Vector<FleetMemberAPI>(10);
        int maxShipsThatCanBeAdded = 30 - info.mothballedShips.size();
        int totalDPRemaining = info.originalDP;

        if(fleetCompData.alwaysInclude != null) {
            for(AlwaysBuildMember member : fleetCompData.alwaysInclude) {
                for(int i = 0; i < member.amount; i++) {
                    FleetMemberAPI newMember = createVariant(member.id, quality);
                    if(newMember.isCivilian()) {
                        civilianShips.add(newMember);
                    } else {
                        combatShips.add(newMember);
                    }
                    totalDPRemaining -= getDPInt(member.id);
                    maxShipsThatCanBeAdded--;
                }
            }
        }

        for(int i = 0; i < fleetCompData.partitions.length; i++) {
            int remainingDpThisPartition = ((int)Math.round(totalDPRemaining * 
            (fleetCompData.partitions[i].partitionWeight / sumPartitionWeights(i, fleetCompData))));

            while(remainingDpThisPartition > 0) {
                if(maxShipsThatCanBeAdded == 0) {
                    return;
                }
                String variantId = pickVariant(fleetCompData.partitions[i], remainingDpThisPartition + MAX_OVERBUDGET);
                if(variantId == null) {
                    break;
                }

                FleetMemberAPI newMember = createVariant(variantId, quality);
                if(newMember.isCivilian()) {
                    civilianShips.add(newMember);
                } else {
                    combatShips.add(newMember);
                }

                int DPofVariant = getDPInt(variantId);
                remainingDpThisPartition -= DPofVariant;
                totalDPRemaining -= DPofVariant;
                maxShipsThatCanBeAdded--;
            }
        }

        // assign officers
        if(combatShips.size() == 0) { // if there is only civilian ships for some reason
            Collections.shuffle(civilianShips);
            for(int i = 0; i < civilianShips.size() && i < info.officers.size(); i++) {
                civilianShips.get(i).setCaptain(info.officers.get(i));
            }

            // ensure flagship is set
            if(civilianShips.size() < info.officers.size()) {
                civilianShips.get(0).setCaptain(info.captain);
            }
        } else {
            // assign officers
            Collections.shuffle(combatShips);
            int flagShipIndex = -1;
            for(int i = 0; i < combatShips.size() && i < info.officers.size(); i++) {
                if(info.officers.get(i) == info.captain) {
                    flagShipIndex = i;
                }
                combatShips.get(i).setCaptain(info.officers.get(i));
            }

            // find highest dp ship
            int highestDP = getDPInt(combatShips.get(0).getVariant().getHullVariantId());
            int highestDPIndex = 0;
            for(int i = 1; i < combatShips.size(); i++) {
                int dp = getDPInt(combatShips.get(i).getVariant().getHullVariantId());
                if(highestDP < dp) {
                    highestDP = dp;
                    highestDPIndex = i;
                }
            }

            // set flagship to highest dp ship
            if(flagShipIndex == -1) {
                combatShips.get(highestDPIndex).setCaptain(info.captain);
            } else {
                PersonAPI temp = combatShips.get(highestDPIndex).getCaptain();
                combatShips.get(highestDPIndex).setCaptain(info.captain);
                combatShips.get(flagShipIndex).setCaptain(temp);
            }
        }

        Collections.sort(combatShips, new SortByDP());
        Collections.sort(civilianShips, new SortByDP());

        // add smods
        if(info.averageSmods > 0.1)
        {
            // kind of a placeholder smodding function
            //log.debug("adding smods");
            for(FleetMemberAPI member : combatShips) {
                int numSModsToAdd = Math.round((float)(info.averageSmods + rand.nextDouble() * 0.5));
                ShipVariantAPI variant  = member.getVariant();

                Collection<String> hullMods = variant.getNonBuiltInHullmods();

                int start = rand.nextInt() & Integer.MAX_VALUE; // get positive int
                start = start % FALLBACK_HULLMODS.length;
                int numHullModsAdded = 0;
                for(int i = 0; i < FALLBACK_HULLMODS.length && numHullModsAdded < numSModsToAdd; i++) {
                    int index = (start + i) % FALLBACK_HULLMODS.length;
                    if(!hullMods.contains(FALLBACK_HULLMODS[index])) {
                        variant.addPermaMod(FALLBACK_HULLMODS[index], true);
                        numHullModsAdded++;
                    }
                }
    
            }
        }


        // add ships to fleet
        for(FleetMemberAPI member : combatShips) {
            //RepairTrackerAPI repairTracker = member.getRepairTracker();
            //repairTracker.setCR(0.7f);
            fleetAPI.getFleetData().addFleetMember(member);
        }
        for(FleetMemberAPI member : civilianShips) {
            //RepairTrackerAPI repairTracker = member.getRepairTracker();
            //repairTracker.setCR(0.7f);
            fleetAPI.getFleetData().addFleetMember(member);
        }
        for(FleetMemberAPI member : info.mothballedShips)
        {
            fleetAPI.getFleetData().addFleetMember(member);
        }
    }

    // gather info relevent for generating new fleet
    private static FleetInfo getInfo(CampaignFleetAPI fleetAPI) 
    {
        PersonAPI captain = fleetAPI.getCommander();
        Vector<PersonAPI> officers = new Vector<PersonAPI>(15);
        float totalDp = 0;
        Vector<FleetMemberAPI> mothballedShips = new Vector<FleetMemberAPI>(5);
        int numShips = 0;
        int numSMods = 0;
        boolean isStationFleet = false;


        // count dp, count dmods and save officers
        List<FleetMemberAPI> members = fleetAPI.getMembersWithFightersCopy();
        for(FleetMemberAPI member : members) {
            if(member.isStation()) {
                isStationFleet = true;
            }

            if(member.isMothballed()) {
                mothballedShips.add(member);
            } else {
                officers.add(member.getCaptain());
                totalDp += member.getBaseDeploymentCostSupplies();
                numShips++;
                numSMods += member.getVariant().getSMods().size();
            }
        }

        return new FleetInfo(captain, officers, Math.round(totalDp), mothballedShips, 
        isStationFleet, ((double)numSMods) / numShips);
    }

    // delete all members in fleet
    private static void clearMembers(CampaignFleetAPI fleetAPI) 
    {
        FleetDataAPI fleetData = fleetAPI.getFleetData();
        List<FleetMemberAPI> members = fleetAPI.getMembersWithFightersCopy();
        for(FleetMemberAPI member : members) {
            fleetData.removeFleetMember(member);
        }
    }

    private static class SortByDP implements Comparator<FleetMemberAPI>
    {
        // Used for sorting in ascending order of
        // roll number
        public int compare(FleetMemberAPI a, FleetMemberAPI b)
        {
            if(b.getVariant().getHullVariantId() == null || a.getVariant().getHullVariantId() == null) {
                log.debug("no variant found rippy dippy");
            }
            return getDPInt(b.getVariant().getHullVariantId()) - getDPInt(a.getVariant().getHullVariantId());
        }
    }

    private static Vector<FleetComposition> getValidFleetChoices(FleetInfo info, String factionId, String fleetType)
    {
        Vector<FleetComposition> fleetComps = new Vector<FleetComposition>(5);
        for(String compId : FactionData.FACTION_DATA.get(factionId).customFleetIds) {
            FleetComposition comp = FleetBuildData.FLEET_DATA.get(compId);
            if(comp != null 
            && comp.maxDP >= info.originalDP 
            && info.originalDP >= comp.minDP 
            && (comp.targetFleetTypes.contains(fleetType) || fleetType.equals(ALWAYS_EDIT))) {
                fleetComps.add(comp);
            }
        }
        return fleetComps;
    }

    private static FleetComposition pickFleet(FleetInfo info, String factionId, String fleetType)
    {
        if(!FactionData.FACTION_DATA.containsKey(factionId)) {
            log.debug(factionId + " not registered");
            return null;
        }

        // get correct special fleet spawn rate
        double specialFleetSpawnRate = 0.0;
        FactionConfig config = FactionData.FACTION_DATA.get(factionId);
        if(config.specialFleetSpawnRateOverrides.containsKey(fleetType)) {
            specialFleetSpawnRate = config.specialFleetSpawnRateOverrides.get(fleetType);
        } else {
            specialFleetSpawnRate = config.specialFleetSpawnRate;
        }

        if(specialFleetSpawnRate < rand.nextDouble()) {
            return null;
        }

        Vector<FleetComposition> validFleetComps = getValidFleetChoices(info, factionId, fleetType);
        //String info1 = "";
        //for(FleetComposition comp : validFleetComps) {
        //    info1 += comp.id + " " + comp.spawnWeight + ", ";
        //}
        //log.debug(info1);

        if(validFleetComps.size() == 0) {
            return null;
        }

        double random = rand.nextDouble();
        double totalWeightsSum = 0;
        for(FleetComposition comp : validFleetComps) {
            totalWeightsSum += comp.spawnWeight;
        }
        //log.debug("rand: " + random + " weightSum: " + totalWeightsSum);
        double runningWeightsSum = 0;
        for(FleetComposition comp : validFleetComps) {
            //log.debug("add: " + comp.spawnWeight / totalWeightsSum);
            runningWeightsSum += comp.spawnWeight / totalWeightsSum;
            if(runningWeightsSum > random) {
                return comp;
            }
        }
        return validFleetComps.get(validFleetComps.size() - 1);
    }

    // give commander any addition skills specified in fleet json
    private static void editCommander(PersonAPI commander, FleetComposition compInfo) 
    {
        if(compInfo.commanderSkills != null) {
            log.debug("adding additional skills");
            MutableCharacterStatsAPI stats = commander.getStats();
            for(String skill : compInfo.commanderSkills) {
                if(!stats.hasSkill(skill)) {
                    stats.increaseSkill(skill);
                }
            }
        }
    }

    public static String editFleet(CampaignFleetAPI fleetAPI, String factionId, String fleetType) 
    {

        log.debug("editing " + fleetAPI.getFullName());
        FleetInfo info = getInfo(fleetAPI);
        //log.debug(info.toString());

        if(info.isStationFleet) {
            log.debug("edit failed, station");
            return null;
        }

        FleetComposition compInfo = pickFleet(info, factionId, fleetType);
        if(compInfo == null) {
            log.debug("fleet not edited");
            return null;
        }

        log.debug("changing to " + compInfo.id);
        clearMembers(fleetAPI);
        createFleet(fleetAPI, info, compInfo);
        editCommander(info.captain, compInfo);

        return compInfo.id;
    }

    public static void setProperCr(CampaignFleetAPI fleetAPI) {
        for(FleetMemberAPI memberAPI : fleetAPI.getMembersWithFightersCopy()) {
            RepairTrackerAPI repairs = memberAPI.getRepairTracker();
            repairs.setCR(Math.max(repairs.getCR(), repairs.getMaxCR()));
        }
    }

    // store important info on fleet before editing
    private static class FleetInfo 
    {
        public final PersonAPI captain;
        public final Vector<PersonAPI> officers;
        public final int originalDP;
        public final Vector<FleetMemberAPI> mothballedShips;
        public final boolean isStationFleet;
        public final double averageSmods;

        public FleetInfo(PersonAPI Captain, Vector<PersonAPI> Officers, int TotalDp, 
        Vector<FleetMemberAPI> MothballedShips, boolean IsStationFleet, double AverageSmods)
        {
            captain = Captain;
            officers = Officers;
            originalDP = TotalDp;
            mothballedShips = MothballedShips;
            isStationFleet = IsStationFleet;
            averageSmods = AverageSmods;
        }

        @Override
        public String toString()
        {
            return "cap: " + captain + " officers: " + officers + " DP: " + originalDP + " smods: " + averageSmods;
        }
    }

    private FleetCompEditing() {} // do nothing
}

/*
    private void test()
    {
        SectorEntityToken yourFleet = Global.getSector().getPlayerFleet();
        LocationAPI currentSystem = (LocationAPI)yourFleet.getContainingLocation();
        List<CampaignFleetAPI> fleets = currentSystem.getFleets();
        for(CampaignFleetAPI fleet : fleets) {
            if(fleet != yourFleet) {
                Console.showMessage(fleet.getFullName() + " " + fleet.getInflater().getQuality());
            }
        }
    }
    
    private static void addKite(CampaignFleetAPI fleetAPI, FleetInfo inf)
    {
        FleetMemberAPI ship = Global.getFactory().createFleetMember(FleetMemberType.SHIP, 
        Global.getSettings().getVariant("kite_hegemony_Interceptor").clone());
        ship.setCaptain(inf.captain);
        fleetAPI.getFleetData().addFleetMember(ship);
    }
    */