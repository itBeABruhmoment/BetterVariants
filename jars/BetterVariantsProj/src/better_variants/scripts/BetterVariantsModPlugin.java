package better_variants.scripts;

import better_variants.data.FactionData;
import better_variants.data.VariantData;
import better_variants.bar_events.BetterVariantsBarEventCreator;
import better_variants.bar_events.TestEvent;
import better_variants.data.CommonStrings;
import better_variants.data.FleetBuildData;
import better_variants.data.SettingsData;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager.GenericBarEventCreator;

import data.scripts.bounty.MagicBountyData;
import data.scripts.bounty.MagicBountyData.bountyData;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONException;

public class BetterVariantsModPlugin extends BaseModPlugin {
    private static final Logger log = Global.getLogger(better_variants.scripts.BetterVariantsModPlugin.class);
    static {
        log.setLevel(Level.ALL);
    }

    @Override
    public void onApplicationLoad() throws IOException, JSONException, Exception
    {
        log.debug(CommonStrings.MOD_ID + ": loading settings");
        SettingsData.loadSettings();
        log.debug(CommonStrings.MOD_ID + ": loading faction data");
        FactionData.loadData();
        log.debug(CommonStrings.MOD_ID + ": loading variant data");
        VariantData.loadData();
        log.debug(CommonStrings.MOD_ID + ": loading fleet build data");
        FleetBuildData.loadData();
    }

    @Override
    public void onGameLoad(boolean newGame)
    {
        log.debug(CommonStrings.MOD_ID + ": adding listener");
        Global.getSector().addTransientListener(new BetterVariantsListener(false));
        log.debug(CommonStrings.MOD_ID + ": initializing faction aggression values");
        UnofficeredPersonalitySetPlugin.innitDefaultAggressionValues();
        log.debug(CommonStrings.MOD_ID + ": adding bar event manager");
        BarEventManager barEventManager = BarEventManager.getInstance();

        // If the prerequisites for the quest have been met (optional) and the game isn't already aware of the bar event,
        // add it to the BarEventManager so that it shows up in bars
        if (!barEventManager.hasEventCreator(BetterVariantsBarEventCreator.class)) {
            barEventManager.addEventCreator(new BetterVariantsBarEventCreator());
        }
        
        // BountyData.addBounty("bv_test", 999999.0f);
        // runcode com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager.getInstance().addEventCreator(new better_variants.bar_events.BetterVariantsBarEventCreator());
    }
}
