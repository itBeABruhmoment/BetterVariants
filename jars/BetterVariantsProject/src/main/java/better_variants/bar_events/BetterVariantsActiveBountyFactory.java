package better_variants.bar_events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.magiclib.bounty.ActiveBounty;
import org.magiclib.bounty.MagicBountyLoader;
import org.magiclib.util.MagicCampaign;
import variants_lib.data.CommonStrings;
import variants_lib.data.VariantsLibFleetFactory;
import variants_lib.data.VariantsLibFleetParams;

import java.util.ArrayList;

public class BetterVariantsActiveBountyFactory {

    private static final Logger log = Global.getLogger(BetterVariantsActiveBountyFactory.class);
    static {
        log.setLevel(Level.ALL);
    }

    private static final ArrayList<String> AVOID_SYSTEM_TAGS = new ArrayList() {{
        add(Tags.THEME_REMNANT_MAIN); add(Tags.THEME_REMNANT_RESURGENT); add(Tags.THEME_REMNANT_SECONDARY);
    }};

    public String bountyKey = null;
    public String faction = Factions.INDEPENDENT;
    public int fleetPoints = 100;
    public long seed = System.currentTimeMillis();



    /**
     *
     * @return An active bounty, or null if one cannot be created
     */
    public ActiveBounty createActiveBounty() {
        /*
        if(key == null) {
            log.error("bounty key null");
            return null;
        }

        SectorEntityToken suitableTargetLocation = MagicCampaign.findSuitableTarget(
                null,
                null,
                "FAR",
                null,
                AVOID_SYSTEM_TAGS,
                null,
                true,
                false,
                Global.getSettings().isDevMode()
        );

        if (suitableTargetLocation == null) {
            log.error("No suitable spawn location could be found for bounty");
            return null;
        }

        final VariantsLibFleetParams fleetParams = new VariantsLibFleetParams();
        fleetParams.averageSMods = 0;
        fleetParams.faction = faction;
        fleetParams.fleetName = Global.getSector().getFaction(faction).getDisplayName() + " Deserters";
        fleetParams.fleetPoints = fleetPoints;
        fleetParams.fleetType = "bvExoticBounty";
        fleetParams.averageOfficerLevel = 5;
        fleetParams.numOfficers = 10;
        fleetParams.quality = 1.0f;
        fleetParams.seed = seed;
        final VariantsLibFleetFactory fleetFactory = VariantsLibFleetFactory.pickFleetFactory(fleetParams);
        if(fleetFactory == null) {
            log.error("No suitable fleet factory for bounty");
            return null;
        }
        final CampaignFleetAPI fleetAPI = fleetFactory.createFleet(fleetParams);
        // fleetAPI.addTag(MagicBountyLoader.BOUNTY_FLEET_TAG); // not sure if this is a good idea
        fleetAPI.addTag(bountyKey);
        return new ActiveBounty(bountyKey, fleetAPI, suitableTargetLocation, new ArrayList<String>(), spec);
        */
        return null;
    }

}
