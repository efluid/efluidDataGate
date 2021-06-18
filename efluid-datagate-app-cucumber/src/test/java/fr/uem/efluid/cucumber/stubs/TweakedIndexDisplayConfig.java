package fr.uem.efluid.cucumber.stubs;

import fr.uem.efluid.config.BusinessServiceConfig;

public class TweakedIndexDisplayConfig extends BusinessServiceConfig.IndexDisplayConfigProperties {

    private long standardMaxCombined;

    public void forceCombineSimilarDiffAfter(long newValue) {
        this.standardMaxCombined = getCombineSimilarDiffAfter();
        setCombineSimilarDiffAfter(newValue);
    }

    public void reset() {
        if (this.standardMaxCombined > 0) {
            setCombineSimilarDiffAfter(this.standardMaxCombined);
            this.standardMaxCombined = 0;
        }
    }
}
