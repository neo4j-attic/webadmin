package org.neo4j.webadmin.task;

import static org.neo4j.webadmin.utils.FileUtils.delTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
        try
        {
            if ( this.isRunning() )
            {
                throw new BackupAlreadyRunningException(
                        "Only one backup foundation task can run at a time." );
            }

            this.running = true;
            this.started = new Date();
            BackupTask.getInstance().needFoundation = false;

            // 1 - Calculate approximate eta
            this.calculateEta();

            // 2 - Create folder structure
            this.setupBackupFolders();

            // 3 - Shut down main database
            lifecycle.stop();

            // 4 - Perform copy
            cpTree( mainDbPath, backupPath );

            // 5 - Ensure logical logging is on
            ServerProperties.getInstance().set( "keep_logical_logs", "true" );

            // 6 - Start up main db again
            lifecycle.start();

            this.running = false;
        }
        catch ( IOException e )
        {
            throw new BackupFailedException(
                    "Unable to write to database configuration file, not sure if logical logging is on or not.",
                    e );
        }
        catch ( URISyntaxException e )
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
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

    /**
     * Copy a file system folder/file tree from one spot to another. This
     * implementation will ignore copying logical logs.
     * 
     * @param src
     * @param target
     * @throws IOException
     */
    protected void cpTree( File src, File target ) throws IOException
    {
        if ( src.isDirectory() )
        {

            if ( !target.exists() )
            {
                target.mkdir();
            }

            for ( File childFile : src.listFiles() )
            {
                // Ignore logical log files
                if ( !childFile.getName().matches( LOGICAL_LOG_REGEX ) )
                {
                    cpTree( childFile, new File( target, childFile.getName() ) );
                }
            }
        }
        else
        {
            InputStream in = new FileInputStream( src );
            OutputStream out = new FileOutputStream( target );

            byte[] buf = new byte[1024];

            int len;

            while ( ( len = in.read( buf ) ) > 0 )
            {
                out.write( buf, 0, len );
            }

            in.close();
            out.close();
        }
    }
}
