package better_variants.scripts;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Vector;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

import com.fs.starfarer.api.campaign.econ.*;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.fleet.RepairTrackerAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.econ.EconomyAPI.EconomyUpdateListener;
import com.fs.starfarer.api.impl.campaign.DModManager;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.econ.impl.ShipQuality;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflater;
import com.fs.starfarer.api.impl.campaign.fleets.DefaultFleetInflaterParams;

import better_variants.data.BetterVariantsTags;
import better_variants.data.AlwaysBuildMember;
import better_variants.data.CommonStrings;
import better_variants.data.FactionData;
import better_variants.data.FleetBuildData;
import better_variants.data.VariantData;
import better_variants.data.FleetComposition;
import better_variants.data.FleetPartition;
import better_variants.data.FleetPartitionMember;

import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.campaign.FleetDataAPI;
import com.fs.starfarer.api.campaign.FleetInflater;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;


public class NoAutofitFleetInflater extends DefaultFleetInflater 
{
    private Random rand;

    NoAutofitFleetInflater(DefaultFleetInflaterParams params) {
        super(params);
    }

    NoAutofitFleetInflater(DefaultFleetInflater defaultInflator, Random Rand) {
        super((DefaultFleetInflaterParams)defaultInflator.getParams());
        rand = Rand;
    }

    @Override
    public void inflate(CampaignFleetAPI fleet)
    {
        float quality = getQuality();
        quality = quality + (0.05f * quality); // noticed a abnormal amount dmods in factions such as diktat
        for(FleetMemberAPI memberAPI : fleet.getMembersWithFightersCopy()) {
            if(!memberAPI.isFighterWing() && !memberAPI.isMothballed()) {
                if(quality <= 0.0f) {
                    DModManager.addDMods(memberAPI, true, 5, rand);
                } else if(quality <= 1.0f) {
                    int numDmods = Math.round(5.0f - (quality + (rand.nextFloat() / 5.0f - 0.1f)) * 5.0f);
                    if(numDmods < 0) {
                        numDmods = 0;
                    }
                    if(numDmods > 5) {
                        numDmods = 5;
                    }
                    DModManager.addDMods(memberAPI, true, numDmods, rand);
                } // otherwise apply no dmods
            }
        }
    }
    
    
}
