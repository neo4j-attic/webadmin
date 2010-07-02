package org.neo4j.webadmin.rest;

import static org.neo4j.webadmin.rest.WebUtils.addHeaders;

import java.util.HashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

    protected ServerProperties properties = ServerProperties.INSTANCE;

    /**
     * Get a full list of available settings.
     */
    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public synchronized Response listAll()
    {

        String entity = JsonRenderers.DEFAULT.render( properties );

        return addHeaders(
                Response.ok( entity, JsonRenderers.DEFAULT.getMediaType() ) ).build();

    }

    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    @Path( "/" )
    public Response jsonSetMany( String data )
    {
        return setMany( null );
    }

    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_FORM_URLENCODED )
    @Path( "/" )
    public Response formSetValueJSON( @FormParam( "value" ) String data )
    {
        return setMany( null );
    }

    // 
    // INTERNALS
    //

    protected Response setMany( HashMap<String, String> values )
    {
        return null;
    }

}
