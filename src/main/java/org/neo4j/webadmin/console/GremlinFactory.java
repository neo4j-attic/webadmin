package org.neo4j.webadmin.console;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.rest.domain.DatabaseBlockedException;
import org.neo4j.rest.domain.DatabaseLocator;
import org.neo4j.webadmin.domain.MockIndexService;

import com.tinkerpop.blueprints.pgm.TransactionalGraph;
import com.tinkerpop.blueprints.pgm.impls.neo4j.Neo4jGraph;
import com.tinkerpop.gremlin.GremlinScriptEngine;

/**
 * Builds gremlin evaluators that come pre-packaged with astonishing connective
 * powers. Such powers include, but are not limited to, connecting to the REST
 * neo4j instance running under the hood of the webadmin system.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
@SuppressWarnings( "restriction" )
public class GremlinFactory
{

    protected volatile static boolean initiated = false;

    public static TransactionalGraph getGremlinWrappedGraph()
            throws DatabaseBlockedException
    {
        GraphDatabaseService dbInstance = DatabaseLocator.getGraphDatabase();
        System.out.println("GremlinFactory: " + dbInstance);
        TransactionalGraph graph;
        try
        {
            graph = new Neo4jGraph( dbInstance,
                    DatabaseLocator.getIndexService() );

        }
        catch ( UnsupportedOperationException e )
        {
            // Tempary until indexing is implemented in webadmin for remote
            // databases
            graph = new Neo4jGraph( dbInstance, new MockIndexService() );
        }

        return graph;
    }

    public static ScriptEngine createGremlinScriptEngine()
    {
        try
        {
            ScriptEngine engine = new GremlinScriptEngine();

            // Inject the local database
            TransactionalGraph graph = getGremlinWrappedGraph();

            engine.getBindings( ScriptContext.ENGINE_SCOPE ).put( "$_g", graph );

            try
            {
                engine.getBindings( ScriptContext.ENGINE_SCOPE ).put( "$_",
                        graph.getVertex( 0l ) );
            }
            catch ( Exception e )
            {
                // Om-nom-nom
            }

            return engine;
        }
        catch ( Throwable e )
        {
            // Pokemon catch b/c fails here get hidden until the server exits.
            e.printStackTrace();
            return null;
        }
    }

    protected synchronized void ensureInitiated()
    {
        if ( initiated == false )
        {
            new ConsoleGarbageCollector();
        }
    }
}
