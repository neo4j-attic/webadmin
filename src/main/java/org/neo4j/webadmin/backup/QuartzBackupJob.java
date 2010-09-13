package org.neo4j.webadmin.backup;

import java.io.File;
import java.util.Date;

import org.neo4j.webadmin.domain.BackupFailedException;
import org.neo4j.webadmin.domain.NoBackupFoundationException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class QuartzBackupJob implements Job
{

    public static final String BACKUP_LOG_KEY = "backupLog";
    public static final String JOB_DESCRIPTION_KEY = "jobDescription";

    public synchronized void execute( JobExecutionContext ctx )
            throws JobExecutionException
    {
        BackupLog log = (BackupLog) ctx.getJobDetail().getJobDataMap().get(
                BACKUP_LOG_KEY );
        BackupJobDescription jobDesc = (BackupJobDescription) ctx.getJobDetail().getJobDataMap().get(
                JOB_DESCRIPTION_KEY );

        log.logInfo( new Date(), jobDesc, "started" );

        try
        {
            BackupPerformer.doBackup( new File( jobDesc.getPath() ) );
            log.logSuccess( new Date(), jobDesc );
        }
        catch ( NoBackupFoundationException e )
        {
            if ( jobDesc.isAutoFoundation() )
            {
                try
                {
                    BackupPerformer.doBackupFoundation( new File(
                            jobDesc.getPath() ) );
                    BackupPerformer.doBackup( new File( jobDesc.getPath() ) );
                    log.logSuccess( new Date(), jobDesc );
                }
                catch ( BackupFailedException e1 )
                {
                    e1.printStackTrace();
                    log.logFailure(
                            new Date(),
                            jobDesc,
                            "Unable to create backup foundation, make sure I have write permissions to '"
                                    + ( new File( jobDesc.getPath() ).getAbsolutePath() )
                                    + "'." );
                }
                catch ( NoBackupFoundationException e2 )
                {
                    e2.printStackTrace();
                    log.logFailure( new Date(), jobDesc,
                            "Fatal: Tried to create backup foundation, but failed: "
                                    + e2.getMessage() );
                }
            }
            else
            {
                log.logFailure( new Date(), jobDesc,
                        "Needs manual backup foundation." );
            }
        }
        catch ( BackupFailedException e )
        {
            log.logFailure( new Date(), jobDesc,
                    "Backup failed: " + e.getMessage() );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }
}
