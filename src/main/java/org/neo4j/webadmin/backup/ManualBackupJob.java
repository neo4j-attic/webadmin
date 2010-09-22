package org.neo4j.webadmin.backup;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.neo4j.webadmin.domain.BackupAlreadyRunningException;
import org.neo4j.webadmin.domain.BackupFailedException;
import org.neo4j.webadmin.domain.NoBackupFoundationException;
import org.neo4j.webadmin.domain.NoBackupPathException;
import org.neo4j.webadmin.domain.NoSuchPropertyException;
import org.neo4j.webadmin.properties.ServerProperties;

/**
 * Performs a backup of a running neo4j database.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class ManualBackupJob implements Runnable
{
    protected static ManualBackupJob INSTANCE;

    protected Date started;
    protected Date eta;

    protected File backupPath;
    protected File mainDbPath;

    protected boolean running = false;

    /**
     * This is set to true if an attempt to perform a backup fails due to
     * logical logs not being turned on or if the backup folder is not
     * initialized.
     * 
     * It essentially means that you have to run a BackupFoundationTask.
     */
    protected boolean needFoundation = false;

    public static ManualBackupJob getInstance() throws IOException
    {

        ServerProperties props = ServerProperties.getInstance();

        File backupPath;

        try
        {
            String strPath = props.get( "general.backup.path" ).getValue();
            if ( strPath.length() > 0 )
            {
                backupPath = new File( strPath );
            }
            else
            {
                throw new NoBackupPathException(
                        "The backup path property is empty, cannot instantiate BackupFoundationTask." );
            }
        }
        catch ( NoSuchPropertyException e )
        {
            throw new NoBackupPathException(
                    "You cannot instantiate a BackupFoundationTask without first having set a backup path in ServerProperties." );
        }

        if ( INSTANCE == null || !INSTANCE.backupPath.equals( backupPath ) )
        {
            INSTANCE = new ManualBackupJob( backupPath );
        }

        return INSTANCE;
    }

    //
    // CONSTRUCT
    //

    public ManualBackupJob( File backupPath )
    {
        this.backupPath = backupPath;
    }

    //
    // PUBLIC
    //

    public synchronized void run()
    {
        try
        {
            if ( this.isRunning() )
            {
                throw new BackupAlreadyRunningException(
                        "Only one manual backup task can run at a time." );
            }

            this.needFoundation = false;
            this.running = true;
            this.started = new Date();

            try
            {
                BackupPerformer.doBackup( this.backupPath );

            }
            finally
            {
                this.running = false;
            }
        }
        catch ( BackupFailedException e )
        {
            e.printStackTrace();
        }
        catch ( NoBackupFoundationException e )
        {
            this.running = false;
            this.needFoundation = true;
        }
    }

    public synchronized Date getEta()
    {
        return new Date( this.eta != null ? this.eta.getTime() : 0 );
    }

    public synchronized Date getStarted()
    {
        return new Date( this.started != null ? this.started.getTime() : 0 );
    }

    public boolean isRunning()
    {
        return this.running;
    }

    /**
     * This returns true if a backup attempt has failed and a
     * BackupFoundationTask is required to make backups possible.
     * 
     * @return
     */
    public boolean needsFoundation()
    {
        return this.needFoundation;
    }
}
