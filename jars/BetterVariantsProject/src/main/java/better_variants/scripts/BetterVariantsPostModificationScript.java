package better_variants.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.FleetInflater;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
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
        final MemoryAPI fleetMem = fleet.getMemoryWithoutUpdate();

        if(!SettingsData.fleetEditingEnabled()) {
            return false;
        }

        if(fleetMem.contains(better_variants.data.CommonStrings.DO_NOT_MODIFY_FLEET)) {
            log.info("do not edit flag set");
            return false;
        }

        if(fleetMem.contains(CommonStrings.VARIANTS_LIB_LISTENER_APPLIED)
                && fleetMem.getLong(CommonStrings.VARIANTS_LIB_LISTENER_APPLIED) > 0) {
            log.info("fleet already edited");
            return false;
        }

        // don't modify fleets from unregistered factions
        if(!FactionData.FACTION_DATA.containsKey(fleet.getFaction().getId())) {
            log.info("refused to modify fleet because faction is not registered");
            return false;
        }

        // don't modify special/important fleets
        for(String flag : DISALLOW_FLEET_MODS_FLAGS) {
            if(fleetMem.contains(flag)) {
                log.info("refused to modify because fleet had the flag " + flag);
                return false;
            }
        }

        for(FleetMemberAPI member : fleet.getMembersWithFightersCopy()) {
            if(member.isStation()) {
                log.debug("refused to modify because fleet had a station");
                return false;
            }
        }

        return true;
    }

    @Override
    public void run(CampaignFleetAPI fleet) {
        final MemoryAPI fleetMemory = fleet.getMemoryWithoutUpdate();
        if(!allowFleetModification(fleet)) {
            return;
        }

        final long seed  = fleetMemory.getLong(MemFlags.SALVAGE_SEED);
        final Random rand = new Random(seed);

        //fleet.setInflater(null);

        final String faction = fleet.getFaction().getId();
        final OfficerFactory officerFactory = new OfficerFactory();
        for(final FleetMemberAPI memberAPI : fleet.getMembersWithFightersCopy()) {
            final ShipVariantAPI originalVariant = ModdedVariantsData.getVariant(memberAPI.getVariant().getOriginalVariant());
            if(originalVariant != null) {
                memberAPI.setVariant(originalVariant, false, true);
            }

            final PersonAPI officer = memberAPI.getCaptain();
            if(Util.isOfficer(officer)) {
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

        final BetterVariantsFleetInflater inflater = createInflater(fleet, seed);
        if(inflater != null) {
            fleet.setInflater(inflater);
            fleet.inflateIfNeeded();
        } else {
            log.info("inflater not created");
        }

        debugKey(fleetMemory, "$better_variants_inflater_added");
    }

    private static BetterVariantsFleetInflater createInflater(CampaignFleetAPI fleet, long seed) {
        final FleetInflater unknownFleetInflater = fleet.getInflater();
        if(unknownFleetInflater instanceof DefaultFleetInflater) {
            DefaultFleetInflater inflater = (DefaultFleetInflater) unknownFleetInflater;
            int averageSMods = 0;
            try {
                DefaultFleetInflaterParams inflaterParams = (DefaultFleetInflaterParams)inflater.getParams();
                averageSMods = inflaterParams.averageSMods;
            } catch(Exception e) {
                log.info("could not get average smods defaulting to none");
            }

            float quality = inflater.getQuality();
            fleet.setInflated(true);

            DefaultFleetInflaterParams inflaterParams = null;
            final Object tempInflaterParams = inflater.getParams();
            if(tempInflaterParams instanceof DefaultFleetInflaterParams) {
                inflaterParams = (DefaultFleetInflaterParams) tempInflaterParams;
            } else {
                inflaterParams = new DefaultFleetInflaterParams();
                inflaterParams.factionId = fleet.getFaction().getId();
                inflaterParams.seed = seed;
            }
            return new BetterVariantsFleetInflater(inflaterParams, quality, averageSMods);
        } else if(unknownFleetInflater == null) {
            final DefaultFleetInflaterParams inflaterParams = new DefaultFleetInflaterParams();
            inflaterParams.factionId = fleet.getFaction().getId();
            inflaterParams.seed = seed;
            fleet.setInflater(new BetterVariantsFleetInflater(inflaterParams, 1.0f, 0.0f));
        }
        return null;
    }

    private void debugKey(final MemoryAPI memoryAPI, final String str) {
        memoryAPI.set("$bv_gen_test_info", str);
    }
}
