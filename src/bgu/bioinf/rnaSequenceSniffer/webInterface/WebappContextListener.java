package bgu.bioinf.rnaSequenceSniffer.webInterface;

import bgu.bioinf.rnaSequenceSniffer.algorithmControl.JobCleaner;
import com.mchange.v2.c3p0.C3P0Registry;
import com.mchange.v2.c3p0.PooledDataSource;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.*;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by matan on 09/01/15.
 */
public class WebappContextListener implements ServletContextListener {
    public static final String BASE_LOCATION = "/opt/"; // "/data/";

    private static int NUMBER_OF_JOB_THREADS = 8;
    public static ExecutorService jobExecutor = Executors.newFixedThreadPool(NUMBER_OF_JOB_THREADS);
    private static int NUMBER_OF_CACHE_THREADS = 4;
    public static ExecutorService cacheExecutor = Executors.newFixedThreadPool(NUMBER_OF_CACHE_THREADS);
    private static Scheduler quartzScheduler = null;
    public static final ReentrantLock maintenanceLock = new ReentrantLock();
    public static final Condition conditionMaintenanceLock = maintenanceLock.newCondition();
    public static final Object cacheLock = new Object();
    // Ohh holy random number generator
    public static final Random rnGesus = new Random();

    ServletContext context;

    public void contextInitialized(ServletContextEvent contextEvent) {
        try {
            // TODO: build a database state tester, only if having a lot of problems.
            quartzScheduler = new StdSchedulerFactory().getScheduler();
            JobDetail maintenanceJob = JobBuilder.newJob(JobCleaner.class).withIdentity("MaintenanceJob").build();
            Trigger dailyChecks = TriggerBuilder.newTrigger().withIdentity("DailyMaintenance")
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0 3 * * ? *"))
                    .build();
            quartzScheduler.scheduleJob(maintenanceJob, dailyChecks);
            quartzScheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
            System.err.println("Error initiating quartz scheduler");
        }
    }

    /**
     * Clean for redeploy
     */
    public void contextDestroyed(ServletContextEvent contextEvent) {
        // close quarts
        if (quartzScheduler != null) {
            try {
                // close scheduler, if tasks are currently running wait for them to finish
                quartzScheduler.shutdown(true);
                // sleep 1 second to avoid tomcat calling out for un-killed thread
                // Discussion: forums.terracotta.org/forums/posts/list/3479.page
                Thread.sleep(1000);
            } catch (SchedulerException e) {
                e.printStackTrace();
                System.err.println("Error Shutting down quartz scheduler");
            } catch (InterruptedException ignore) {
            }
        }
        // close c3p0 connection pool
        C3P0Registry.getNumPooledDataSources();
        PooledDataSource dataSource;
        @SuppressWarnings({"unchecked", "rawtypes"})
        Iterator<Set> it = C3P0Registry.getPooledDataSources().iterator();
        while (it.hasNext()) {
            try {
                dataSource = (PooledDataSource) it.next();
                dataSource.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Stop executor and wait for termination
        if (jobExecutor != null) {
            jobExecutor.shutdownNow();
            try {
                jobExecutor.awaitTermination(5, TimeUnit.MINUTES);
            } catch (Exception ignore) {
            }
        }
        if (cacheExecutor != null) {
            cacheExecutor.shutdownNow();
            try {
                cacheExecutor.awaitTermination(5, TimeUnit.MINUTES);
            } catch (Exception ignore) {
            }
        }
    }
}
