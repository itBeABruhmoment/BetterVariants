package better_variants.bar_events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

import java.nio.ByteBuffer;
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
        float quality = 0.6f + 0.1f * difficulty;
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

    public static long createSeedForBounty(MarketAPI createdAt, int changeSeed) {
        // build byte buffer
        final CampaignClockAPI clock = Global.getSector().getClock();
        final ByteBuffer bytesForSeed = ByteBuffer.allocate(8 + 4 * 3);
        bytesForSeed.putInt(changeSeed);
        bytesForSeed.putLong(createdAt.getPrimaryEntity().getMemoryWithoutUpdate().getLong("$salvageSeed"));
        bytesForSeed.putInt(clock.getCycle());
        bytesForSeed.putInt(clock.getMonth());

        // hash to get a singular long
        final byte[] bytesForSeedArr = bytesForSeed.array();
        final long fnv1Init = 0xcbf29ce484222325L;
        final long fnv1Prime = 1099511628211L;
        long hash = fnv1Init;
        for(int i = 0; i < bytesForSeedArr.length; i++) {
            hash ^= (bytesForSeedArr[i] & 0xff);
            hash *= fnv1Prime;
        }

        return hash;
    }
}
