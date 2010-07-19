package org.neo4j.webadmin.gremlin;

import java.net.URI;
import java.net.URISyntaxException;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.rest.WebServer;
import org.neo4j.rest.domain.DatabaseLocator;
import org.neo4j.webadmin.Main;
import org.neo4j.webadmin.gremlin.tmpimpl.Neo4jGraphTemp;

import com.tinkerpop.gremlin.GremlinEngine;

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

    public static ScriptEngine createGremlinScriptEngine()
    {
        try
        {
            ScriptEngine engine = new GremlinEngine();

            // Inject the local database
            GraphDatabaseService dbInstance = DatabaseLocator.getGraphDatabase( new URI(
                    WebServer.getLocalhostBaseUri( Main.restPort ) ) );

            engine.getBindings( ScriptContext.ENGINE_SCOPE ).put(
                    "$_g",
                    new Neo4jGraphTemp( dbInstance,
                            DatabaseLocator.getIndexService( dbInstance ) ) );

            // ge.evaluate(
            // "include 'org.neo4j.webadmin.gremlin.WebAdminFunctions'");

            return engine;
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeException(
                    "Db path is corrupt, see nested exception.", e );
        }
    }

    protected synchronized void ensureInitiated()
    {
        if ( initiated == false )
        {
            new GremlinGarbageCollector();
        }
    }
}
