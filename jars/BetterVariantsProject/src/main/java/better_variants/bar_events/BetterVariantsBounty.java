package better_variants.bar_events;

import better_variants.data.BetterVariantsBountyData;
import better_variants.data.BetterVariantsBountyDataMember;
import better_variants.data.CommonStrings;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.rules.HasMemory;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Ranks;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Voices;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBDeserter;
import com.fs.starfarer.api.impl.campaign.missions.cb.CBStats;
import com.fs.starfarer.api.impl.campaign.missions.cb.CustomBountyCreator;
import com.fs.starfarer.api.impl.campaign.missions.cb.MilitaryCustomBounty;
import com.fs.starfarer.api.impl.campaign.missions.hub.*;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.lwjgl.util.vector.Vector2f;
import variants_lib.data.FleetBuildData;
import variants_lib.data.VariantsLibFleetFactory;
import variants_lib.data.VariantsLibFleetParams;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// TODO: read callaction of base custom bounty

/**
 * Notes:
 * Override the create method from the base bounty to gen what I need
 * Set protected low normal and high bounty data fields
 *  Create the needed bounty creator classes
 *  Lets start with deserter
 */
public class BetterVariantsBounty extends MilitaryCustomBounty {
    private static final Logger log = Global.getLogger(better_variants.bar_events.BetterVariantsBounty.class);
    static {
        log.setLevel(Level.ALL);
    }

    public static final ArrayList<CustomBountyCreator> CREATORS = new ArrayList<CustomBountyCreator>() {{add(new BetterVariantsDeserterBountyCreator());}};

    @Override
    public List<CustomBountyCreator> getCreators() {
        log.info("BetterVariantsBountyCreate creator #############################");
        return CREATORS;
    }

    @Override
    protected boolean create(MarketAPI createdAt, boolean barEvent) {
        if ("pirates".equals(createdAt.getFaction().getId())) {
            return false;
        }

        log.info("BetterVariantsBountyCreate ########################################################################");
        if (barEvent) {
            createBarGiver(createdAt);
        }

        PersonAPI person = getPerson();
        if (person == null) {
            log.info("BetterVariantsBountyCreate r1");
            return false;
        }

        String id = getMissionId();
        if (!setPersonMissionRef(person, "$" + id + "_ref")) {
            log.info("BetterVariantsBountyCreate r2");
            return false;
        }

        setStartingStage(Stage.BOUNTY);
        setSuccessStage(Stage.COMPLETED);
        setFailureStage(Stage.FAILED);
        addNoPenaltyFailureStages(new Object[] { Stage.FAILED_NO_PENALTY });

        connectWithMemoryFlag(Stage.BOUNTY, Stage.COMPLETED, (HasMemory)person, "$" + id + "_completed");
        connectWithMemoryFlag(Stage.BOUNTY, Stage.FAILED, (HasMemory)person, "$" + id + "_failed");

        addTag("Bounties");

        int dLow = pickDifficulty(DifficultyChoice.LOW);
        this.creatorLow = pickCreator(dLow, DifficultyChoice.LOW);

        if (this.creatorLow != null) {
            log.info("creator not null");
            this.dataLow = this.creatorLow.createBounty(createdAt, this, dLow, Stage.BOUNTY);
        } else {
            log.info("creator null"); // creator appears to be null in current iteration
        }
        if (this.dataLow == null || this.dataLow.fleet == null) {
            // the issue
            log.info(String.format("BetterVariantsBountyCreate r3 %s", dataLow));
            return false;
        }

        int dNormal = pickDifficulty(DifficultyChoice.NORMAL);
        this.creatorNormal = pickCreator(dNormal, DifficultyChoice.NORMAL);
        if (this.creatorNormal != null) {
            this.dataNormal = this.creatorNormal.createBounty(createdAt, this, dNormal, Stage.BOUNTY);
        }
        if (this.dataNormal == null || this.dataNormal.fleet == null) {
            log.info(String.format("BetterVariantsBountyCreate r4 %s", dataNormal));
            return false;
        }

        int dHigh = pickDifficulty(DifficultyChoice.HIGH);
        this.creatorHigh = pickCreator(dHigh, DifficultyChoice.HIGH);
        if (this.creatorHigh != null) {
            log.info(String.format("BetterVariantsBountyCreate r5 %s", dataHigh));
            this.dataHigh = this.creatorHigh.createBounty(createdAt, this, dHigh, Stage.BOUNTY);
        }

        if (this.dataHigh == null || this.dataHigh.fleet == null) {
            log.info("BetterVariantsBountyCreate r5");
            return false;
        }

        log.info("BetterVariantsBountyCreate return true");
        return true;
    }

    @Override
    protected CustomBountyCreator pickCreator(int difficulty, DifficultyChoice choice) {
        log.info(CREATORS);
        return CREATORS.get(0);
    }
}
