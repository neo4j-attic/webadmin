package org.neo4j.webadmin.backup;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import org.neo4j.webadmin.domain.BackupAlreadyRunningException;
import org.neo4j.webadmin.domain.BackupFailedException;
import org.neo4j.webadmin.domain.NoBackupPathException;
import org.neo4j.webadmin.domain.NoSuchPropertyException;
import org.neo4j.webadmin.properties.ServerProperties;
import org.neo4j.webadmin.rest.LifeCycleService;

/**
 * A task for creating the foundation for doing online backups. In effect, this
 * will:
 * <ol>
 * <li>ensure that the file system path provided for the backup database is an
 * empty folder</li>
 * <li>shut down the main database</li>
 * <li>copy the folder structure from the main database to the backup database</li>
 * <li>enable logican logging</li>
 * <li>start up the main database again</li>
 * </ol>
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class ManualBackupFoundationJob implements Runnable
{

    protected static ManualBackupFoundationJob INSTANCE;

    protected Date started;
    protected Date eta;

    protected File backupPath;
    protected File mainDbPath;

    protected boolean running = false;

    protected LifeCycleService lifecycle = new LifeCycleService();

    public static ManualBackupFoundationJob getInstance()
            throws URISyntaxException, IOException
    {

        ServerProperties props = ServerProperties.getInstance();

        File backupPath;

        try
        {
            String strPath = props.get( "general.backup.path" ).getFullValue();
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

        if ( INSTANCE == null
             || !INSTANCE.backupPath.equals( backupPath.getAbsoluteFile() ) )
        {
            INSTANCE = new ManualBackupFoundationJob( backupPath );
        }

        return INSTANCE;
    }

    //
    // CONSTRUCT
    //

    protected ManualBackupFoundationJob( File backupPath )
                                                          throws URISyntaxException
    {
        this.backupPath = backupPath.getAbsoluteFile();
        this.mainDbPath = new File(
                System.getProperty( "org.neo4j.graphdb.location" ) ).getAbsoluteFile();
    }

    //
    // PUBLIC
    //

    public void run()
    {
        try
        {
            if ( this.isRunning() )
            {
                throw new BackupAlreadyRunningException(
                        "Only one backup foundation task can run at a time." );
            }

            this.running = true;
            this.started = new Date();

            BackupPerformer.doBackupFoundation( backupPath );

            ManualBackupJob.getInstance().needFoundation = false;

            this.running = false;
        }
        catch ( IOException e )
        {
            throw new RuntimeException(
                    "IOError when trying to do backup foundation, see nested exception.",
                    e );
        }
        catch ( BackupFailedException e )
        {
            throw new RuntimeException(
                    "Exception when creating backup foundation, see nested exception.",
                    e );
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

}
