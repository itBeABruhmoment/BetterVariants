package better_variants.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

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

        
    }
}
