package org.jenkinsci.plugins.beakerbuilder;

import java.io.IOException;
import java.util.logging.Logger;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;

/**
 * Exports Beaker related stuff like job ID in build env. variables for further use of subsequent steps.
 * 
 * @author vjuranek
 * 
 */

@Extension
public class BeakerEnvContributor extends EnvironmentContributor {

    @Override
    public void buildEnvironmentFor(@SuppressWarnings("rawtypes") Run r, EnvVars envs, TaskListener listener)
            throws IOException, InterruptedException {
        BeakerBuildAction bba = r.getAction(BeakerBuildAction.class);
        if (bba != null)
            try {
                envs.put("BEAKER_JOB_ID", String.valueOf(bba.getJobNumber()));
            } catch (NumberFormatException e) {
                LOGGER.warning("Cannot convert " + bba.getJobNumber() + " to integer");
            }
    }
    
    private static final Logger LOGGER = Logger.getLogger(BeakerEnvContributor.class.getName());
}
