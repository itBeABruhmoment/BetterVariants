package better_variants.bar_events;

import better_variants.data.CommonStrings;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBDeserter;
import com.fs.starfarer.api.impl.campaign.missions.cb.MilitaryCustomBounty;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionFleetAutoDespawn;
import com.fs.starfarer.api.impl.campaign.missions.hub.MissionTrigger;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;
import variants_lib.data.FleetBuildData;
import variants_lib.data.VariantsLibFleetFactory;
import variants_lib.data.VariantsLibFleetParams;

import java.util.Random;

public class BetterVariantsBounty extends MilitaryCustomBounty {
    private static final Logger log = Global.getLogger(better_variants.bar_events.BetterVariantsBounty.class);
    static {
        log.setLevel(Level.ALL);
    }

    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
        log.info("BetterVariantsBountyCreate");
        return super.create(createdAt, barEvent);
    }

    public static class BetterVariantsDeserterBountyCreator extends CBDeserter {

    }

    // TODO: figure out if hyperspace location is important
    public static class BetterVariantsCreateFleetAction implements MissionTrigger.TriggerAction {
        private static final Logger log = Global.getLogger(better_variants.bar_events.BetterVariantsBounty.BetterVariantsCreateFleetAction.class);
        static {
            log.setLevel(Level.ALL);
        }

        public String fleetId = "";
        public VariantsLibFleetParams params = new VariantsLibFleetParams();
        public long seed = 0;
        public Vector2f locInHyper = new Vector2f(0, 0);

        BetterVariantsCreateFleetAction(String fleetId, VariantsLibFleetParams params, long seed, Vector2f locInHyper) {
            this.fleetId = fleetId;
            this.params = params;
            this.seed = seed;
            this.locInHyper = locInHyper;
        }

        @Override
        public void doAction(MissionTrigger.TriggerActionContext context) {
            final Random rand = new Random(seed);
            final VariantsLibFleetFactory fleetFactory = FleetBuildData.FLEET_DATA.get(fleetId);
            if(fleetFactory == null) {
                log.info(String.format("%s:no fleet factory with the id \"%s\" could be found", CommonStrings.MOD_ID, fleetId));
                return;
            }

            final CampaignFleetAPI bountyFleet = fleetFactory.createFleet(params);
            context.fleet = bountyFleet;
            context.fleet.setFacing(rand.nextFloat() * 360.0f);
            context.fleet.getMemoryWithoutUpdate().set("$core_fleetBusy", Boolean.TRUE);
            context.allFleets.add(bountyFleet);
            if (!context.fleet.hasScriptOfClass(MissionFleetAutoDespawn.class)) {
                context.fleet.addScript(new MissionFleetAutoDespawn(context.mission, context.fleet));
            }
        }
    }
}
