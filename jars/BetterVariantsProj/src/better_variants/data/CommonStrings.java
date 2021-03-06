package better_variants.data;

import java.util.HashSet;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;

// stores important strings
public class CommonStrings {
    public static final String VARIANT_TAGS_CSV_PATH = "data/bettervariants/variant_tags.csv";
    public static final String FACTION_TAGS_CSV_PATH = "data/bettervariants/faction_tags.csv";
    public static final String FLEETS_CSV_PATH = "data/bettervariants/fleets/fleets.csv";
    public static final String FLEETS_FOLDER_PATH = "data/bettervariants/fleets/";
    public static final String MOD_ID = "better_variants";
    public static final String DO_NOT_CHANGE_PERSONALITY_KEY = "$bvNoPersonalityChange";
    public static final String MODIFIED_IN_BATTLE_KEY = "$bvModifiedInBattle";
    public static final String FLEET_VARIANT_KEY = "$bvType";
    public static final HashSet<String> PERSONALITIES =  new HashSet<String>() {{
        add(Personalities.AGGRESSIVE);  add(Personalities.CAUTIOUS); add(Personalities.RECKLESS);
        add(Personalities.STEADY);      add(Personalities.TIMID);
    }};
    public static final String NEX_VENGANCE_FLEET = "vengeanceFleet";
    public static final String NEX_INVASION_FLEET = "exerelinInvasionFleet";
    public static final String NEX_INVASION_SUPPORT_FLEET = "exerelinInvasionSupportFleet";
    public static final String NEX_SUPPRESSION_FLEET = "nex_suppressionFleet";
    public static final String NEX_SPECIAL_FORCES_FLEET = "nex_specialForces";
    public static final String NEX_RELIEF_FLEET = "nex_reliefFleet";
    public static final String NEX_RESPONSE_FLEET =  "exerelinResponseFleet";
    public static final String NEX_VULTURE_FLEET =  "nex_vultureFleet";
    public static final String NEX_MINING_FLEET =  "exerelinMiningFleet";

    private CommonStrings() {}
}
