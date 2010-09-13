package org.neo4j.webadmin.utils;

import java.net.URI;
import java.net.URISyntaxException;

import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.rest.domain.DatabaseLocator;
import org.neo4j.webadmin.AdminServer;

/**
 * Utilities for the local underlying graph database.
 * 
 * TODO: Implement system-wide "database-is-turned-off-don't-touch-it".
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class GraphDatabaseUtils
{

    private static boolean isRunning = false;
    private static boolean isBlocked = false;

    public static EmbeddedGraphDatabase getLocalDatabase()
    {
        while ( isBlocked )
        {
            try
            {
                Thread.sleep( 13 );
            }
            catch ( InterruptedException e )
            {
                throw new RuntimeException(
                        "Waiting for database to unlock failed, got interruptedexception.",
                        e );
            }
        }

        try
        {
            EmbeddedGraphDatabase db = (EmbeddedGraphDatabase) DatabaseLocator.getGraphDatabase( new URI(
                    AdminServer.INSTANCE.getBaseUri() ) );

            isRunning = true;

            return db;
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeException(
                    "FATAL: URI to local db is corrupt, see nested exception. ",
                    e );
        }
    }

    public static void shutdownLocalDatabase()
    {
        stop();
    }

    /**
     * Turn off the database, and disallow access to it.
     */
    public synchronized static void shutdownAndBlock()
    {
        isBlocked = true;
        stop();
    }

    public static void unblock()
    {
        isBlocked = false;
    }

    public static boolean isRunning()
    {
        return isRunning;
    }

    //
    // INTERNALS
    //

    private static void stop()
    {
        if ( isRunning )
        {
            try
            {
                DatabaseLocator.shutdownGraphDatabase( new URI(
                        AdminServer.INSTANCE.getBaseUri() ) );
                isRunning = false;
            }
            catch ( URISyntaxException e )
            {
                throw new RuntimeException(
                        "FATAL: URI to local db is corrupt, see nested exception. ",
                        e );
            }
        }
    }

}
