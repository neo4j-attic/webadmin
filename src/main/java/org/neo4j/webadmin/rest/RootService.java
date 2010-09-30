package org.neo4j.webadmin.rest;

import static org.neo4j.webadmin.rest.WebUtils.addHeaders;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.neo4j.rest.domain.JsonRenderers;
import org.neo4j.webadmin.domain.ServerRootRepresentation;
import org.neo4j.webadmin.domain.ServerRootRepresentation.Mode;

/**
 * Serves info to clients about what is available on this management server.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
@Path( RootService.ROOT_PATH )
public class RootService
{

    public static final String ROOT_PATH = "/";

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public Response getServiceDefinition( @Context UriInfo uriInfo )
    {
        Mode serverMode = Mode.EMBEDDED;

        String entity = JsonRenderers.DEFAULT.render( new ServerRootRepresentation(
                uriInfo.getBaseUri(), serverMode ) );

        return addHeaders(
                Response.ok( entity, JsonRenderers.DEFAULT.getMediaType() ) ).build();
    }

}
