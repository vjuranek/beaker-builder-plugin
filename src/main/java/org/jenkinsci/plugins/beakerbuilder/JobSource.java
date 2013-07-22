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

/**
 * Abstract class which represent source of Beaker job XML.
 * 
 * @author vjuranek
 * 
 */
public abstract class JobSource implements Describable<JobSource> {

    protected static final String DEFAULT_JOB_PREFIX = "beakerJob";
    protected static final String DEFAULT_JOB_SUFFIX = ".xml";

    protected String jobName;

    public String getJobName() {
        return jobName;
    }

    /**
     * Path to the job file which will be scheduled on Beaker. Default doesn't mean that it returns path where these
     * file are by default, but it's default path of job, which should be scheduled.
     * 
     * @return Path to the job file which will be scheduled on Beaker.
     */
    public abstract String getDefaultJobPath();

    /**
     * Creates temporal file with job XML
     * 
     * @param build
     * @param listener
     * @throws InterruptedException
     * @throws IOException
     */
    public abstract void createJobFile(AbstractBuild<?, ?> build, BuildListener listener) throws InterruptedException,
            IOException;

    /**
     * Expands parameter and environment variable in job XML and stores expanded XML into temporal file. It's assumed,
     * that Beaker will execute file create by this method, not directly the XML provided by user in job config page.
     * 
     * @param jobContent
     *            Job XML
     * @param build
     * @param listener
     * 
     * @return {@link FilePath} to temporary file final job XML
     * 
     * @throws InterruptedException
     * @throws IOException
     */
    public FilePath createDefaultJobFile(String jobContent, AbstractBuild<?, ?> build, BuildListener listener)
            throws InterruptedException, IOException {
        String job = jobContent;
        ParametersAction pa = build.getAction(ParametersAction.class);
        // expand build parameters
        if (pa != null)
            job = pa.substitute(build, job);
        // expand environment variables
        jobContent = build.getEnvironment(listener).expand(jobContent);
        FilePath path = build.getWorkspace().createTextTempFile(DEFAULT_JOB_PREFIX, DEFAULT_JOB_SUFFIX, jobContent,
                true);
        return path;
    }

    public static DescriptorExtensionList<JobSource, JobSource.JobSourceDescriptor> all() {
        return Jenkins.getInstance().getDescriptorList(JobSource.class);
    }

    public static abstract class JobSourceDescriptor extends Descriptor<JobSource> {
    }

}
