package org.neo4j.webadmin.utils;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.rest.domain.DatabaseLocator;

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

    public static GraphDatabaseService getLocalDatabase()
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

        GraphDatabaseService db = DatabaseLocator.getGraphDatabase();

        isRunning = true;

        return db;
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
            DatabaseLocator.shutdownGraphDatabase();
            isRunning = false;
        }
    }

}
