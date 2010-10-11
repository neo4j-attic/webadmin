package org.neo4j.webadmin;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.rest.domain.DatabaseLocator;

/**
 * Used to get an {@link MBeanServer} instance for the currently connected
 * database. Also contains convinience methods for accessing MBeans for the
 * current database.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
@SuppressWarnings( "restriction" )
public class MBeanServerFactory
{

    /**
     * Used to check if the database has changed. If the database has changed,
     * the MBeanServer will be re-instantiated.
     */
    private static GraphDatabaseService cachedDb;

    /**
     * This will be changed if the database instance has changed, to allow
     * switching over to a remote database. This is used to allow hammering the
     * {@link #getServer()} method.
     */
    private static MBeanServer cachedServer;

    public static MBeanServer getServer()
    {

        GraphDatabaseService db = DatabaseLocator.getGraphDatabase();
        if ( db != cachedDb )
        {

            cachedDb = db;

            if ( db instanceof EmbeddedGraphDatabase )
            {
                cachedServer = ManagementFactory.getPlatformMBeanServer();
            }
            else
            {
                cachedServer = null;
            }

        }
        return cachedServer;

    }

}
