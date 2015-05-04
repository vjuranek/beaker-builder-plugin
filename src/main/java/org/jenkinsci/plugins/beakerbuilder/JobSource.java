package org.jenkinsci.plugins.beakerbuilder;

import hudson.DescriptorExtensionList;
import hudson.FilePath;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.util.VariableResolver;

import java.io.File;
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

    /**
     * Creates temporal file with job XML
     * 
     * @param build
     * @param listener
     * @throws InterruptedException
     * @throws IOException
     */
    public abstract File createJobFile(AbstractBuild<?, ?> build, BuildListener listener) throws InterruptedException,
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
        // expand environment variables
        VariableResolver<String> variableResolver = new XMLEscapingVariableResolver(
                new VariableResolver.ByMap<String>(build.getEnvironment(listener)));
        jobContent = Util.replaceMacro(jobContent, variableResolver);
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
