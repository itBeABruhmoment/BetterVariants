package better_variants.data;

import com.fs.starfarer.api.impl.campaign.ids.Factions;

public class BetterVariantsBountyDataMember {
    private String fleetId = "";
    private float weight = 1.0f;
    private int minFleetPoints = 10;
    private int maxFleetPoints = 100;
    private int minDifficulty = 1;
    private String faction = Factions.INDEPENDENT;

    public BetterVariantsBountyDataMember(String fleetId, float weight, int minFleetPoints, int maxFleetPoints,
                                          String faction, int minDifficulty
    ) {
        this.fleetId = fleetId;
        this.weight = weight;
        this.minFleetPoints = minFleetPoints;
        this.maxFleetPoints = maxFleetPoints;
        this.faction = faction;
        this.minDifficulty = minDifficulty;
    }

    public String getFleetId() {
        return fleetId;
    }

    public float getWeight() {
        return weight;
    }

    public int getMinFleetPoints() {
        return minFleetPoints;
    }

    public int getMaxFleetPoints() {
        return maxFleetPoints;
    }

    public String getFaction() { return faction; }
    public int getMinDifficulty() { return minDifficulty; }

    @Override
    public String toString() {
        return "BetterVariantsBountyDataMember{" +
                "fleetId='" + fleetId + '\'' +
                ", weight=" + weight +
                ", minFleetPoints=" + minFleetPoints +
                ", maxFleetPoints=" + maxFleetPoints +
                ", minDifficulty=" + minDifficulty +
                ", faction='" + faction + '\'' +
                '}';
    }

}