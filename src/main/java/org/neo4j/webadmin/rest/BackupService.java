package org.neo4j.webadmin.rest;

import static org.neo4j.rest.domain.JsonHelper.jsonToMap;
import static org.neo4j.webadmin.rest.WebUtils.addHeaders;
import static org.neo4j.webadmin.rest.WebUtils.buildExceptionResponse;

import java.io.IOException;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.neo4j.rest.domain.JsonParseRuntimeException;
import org.neo4j.rest.domain.JsonRenderers;
import org.neo4j.webadmin.backup.BackupJobDescription;
import org.neo4j.webadmin.backup.BackupManager;
import org.neo4j.webadmin.backup.ManualBackupFoundationJob;
import org.neo4j.webadmin.backup.ManualBackupJob;
import org.neo4j.webadmin.domain.BackupJobDescriptionRepresentation;
import org.neo4j.webadmin.domain.BackupStatusRepresentation;
import org.neo4j.webadmin.domain.NoBackupPathException;
import org.neo4j.webadmin.properties.ServerProperties;
import org.neo4j.webadmin.task.DeferredTask;

/**
 * Lays the groundwork for online backups, allows triggering of backup jobs,
 * exposes backup status.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
@Path( BackupService.ROOT_PATH )
public class BackupService
{
    public static final String ROOT_PATH = "/server/backup";

    public static final String STATUS_PATH = "/status";
    public static final String MANUAL_TRIGGER_PATH = "/trigger";
    public static final String MANUAL_FOUNDATION_TRIGGER_PATH = "/triggerfoundation";
    public static final String JOBS_PATH = "/job";
    public static final String JOB_PATH = JOBS_PATH + "/{name}";

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
    @Path( STATUS_PATH )
    public synchronized Response status()
    {

        try
        {
            // Is there some sort of action running?

            BackupStatusRepresentation.CurrentAction currentAction = BackupStatusRepresentation.CurrentAction.IDLE;
            Date actionStarted = null;
            Date actionEta = null;

            try
            {

                if ( ManualBackupFoundationJob.getInstance().isRunning() )
                {
                    currentAction = BackupStatusRepresentation.CurrentAction.CREATING_FOUNDATION;
                    actionStarted = ManualBackupFoundationJob.getInstance().getStarted();
                    actionEta = ManualBackupFoundationJob.getInstance().getEta();
                }

            }
            catch ( IllegalStateException e )
            {
                /* NOP */
            }
            catch ( NoBackupPathException e )
            {
                /* NOP */
            }

            try
            {

                if ( ManualBackupJob.getInstance().isRunning() )
                {
                    currentAction = BackupStatusRepresentation.CurrentAction.BACKING_UP;
                    actionStarted = ManualBackupJob.getInstance().getStarted();
                    actionEta = ManualBackupJob.getInstance().getEta();
                }
                else if ( ManualBackupJob.getInstance().needsFoundation() )
                {
                    currentAction = BackupStatusRepresentation.CurrentAction.WAITING_FOR_FOUNDATION;
                }

            }
            catch ( IllegalStateException e )
            {
                /* NOP */
            }
            catch ( NoBackupPathException e )
            {
                /* NOP */
            }

            BackupStatusRepresentation backupInfo = new BackupStatusRepresentation(
                    currentAction, actionStarted, actionEta );
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
    @Path( MANUAL_TRIGGER_PATH )
    @Produces( MediaType.APPLICATION_JSON )
    public Response triggerBackup()
    {
        try
        {

            DeferredTask.defer( ManualBackupJob.getInstance() );
            return addHeaders( Response.ok() ).build();

        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return buildExceptionResponse( Status.INTERNAL_SERVER_ERROR,
                    "An unexpected internal server error occurred.", e,
                    JsonRenderers.DEFAULT );
        }
    }

    @POST
    @Path( MANUAL_FOUNDATION_TRIGGER_PATH )
    @Produces( MediaType.APPLICATION_JSON )
    public Response triggerBackupFoundation()
    {
        try
        {

            DeferredTask.defer( ManualBackupFoundationJob.getInstance() );
            return addHeaders( Response.ok() ).build();

        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return buildExceptionResponse( Status.INTERNAL_SERVER_ERROR,
                    "An unexpected internal server error occurred.", e,
                    JsonRenderers.DEFAULT );
        }
    }

    @GET
    @Path( JOBS_PATH )
    @Produces( MediaType.APPLICATION_JSON )
    public Response listBackupJobs()
    {
        try
        {
            String entity = JsonRenderers.DEFAULT.render( BackupManager.INSTANCE.getConfig() );

            return addHeaders(
                    Response.ok( entity, JsonRenderers.DEFAULT.getMediaType() ) ).build();

        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return buildExceptionResponse( Status.INTERNAL_SERVER_ERROR,
                    "An unexpected internal server error occurred.", e,
                    JsonRenderers.DEFAULT );
        }
    }

    @PUT
    @Path( JOBS_PATH )
    @Produces( MediaType.APPLICATION_JSON )
    @Consumes( MediaType.APPLICATION_JSON )
    public Response setBackupJob( String json )
    {
        try
        {
            BackupJobDescription jobDesc = BackupJobDescriptionRepresentation.deserialize( jsonToMap( json ) );

            BackupManager.INSTANCE.getConfig().setJobDescription( jobDesc );
            BackupManager.INSTANCE.restart();

            return addHeaders( Response.ok() ).build();
        }
        catch ( JsonParseRuntimeException e )
        {
            return buildExceptionResponse( Status.BAD_REQUEST,
                    "The json data you provided is invalid.", e,
                    JsonRenderers.DEFAULT );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return buildExceptionResponse( Status.INTERNAL_SERVER_ERROR,
                    "An unexpected internal server error occurred.", e,
                    JsonRenderers.DEFAULT );
        }
    }

    @DELETE
    @Path( JOB_PATH )
    @Produces( MediaType.APPLICATION_JSON )
    public Response deleteBackupJob( @PathParam( "name" ) String name )
    {
        try
        {
            BackupManager.INSTANCE.getConfig().removeJobDescription( name );
            BackupManager.INSTANCE.restart();

            return addHeaders( Response.ok() ).build();

        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return buildExceptionResponse( Status.INTERNAL_SERVER_ERROR,
                    "An unexpected internal server error occurred.", e,
                    JsonRenderers.DEFAULT );
        }
    }
}
