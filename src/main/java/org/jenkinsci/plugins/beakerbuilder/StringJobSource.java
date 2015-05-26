package org.jenkinsci.plugins.beakerbuilder;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;

import java.io.File;
import java.io.IOException;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Represent job XML which is entered directly in job config page.
 *
 * @author vjuranek
 */
public class StringJobSource extends JobSource {

    private final String jobContent;

    @DataBoundConstructor
    public StringJobSource(String jobContent){
        this.jobContent = jobContent;
    }

    public String getJobContent() {
        return jobContent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File createJobFile(AbstractBuild<?,?> build, BuildListener listener) throws InterruptedException, IOException {
        FilePath path = createDefaultJobFile(jobContent, build, listener);
        return new File(path.getRemote());
    }

    public DescriptorImpl getDescriptor(){
        return (DescriptorImpl)Jenkins.getInstance().getDescriptor(getClass());
    }

    @Extension
    public static class DescriptorImpl extends JobSourceDescriptor{
        @Override
        public String getDisplayName() {
            return "String script source";
        }
    }
}
