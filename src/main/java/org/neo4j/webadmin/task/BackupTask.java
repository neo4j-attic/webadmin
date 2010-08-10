package org.neo4j.webadmin.task;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.onlinebackup.Backup;
import org.neo4j.onlinebackup.Neo4jBackup;
import org.neo4j.rest.WebServer;
import org.neo4j.rest.domain.DatabaseLocator;
import org.neo4j.webadmin.Main;
import org.neo4j.webadmin.domain.BackupAlreadyRunningException;
import org.neo4j.webadmin.domain.NoBackupPathException;
import org.neo4j.webadmin.domain.NoSuchPropertyException;
import org.neo4j.webadmin.properties.ServerProperties;

/**
 * Performs a backup of a running neo4j database.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class BackupTask implements Runnable
{
    protected static BackupTask INSTANCE;

    protected Date started;
    protected Date eta;

    protected File backupPath;
    protected File mainDbPath;

    protected boolean running = false;

    /**
     * This is set to true if an attempt to perform a backup fails due to
     * logical logs not beeing turned on or if the backup folder is not
     * initialized.
     * 
     * It essentially means that you have to run a BackupFoundationTask.
     */
    protected boolean needFoundation = false;

    public static BackupTask getInstance() throws URISyntaxException,
            IOException
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
            INSTANCE = new BackupTask( backupPath );
        }

        return INSTANCE;
    }

    //
    // CONSTRUCT
    //

    public BackupTask( File backupPath )
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
                        "Only one backup task can run at a time." );
            }

            // Naive check to see if folder is initialized
            // I don't want to add an all-out check here, it'd be better
            // for the Neo4jBackup class to throw an exception.
            if ( this.backupPath.listFiles() == null
                 || this.backupPath.listFiles().length == 0
                 || !( new File( this.backupPath, "neostore" ) ).exists() )
            {
                throw new IllegalStateException(
                        "Database has not been copied to backup folder." );
            }

            this.needFoundation = false;
            this.running = true;
            this.started = new Date();

            // Perform backup
            EmbeddedGraphDatabase db = (EmbeddedGraphDatabase) DatabaseLocator.getGraphDatabase( new URI(
                    WebServer.getLocalhostBaseUri( Main.restPort ) ) );

            Backup backup = Neo4jBackup.allDataSources( db,
                    this.backupPath.getAbsolutePath() );

            backup.doBackup();

            this.running = false;
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeException( "Fatal: Database location is corrupt.",
                    e );
        }
        catch ( IllegalStateException e )
        {
            this.running = false;
            this.needFoundation = true;
            System.out.println( "Backup failed, backup folder is not initialized, or logical logs are not turned on." );
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
