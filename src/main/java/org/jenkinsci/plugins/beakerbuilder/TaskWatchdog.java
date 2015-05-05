package org.jenkinsci.plugins.beakerbuilder;

import java.util.TimerTask;

import org.apache.xmlrpc.XmlRpcException;

import com.github.vjuranek.beaker4j.remote_model.BeakerTask;
import com.github.vjuranek.beaker4j.remote_model.BeakerTask.TaskInfo;
import com.github.vjuranek.beaker4j.remote_model.TaskStatus;

/**
 * {@link TimerTask} which which wait some time and then checks the task status periodically. It keeps information about
 * previous task status so that it can notify the caller that task status has changed.
 * 
 * @author vjuranek
 * 
 */
public class TaskWatchdog extends TimerTask {

    /**
     * Default delay before watchdog starts to monitor status task. It's here for convenience so that all callers can
     * use same default.
     */
    public static final int DEFAULT_DELAY = 1000 * 60 * 5; // 5 minutes
    /**
     * Default period how often the task status is checked in Beaker. It's here for convenience so that all callers can
     * use same default.
     */
    public static final int DEFAULT_PERIOD = 1000 * 60 * 5; // 5 minutes

    /**
     * Task to be monitored
     */
    private BeakerTask task;
    
    /**
     * Current status of the task 
     */
    private TaskStatus status;
    
    /**
     * Previous status of the task. Used for comparison whether the task status has been changed
     */
    private TaskStatus oldStatus;
    
    /**
     * Convenient variable which provides directly information if the task is already finished or not
     */
    private boolean isFinished;

    /**
     * 
     * @param task Task to be monitored
     * @param status Initial task status
     */
    public TaskWatchdog(BeakerTask task, TaskStatus status) {
        this.task = task;
        this.status = status;
        this.oldStatus = status;
        this.isFinished = false;
    }

    /**
     * Gets task status info from Beaker server and if the status has changed, notifies all waiting classes.
     */
    public synchronized void run() {
        try {
            TaskInfo info = task.getInfo();
            oldStatus = status;
            status = info.getState();
            isFinished = info.isFinished();
            if (oldStatus != status) {
                notifyAll();
            }
        } catch (XmlRpcException e) {

        }
    }

    /**
     * 
     * @return Current status of the task
     */
    public TaskStatus getStatus() {
        return status;
    }

    /**
     * 
     * @return Previous state of the task
     */
    public TaskStatus getOldStatus() {
        return oldStatus;
    }

    /**
     * 
     * @return True, if task has been finished
     */
    public boolean isFinished() {
        return isFinished;
    }

}
