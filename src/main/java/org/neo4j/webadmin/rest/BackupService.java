package org.neo4j.webadmin.rest;

import static org.neo4j.rest.domain.JsonHelper.jsonToMap;
import static org.neo4j.webadmin.rest.WebUtils.addHeaders;
import static org.neo4j.webadmin.rest.WebUtils.buildExceptionResponse;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.neo4j.rest.domain.JsonRenderers;
import org.neo4j.webadmin.domain.BackupStatusRepresentation;
import org.neo4j.webadmin.domain.NoSuchPropertyException;
import org.neo4j.webadmin.domain.ServerProperties;
import org.neo4j.webadmin.task.BackupFoundationTask;

/**
 * Lays the groundwork for online backups, allows triggering of backup jobs,
 * exposes backup status.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
@Path( "/server/backup" )
public class BackupService
{

    protected ServerProperties properties;

    //
    // CONSTRUCT
    //

    public BackupService() throws IOException
    {
        properties = ServerProperties.getInstance();
    }

    @GET
    @Produces( MediaType.APPLICATION_JSON )
    @Path( "/status" )
    public synchronized Response status()
    {

        try
        {
            // Is backup enabled?

            BackupStatusRepresentation.Status status;
            try
            {
                status = properties.get( "backup.enabled" ).equals( "true" ) ? BackupStatusRepresentation.Status.ENABLED
                        : BackupStatusRepresentation.Status.DISABLED;
            }
            catch ( NoSuchPropertyException e )
            {
                status = BackupStatusRepresentation.Status.DISABLED;
            }

            // Is there some sort of action running?

            BackupStatusRepresentation.CurrentAction currentAction = BackupStatusRepresentation.CurrentAction.IDLE;
            Date actionStarted = null;
            Date actionEta = null;

            try
            {

                if ( BackupFoundationTask.getInstance().isRunning() )
                {
                    currentAction = BackupStatusRepresentation.CurrentAction.CREATING_FOUNDATION;
                    actionStarted = BackupFoundationTask.getInstance().getStarted();
                    actionEta = BackupFoundationTask.getInstance().getEta();
                }

            }
            catch ( IllegalStateException e )
            {
                /* NOP */
            }

            BackupStatusRepresentation backupInfo = new BackupStatusRepresentation(
                    status, currentAction, actionStarted, actionEta );
            String entity = JsonRenderers.DEFAULT.render( backupInfo );

            return addHeaders(
                    Response.ok( entity, JsonRenderers.DEFAULT.getMediaType() ) ).build();
        }
        catch ( Exception e )
        {
            return buildExceptionResponse( Status.INTERNAL_SERVER_ERROR,
                    "An unexpected internal server error occurred.", e,
                    JsonRenderers.DEFAULT );
        }
    }

    @POST
    @Path( "/backupfolder/create" )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_FORM_URLENCODED )
    public Response formInitBackupFolder( @FormParam( "value" ) String data )
    {
        return initBackupFolder( data );
    }

    @POST
    @Path( "/backupfolder/create" )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    public Response initBackupFolder( String data )
    {
        try
        {
            Map<String, Object> args = jsonToMap( data );

            if ( !args.containsKey( "backup_folder" ) )
            {
                throw new IllegalArgumentException(
                        "No backup folder specified." );
            }

            return null;
        }
        catch ( Exception e )
        {
            return buildExceptionResponse( Status.INTERNAL_SERVER_ERROR,
                    "An unexpected internal server error occurred.", e,
                    JsonRenderers.DEFAULT );
        }
    }
}
