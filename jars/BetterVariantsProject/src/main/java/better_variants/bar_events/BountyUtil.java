package better_variants.bar_events;

public class BountyUtil {
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
}
