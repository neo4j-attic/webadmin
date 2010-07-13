package org.neo4j.webadmin.gremlin.functions;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.jxpath.ExpressionContext;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.rest.WebServer;
import org.neo4j.rest.domain.DatabaseLocator;
import org.neo4j.webadmin.Main;
import org.neo4j.webadmin.gremlin.tmpimpl.Neo4jGraphTemp;

import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.gremlin.functions.Function;

/**
 * A gremlin extension function for loading the running neo4j db.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class LoadDbFunction implements Function
{

    public String getName()
    {
        return "loadLocalDb";
    }

    public Graph invoke( ExpressionContext arg0, Object[] arg1 )
    {

        GraphDatabaseService dbInstance;
        try
        {
            dbInstance = DatabaseLocator.getGraphDatabase( new URI(
                    WebServer.getLocalhostBaseUri( Main.restPort ) ) );

            return new Neo4jGraphTemp( dbInstance,
                    DatabaseLocator.getIndexService( dbInstance ) );
        }
        catch ( URISyntaxException e )
        {
            // FIXME: Throw proper exception
            return null;
        }
    }
}
