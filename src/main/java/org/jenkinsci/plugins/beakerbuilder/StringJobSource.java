package org.jenkinsci.plugins.beakerbuilder;

import hudson.Extension;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;

import javax.annotation.Nonnull;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Represent job XML which is entered directly in job config page.
 *
 * @author vjuranek
 */
public class StringJobSource extends JobSource {

    private final @Nonnull String jobContent;

    @DataBoundConstructor
    public StringJobSource(String jobContent){
        this.jobContent = Util.fixNull(jobContent);
    }

    public @Nonnull String getJobContent() {
        return jobContent;
    }

    @Override
    protected @Nonnull String getDeclaredContent(@Nonnull AbstractBuild<?,?> build, @Nonnull BuildListener listener) {
        return jobContent;
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
