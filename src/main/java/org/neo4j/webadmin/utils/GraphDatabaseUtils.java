package org.neo4j.webadmin.utils;

import java.net.URI;
import java.net.URISyntaxException;

import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.rest.domain.DatabaseLocator;
import org.neo4j.webadmin.AdminServer;

/**
 * Utilities for the local underlying graph database.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class GraphDatabaseUtils
{

    public static EmbeddedGraphDatabase getLocalDatabase()
    {
        try
        {
            return (EmbeddedGraphDatabase) DatabaseLocator.getGraphDatabase( new URI(
                    AdminServer.INSTANCE.getBaseUri() ) );
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeException(
                    "FATAL: URI to local db is corrupt, see nested exception. ",
                    e );
        }
    }

    public static void shutdownLocalDatabase()
    {
        try
        {
            DatabaseLocator.shutdownGraphDatabase( new URI(
                    AdminServer.INSTANCE.getBaseUri() ) );
        }
        catch ( URISyntaxException e )
        {
            throw new RuntimeException(
                    "FATAL: URI to local db is corrupt, see nested exception. ",
                    e );
        }
    }

}
