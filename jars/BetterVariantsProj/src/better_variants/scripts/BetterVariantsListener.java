package better_variants.scripts;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCampaignEventListener;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CommDirectoryAPI;
import com.fs.starfarer.api.campaign.CommDirectoryEntryAPI;
import com.fs.starfarer.api.campaign.CommDirectoryEntryAPI.EntryType;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;

import java.util.HashMap;
import java.util.List;

import better_variants.scripts.FleetRandomizer;


import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class BetterVariantsListener extends BaseCampaignEventListener{
    private static final Logger log = Global.getLogger(better_variants.scripts.BetterVariantsListener.class);
    static {
        log.setLevel(Level.ALL);
    }

    public BetterVariantsListener(boolean permaRegister) {
        super(permaRegister);
    }
    
    @Override
    public void reportFleetSpawned(CampaignFleetAPI fleet) {
        FleetRandomizer.modify(fleet);
    }
}
