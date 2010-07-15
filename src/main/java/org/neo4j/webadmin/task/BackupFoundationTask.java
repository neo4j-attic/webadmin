package org.neo4j.webadmin.task;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;

import org.neo4j.webadmin.domain.NoSuchPropertyException;
import org.neo4j.webadmin.domain.ServerProperties;
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
public class BackupFoundationTask implements Runnable
{

    public final static String LOGICAL_LOG_REGEX = "\\.v[0-9]+$";

    protected static BackupFoundationTask INSTANCE;

    protected Date started;
    protected Date eta;

    protected File backupPath;
    protected File mainDbPath;

    protected boolean running = false;

    protected LifeCycleService lifecycle = new LifeCycleService();

    public static BackupFoundationTask getInstance() throws URISyntaxException,
            IOException
    {

        ServerProperties props = ServerProperties.getInstance();

        File backupPath;

        try
        {
            backupPath = new File( props.get( "backup.path" ).getValue() );
        }
        catch ( NoSuchPropertyException e )
        {
            throw new IllegalStateException(
                    "You cannot instantiate a BackupFoundationTask without first having set a backup path in ServerProperties." );
        }

        if ( INSTANCE == null
             || INSTANCE.backupPath != backupPath.getAbsoluteFile() )
        {
            INSTANCE = new BackupFoundationTask( backupPath );
        }

        return INSTANCE;
    }

    //
    // CONSTRUCT
    //

    protected BackupFoundationTask( File backupPath ) throws URISyntaxException
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
        if ( this.isRunning() )
        {
            throw new IllegalStateException(
                    "Only one backup foundation task can run at a time." );
        }

        this.running = true;
        this.started = new Date();

        // 1 - Calculate approximate eta
        this.calculateEta();

        // 2 - Create folder structure
        this.setupBackupFolders();

        // 3 - Shut down main database
        lifecycle.stop();

        // 4 - Perform copy

        // 5 - Ensure logical logging is on

        // 6 - Start up main db again
        lifecycle.start();

        this.running = false;
    }

    public synchronized Date getEta()
    {
        return new Date( this.eta.getTime() );
    }

    public synchronized Date getStarted()
    {
        return new Date( this.started.getTime() );
    }

    public boolean isRunning()
    {
        return this.running;
    }

    //
    // INTERNALS
    //

    /**
     * Creates any folders not existing on the backupPath, deletes any files in
     * the bottom folder.
     */
    protected void setupBackupFolders()
    {
        // Delete any pre-existing files in backup folder (if it is a folder)
        if ( backupPath.exists() )
        {
            if ( backupPath.isDirectory() )
            {
                delTree( backupPath );
            }

            backupPath.delete();
        }

        // Create new, empty folder
        backupPath.mkdirs();
    }

    protected void calculateEta()
    {
        long size = getFileSize( this.mainDbPath );
        System.out.println( "Backup size:" + size );
    }

    /**
     * Get the size of a file or folder and any sub-files. This ignores logical
     * log files, ie. files that end with .v[DIGIT], like mylog.v0
     * 
     * @param file
     * @return
     */
    protected long getFileSize( File file )
    {
        if ( file.isDirectory() )
        {
            long size = 0l;
            for ( File subFile : file.listFiles() )
            {
                size += getFileSize( subFile );
            }
            return size;
        }
        else
        {
            // Ignore logical log files.
            if ( file.getName().matches( LOGICAL_LOG_REGEX ) )
            {
                return 0l;
            }
            else
            {
                return file.length();
            }
        }
    }

    protected void delTree( File file )
    {
        for ( File childFile : file.listFiles() )
        {
            if ( childFile.isDirectory() )
            {
                delTree( childFile );
            }
            childFile.delete();
        }
    }
}
