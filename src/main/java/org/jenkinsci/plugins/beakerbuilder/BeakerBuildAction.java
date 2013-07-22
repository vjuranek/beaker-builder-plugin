package org.jenkinsci.plugins.beakerbuilder;

import hudson.model.Action;

/**
 * Action, which provides link to the real job running in Beaker in the job menu. 
 * 
 * @author vjuranek
 *
 */
public class BeakerBuildAction implements Action {
    
    private final int jobNumber;
    private final String beakerURL;
    
    public BeakerBuildAction(int jobNumber, String beakerURL) {
        this.jobNumber = jobNumber;
        this.beakerURL = beakerURL;
    }
    
    public String getIconFileName() {
        return "/plugin/beaker-builder/icons/beaker24.png";
    }
    
    public String getDisplayName() {
        return "Beaker job J:" + jobNumber;
    }
    
    public String getUrlName() {
        return beakerURL + "/jobs/" + jobNumber;
    }

}
