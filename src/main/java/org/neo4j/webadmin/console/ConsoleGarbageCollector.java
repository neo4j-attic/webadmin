package org.neo4j.webadmin.console;

import java.util.Collection;

/**
 * Remove gremlin sessions that have been idle for too long.
 * 
 * Based on Webling garbage collector by Pavel A. Yaskevich.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class ConsoleGarbageCollector extends Thread
{

    long updateInterval = 3000000; // 50 minutes
    long maxIdleInterval = 1790000; // 29 minutes

    ConsoleGarbageCollector()
    {
        setDaemon( true );
        start();
    }

    @Override
    public void run()
    {

        while ( true )
        {
            try
            {
                Thread.sleep( updateInterval );
            }
            catch ( InterruptedException e )
            {
            }

            Collection<String> sessionIds = ConsoleSessions.getSessionIds();

            for ( String sessionId : sessionIds )
            {
                // Make sure session exists (otherwise
                // GremlinSessions.getSession() will create it)
                if ( ConsoleSessions.hasSession( sessionId ) )
                {
                    // If idle time is above our threshold
                    if ( ConsoleSessions.getSession( sessionId ).getIdleTime() > maxIdleInterval )
                    {
                        // Throw the GremlinSession instance to the wolves
                        ConsoleSessions.destroySession( sessionId );
                    }
                }
            }
        }
    }

}
