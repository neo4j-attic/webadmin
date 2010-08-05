package org.neo4j.webadmin.rest;

import static org.neo4j.webadmin.rest.WebUtils.addHeaders;
import static org.neo4j.webadmin.rest.WebUtils.buildExceptionResponse;

import java.io.IOException;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.neo4j.rest.domain.JsonRenderers;
import org.neo4j.webadmin.domain.BackupStatusRepresentation;
import org.neo4j.webadmin.domain.NoBackupPathException;
import org.neo4j.webadmin.domain.ServerProperties;
import org.neo4j.webadmin.task.BackupFoundationTask;
import org.neo4j.webadmin.task.BackupTask;
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
    protected static final String ROOT_PATH = "/server/backup";
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
            catch ( NoBackupPathException e )
            {
                /* NOP */
            }

            try
            {

                if ( BackupTask.getInstance().isRunning() )
                {
                    currentAction = BackupStatusRepresentation.CurrentAction.BACKING_UP;
                    actionStarted = BackupTask.getInstance().getStarted();
                    actionEta = BackupTask.getInstance().getEta();
                }
                else if ( BackupTask.getInstance().needsFoundation() )
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
    @Path( "/trigger" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response triggerBackup()
    {
        try
        {

            DeferredTask.defer( BackupTask.getInstance() );
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
    @Path( "/triggerfoundation" )
    @Produces( MediaType.APPLICATION_JSON )
    public Response triggerBackupFoundation()
    {
        try
        {

            DeferredTask.defer( BackupFoundationTask.getInstance() );
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
