package better_variants.fleets;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.AICoreOfficerPluginImpl;
import variants_lib.data.OfficerFactory;
import variants_lib.data.OfficerFactoryParams;
import variants_lib.data.Util;

public class AIOfficerFactory extends OfficerFactory {
    private static String[] AI_CORE_IDS = {"alpha_core", "beta_core", "gamma_core"};
    private static int ALPHA_LEVEL = 8;
    private static int BETA_LEVEL = 6;
    private static int GAMMA_LEVEL = 4;

    @Override
    public PersonAPI createOfficer(final OfficerFactoryParams params) {


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

//        final FactionAPI faction = Global.getSector().getFaction(params.faction);
        final PersonAPI officer = new AICoreOfficerPluginImpl().createPerson(AI_CORE_IDS[minIdx], params.faction, params.rand);
//        officer.setAICoreId(AI_CORE_IDS[minIdx]);

        addSkills(officer, params);
        return officer;
    }

    @Override
    protected void addSkills(final PersonAPI officer, final OfficerFactoryParams params) {
        final MutableCharacterStatsAPI stats = officer.getStats();
        int skillsAdded = 0;

        // add skills in skillsToAdd until done or level does not allow it
        while (skillsAdded < params.level && skillsAdded < params.skillsToAdd.size()) {
            final String skill = params.skillsToAdd.get(skillsAdded);
            if (VALID_ELITE_SKILLS.contains(skill) && params.rand.nextFloat() < params.percentEliteSkills) {
                stats.setSkillLevel(skill, 2.0f);
            } else {
                stats.setSkillLevel(skill, 1.0f);
            }
            skillsAdded++;
        }

        // fill remaining levels with empty skills
        if (skillsAdded < params.level) {
            final int[] randomIndices = Util.createRandomNumberSequence(FILLER_SKILLS.length, params.rand);
            int i = 0;
            while (i < randomIndices.length && skillsAdded < params.level) {
                final String skill = FILLER_SKILLS[randomIndices[i]];
                if (!stats.hasSkill(skill)) {
                    float skillLevel;
                    if (params.rand.nextFloat() < params.percentEliteSkills) {
                        skillLevel = 2.0f;
                    } else {
                        skillLevel = 1.0f;
                    }
                    stats.setSkillLevel(skill, skillLevel);
                    skillsAdded++;
                }
                i++;
            }
        }
    }
}
