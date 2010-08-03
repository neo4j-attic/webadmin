package org.neo4j.webadmin.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * A proxy to allow cross-site AJAX calls for browsers that do not support it
 * properly.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
@Path( AjaxProxyService.ROOT_PATH )
public class AjaxProxyService
{

    public static final String ROOT_PATH = "/proxy";

    @GET
    public Response get()
    {
        return null;
    }

    @POST
    public Response post()
    {
        return null;
    }

    @PUT
    public Response put()
    {
        return null;
    }

    @DELETE
    public Response delete()
    {
        return null;
    }

}
