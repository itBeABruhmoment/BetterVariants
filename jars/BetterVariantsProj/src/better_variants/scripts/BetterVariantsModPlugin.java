package better_variants.scripts;

import better_variants.data.FactionData;
import better_variants.data.VariantData;
import better_variants.data.CommonStrings;
import better_variants.data.FleetBuildData;

import java.io.IOException;
import javax.xml.bind.JAXBException;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;

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
        Global.getSector().addListener(new BetterVariantsListener(false));
        log.debug(CommonStrings.MOD_ID + ": initializing faction aggression values");
        UnofficeredPersonalitySetPlugin.innitDefaultAggressionValues();
    }
}
