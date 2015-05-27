/*
 * The MIT License
 *
 * Copyright (c) 2015 Red Hat, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.beakerbuilder;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import hudson.ExtensionList;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.Descriptor;
import hudson.model.FreeStyleProject;

import org.jenkinsci.plugins.beakerbuilder.BeakerBuilder.DescriptorImpl;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.SingleFileSCM;
import org.mockito.Mockito;

import com.github.vjuranek.beaker4j.client.BeakerClient;
import com.github.vjuranek.beaker4j.remote_model.BeakerJob;
import com.github.vjuranek.beaker4j.remote_model.BeakerTask;
import com.github.vjuranek.beaker4j.remote_model.BeakerTask.TaskInfo;
import com.github.vjuranek.beaker4j.remote_model.Identity;
import com.github.vjuranek.beaker4j.remote_model.TaskResult;
import com.github.vjuranek.beaker4j.remote_model.TaskStatus;

public class BuildTest {

    public @Rule JenkinsRule j = new JenkinsRule();

    private Identity identity;
    private BeakerClient client;

    static { // Reduce wait times for testing to 1 second
        System.setProperty("org.jenkinsci.plugins.beakerbuilder.TaskWatchdog.DEFAULT_DELAY", "1");
        System.setProperty("org.jenkinsci.plugins.beakerbuilder.TaskWatchdog.DEFAULT_PERIOD", "1");
    }

    @Test
    public void notConfigured() throws Exception {
        FreeStyleProject p = j.createFreeStyleProject();
        p.getBuildersList().add(new BeakerBuilder(new StringJobSource("<job>"), false));

        FreeStyleBuild b = p.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, b);
        j.assertLogContains("Beaker connection not configured properly", b);
    }

    @Test
    public void pass() throws Exception {
        mockBeakerConnection();
        mockBeakerExecution("<job>", 42, TaskStatus.COMPLETED, TaskResult.PASS);

        FreeStyleProject p = j.createFreeStyleProject();
        final BeakerBuilder builder = new BeakerBuilder(new StringJobSource("<job>"), false);
        p.getBuildersList().add(builder);

        FreeStyleBuild build = j.buildAndAssertSuccess(p);
        assertEquals(42, build.getAction(BeakerBuildAction.class).getJobNumber());
    }

    @Test
    public void warn() throws Exception {
        mockBeakerConnection();
        mockBeakerExecution("<job>", 42, TaskStatus.COMPLETED, TaskResult.WARN);

        FreeStyleProject p = j.createFreeStyleProject();
        final BeakerBuilder builder = new BeakerBuilder(new StringJobSource("<job>"), false);
        p.getBuildersList().add(builder);

        FreeStyleBuild build = p.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.UNSTABLE, build);
        assertEquals(42, build.getAction(BeakerBuildAction.class).getJobNumber());
    }

    @Test
    public void fail() throws Exception {
        mockBeakerConnection();
        mockBeakerExecution("<job>", 42, TaskStatus.COMPLETED, TaskResult.FAIL);

        FreeStyleProject p = j.createFreeStyleProject();
        final BeakerBuilder builder = new BeakerBuilder(new StringJobSource("<job>"), false);
        p.getBuildersList().add(builder);

        FreeStyleBuild build = p.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, build);
        assertEquals(42, build.getAction(BeakerBuildAction.class).getJobNumber());
    }

    @Test
    public void panic() throws Exception {
        mockBeakerConnection();
        mockBeakerExecution("<file_job>", 42, TaskStatus.COMPLETED, TaskResult.PANIC);

        FreeStyleProject p = j.createFreeStyleProject();
        p.setScm(new SingleFileSCM("job.xml", "<file_job>"));
        final BeakerBuilder builder = new BeakerBuilder(new FileJobSource("job.xml"), false);
        p.getBuildersList().add(builder);

        FreeStyleBuild build = p.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, build);
        assertEquals(42, build.getAction(BeakerBuildAction.class).getJobNumber());
    }

    @Test
    public void noSuchFile() throws Exception {
        mockBeakerConnection();
        mockBeakerExecution("<file_job>", 42, TaskStatus.COMPLETED, TaskResult.PANIC);

        FreeStyleProject p = j.createFreeStyleProject();
        final BeakerBuilder builder = new BeakerBuilder(new FileJobSource("job.xml"), false);
        p.getBuildersList().add(builder);

        FreeStyleBuild build = p.scheduleBuild2(0).get();
        j.assertBuildStatus(Result.FAILURE, build);

        assertThat(build.getLog(), containsString("No such file or directory"));
    }

    private void mockBeakerExecution(String xml, int number, TaskStatus status, TaskResult result) throws Exception {
        BeakerJob job = Mockito.mock(BeakerJob.class);
        Mockito.when(client.scheduleJob(xml)).thenReturn(job);
        Mockito.when(job.getJobId()).thenReturn("J:" + number);
        Mockito.when(job.getJobNumber()).thenReturn(number);

        BeakerTask task = Mockito.mock(BeakerTask.class);
        Mockito.when(job.getBeakerTask()).thenReturn(task);

        TaskInfo info = Mockito.mock(TaskInfo.class);
        Mockito.when(task.getInfo()).thenReturn(info);
        Mockito.when(info.isFinished()).thenReturn(true);
        Mockito.when(info.getState()).thenReturn(status);
        Mockito.when(info.getResult()).thenReturn(result);
    }

    private void mockBeakerConnection() {
        DescriptorImpl desc = j.jenkins.getDescriptorByType(BeakerBuilder.DescriptorImpl.class);

        // Mock Jenkins descriptor
        ExtensionList<Descriptor> descriptors = j.jenkins.getExtensionList(Descriptor.class);
        descriptors.remove(desc);
        DescriptorImpl spy = Mockito.spy(desc);
        descriptors.add(spy);

        identity = Mockito.mock(Identity.class);
        client = Mockito.mock(BeakerClient.class);

        Mockito.when(spy.getIdentity()).thenReturn(identity);
        Mockito.when(spy.getBeakerClient()).thenReturn(client);
    }
}
