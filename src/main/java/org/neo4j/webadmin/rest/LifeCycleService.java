package org.neo4j.webadmin.rest;

import static org.neo4j.webadmin.rest.WebUtils.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.neo4j.rest.domain.JsonRenderers;
import org.neo4j.webadmin.domain.LifecycleRepresentation;

/**
 * REST service to start, stop and restart the neo4j backend.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 *
 */
@Path("/server")
public class LifeCycleService {

	// Just making sure this works..
	@GET
    @Produces(MediaType.TEXT_HTML)
	public String root() {
		return "Hello world!";
	}
	
	@POST
    @Consumes( MediaType.APPLICATION_JSON )
    @Produces( MediaType.APPLICATION_JSON )
    @Path("/start")
	public Response start() {
		String entity = JsonRenderers.DEFAULT.render( new LifecycleRepresentation( LifecycleRepresentation.Status.RUNNING ) );
		return addHeaders( Response.ok( entity, JsonRenderers.DEFAULT.getMediaType() ) ).build();
	}
}
