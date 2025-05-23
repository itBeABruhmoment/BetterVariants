package better_variants.scripts;

import better_variants.data.BetterVariantsBountyData;
import better_variants.data.CommonStrings;
import better_variants.data.SettingsData;

import java.io.IOException;
import java.util.ArrayList;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONException;

public class BetterVariantsModPlugin extends BaseModPlugin {
    private static final Logger log = Global.getLogger(better_variants.scripts.BetterVariantsModPlugin.class);
    static {
        log.setLevel(Level.ALL);
    }

    @Override
    public void onApplicationLoad() throws IOException, JSONException, Exception
    {
        log.info(CommonStrings.MOD_ID + ":loading bounties");
        BetterVariantsBountyData.getInstance().loadData();
        // settings are loaded in ApplySettings which runs after VariantsLibModPlugin.onApplicationLoad()
    }
}
