package org.neo4j.webadmin;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.remote.RemoteGraphDatabase;
import org.neo4j.rest.domain.DatabaseBlockedException;
import org.neo4j.rest.domain.DatabaseLocator;
import org.neo4j.webadmin.properties.ServerConfiguration;

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
    private static MBeanServerConnection cachedServer;

    public static MBeanServerConnection getServer()
    {

        GraphDatabaseService db;
        try
        {
            db = DatabaseLocator.getGraphDatabase();
            if ( db != cachedDb )
            {

                cachedDb = db;

                if ( db instanceof EmbeddedGraphDatabase )
                {
                    cachedServer = ManagementFactory.getPlatformMBeanServer();
                }
                else if ( db instanceof RemoteGraphDatabase )
                {
                    try
                    {
                        JMXServiceURL address = new JMXServiceURL(
                                ServerConfiguration.getInstance().get(
                                        "general.jmx.uri" ).getValue() );

                        JMXConnector connector = JMXConnectorFactory.connect(
                                address, null );

                        cachedServer = connector.getMBeanServerConnection();

                    }
                    catch ( MalformedURLException e )
                    {
                        // TODO Show proper error to user.
                        throw new RuntimeException( e );
                    }
                    catch ( IOException e )
                    {
                        throw new RuntimeException(
                                "Unable get JMX access to remote server, monitoring will be disabled.",
                                e );
                    }
                }

            }
        }
        catch ( DatabaseBlockedException e1 )
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return cachedServer;

    }

}
