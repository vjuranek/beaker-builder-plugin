package org.jenkinsci.plugins.beakerbuilder;

import java.io.IOException;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;

@Extension
public class BeakerEnvContributor extends EnvironmentContributor {
    
    public void buildEnvironmentFor(Run r, EnvVars envs, TaskListener listener) throws IOException, InterruptedException {
        BeakerBuildAction bba = r.getAction(BeakerBuildAction.class);
        if(bba != null)
            try {
                envs.put("BEAKER_JOB_ID", String.valueOf(bba.getJobNumber()));
            } catch(NumberFormatException e) {
                // TODO log the exception
            }
    }

}
