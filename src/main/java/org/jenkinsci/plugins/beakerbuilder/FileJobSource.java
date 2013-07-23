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
 * Represents job XML which is entered as path to the existing file within the workspace.
 * 
 * @author vjuranek
 * 
 */
public class FileJobSource extends JobSource {

    private String jobPath;

    @DataBoundConstructor
    public FileJobSource(String jobName, String jobPath) {
        this.jobName = jobName;
        this.jobPath = jobPath;
    }

    /**
     * {@inheritDoc}
     */
    public String getJobPath() {
        return jobPath;
    }

    /**
     * Reads job XML from file and expands variable by calling
     * {@link JobSource#createDefaultJobFile(String, AbstractBuild, BuildListener)}. For security reasons file path is
     * assumes that file path is relative to workspace directory (i.e. file is within workspace).
     */
    @Override
    public File createJobFile(AbstractBuild<?, ?> build, BuildListener listener) throws InterruptedException,
            IOException {
        // TODO check, if file really exists
        // TODO check, is path is really relative to WS root
        FilePath fp = new FilePath(build.getWorkspace(), getJobPath()); 
        // TODO not very safe, if e.g. some malicious user provide path to a huge file                  
        String jobContent = fp.readToString(); 
        FilePath path = createDefaultJobFile(jobContent, build, listener);
        return new File(path.getRemote());
    }

    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Jenkins.getInstance().getDescriptor(getClass());
    }

    @Extension
    public static class DescriptorImpl extends JobSourceDescriptor {
        public String getDisplayName() {
            return "File job source";
        }
    }

}
