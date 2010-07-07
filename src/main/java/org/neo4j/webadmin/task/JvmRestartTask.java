package org.neo4j.webadmin.task;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.neo4j.rest.WebServer;
import org.neo4j.rest.domain.DatabaseLocator;
import org.neo4j.webadmin.AdminServer;
import org.neo4j.webadmin.Main;
import org.neo4j.webadmin.PlatformUtils;
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
                WebServer.INSTANCE.stopServer();
                DatabaseLocator.shutdownGraphDatabase( new URI(
                        WebServer.getLocalhostBaseUri( Main.restPort ) ) );
                AdminServer.INSTANCE.stopServer();

                if ( PlatformUtils.useBatScripts() )
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
        catch ( URISyntaxException e )
        {
            e.printStackTrace();
        }
    }
}
