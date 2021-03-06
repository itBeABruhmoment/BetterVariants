package better_variants.scripts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import better_variants.data.BetterVariantsTags;
import better_variants.data.CommonStrings;
import better_variants.data.FactionData;
import better_variants.data.SettingsData;
import better_variants.data.VariantData;
import better_variants.scripts.fleetedit.FleetCompEditing;
import better_variants.scripts.fleetedit.OfficerEditing;

import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetInflater;


/*
testing commands:

runcode
for(String str : (String[]) data.BetterVariants_FactionData.FACTION_DATA.get("luddic_church")) {
    Console.showMessage(str);
}

runcode
for(String str : (String[]) data.BetterVariants_VariantData.VARIANT_DATA.get("afflictor_d_pirates_Strike_bv")) {
    Console.showMessage("|" + str + "|");
}
*/


public class FleetRandomizer {
    private static final Logger log = Global.getLogger(better_variants.scripts.FleetRandomizer.class);
    static {
        log.setLevel(Level.ALL);
    }

    public static final String HAS_BEEN_MODIFYED_KEY = "$bvm";
    public boolean scriptEnded = false;
    private static final HashSet<String> DISALLOW_FLEET_MODS_FLAGS = new HashSet<String>() {{
        add(MemFlags.ENTITY_MISSION_IMPORTANT); add(MemFlags.MEMORY_KEY_MISSION_IMPORTANT); add(MemFlags.STORY_CRITICAL);
        add(MemFlags.STATION_BASE_FLEET);       add(MemFlags.STATION_FLEET);
    }};
    
    private static HashMap<String, FleetInflater> inflators = new HashMap<String, FleetInflater>();
    private static final Random rand = new Random();

    private static String getFleetType(CampaignFleetAPI fleet)
    {
        String type = fleet.getMemoryWithoutUpdate().getString(MemFlags.MEMORY_KEY_FLEET_TYPE);
        if(type == null) {
            type = "";
        }
        return type;
    }

    private static boolean allowModificationFleet(CampaignFleetAPI fleet)
    {
        
        // don't modify fleets from unregistered factions
        if(!FactionData.FACTION_DATA.containsKey(fleet.getFaction().getId())) {
            log.debug("refused to modify fleet because faction is not registered");
            return false;
        }

        // don't modify special/important fleets
        for(String flag : DISALLOW_FLEET_MODS_FLAGS) {
            if(fleet.getMemoryWithoutUpdate().contains(flag)) {
                log.debug("refused to modify because fleet had the flag " + flag);
                return false;
            }
        }

        return true;
    }

    

    public static void modify(CampaignFleetAPI fleet)
    {
        // disable autofit
        String factionId = fleet.getFaction().getId();

        FleetInflater inflater = fleet.getInflater();
        if(!SettingsData.autofitEnabled()
        && FactionData.FACTION_DATA.get(factionId) != null 
        && FactionData.FACTION_DATA.get(factionId).hasTag("no_autofit")
        && inflater instanceof DefaultFleetInflater
        && !(inflater instanceof NoAutofitFleetInflater)) {
            fleet.setInflater(new NoAutofitFleetInflater((DefaultFleetInflater) inflater, rand));
        }

        
        if(fleet.getMemoryWithoutUpdate().contains(HAS_BEEN_MODIFYED_KEY)) {
            return;
        }

        log.debug("trying to modify " + fleet.getFullName());
        fleet.getMemoryWithoutUpdate().set(HAS_BEEN_MODIFYED_KEY, true);
        if(!allowModificationFleet(fleet)) {
            log.debug("modification barred");
            return;
        }

        String fleetCompId = FleetCompEditing.editFleet(fleet, fleet.getFaction().getId(), getFleetType(fleet));
        if(fleetCompId != null) {
            fleet.getMemoryWithoutUpdate().set(CommonStrings.FLEET_VARIANT_KEY, fleetCompId);
        }

        // edit officers of fleet
        if(SettingsData.OfficerEditingEnabled()) {
            OfficerEditing.editAllOfficers(fleet, fleetCompId);
        }

        FleetCompEditing.setProperCr(fleet);
    }

    public static boolean alreadyModified(CampaignFleetAPI fleet)
    {
        return fleet.getMemoryWithoutUpdate().contains(HAS_BEEN_MODIFYED_KEY);
    }

    private FleetRandomizer() {}
}
/*
SectorEntityToken yourFleet = Global.getSector().getPlayerFleet();
        LocationAPI currentSystem = (LocationAPI)yourFleet.getContainingLocation();
        List<CampaignFleetAPI> fleets = currentSystem.getFleets();
        for(CampaignFleetAPI fleetAPI : fleets) {
            try {
                Console.showMessage(fleetAPI.getInflater());
                Console.showMessage(fleetAPI.getFullName() + " " + fleetAPI.getInflater().getQuality());
            } catch(Exception e) {

            }
            
        }
*/