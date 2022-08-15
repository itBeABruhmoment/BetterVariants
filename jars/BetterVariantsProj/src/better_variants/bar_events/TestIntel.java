package better_variants.bar_events;



import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.fs.starfarer.api.Global;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.SectorMapAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class TestIntel extends BaseIntelPlugin {
    private static final Logger log = Global.getLogger(better_variants.bar_events.TestIntel.class);
    static {
        log.setLevel(Level.ALL);
    }


    TestIntel()
    {
        super();
        log.debug("intellignece innit");
    }

    @Override
    public String getIcon() {
        return "graphics/icons/intel/player.png";
    }

    @Override
    public String getName() {
        return "Test Quest";
    }

    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        // The call to super will add the quest name, so we just need to add the summary
        super.createIntelInfo(info, mode);

        info.addPara("Destination: %s", // text to show. %s is highlighted.
                3f, // padding on left side of text. Vanilla hardcodes these values so we will too
                super.getBulletColorForMode(mode), // color of text
                Misc.getHighlightColor(), // color of highlighted text
                "susSector"); // highlighted text

        // This will display in the intel manager like:
        // Demo Quest
        //     Destination: Ancyra
    }

    @Override
    public boolean shouldRemoveIntel() {
        return true;
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        return new HashSet<>(Arrays.asList(Tags.INTEL_EXPLORATION, Tags.INTEL_STORY));
    }
}