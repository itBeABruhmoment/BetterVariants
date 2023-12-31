package better_variants.fleets;

import com.fs.starfarer.api.characters.PersonAPI;
import variants_lib.data.OfficerFactory;
import variants_lib.data.OfficerFactoryParams;
import variants_lib.data.VariantsLibFleetFactory;
import variants_lib.data.VariantsLibFleetParams;

import java.util.Random;

public class AIFleetFactory extends VariantsLibFleetFactory {
    @Override
    protected OfficerFactory createOfficerFactory(VariantsLibFleetParams params) {
        return new AIOfficerFactory();
    }

    @Override
    protected PersonAPI createCommander(
            OfficerFactory officerFactory,
            VariantsLibFleetParams fleetParams,
            Random rand, String variantId,
            String defaultPersonality
    ) {
        OfficerFactoryParams officerFactoryParams = new OfficerFactoryParams(
                variantId,
                fleetParams.faction,
                rand,
                8
        );
        officerFactoryParams.skillsToAdd.addAll(commanderSkills);
        //officerFactoryParams.personality = defaultPersonality;
        return officerFactory.createOfficer(officerFactoryParams);
    }
}
