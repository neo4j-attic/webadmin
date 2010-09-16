package org.neo4j.webadmin.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.neo4j.webadmin.task.ImportTask;

/**
 * Handles importing graphml into the underlying database, either by uploading
 * or file or by providing a url to a graphml file.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
@Path( ImportService.ROOT_PATH )
public class ImportService
{

    public static final String ROOT_PATH = "/server/import";
    public static final String IMPORT_UPLOAD_PATH = "";
    public static final String IMPORT_URL_PATH = "/url";

    @GET
    // @Produces( MediaType.APPLICATION_JSON )
    // @Consumes( MediaType.APPLICATION_JSON )
    @Path( IMPORT_URL_PATH )
    public Response importFromUrl()
    {

        ImportTask task = new ImportTask(
                "http://github.com/tinkerpop/gremlin/raw/master/data/graph-example-2.xml" );
        task.run();
        return null;
    }

}
