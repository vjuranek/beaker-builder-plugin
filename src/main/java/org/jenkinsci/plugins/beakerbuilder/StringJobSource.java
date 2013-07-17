package org.jenkinsci.plugins.beakerbuilder;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;

import java.io.File;
import java.io.IOException;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;

public class StringJobSource extends JobSource {
    
    private final String jobContent;
    private transient File tmpJobFile;
    
    @DataBoundConstructor
    public StringJobSource(String jobName, String jobContent){
        this.jobName = jobName;
        this.jobContent = jobContent;
    }
    
    public String getJobContent() {
        return jobContent;
    }
    
    @Override
    public void createJobFile(AbstractBuild<?,?> build, BuildListener listener) throws InterruptedException, IOException {
        FilePath path = createDefaultJobFile(jobContent, build, listener);
        tmpJobFile = new File(path.getRemote());
    }
    
    public String getDefaultJobPath(){
        return tmpJobFile.getPath();
    }
    
    public DescriptorImpl getDescriptor(){
        return (DescriptorImpl)Jenkins.getInstance().getDescriptor(getClass());
    }
    
    @Extension
    public static class DescriptorImpl extends JobSourceDescriptor{
        public String getDisplayName() {
            return "String script source";
        }
    }

}
