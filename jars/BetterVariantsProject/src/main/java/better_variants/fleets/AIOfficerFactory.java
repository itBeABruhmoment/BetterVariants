package better_variants.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import variants_lib.data.OfficerFactory;
import variants_lib.data.OfficerFactoryParams;

public class AIOfficerFactory extends OfficerFactory {
    private static String[] AI_CORE_IDS = {"alpha_core", "beta_core", "gamma_core"};
    private static int ALPHA_LEVEL = 8;
    private static int BETA_LEVEL = 6;
    private static int GAMMA_LEVEL = 4;

    @Override
    public PersonAPI createOfficer(final OfficerFactoryParams params) {
        final FactionAPI faction = Global.getSector().getFaction(params.faction);
        final PersonAPI officer = faction.createRandomPerson(params.rand);

        // determine what type of core the officer should be based on level
        final int[] levelDiff = {ALPHA_LEVEL, BETA_LEVEL, GAMMA_LEVEL};
        for(int i = 0; i < levelDiff.length; i++) {
            levelDiff[i] = Math.abs(params.level - levelDiff[i]);
        }

        int minIdx = 0;
        for(int i = 0; i < levelDiff.length - 1; i++) {
            if(levelDiff[minIdx] > levelDiff[i]) {
                minIdx = i;
            }
        }
        officer.setAICoreId(AI_CORE_IDS[minIdx]);

        addSkills(officer, params);
        return officer;
    }
}
