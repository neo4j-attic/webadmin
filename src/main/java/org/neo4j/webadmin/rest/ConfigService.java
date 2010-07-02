package org.neo4j.webadmin.rest;

import static org.neo4j.webadmin.rest.WebUtils.addHeaders;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.neo4j.rest.domain.JsonRenderers;
import org.neo4j.webadmin.domain.ServerProperties;

/**
 * A web service that exposes various configuration settings for a running neo4j
 * REST server.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
@Path( "/server/config" )
public class ConfigService
{

    /**
     * Get a full list of available settings.
     * 
     * @return
     */
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public synchronized Response status()
    {

        String entity = JsonRenderers.DEFAULT.render( ServerProperties.INSTANCE );

        return addHeaders(
                Response.ok( entity, JsonRenderers.DEFAULT.getMediaType() ) ).build();

    }
}
