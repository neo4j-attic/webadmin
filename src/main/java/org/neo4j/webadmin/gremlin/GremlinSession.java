package org.neo4j.webadmin.gremlin;

import java.util.Date;
import java.util.List;

import com.tinkerpop.gremlin.GremlinEvaluator;

/**
 * A wrapper that connects a given gremlin instance to some specific web client.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class GremlinSession
{

    /**
     * Keep track of the last time this was used.
     */
    protected Date lastTimeUsed = new Date();

    /**
     * The gremlin evaluator instance beeing wrapped.
     */
    protected GremlinEvaluator gremlin = GremlinFactory.createGremlinEvaluator();

    //
    // PUBLIC
    //

    /**
     * Evaluate a gremlin statement.
     */
    @SuppressWarnings( "unchecked" )
    public synchronized List<String> evaluate( String line )
    {
        return (List<String>) gremlin.evaluate( line );
    }

    /**
     * Destroy the internal gremlin evaluator and replace it with a clean slate.
     */
    public synchronized void reset()
    {
        this.gremlin = GremlinFactory.createGremlinEvaluator();
    }

    /**
     * Get the number of milliseconds this worker has been idle.
     */
    public long getIdleTime()
    {
        return ( new Date() ).getTime() - lastTimeUsed.getTime();
    }

}
