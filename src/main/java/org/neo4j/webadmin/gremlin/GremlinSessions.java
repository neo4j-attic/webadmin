package org.neo4j.webadmin.gremlin;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps track of currently running gremlin sessions. Each one is associated
 * with a web client.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class GremlinSessions
{

    protected static ConcurrentHashMap<String, GremlinSession> sessions = new ConcurrentHashMap<String, GremlinSession>();

    //
    // PUBLIC
    //

    public static GremlinSession getSession( String sessionId )
    {
        ensureSessionExists( sessionId );
        return sessions.get( sessionId );
    }

    //
    // INTERNALS
    //

    protected static void ensureSessionExists( String sessionId )
    {
        if ( !sessions.containsKey( sessionId ) )
        {
            sessions.put( sessionId, new GremlinSession() );
        }
    }

}
