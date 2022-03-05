package better_variants.data;

import com.fs.starfarer.api.Global;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class FleetPartitionMember {
    private static final Logger log = Global.getLogger(better_variants.data.FleetPartitionMember.class);
    static {
        log.setLevel(Level.ALL);
    }
    
    public String id;
    public double weight;

    public void makeWeightPercentage(double outOf)
    {
        weight /= outOf;
    }

    public FleetPartitionMember(String variantId, double spawningWeight)
    {
        id = variantId;
        weight = spawningWeight;
    }
}
