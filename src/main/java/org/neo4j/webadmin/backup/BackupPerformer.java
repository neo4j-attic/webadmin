package org.neo4j.webadmin.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.onlinebackup.Backup;
import org.neo4j.onlinebackup.Neo4jBackup;
import org.neo4j.rest.domain.DatabaseBlockedException;
import org.neo4j.rest.domain.DatabaseLocator;
import org.neo4j.webadmin.domain.BackupFailedException;
import org.neo4j.webadmin.domain.NoBackupFoundationException;
import org.neo4j.webadmin.properties.ServerProperties;

public class BackupPerformer
{

    public final static String LOGICAL_LOG_REGEX = "\\.v[0-9]+$";

    /**
     * Keep track of what directories are currently being used, to avoid
     * multiple backup jobs working in the same directory at once.
     */
    private static final Set<File> lockedPaths = Collections.synchronizedSet( new HashSet<File>() );

    public static void doBackup( File backupPath )
            throws NoBackupFoundationException, BackupFailedException,
            DatabaseBlockedException
    {
        ensurePathIsLocked( backupPath );

        try
        {
            // Naive check to see if folder is initialized
            // I don't want to add an all-out check here, it'd be better
            // for the Neo4jBackup class to throw an exception.
            if ( backupPath.listFiles() == null
                 || backupPath.listFiles().length == 0
                 || !( new File( backupPath, "neostore" ) ).exists() )
            {
                throw new NoBackupFoundationException(
                        "No foundation in: " + backupPath.getAbsolutePath() );
            }

            // Perform backup
            GraphDatabaseService genericDb = DatabaseLocator.getGraphDatabase();

            if ( genericDb instanceof EmbeddedGraphDatabase )
            {

                Backup backup = Neo4jBackup.allDataSources(
                        (EmbeddedGraphDatabase) genericDb,
                        backupPath.getAbsolutePath() );

                backup.doBackup();
            }
            else
            {
                throw new UnsupportedOperationException(
                        "Performing backups on non-local databases is currently not supported." );
            }
        }
        catch ( IOException e )
        {
            throw new BackupFailedException(
                    "IOException while performing backup, see nested.", e );
        }
        finally
        {
            lockedPaths.remove( backupPath );
        }
    }

    public static void doBackupFoundation( File backupPath )
            throws BackupFailedException
    {
        ensurePathIsLocked( backupPath );

        try
        {

            File mainDbPath = new File( DatabaseLocator.getDatabaseLocation() ).getAbsoluteFile();

            setupBackupFolders( backupPath );

            boolean wasRunning = DatabaseLocator.databaseIsRunning();

            if ( wasRunning )
            {
                DatabaseLocator.shutdownAndBlockGraphDatabase();
            }

            cpTree( mainDbPath, backupPath );

            ServerProperties.getInstance().set( "keep_logical_logs", "true" );

            if ( wasRunning )
            {
                DatabaseLocator.unblockGraphDatabase();
                DatabaseLocator.getGraphDatabase();
            }
        }
        catch ( IOException e )
        {
            throw new BackupFailedException(
                    "IOException while creating backup foundation, see nested.",
                    e );
        }
        catch ( DatabaseBlockedException e )
        {
            e.printStackTrace();
        }
        finally
        {
            lockedPaths.remove( backupPath );
        }
    }

    //
    // INTERNALS
    //

    /**
     * Creates any folders not existing on the backupPath, deletes any files in
     * the bottom folder.
     */
    private static void setupBackupFolders( File backupPath )
    {
        // Delete any pre-existing files in backup folder (if it is a folder)
        // if ( backupPath.exists() )
        // {
        // if ( backupPath.isDirectory() )
        // {
        // delTree( backupPath );
        // }
        //
        // backupPath.delete();
        // }

        // Create new, empty folder
        backupPath.mkdirs();
    }

    /**
     * Try to add a given file to the lockedPaths set.
     * 
     * @param path
     * @return true if path was added, false if path was already locked.
     */
    private static synchronized boolean lockPath( File path )
    {
        if ( lockedPaths.contains( path ) )
        {
            return false;
        }
        else
        {
            lockedPaths.add( path );
            return true;
        }
    }

    /**
     * Runs until it is able to lock a given path.
     * 
     * @param path
     */
    private static void ensurePathIsLocked( File path )
    {
        try
        {
            while ( !lockPath( path ) )
            {
                Thread.sleep( 13 );
            }
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e );
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
    private static void cpTree( File src, File target ) throws IOException
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
