package org.jenkinsci.plugins.beakerbuilder;

import static org.junit.Assert.assertEquals;
import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.StreamBuildListener;
import hudson.model.BooleanParameterDefinition;
import hudson.model.FreeStyleProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class FileJobSourceTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void expandBuildEnvAndParams() throws IOException, ExecutionException, InterruptedException {
        FreeStyleProject project = j.createFreeStyleProject();
        ParameterDefinition stringParDef = new StringParameterDefinition("TestStringParam", "My test string parameter",
                "String description");
        ParameterDefinition boolParDef = new BooleanParameterDefinition("TestBooleanParam", true, "Bool description");
        project.addProperty(new ParametersDefinitionProperty(stringParDef, boolParDef));   
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        
        FilePath jobPath = build.getWorkspace().createTempFile("beaker_test1", "xml");
        String jobParamXML = "<test>Build #${BUILD_NUMBER}: My test job with string param of with value ${TestStringParam} and boolean param with value ${TestBooleanParam}</test>";
        jobPath.write(jobParamXML, Charset.defaultCharset().name());

        FileJobSource job = new FileJobSource("testJob", jobPath.getRemote());
        job.createJobFile(build, new StreamBuildListener(System.out, Charset.defaultCharset()));
        BufferedReader br = new BufferedReader(new FileReader(job.getDefaultJobPath()));
        String actualJob = br.readLine();
        br.close();
        assertEquals(
                "<test>Build #1: My test job with string param of with value My test string parameter and boolean param with value true</test>",
                actualJob);
    }
}
