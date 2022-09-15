package better_variants.scripts;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAI;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetAssignmentAIWrapper;
import com.fs.starfarer.api.impl.campaign.fleets.EconomyFleetRouteManager;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager;

import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lazywizard.console.Console;

import variants_lib.scripts.FleetEditingScript;

public class ConvoyExpander implements FleetEditingScript{
    private static final Logger log = Global.getLogger(better_variants.scripts.ConvoyExpander.class);
    static {
        log.setLevel(Level.ALL);
    }

    @Override
    public void run(CampaignFleetAPI fleet) 
    {
        MemoryAPI fleetMemory = fleet.getMemoryWithoutUpdate();
        if(fleetMemory.contains(variants_lib.data.CommonStrings.FLEET_EDITED_MEMKEY)) {
            return;
        }
        
        for(Object o : fleet.getScripts()) {
            log.debug(o);
        }
    }

    
}
/*
        //TODO: look into fleet spawned listener
        for(Object o : Global.getSector().getScripts())
        {
            Console.showMessage(o);
        }

        for(CampaignFleetAPI fleet1 : Global.getSector().getCurrentLocation().getFleets())
        {
            Console.showMessage(fleet1.getFullName());
            try {
                for(com.fs.starfarer.api.campaign.ai.FleetAssignmentDataAPI assignmentDataAPI : fleet1.getAssignmentsCopy()) {
                    Console.showMessage("1:" + assignmentDataAPI.getTarget().getName());
                    Console.showMessage("2:" + assignmentDataAPI.getAssignment().getDescription());
                }
            } catch(Exception e) {

            }
            try {
                for(EveryFrameScript s :fleet1.getScripts()) {
                    Console.showMessage(s);
                }
            }catch(Exception e) {

            }
        }
 */
