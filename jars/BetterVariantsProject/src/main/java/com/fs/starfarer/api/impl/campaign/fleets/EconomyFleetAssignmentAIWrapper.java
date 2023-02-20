package com.fs.starfarer.api.impl.campaign.fleets;

public class EconomyFleetAssignmentAIWrapper {
    EconomyFleetAssignmentAI ai;

    public EconomyFleetAssignmentAIWrapper(EconomyFleetAssignmentAI AI)
    {
        ai = AI;
    }

    public EconomyFleetAssignmentAI.EconomyRouteData getData() 
    {
        return ai.getData();
    }
}
