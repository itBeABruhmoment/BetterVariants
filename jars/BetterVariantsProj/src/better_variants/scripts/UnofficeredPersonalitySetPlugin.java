package better_variants.scripts;

import java.util.List;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lazywizard.console.Console;

import better_variants.data.BetterVariantsTags;
import better_variants.data.CommonStrings;
import better_variants.data.FactionData;
import better_variants.data.FleetBuildData;
import better_variants.data.VariantData;
import better_variants.scripts.fleetedit.FleetCompEditing;
import better_variants.scripts.fleetedit.OfficerEditing;

import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI.SkillLevelAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.campaign.FactionAPI;

/*
for(ShipAPI ship: Global.getCombatEngine().getShips()) {
    try{
        Console.showMessage(ship.getCaptain().getPersonalityAPI().getId() + ship.getHullSpec().getHullId());
    } catch(Exception e) {

    }
}


for(FleetMemberAPI ship: Global.getCombatEngine().getContext().getOtherFleet().getMembersWithFightersCopy()) {
    try{
        Console.showMessage(ship.getFleetData().getFleet().getFullName() + " " + ship.getHullId());
    } catch(Exception e) {

    }
}
*/

public class UnofficeredPersonalitySetPlugin implements EveryFrameCombatPlugin {
    private static final Logger log = Global.getLogger(better_variants.scripts.UnofficeredPersonalitySetPlugin.class);
    static {
        log.setLevel(Level.ALL);
    }

    public static HashMap<String, Integer> FACTION_DEFAULT_AGRESSION;

    private static final HashMap<String, Integer> AGRESSION = new HashMap<String, Integer>() {{
        put(Personalities.CAUTIOUS, 1);     put(Personalities.TIMID, 2);    put(Personalities.STEADY, 3);
        put(Personalities.AGGRESSIVE, 4);   put(Personalities.RECKLESS, 5);
    }};
    /*
    static void test()
    {
        for(FleetMemberAPI memberAPI : Global.getCombatEngine().getContext().getOtherFleet().getMembersWithFightersCopy()) {
            try {
                Console.showMessage(memberAPI.getHullId() + " " + memberAPI.getCaptain().getPersonalityAPI().getId());
            } catch(Exception e) {

            }
        }
    }
    */

    public static void innitDefaultAggressionValues()
    {
        log.debug("resetting faction aggression to defaults");
        FACTION_DEFAULT_AGRESSION = new HashMap<String, Integer>();
        for(FactionAPI faction : Global.getSector().getAllFactions()) {
            FACTION_DEFAULT_AGRESSION.put(faction.getId(), faction.getDoctrine().getAggression());
        }
    }

    @Override
    public void init(CombatEngineAPI combatEngine) {
        CampaignFleetAPI enemyFleet = null;
        String fleetWidePersonality = null;

        if(combatEngine == null) {
            log.debug(CommonStrings.MOD_ID + ": combat engine null");
            return;
        }
        if(Global.getCurrentState() == GameState.TITLE) {
            log.debug(CommonStrings.MOD_ID + ": title");
            return;
        }
        enemyFleet = combatEngine.getContext().getOtherFleet();
        if(enemyFleet == null) {
            log.debug(CommonStrings.MOD_ID + ": enemy fleet null(huh?)");
            return;
        }

        if(enemyFleet.getMemoryWithoutUpdate().contains(CommonStrings.FLEET_VARIANT_KEY)) {
            String fleetType = enemyFleet.getMemoryWithoutUpdate().getString(CommonStrings.FLEET_VARIANT_KEY);
            fleetWidePersonality = FleetBuildData.FLEET_DATA.get(fleetType).defaultFleetWidePersonality;
            log.debug(CommonStrings.MOD_ID + ": fleet is of type " + fleetType + " with personality " + fleetWidePersonality);
        } else {
            String factionId = enemyFleet.getFaction().getId();
            if(factionId != null && FACTION_DEFAULT_AGRESSION.containsKey(factionId)) {
                enemyFleet.getFaction().getDoctrine().setAggression(FACTION_DEFAULT_AGRESSION.get(factionId));
            }
            log.debug(CommonStrings.MOD_ID + ": fleet has no default personality, set to default");
            return;
        }
        if(fleetWidePersonality == null) {
            String factionId = enemyFleet.getFaction().getId();
            if(factionId != null && FACTION_DEFAULT_AGRESSION.containsKey(factionId)) {
                enemyFleet.getFaction().getDoctrine().setAggression(FACTION_DEFAULT_AGRESSION.get(factionId));
            }
            log.debug(CommonStrings.MOD_ID + ": fleet has no default personality, set to default");
            return;
        }

        if(AGRESSION.containsKey(fleetWidePersonality)) {
            log.debug(CommonStrings.MOD_ID + ": setting aggression to \"" + fleetWidePersonality + "\"");
            int agressionValue = AGRESSION.get(fleetWidePersonality);
            enemyFleet.getFaction().getDoctrine().setAggression(agressionValue);
        } else {
            log.debug(CommonStrings.MOD_ID + ": combat script not run, personality \"" + fleetWidePersonality + "\" not registered");
            return;
        }
        
    }

    @Override
    public void advance(float amount, List<InputEventAPI> arg1) {
        // do nothing
    }

    @Override
    public void processInputPreCoreControls(float arg0, List<InputEventAPI> arg1) {
        // do nothing
    }

    @Override
    public void renderInUICoords(ViewportAPI arg0) {
        // do nothing 
    }

    @Override
    public void renderInWorldCoords(ViewportAPI arg0) {
        // do nothing
    }
    
}