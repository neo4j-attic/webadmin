package org.neo4j.webadmin.task;

import java.io.IOException;

import org.neo4j.rest.WebServerFactory;
import org.neo4j.webadmin.AdminServer;
import org.neo4j.webadmin.utils.GraphDatabaseUtils;
import org.neo4j.webadmin.utils.PlatformUtils;
import org.tanukisoftware.wrapper.WrapperManager;

/**
 * Triggers a full restart of the enclosing JVM upon instantiation.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class JvmRestartTask implements Runnable
{

    public synchronized void run()
    {
        try
        {

            if ( PlatformUtils.isProductionMode() )
            {
                // PRODUCTION MODE
                WrapperManager.restart();
            }
            else
            {
                // DEVELOPMENT MODE

                // Stop running servers
                System.out.println( "JVM Reboot. Shutting down server." );
                WebServerFactory.getDefaultWebServer().stopServer();
                AdminServer.INSTANCE.stopServer();
                GraphDatabaseUtils.shutdownAndBlock();

                if ( PlatformUtils.isWindows() )
                {
                    Runtime.getRuntime().exec( "start.bat" );
                }
                else
                {
                    Runtime.getRuntime().exec( "./start" );
                }

                // Sepukko!
                Runtime.getRuntime().exit( 0 );
            }
        }
        catch ( IOException e )
        {
            // TODO: Attempt to resolve the problems that are resolveable
            e.printStackTrace();
        }
    }
}
