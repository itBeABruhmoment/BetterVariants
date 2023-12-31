package better_variants.fleets;

import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.AICoreOfficerPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import variants_lib.data.OfficerFactory;
import variants_lib.data.OfficerFactoryParams;
import variants_lib.data.VariantsLibFleetFactory;
import variants_lib.data.VariantsLibFleetParams;

import java.util.Random;

public class AIFleetFactory extends VariantsLibFleetFactory {
    protected static AICoreOfficerPluginImpl AIOfficerFactory = new AICoreOfficerPluginImpl();
    protected static String ALPHA = "alpha_core";
    protected static String BETA = "beta_core";
    protected static String GAMMA = "gamma_core";
//    @Override
//    protected OfficerFactory createOfficerFactory(VariantsLibFleetParams params) {
//        return new AIOfficerFactory();
//    }

    protected String pickAICore(final Random rand) {
        final float num = rand.nextFloat();
        if(num < 0.33) {
            return ALPHA;
        } else if(num > 0.66){
            return GAMMA;
        } else {
            return BETA;
        }
    }

    @Override
    protected PersonAPI createCommander(
            OfficerFactory officerFactory,
            VariantsLibFleetParams fleetParams,
            Random rand, String variantId,
            String defaultPersonality
    ) {
        return AIOfficerFactory.createPerson(ALPHA, fleetParams.faction, rand);
    }

    @Override
    protected PersonAPI createOfficer(OfficerFactory officerFactory, VariantsLibFleetParams fleetParams, Random rand, String variantId, String defaultPersonality) {
        return AIOfficerFactory.createPerson(pickAICore(rand), fleetParams.faction, rand);
    }
}
