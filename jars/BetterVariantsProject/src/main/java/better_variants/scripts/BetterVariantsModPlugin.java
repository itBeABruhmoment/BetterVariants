package better_variants.scripts;

import better_variants.data.BetterVariantsBountyData;
import better_variants.data.CommonStrings;
import better_variants.data.SettingsData;

import java.io.IOException;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;

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
        log.info(CommonStrings.MOD_ID + ": loading bounty data");
        //BountyCreationData.loadData();
        log.info(CommonStrings.MOD_ID + ": loading settings");
        SettingsData.loadSettings();
        log.info(CommonStrings.MOD_ID + ":loading bounties");
        BetterVariantsBountyData.getInstance().loadData();
    }

    @Override
    public void onGameLoad(boolean newGame)
    {
        //log.debug(CommonStrings.MOD_ID + ": adding bar event manager");
        //BarEventManager barEventManager = BarEventManager.getInstance();

        // If the prerequisites for the quest have been met (optional) and the game isn't already aware of the bar event,
        // add it to the BarEventManager so that it shows up in bars
        //if (!barEventManager.hasEventCreator(BetterVariantsBarEventCreator.class)) {
        //    barEventManager.addEventCreator(new BetterVariantsBarEventCreator());
        //}
    }


}
