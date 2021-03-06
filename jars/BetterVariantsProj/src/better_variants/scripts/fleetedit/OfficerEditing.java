package better_variants.scripts.fleetedit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;

import com.fs.starfarer.api.impl.campaign.skills.WolfpackTactics;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import org.apache.log4j.Logger;
import org.lazywizard.console.Console;

import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI.SkillLevelAPI;

import better_variants.data.BetterVariantsTags;
import better_variants.data.CommonStrings;
import better_variants.data.FactionData;
import better_variants.data.FleetBuildData;
import better_variants.data.VariantData;

import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.campaign.FactionAPI;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class OfficerEditing {

    private static final Logger log = Global.getLogger(better_variants.scripts.fleetedit.OfficerEditing.class);
    static {
        log.setLevel(Level.ALL);
    }

    // the skill editing tags mapped to their corresponding skill Id's
    private static final HashMap<String, String> SKILL_EDIT_TAGS = new HashMap<String, String>() {{
        put("hs", "helmsmanship");          put("ce", "combat_endurance");          put("im", "impact_mitigation");
        put("dc", "damage_control");        put("fm", "field_modulation");          put("pd", "point_defense");
        put("ta", "target_analysis");       put("bm", "ballistic_mastery");         put("se", "systems_expertise");
        put("ms", "missile_specialization");put("gi", "gunnery_implants");          put("ew", "energy_weapon_mastery");
        put("oa", "ordnance_expert");       put("pa", "polarized_armor");
    }};

    // the personality editing tags mapped to their corresponding personality Id's
    private static final HashMap<String, String> PERSONALITY_EDIT_TAGS =  new HashMap<String, String>() {{
        put("ca", Personalities.CAUTIOUS);         put("ti", Personalities.TIMID);          put("st", Personalities.STEADY);
        put("ag", Personalities.AGGRESSIVE);       put("re", Personalities.RECKLESS);
    }};
    
    // used to store skills that can be changed
    private static final HashSet<String> MODIFYABLE_SKILLS = new HashSet<String>() {{
        add("helmsmanship");        add("combat_endurance");    add("impact_mitigation");   add("damage_control"); 
        add("field_modulation");    add("point_defense");       add("target_analysis");     add("ballistic_mastery"); 
        add("systems_expertise");   add("missile_specialization"); add("gunnery_implants"); add("energy_weapon_mastery"); 
        add("ordnance_expert");     add("polarized_armor");
    }};

    private static boolean hasOfficer(FleetMemberAPI fleetMember) 
    {
        // there isn't a hasOfficer method in the API. Let's get creative!
        if(fleetMember.getCaptain().getStats().getLevel() <= 1) {
            return false;
        }
        return true;
    }

    // creates a array with the elements {0, 1, 2, ... , n-1}
    private static int[] intArrayFromSize(int n)
    {
        int[] intArr = new int[n];
        for(int i = 0; i < n; i++) {
            intArr[i] = i;
        }
        return intArr;
    }

    // stack overflow ctrl-c ctrl-v
    private static void shuffleArray(int[] ar)
    {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            int a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    private static void editSkills(Vector<String> skillEditQueue, FleetMemberAPI fleetMember)
    {
        MutableCharacterStatsAPI officerStatsAPI = fleetMember.getCaptain().getStats();
        List<SkillLevelAPI> officerSkills = officerStatsAPI.getSkillsCopy();

        // random indices for choosing random skills to replace
        int[] randomizedIntSeries = intArrayFromSize(officerSkills.size()); 
        shuffleArray(randomizedIntSeries);

        // run until there are no more skills to give or no more that can be modified
        int frontOfQueue = 0;
        int intArrIndex = 0;
        while(frontOfQueue < skillEditQueue.size() && intArrIndex < randomizedIntSeries.length) {
            // if the skill in the queue is already known move on to applying the next
            if(officerStatsAPI.hasSkill(skillEditQueue.get(frontOfQueue))) {
                frontOfQueue++;
            } else {
                // iterates until a skill is replaced by the skill specified by skillEditQueue.get(frontOfQueue) or no skills
                // that are allowed to be replaced are left
                while(intArrIndex < randomizedIntSeries.length) {
                    // randomly select a skill from the list to replace
                    String skillIdToOverride = officerSkills.get(randomizedIntSeries[intArrIndex]).getSkill().getId();
                    if(MODIFYABLE_SKILLS.contains(skillIdToOverride) && !skillEditQueue.contains(skillIdToOverride)) {
                        Boolean skillIsElite = officerStatsAPI.getSkillLevel(skillIdToOverride) > 1.0f;
                        officerStatsAPI.setSkillLevel(skillIdToOverride, 0.0f);
                        if(skillIsElite) {
                            officerStatsAPI.setSkillLevel(skillEditQueue.get(frontOfQueue), 2.0f);
                        } else {
                            officerStatsAPI.setSkillLevel(skillEditQueue.get(frontOfQueue), 1.0f);
                        }

                        intArrIndex++;
                        frontOfQueue++;
                        break;
                    } else {
                        // if the skill should not be modified try to change another
                        intArrIndex++;
                    }
                }
            }
        }
    }

    public static void editAllOfficers(CampaignFleetAPI fleet, String fleetCompId)
    {
        for(FleetMemberAPI member : fleet.getMembersWithFightersCopy()) {
            String variantId = VariantData.isRegisteredVariant(member);
            if(variantId != null) {
                OfficerEditing.editOfficer(member, variantId, fleetCompId);
            }
        }
    }
    
    public static void editOfficer(FleetMemberAPI fleetMember, String variantId, String fleetCompId)
    {
        if(!hasOfficer(fleetMember)) { // don't edit unofficered ships
            return;
        }
        
        try{
            Vector<String> tags = null;
            if(VariantData.VARIANT_DATA.containsKey(variantId)) {
                tags = VariantData.VARIANT_DATA.get(variantId);
            } else { // don't edit unregistered variants
                return;
            }

            if(fleetCompId != null) {
                fleetMember.getCaptain().setPersonality(FleetBuildData.FLEET_DATA.get(fleetCompId).defaultFleetWidePersonality);
            }

            Vector<String> skillEditQueue = new Vector<String>(10);
            for(String tag : tags) {
                if(tag.equals("no")) { // do not edit if this tag is present
                    return;
                } else if(PERSONALITY_EDIT_TAGS.containsKey(tag)) { // edit personality if personality editing tag is identified
                    fleetMember.getCaptain().setPersonality(PERSONALITY_EDIT_TAGS.get(tag));
                    // add flag to ensure it doesn't get overriden by an in battle feature
                    MemoryAPI memory = fleetMember.getCaptain().getMemoryWithoutUpdate();
                    if(memory != null && !memory.contains(CommonStrings.DO_NOT_CHANGE_PERSONALITY_KEY)) {
                        memory.set(CommonStrings.DO_NOT_CHANGE_PERSONALITY_KEY, true);
                    }
                } else if(SKILL_EDIT_TAGS.containsKey(tag)) { // put tags that require skill editing in a queue
                    skillEditQueue.add(SKILL_EDIT_TAGS.get(tag));
                }
            }
            editSkills(skillEditQueue, fleetMember);
        } catch(Exception e) {
            log.debug("failed to edit " + variantId + " !?!?!?!?!");
        }
    }

    private OfficerEditing() {} // do nothing
}


/*
testing commands 

runcode SectorEntityToken yourFleet = Global.getSector().getPlayerFleet();
        LocationAPI currentSystem = (LocationAPI)yourFleet.getContainingLocation();
        List<CampaignFleetAPI> fleets = currentSystem.getFleets();
        for(CampaignFleetAPI fleet : fleets) {
            List<FleetMemberAPI> members = fleet.getMembersWithFightersCopy();
            for(FleetMemberAPI member : members) {
                Console.showMessage(member.getVariant().getHullVariantId());
            }
        }

        List<FleetMemberAPI> members = Global.getSector().getPlayerFleet().getMembersWithFightersCopy();
        FleetMemberAPI member = (FleetMemberAPI) members.get(2);
        member.getCaptain().setPersonality(Personalities.RECKLESS);

        List<FleetMemberAPI> members = Global.getSector().getPlayerFleet().getMembersWithFightersCopy();
        Console.showMessage(members.get(2) == null);

        List<FleetMemberAPI> members = Global.getSector().getPlayerFleet().getMembersWithFightersCopy();
        Console.showMessage(((FleetMemberAPI)members.get(2)).getCaptain().getNameString().length());

runcode SectorEntityToken yourFleet = Global.getSector().getPlayerFleet();
        LocationAPI currentSystem = (LocationAPI)yourFleet.getContainingLocation();
        List<CampaignFleetAPI> fleets = currentSystem.getFleets();
        for(CampaignFleetAPI fleet : fleets) {
            Console.showMessage("**********");
            List<FleetMemberAPI> members = fleet.getMembersWithFightersCopy();
            for(FleetMemberAPI member : members) {
                Console.showMessage(member.getCaptain().getNameString().length());
            }
        }


        List<FleetMemberAPI> members = Global.getSector().getPlayerFleet().getMembersWithFightersCopy();
        FleetMemberAPI member = (FleetMemberAPI) members.get(14);
        Console.showMessage(member.getCaptain().getPersonalityAPI().getId());

        List<FleetMemberAPI> members = Global.getSector().getPlayerFleet().getMembersWithFightersCopy();
        FleetMemberAPI member = (FleetMemberAPI) members.get(0);
        for(MutableCharacterStatsAPI.SkillLevelAPI skill : member.getCaptain().getStats().getSkillsCopy()) {
            Console.showMessage(skill.getSkill().getId());
        }

        List<FleetMemberAPI> members = Global.getSector().getPlayerFleet().getMembersWithFightersCopy();
        FleetMemberAPI member = (FleetMemberAPI) members.get(0);
        //Console.showMessage(member.getCaptain().getStats().getSkillLevel("systems_expertise"));
        //member.getCaptain().getStats().increaseSkill("systems_expertise");
        member.getCaptain().getStats().decreaseSkill("systems_expertise");
*/
