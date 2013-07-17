package org.jenkinsci.plugins.beakerbuilder;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONObject;

import org.fedorahosted.beaker4j.beaker.BeakerServer;
import org.fedorahosted.beaker4j.client.BeakerClient;
import org.fedorahosted.beaker4j.remote_model.Identity;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

public class BeakerBuilder extends Builder {

    private final JobSource jobSource;
    
    @DataBoundConstructor
    public BeakerBuilder(JobSource jobSource) {
        this.jobSource = jobSource;
    }

    public JobSource getJobSource() {
        return jobSource;
    }
    
    @Override
    public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException {
        if (!prepareJob(build, listener))
            return false;
        FilePath fp = new FilePath(build.getWorkspace(), jobSource.getDefaultJobPath());
        try {
            System.out.println("Running job " + fp.readToString());
        } catch(IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean prepareJob(AbstractBuild<?, ?> build, BuildListener listener) throws InterruptedException {
        
        // create temporary file with Beaker job 
        try {
            jobSource.createJobFile(build, listener);
        } catch (IOException ioe) {
            log("[Beaker] ERROR: Could not get canonical path to workspace:" + ioe);
            ioe.printStackTrace();
            build.setResult(Result.FAILURE);
            return false;
        }
        
        // verify that file really exists in workspace
        FilePath fp = new FilePath(build.getWorkspace(), jobSource.getDefaultJobPath());
        try {
            if ( !fp.exists()) {
                log("[Beaker] ERROR: Job file " + fp.getName() + " doesn't exists on channel" + fp.getChannel() + "!");
                build.setResult(Result.FAILURE);
                return false;
            }
        } catch (IOException e) {
            log("[Beaker] ERROR: failed to verify that " + fp.getName() + " exists on channel" + fp.getChannel() + "! IOException cought, check Jenkins log for more details");
            LOGGER.log(Level.INFO, "Beaker error: failed to verify that " + fp.getName() + " exists on channel" + fp.getChannel() + "!", e);
            build.setResult(Result.FAILURE);
            return false;
        }
        
        return true;
    }
    
    private void log(String message){
        //console.logAnnot(message);
    }
    
    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private String beakerURL;
        private String login;
        private String password;

        private transient final BeakerClient beakerClient;

        public DescriptorImpl() {
            load();
            beakerClient = BeakerServer.getXmlRpcClient(beakerURL);
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Execute Beaker task";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            req.bindJSON(this, formData);
            save();
            return super.configure(req, formData);
        }

        public FormValidation doTestConnection(@QueryParameter("beakerURL") final String beakerURL,
                @QueryParameter("login") final String login, @QueryParameter("password") final String password) {
            System.out.println("Trying to get client for " + beakerURL);
            BeakerClient bc = BeakerServer.getXmlRpcClient(beakerURL);
            Identity ident = new Identity(login, password, bc);
            try {
                if (!ident.authenticate())
                    // TODO localization
                    return FormValidation.error("Cannot connect to " + beakerURL + " as " + login);
                return FormValidation.ok("Connected as " + ident.whoAmI());
            } catch (Exception e) {
                e.printStackTrace();
                return FormValidation.error("Somethign went wrong, cannot connect to " + beakerURL + ", cause: " + e.getCause());
            }
        }

        public String getBeakerURL() {
            return beakerURL;
        }

        public void setBeakerURL(String beakerURL) {
            this.beakerURL = beakerURL;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public BeakerClient getBeakerClient() {
            return beakerClient;
        }

    }
    
    private static final Logger LOGGER = Logger.getLogger(BeakerBuilder.class.getName());

}
