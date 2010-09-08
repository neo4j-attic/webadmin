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

public class ScheduleBackupFunctionalTest
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
        WebResource createResource = client.resource( TestUtil.SERVER_BASE
                                                      + BackupService.ROOT_PATH
                                                      + BackupService.JOBS_PATH );

        String properJSON = "{" + "\"name\":\"Daily\","
                            + "\"cronExpression\":\"0 0 0 ? 0 0\","
                            + "\"autoFoundation\":\"true\","
                            + "\"backupPath\":\"backup1\"}";

        ClientResponse scheduleResponse = createResource.type(
                MediaType.APPLICATION_JSON ).accept( MediaType.APPLICATION_JSON ).entity(
                properJSON ).put( ClientResponse.class );

        assertEquals( 200, scheduleResponse.getStatus() );
    }

    @Test
    public void shouldGet400WhenSendingMalformedJSON()
    {
        Client client = Client.create();
        WebResource createResource = client.resource( TestUtil.SERVER_BASE
                                                      + BackupService.ROOT_PATH
                                                      + BackupService.JOBS_PATH );

        String badJSON = "this:::isNot::JSON}";

        ClientResponse scheduleResponse = createResource.type(
                MediaType.APPLICATION_JSON ).accept( MediaType.APPLICATION_JSON ).entity(
                badJSON ).put( ClientResponse.class );

        System.out.println( scheduleResponse );
        assertEquals( 400, scheduleResponse.getStatus() );
    }
}
