package org.neo4j.webadmin.gremlin;

import java.util.Collection;
import java.util.Iterator;
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

    /**
     * Gets a GremlinSesssion for a given sessionId, creating a GremlinSession
     * if one does not exist.
     */
    public static GremlinSession getSession( String sessionId )
    {
        ensureSessionExists( sessionId );
        return sessions.get( sessionId );
    }

    public static void destroySession( String sessionId )
    {
        sessions.get( sessionId ).die();
        sessions.remove( sessionId );
    }

    public static void destroyAllSessions()
    {
        Iterator<String> keys = sessions.keySet().iterator();
        while ( keys.hasNext() )
        {
            destroySession( keys.next() );
        }
    }

    public static boolean hasSession( String sessionId )
    {
        return sessions.containsKey( sessionId );
    }

    public static Collection<String> getSessionIds()
    {
        return sessions.keySet();
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
