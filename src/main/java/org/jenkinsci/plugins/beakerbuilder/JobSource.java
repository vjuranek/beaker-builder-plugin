package org.jenkinsci.plugins.beakerbuilder;

import hudson.DescriptorExtensionList;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.model.Describable;
import hudson.model.AbstractBuild;
import hudson.model.Descriptor;
import hudson.util.VariableResolver;

import java.io.IOException;

import javax.annotation.Nonnull;

import jenkins.model.Jenkins;

/**
 * Abstract class which represent source of Beaker job XML.
 *
 * @author vjuranek
 */
public abstract class JobSource implements Describable<JobSource> {

    /**
     * Get job xml template content as string.
     */
    protected abstract @Nonnull String getDeclaredContent(
            @Nonnull AbstractBuild<?, ?> build,
            @Nonnull BuildListener listener
    ) throws IOException, InterruptedException;

    /**
     * Get job xml to be sent to Beaker.
     *
     * Expands parameter and environment variable in job XML.
     */
    public final @Nonnull String getJobContent(
            @Nonnull AbstractBuild<?, ?> build,
            @Nonnull BuildListener listener
    ) throws IOException, InterruptedException {

        VariableResolver<String> variableResolver = new XMLEscapingVariableResolver(
                new VariableResolver.ByMap<String>(build.getEnvironment(listener))
        );

        return Util.replaceMacro(getDeclaredContent(build, listener), variableResolver);
    }

    public static DescriptorExtensionList<JobSource, JobSource.JobSourceDescriptor> all() {
        return Jenkins.getInstance().getDescriptorList(JobSource.class);
    }

    public static abstract class JobSourceDescriptor extends Descriptor<JobSource> {
    }
}
