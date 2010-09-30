package org.neo4j.webadmin.rest;

import static org.neo4j.rest.domain.JsonHelper.jsonToMap;
import static org.neo4j.webadmin.rest.WebUtils.addHeaders;
import static org.neo4j.webadmin.rest.WebUtils.buildExceptionResponse;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.neo4j.rest.domain.JsonRenderers;
import org.neo4j.webadmin.domain.ImportServiceRepresentation;
import org.neo4j.webadmin.task.ImportTask;

import com.sun.jersey.multipart.BodyPartEntity;
import com.sun.jersey.multipart.FormDataMultiPart;
import com.sun.jersey.multipart.MultiPart;

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

    public static final String URL_KEY = "url";

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    public Response getServiceDefinition( @Context UriInfo uriInfo )
    {

        String entity = JsonRenderers.DEFAULT.render( new ImportServiceRepresentation(
                uriInfo.getBaseUri() ) );

        return addHeaders(
                Response.ok( entity, JsonRenderers.DEFAULT.getMediaType() ) ).build();
    }

    /**
     * Import a graphml file from any given URL. This includes local files.
     * 
     * @param json
     * @return
     */
    @POST
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    @Path( IMPORT_URL_PATH )
    public Response importFromUrl( String json )
    {

        try
        {
            Map<String, Object> req = jsonToMap( json );

            if ( req.containsKey( URL_KEY ) )
            {
                ImportTask task = new ImportTask( req.get( URL_KEY ).toString() );
                task.run();
            }
            else
            {
                throw new IllegalArgumentException(
                        "You have to specify a url." );
            }

            return addHeaders( Response.ok() ).build();
        }
        catch ( Exception e )
        {
            return buildExceptionResponse( Status.BAD_REQUEST,
                    "Request failed.", e, JsonRenderers.DEFAULT );
        }
    }

    @POST
    @Consumes( "multipart/form-data" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response importFromStream( @Context HttpHeaders headers,
            MultiPart rawMultiPart )
    {
        try
        {
            FormDataMultiPart formData = (FormDataMultiPart) rawMultiPart;

            String redirect = formData.getField( "redirect" ).getValue().toString();
            BodyPartEntity bpe = (BodyPartEntity) formData.getField( "file" ).getEntity();
            InputStream file = bpe.getInputStream();

            ImportTask task = new ImportTask( file );
            task.run();
            file.close();

            URI redirectTo = new URI( redirect );

            return addHeaders( Response.seeOther( redirectTo ) ).build();

        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return buildExceptionResponse( Status.BAD_REQUEST,
                    "Request failed.", e, JsonRenderers.DEFAULT );
        }
    }
}
