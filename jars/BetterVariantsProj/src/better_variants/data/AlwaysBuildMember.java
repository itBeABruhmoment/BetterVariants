package better_variants.data;

import com.fs.starfarer.api.Global;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class AlwaysBuildMember {
    private static final Logger log = Global.getLogger(better_variants.data.AlwaysBuildMember.class);
    static {
        log.setLevel(Level.ALL);
    }
    
    public String id;
    public int amount;

    public AlwaysBuildMember(String Id, int Amount) 
    {
        id = Id;
        amount = Amount;
    }
}
