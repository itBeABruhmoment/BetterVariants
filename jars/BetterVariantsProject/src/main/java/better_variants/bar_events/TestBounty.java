package better_variants.bar_events;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.PersonImportance;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Voices;
import com.fs.starfarer.api.impl.campaign.missions.cb.BaseCustomBounty;
import com.fs.starfarer.api.impl.campaign.missions.hub.HubMissionWithBarEvent;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class TestBounty extends HubMissionWithBarEvent {
    private static final Logger log = Global.getLogger(better_variants.bar_events.TestBounty.class);
    static {
        log.setLevel(Level.ALL);
    }

    @Override
    public boolean shouldShowAtMarket(MarketAPI market) {
        log.debug("I should show #################################");
        return true;
    }

    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
        log.debug("I ran ##################################");
        // if bar event, let's create a person to actually give it to us
        if (barEvent) {
            setGiverRank(Ranks.AGENT);
            setGiverPost(Ranks.POST_EXECUTIVE);
            setGiverImportance(PersonImportance.HIGH);
            setGiverFaction(Factions.TRITACHYON);
            setGiverTags(Tags.CONTACT_MILITARY);
            setGiverVoice(Voices.BUSINESS);
            findOrCreateGiver(createdAt, false, false);
        }

        PersonAPI person = getPerson();
        if (person == null) return false;

        return true;
    }
}