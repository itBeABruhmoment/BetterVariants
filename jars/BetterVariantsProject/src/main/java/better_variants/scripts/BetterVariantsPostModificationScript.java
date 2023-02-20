package better_variants.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import variants_lib.data.*;
import variants_lib.scripts.FleetBuildingUtils;
import variants_lib.scripts.FleetEditingScript;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;

import java.util.HashSet;
import java.util.Random;

public class BetterVariantsPostModificationScript implements FleetEditingScript {
    private static final Logger log = Global.getLogger(better_variants.scripts.BetterVariantsPostModificationScript.class);
    static {
        log.setLevel(Level.ALL);
    }

    private static final HashSet<String> DISALLOW_FLEET_MODS_FLAGS = new HashSet<String>() {{
        add(MemFlags.ENTITY_MISSION_IMPORTANT); add(MemFlags.STORY_CRITICAL);
        add(MemFlags.STATION_BASE_FLEET);       add(MemFlags.STATION_FLEET);
    }};

    private static boolean allowFleetModification(CampaignFleetAPI fleet) {
        // don't modify fleets from unregistered factions
        FactionData.FactionConfig factionConfig = FactionData.FACTION_DATA.get(fleet.getFaction().getId());
        if(factionConfig == null) {
            return false;
        }

        if(!factionConfig.hasTag(CommonStrings.NO_AUTOFIT_TAG)) {
            return false;
        }

        // don't modify special/important fleets
        for(String flag : DISALLOW_FLEET_MODS_FLAGS) {
            if(fleet.getMemoryWithoutUpdate().contains(flag)) {
                return false;
            }
        }

        for(FleetMemberAPI member : fleet.getMembersWithFightersCopy()) {
            if(member.isStation()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void run(CampaignFleetAPI fleet) {
        final MemoryAPI fleetMemory = fleet.getMemoryWithoutUpdate();
        if(!fleetMemory.contains(CommonStrings.FLEET_VARIANT_KEY)) {
            return;
        }

        Random rand = new Random(fleetMemory.getLong(MemFlags.SALVAGE_SEED));
        if(!fleetMemory.contains(CommonStrings.NO_AUTOFIT_APPLIED)) {
            int averageSMods = 0;
            try {
                DefaultFleetInflaterParams inflaterParams = (DefaultFleetInflaterParams)fleet.getInflater().getParams();
                averageSMods = inflaterParams.averageSMods;
            } catch(Exception e) {
                log.info("could not get average smods defaulting to none");
            }

            float quality = 0.0f;
            try {
                quality = fleet.getInflater().getQuality();
            } catch(Exception e) {
                log.info("could not get quality defaulting to max");
            }
            fleet.setInflated(true);
            FleetBuildingUtils.addDMods(fleet, rand, quality);
            FleetBuildingUtils.addSMods(fleet, rand, averageSMods);
        }

        final String faction = fleet.getFaction().getId();
        final OfficerFactory officerFactory = new OfficerFactory();
        for(final FleetMemberAPI memberAPI : fleet.getMembersWithFightersCopy()) {
            final PersonAPI officer = memberAPI.getCaptain();
            if(officer != null && officer.getStats().getLevel() == 0) {
                final String variant = memberAPI.getVariant().getOriginalVariant();
                OfficerFactoryParams officerFactoryParams = new OfficerFactoryParams(
                        variant,
                        faction,
                        rand,
                        5
                );
                officerFactoryParams.level = officer.getStats().getLevel();
                officerFactory.editOfficer(officer, officerFactoryParams);
            }
        }
    }
}
