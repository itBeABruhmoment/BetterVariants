package better_variants.bar_events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

import java.util.ArrayList;

public class BountyUtil {
    public static final ArrayList<String> TARGET_FACTIONS = new ArrayList<String>() {{
        add(Factions.HEGEMONY); add(Factions.LUDDIC_CHURCH); add(Factions.DIKTAT); add(Factions.LUDDIC_PATH);
        add(Factions.PERSEAN); add(Factions.INDEPENDENT); add(Factions.TRITACHYON);
    }};

    public static int fpByDifficulty(int difficulty) {
        return 100 + difficulty * 50;
    }

    public static float avgOfficerLevelByDifficulty(int difficulty) {
        float avgLevel = 1.0f + (5.0f / 7) * difficulty;
        if(avgLevel > 6.0f) {
            avgLevel = 6.0f;
        }
        return avgLevel;
    }

    public static int maxOfficersByDifficulty(int difficulty) {
        return 10 + difficulty * 2;
    }

    public static float qualityByDifficulty(int difficulty) {
        float quality = 0.5f + 0.1f * difficulty;
        if(quality > 1.0f) {
            quality = 1.0f;
        }
        return quality;
    }

    public static ArrayList<String> getFactionsWithRelation(
            final String factionId, final RepLevel atLeast, final RepLevel atMost
    ) {
        final ArrayList<String> filteredTargetFactions = new ArrayList<>(10);
        for(final String faction : TARGET_FACTIONS) {
            final RepLevel rel = Global.getSector().getFaction(faction).getRelationshipLevel(factionId);
            if(!faction.equals(factionId) && rel.isAtBest(atMost) && rel.isAtWorst(atLeast)) {
                filteredTargetFactions.add(faction);
            }
        }
        return filteredTargetFactions;
    }

    public static boolean isFactionWithBounties(final String factionId) {
        return TARGET_FACTIONS.contains(factionId);
    }
}
