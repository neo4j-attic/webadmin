package org.neo4j.webadmin.gremlin;

import java.util.Collection;

/**
 * Remove gremlin sessions that have been idle for too long.
 * 
 * Based on Webling garbage collector by Pavel A. Yaskevich.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class GremlinGarbageCollector extends Thread
{

    long updateInterval = 3000000; // 50 minutes
    long maxIdleInterval = 1790000; // 29 minutes

    GremlinGarbageCollector()
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

            Collection<String> sessionIds = GremlinSessions.getSessionIds();

            for ( String sessionId : sessionIds )
            {
                // Make sure session exists (otherwise
                // GremlinSessions.getSession() will create it)
                if ( GremlinSessions.hasSession( sessionId ) )
                {
                    // If idle time is above our threshold
                    if ( GremlinSessions.getSession( sessionId ).getIdleTime() > maxIdleInterval )
                    {
                        // Throw the GremlinSession instance to the wolves
                        GremlinSessions.destroySession( sessionId );
                    }
                }
            }
        }
    }

}
