package org.jenkinsci.plugins.beakerbuilder;

import hudson.Extension;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.ParametersAction;

import java.io.IOException;

import jenkins.model.Jenkins;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Represents job XML which is entered as path to the existing file within the workspace.
 *
 * @author vjuranek
 */
public class FileJobSource extends JobSource {

    private String jobPath;

    @DataBoundConstructor
    public FileJobSource(String jobPath) {
        this.jobPath = jobPath;
    }

    public String getJobPath() {
        return jobPath;
    }

    @Override
    protected String getDeclaredContent(AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException {
        // TODO check, if file really exists
        // TODO check, is path is really relative to WS root
        FilePath fp = new FilePath(build.getWorkspace(), expandJobPath(build, listener));
        // TODO not very safe, if e.g. some malicious user provide path to a huge file
        return fp.readToString();
    }

    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) Jenkins.getInstance().getDescriptor(getClass());
    }

    protected String expandJobPath(AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException {
        String expandedPath = getJobPath();
        ParametersAction pa = build.getAction(ParametersAction.class);
        // expand build parameters
        if (pa != null)
            expandedPath = pa.substitute(build, expandedPath);

        return build.getEnvironment(listener).expand(expandedPath);
    }

    @Extension
    public static class DescriptorImpl extends JobSourceDescriptor {
        @Override
        public String getDisplayName() {
            return "File job source";
        }
    }
}
