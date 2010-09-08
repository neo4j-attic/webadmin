package org.neo4j.webadmin.backup;

import java.io.IOException;
import java.text.ParseException;

import org.neo4j.webadmin.domain.BackupFailedException;
import org.neo4j.webadmin.properties.ConfigFileFactory;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

/**
 * Handles scheduling of backup jobs.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public enum BackupManager
{

    INSTANCE;

    private boolean running = false;

    private BackupLog log;
    private BackupConfig config;

    private SchedulerFactory schedulerFactory = new StdSchedulerFactory();
    private Scheduler scheduler;

    public void start() throws IOException, SchedulerException,
            BackupFailedException
    {
        if ( !running )
        {
            log = new BackupLog( ConfigFileFactory.getBackupLogFile() );

            config = new BackupConfig( ConfigFileFactory.getBackupConfigFile() );

            scheduler = schedulerFactory.getScheduler();

            scheduleBackups();

            running = true;

        }
    }

    public void stop() throws SchedulerException
    {
        if ( !running )
        {
            running = false;
            scheduler.shutdown();
        }
    }

    public void restart() throws IOException, SchedulerException,
            BackupFailedException
    {
        stop();
        start();
    }

    public BackupConfig getConfig()
    {
        return config;
    }

    public BackupLog getLog()
    {
        return log;
    }

    //
    // INTERNALS
    //

    private void scheduleBackups() throws SchedulerException,
            BackupFailedException
    {

        for ( BackupJobDescription desc : config.getJobDescriptions() )
        {

            JobDataMap jobData = new JobDataMap();
            jobData.put( QuartzBackupJob.JOB_DESCRIPTION_KEY, desc );
            jobData.put( QuartzBackupJob.BACKUP_LOG_KEY, log );

            JobDetail jobDetail = new JobDetail( desc.getName(),
                    "Backup trigger group", QuartzBackupJob.class );

            CronTrigger trigger = new CronTrigger( desc.getName() + " Trigger",
                    "Backup trigger group" );

            try
            {
                CronExpression cron = new CronExpression(
                        desc.getCronExpression() );
                trigger.setCronExpression( cron );
            }
            catch ( ParseException e )
            {
                throw new BackupFailedException(
                        "Failed to initialize backup schedule for job '"
                                + desc.getName() + "', the cron expression '"
                                + desc.getCronExpression() + "' is invalid.", e );
            }

            scheduler.scheduleJob( jobDetail, trigger );
        }

        scheduler.start();
    }
}
