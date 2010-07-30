package org.neo4j.webadmin;

import java.io.IOException;

import org.neo4j.helpers.Args;
import org.neo4j.rest.WebServer;
import org.neo4j.webadmin.rrd.RrdManager;
import org.neo4j.webadmin.rrd.RrdSampler;
import org.neo4j.webadmin.utils.GraphDatabaseUtils;

/**
 * Main entry point for the neo4j stand-alone REST system with web
 * administration. This code based on Main class for the REST neo4j
 * distribution.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class Main
{

    public static int restPort;
    public static int adminPort;

    public static void main( String[] strArgs ) throws Exception
    {
        Args args = new Args( strArgs );

        //
        // 1. ARGUMENT HANDLING
        //

        System.setProperty( "org.neo4j.graphdb.location",
                args.get( "dbPath", "neo4j-rest-db" ) );
        System.setProperty( "org.neo4j.webadmin.rrdb.location",
                args.get( "rrdbPath", "neo4j-rrdb" ) );

        restPort = args.getNumber( "restPort", WebServer.DEFAULT_PORT ).intValue();
        adminPort = args.getNumber( "adminPort", AdminServer.DEFAULT_PORT ).intValue();

        String webRoot = args.get( "webRoot", AdminServer.DEFAULT_WEBROOT );

        // We need this to close the graph db backend
        final String restBaseUri = WebServer.getLocalhostBaseUri( restPort );

        //
        // 2. START SERVERS
        //

        WebServer.INSTANCE.startServer( restPort );
        AdminServer.INSTANCE.startServer( adminPort, webRoot );

        System.out.println( "Starting round-robin system state sampler.." );
        RrdSampler.INSTANCE.start();

        System.out.println( String.format( "Running REST at [%s]", restBaseUri ) );
        System.out.println( String.format( "Running admin interface at [%s]",
                AdminServer.getLocalhostBaseUri( adminPort ) ) );
        System.out.println( "\nPress Ctrl-C to kill the server" );

        //
        // 3. AWAIT THE GRIM REAPER
        //

        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                try
                {
                    // Kill the round robin sampler
                    System.out.println( "\nShutting down the round robin database" );
                    RrdSampler.INSTANCE.stop();
                    RrdManager.getRrdDB().close();
                }
                catch ( IOException e )
                {
                    throw new RuntimeException(
                            "IO Error trying to access round robin database path. See nested exception.",
                            e );
                }

                // Kill the REST-server
                System.out.println( "Shutting down the REST server.." );
                WebServer.INSTANCE.stopServer();
                GraphDatabaseUtils.shutdownLocalDatabase();

                // Kill the admin-server
                System.out.println( "Shutting down the admin server.." );
                AdminServer.INSTANCE.stopServer();

                System.out.println( "Shutdown complete." );
            }
        } );

    }

}
