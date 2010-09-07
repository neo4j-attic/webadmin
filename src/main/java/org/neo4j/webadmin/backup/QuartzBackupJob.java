package org.neo4j.webadmin.backup;

import java.io.File;

import org.neo4j.webadmin.domain.BackupFailedException;
import org.neo4j.webadmin.domain.NoBackupFoundationException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class QuartzBackupJob implements Job
{

    public static final String BACKUP_LOG_KEY = "backupLog";
    public static final String JOB_DESCRIPTION_KEY = "jobDescription";

    public void execute( JobExecutionContext ctx ) throws JobExecutionException
    {
        BackupLog log = (BackupLog) ctx.getJobDetail().getJobDataMap().get(
                BACKUP_LOG_KEY );
        BackupJobDescription jobDesc = (BackupJobDescription) ctx.getJobDetail().getJobDataMap().get(
                JOB_DESCRIPTION_KEY );

        try
        {
            BackupPerformer.doBackup( new File( jobDesc.getPath() ) );
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
                }
                catch ( BackupFailedException e1 )
                {
                    // Failed to create backup foundation
                }
                catch ( NoBackupFoundationException e2 )
                {
                    // Failed to create backup foundation
                }
            }
            else
            {
                // No backup foundation, manual intervention required
            }
        }
        catch ( BackupFailedException e )
        {
            // Backup failed, usually due to IO error
        }
    }
}
