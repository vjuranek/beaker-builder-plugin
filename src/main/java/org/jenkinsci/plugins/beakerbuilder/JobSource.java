package org.jenkinsci.plugins.beakerbuilder;

import hudson.DescriptorExtensionList;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.model.ParametersAction;

import java.io.IOException;

import jenkins.model.Jenkins;

public abstract class JobSource implements Describable<JobSource> {
    
    protected static final String DEFAULT_JOB_PREFIX = "beakerJob";
    protected static final String DEFAULT_JOB_SUFFIX = ".xml";
        
    protected String jobName;

    public String getJobName() {
        return jobName;
    }
    
    public abstract String getDefaultJobPath();
    
    public abstract void createJobFile(AbstractBuild<?,?> build, BuildListener listener) throws InterruptedException, IOException;
    
    public FilePath createDefaultJobFile(String jobContent, AbstractBuild<?,?> build, BuildListener listener) throws InterruptedException, IOException {
        String job = jobContent;
        ParametersAction pa = build.getAction(ParametersAction.class);
        // expand build parameters
        if(pa != null)
            job = pa.substitute(build, job);
        // expand environment variables
        jobContent = build.getEnvironment(listener).expand(jobContent);
        FilePath path = build.getWorkspace().createTextTempFile(DEFAULT_JOB_PREFIX, DEFAULT_JOB_SUFFIX, jobContent, true);
        return path;
    }
    
    public static DescriptorExtensionList<JobSource, JobSource.JobSourceDescriptor> all() {
        return Jenkins.getInstance().getDescriptorList(JobSource.class);
    }
    
    public static abstract class JobSourceDescriptor extends Descriptor<JobSource> {
    }

}
