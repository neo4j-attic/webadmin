package org.neo4j.webadmin.rest;

import static org.neo4j.webadmin.rest.WebUtils.addHeaders;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.neo4j.rest.domain.JsonRenderers;
import org.neo4j.webadmin.rrd.RrdManager;
import org.rrd4j.ConsolFun;
import org.rrd4j.core.FetchData;
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
    protected static final String ROOT_PATH = "/server/monitor";

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public Response currentState()
    {

        FetchRequest request = RrdManager.getRrdDB().createFetchRequest(
                ConsolFun.AVERAGE, System.currentTimeMillis() - 100000,
                System.currentTimeMillis() );

        FetchData fetchData;
        try
        {
            fetchData = request.fetchData();
            System.out.println( fetchData.toString() );
        }
        catch ( IOException e1 )
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return addHeaders(
                Response.ok( "", JsonRenderers.DEFAULT.getMediaType() ) ).build();
    }

}
