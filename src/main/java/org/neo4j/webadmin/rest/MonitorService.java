package org.neo4j.webadmin.rest;

import static org.neo4j.webadmin.rest.WebUtils.addHeaders;
import static org.neo4j.webadmin.rest.WebUtils.buildExceptionResponse;

import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.neo4j.rest.domain.JsonRenderers;
import org.neo4j.webadmin.domain.RrdDataRepresentation;
import org.neo4j.webadmin.rrd.RrdManager;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.FetchRequest;

/**
 * This exposes data from an internal round-robin database that tracks various
 * system KPIs over time.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
@Path( MonitorService.ROOT_PATH )
public class MonitorService
{
    public static final String ROOT_PATH = "/server/monitor";
    public static final String DATA_PATH = "";
    public static final String DATA_FROM_PATH = "/{start}";
    public static final String DATA_SPAN_PATH = "/{start}/{stop}";

    public static final long MAX_TIMESPAN = 1000 * 60 * 60 * 24 * 14;
    public static final long DEFAULT_TIMESPAN = 1000 * 60 * 60 * 2;

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public Response getData()
    {
        return getData( new Date().getTime() - DEFAULT_TIMESPAN,
                new Date().getTime() );
    }

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @Path( DATA_FROM_PATH )
    public Response getData( @PathParam( "start" ) long start )
    {
        return getData( start, new Date().getTime() );
    }

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @Path( DATA_SPAN_PATH )
    public Response getData( @PathParam( "start" ) long start,
            @PathParam( "stop" ) long stop )
    {

        if ( start >= stop || ( stop - start ) > MAX_TIMESPAN )
        {
            return buildExceptionResponse(
                    Status.BAD_REQUEST,
                    "Start time must be before stop time, and the total time span can be no bigger than "
                            + MAX_TIMESPAN + "ms.",
                    new IllegalArgumentException(), JsonRenderers.DEFAULT );
        }

        try
        {

            FetchRequest request = RrdManager.getRrdDB().createFetchRequest(
                    ConsolFun.AVERAGE, start, stop );

            String entity = JsonRenderers.DEFAULT.render( new RrdDataRepresentation(
                    request.fetchData() ) );

            return addHeaders(
                    Response.ok( entity, JsonRenderers.DEFAULT.getMediaType() ) ).build();
        }
        catch ( Exception e )
        {
            return buildExceptionResponse( Status.INTERNAL_SERVER_ERROR,
                    "SEVERE: Round robin IO error.", e, JsonRenderers.DEFAULT );
        }
    }

}
