package org.neo4j.webadmin.rest;

import static org.neo4j.webadmin.rest.WebUtils.addHeaders;
import static org.neo4j.webadmin.utils.GraphDatabaseUtils.shutdownLocalDatabase;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.neo4j.rest.WebServer;
import org.neo4j.rest.domain.JsonRenderers;
import org.neo4j.webadmin.Main;
import org.neo4j.webadmin.domain.LifecycleRepresentation;
import org.neo4j.webadmin.gremlin.GremlinSessions;

/**
 * REST service to start, stop and restart the neo4j backend.
 * 
 * TODO: This currently starts and stops the rest Grizzly server rather
 * directly, it might be more preferrable to move this functionality into the
 * actual REST interface.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
@Path( "/server" )
public class LifeCycleService
{

    /**
     * TODO: This is a bad way of keeping track of the status of the neo4j
     * server, it would be better to add this capability to DatabaseLocator,
     * which actually has the capability to check if the server is running or
     * not.
     */
    protected static volatile LifecycleRepresentation.Status serverStatus = LifecycleRepresentation.Status.RUNNING;

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @Path( "/status" )
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
    @Path( "/start" )
    public synchronized Response start()
    {
        LifecycleRepresentation status;

        if ( serverStatus != LifecycleRepresentation.Status.RUNNING )
        {
            WebServer.INSTANCE.startServer( Main.restPort );
            GremlinSessions.destroyAllSessions();
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
    @Path( "/stop" )
    public synchronized Response stop()
    {
        LifecycleRepresentation status;

        if ( serverStatus != LifecycleRepresentation.Status.STOPPED )
        {
            WebServer.INSTANCE.stopServer();
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
    @Path( "/restart" )
    public synchronized Response restart()
    {

        WebServer.INSTANCE.stopServer();
        shutdownLocalDatabase();
        WebServer.INSTANCE.startServer( Main.restPort );
        GremlinSessions.destroyAllSessions();

        LifecycleRepresentation status = new LifecycleRepresentation(
                LifecycleRepresentation.Status.RUNNING,
                LifecycleRepresentation.PerformedAction.RESTARTED );
        String entity = JsonRenderers.DEFAULT.render( status );

        serverStatus = LifecycleRepresentation.Status.RUNNING;

        return addHeaders(
                Response.ok( entity, JsonRenderers.DEFAULT.getMediaType() ) ).build();
    }
}
