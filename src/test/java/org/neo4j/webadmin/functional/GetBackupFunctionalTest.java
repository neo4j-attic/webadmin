package org.neo4j.webadmin.functional;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URI;

import javax.ws.rs.core.MediaType;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.rest.WebServerFactory;
import org.neo4j.rest.domain.DatabaseLocator;
import org.neo4j.webadmin.AdminServer;
import org.neo4j.webadmin.TestUtil;
import org.neo4j.webadmin.backup.BackupManager;
import org.neo4j.webadmin.domain.BackupFailedException;
import org.neo4j.webadmin.rest.BackupService;
import org.quartz.SchedulerException;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class GetBackupFunctionalTest
{
    @BeforeClass
    public static void startWebServer() throws IOException, SchedulerException,
            BackupFailedException
    {
        TestUtil.deleteTestDb();
        AdminServer.INSTANCE.startServer();
        BackupManager.INSTANCE.start();
    }

    @AfterClass
    public static void stopWebServer() throws Exception
    {
        AdminServer.INSTANCE.stopServer();
        BackupManager.INSTANCE.stop();
        DatabaseLocator.shutdownGraphDatabase( new URI(
                WebServerFactory.getDefaultWebServer().getBaseUri() ) );
    }

    @Test
    public void shouldGet200ForProperRequest()
    {
        Client client = Client.create();

        WebResource getResource = client.resource( TestUtil.SERVER_BASE
                                                   + BackupService.ROOT_PATH
                                                   + BackupService.JOBS_PATH );

        ClientResponse getResponse = getResource.accept(
                MediaType.APPLICATION_JSON ).get( ClientResponse.class );

        assertEquals( 200, getResponse.getStatus() );

    }

}
