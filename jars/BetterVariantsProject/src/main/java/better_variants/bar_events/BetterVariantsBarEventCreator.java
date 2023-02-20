package better_variants.bar_events;

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;

public class BetterVariantsBarEventCreator extends BaseBarEventCreator {
    @Override
    public PortsideBarEvent createBarEvent() {
        return new BetterVariantsBountyEvent();
    }

    @Override
    public float getBarEventFrequencyWeight() {
        return 10.0f;
    }

    @Override
    public boolean isPriority() {
        return false;
    }
    /*
    com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager barEventManager = com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager.getInstance();
    for(com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager.GenericBarEventCreator event : barEventManager.getCreators())
    {
        Console.showMessage(event.getBarEventId() + event.getBarEventFrequencyWeight());
    }

    */
}