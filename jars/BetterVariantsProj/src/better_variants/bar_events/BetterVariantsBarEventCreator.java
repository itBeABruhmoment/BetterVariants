package better_variants.bar_events;

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;

public class BetterVariantsBarEventCreator extends BaseBarEventCreator {
    @Override
    public PortsideBarEvent createBarEvent() {
        return new BetterVariantsBountyEvent();
    }

    @Override
    public boolean isPriority() {
        return true;
    }
}