package org.jenkinsci.plugins.beakerbuilder;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;

import java.io.File;
import java.io.IOException;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;

public class FileJobSource extends JobSource {

    private String jobPath;
    private File tmpJobFile;
    
    @DataBoundConstructor
    public FileJobSource(String jobName, String jobPath){
        this.jobName = jobName;
        this.jobPath = jobPath;
    }
    
    public String getJobPath() {
        return jobPath;
    }
    
    public String getDefaultJobPath() {
        if(tmpJobFile != null)
            return tmpJobFile.getPath();
        return jobPath;
    }
    
    @Override
    public void createJobFile(AbstractBuild<?,?> build, BuildListener listener) throws InterruptedException, IOException {
        FilePath fp = new FilePath(build.getWorkspace(), getJobPath());
        String jobContent = fp.readToString(); // TODO not very safe, if e.g. some malicious user provide path to a huge file
        FilePath path = createDefaultJobFile(jobContent, build, listener);
        tmpJobFile = new File(path.getRemote());
    }
    
    public DescriptorImpl getDescriptor(){
        return (DescriptorImpl)Jenkins.getInstance().getDescriptor(getClass());
    }
    
    @Extension
    public static class DescriptorImpl extends JobSourceDescriptor{
        public String getDisplayName() {
            return "File job source";
        }
    }

}

