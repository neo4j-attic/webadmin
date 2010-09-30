package org.neo4j.webadmin.rest;

import static org.neo4j.webadmin.rest.WebUtils.addHeaders;
import static org.neo4j.webadmin.utils.GraphDatabaseUtils.shutdownLocalDatabase;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.neo4j.rest.WebServerFactory;
import org.neo4j.rest.domain.JsonRenderers;
import org.neo4j.webadmin.console.ConsoleSessions;
import org.neo4j.webadmin.domain.LifecycleRepresentation;
import org.neo4j.webadmin.domain.LifecycleServiceRepresentation;

/**
 * REST service to start, stop and restart the neo4j backend.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
@Path( LifecycleService.ROOT_PATH )
public class LifecycleService
{

    public static final String ROOT_PATH = "/lifecycle";
    public static final String STATUS_PATH = "/status";
    public static final String START_PATH = "/start";
    public static final String STOP_PATH = "/stop";
    public static final String RESTART_PATH = "/restart";

    /**
     * TODO: This is a bad way of keeping track of the status of the neo4j
     * server, it would be better to add this capability to DatabaseLocator,
     * which actually has the capability to check if the server is running or
     * not.
     */
    protected static volatile LifecycleRepresentation.Status serverStatus = LifecycleRepresentation.Status.RUNNING;

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public Response getServiceDefinition( @Context UriInfo uriInfo )
    {

        String entity = JsonRenderers.DEFAULT.render( new LifecycleServiceRepresentation(
                uriInfo.getBaseUri() ) );

        return addHeaders(
                Response.ok( entity, JsonRenderers.DEFAULT.getMediaType() ) ).build();
    }

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @Path( STATUS_PATH )
    public synchronized Response status()
    {

        LifecycleRepresentation status = new LifecycleRepresentation(
                serverStatus, LifecycleRepresentation.PerformedAction.NONE );
        String entity = JsonRenderers.DEFAULT.render( status );

        return addHeaders(
                Response.ok( entity, JsonRenderers.DEFAULT.getMediaType() ) ).build();

    }

    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Path( START_PATH )
    public synchronized Response start()
    {
        LifecycleRepresentation status;

        if ( serverStatus != LifecycleRepresentation.Status.RUNNING )
        {
            int restPort = WebServerFactory.getDefaultWebServer().getPort();
            WebServerFactory.getDefaultWebServer().startServer( restPort );
            ConsoleSessions.destroyAllSessions();
            status = new LifecycleRepresentation(
                    LifecycleRepresentation.Status.RUNNING,
                    LifecycleRepresentation.PerformedAction.STARTED );
        }
        else
        {
            status = new LifecycleRepresentation(
                    LifecycleRepresentation.Status.RUNNING,
                    LifecycleRepresentation.PerformedAction.NONE );
        }

        serverStatus = LifecycleRepresentation.Status.RUNNING;

        String entity = JsonRenderers.DEFAULT.render( status );

        return addHeaders(
                Response.ok( entity, JsonRenderers.DEFAULT.getMediaType() ) ).build();
    }

    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Path( STOP_PATH )
    public synchronized Response stop()
    {
        LifecycleRepresentation status;

        if ( serverStatus != LifecycleRepresentation.Status.STOPPED )
        {
            try
            {
                WebServerFactory.getDefaultWebServer().stopServer();
            }
            catch ( NullPointerException e )
            {
                // REST server was not running
            }
            shutdownLocalDatabase();
            status = new LifecycleRepresentation(
                    LifecycleRepresentation.Status.STOPPED,
                    LifecycleRepresentation.PerformedAction.STOPPED );
        }
        else
        {
            status = new LifecycleRepresentation(
                    LifecycleRepresentation.Status.STOPPED,
                    LifecycleRepresentation.PerformedAction.NONE );
        }

        serverStatus = LifecycleRepresentation.Status.STOPPED;
        String entity = JsonRenderers.DEFAULT.render( status );

        return addHeaders(
                Response.ok( entity, JsonRenderers.DEFAULT.getMediaType() ) ).build();
    }

    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Path( RESTART_PATH )
    public synchronized Response restart()
    {

        try
        {
            WebServerFactory.getDefaultWebServer().stopServer();
        }
        catch ( NullPointerException e )
        {
            // REST server was not running
        }
        shutdownLocalDatabase();

        int restPort = WebServerFactory.getDefaultWebServer().getPort();

        WebServerFactory.getDefaultWebServer().startServer( restPort );
        ConsoleSessions.destroyAllSessions();

        LifecycleRepresentation status = new LifecycleRepresentation(
                LifecycleRepresentation.Status.RUNNING,
                LifecycleRepresentation.PerformedAction.RESTARTED );
        String entity = JsonRenderers.DEFAULT.render( status );

        serverStatus = LifecycleRepresentation.Status.RUNNING;

        return addHeaders(
                Response.ok( entity, JsonRenderers.DEFAULT.getMediaType() ) ).build();
    }
}
