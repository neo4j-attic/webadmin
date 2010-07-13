package org.neo4j.webadmin.gremlin;

import com.tinkerpop.gremlin.GremlinEvaluator;

/**
 * Builds gremlin evaluators that come pre-packaged with astonishing connective
 * powers. Such powers include, but are not limited to, connecting to the REST
 * neo4j instance running under the hood of the webadmin system.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class GremlinFactory
{

    protected volatile static boolean initiated = false;

    public static GremlinEvaluator createGremlinEvaluator()
    {
        GremlinEvaluator ge = new GremlinEvaluator();
        return ge;
    }

    protected synchronized void ensureInitiated()
    {
        if ( initiated == false )
        {
            new GremlinGarbageCollector();
        }
    }
}
