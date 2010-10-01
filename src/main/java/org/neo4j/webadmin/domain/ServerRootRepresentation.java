package org.neo4j.webadmin.domain;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.remote.RemoteGraphDatabase;
import org.neo4j.rest.domain.DatabaseBlockedException;
import org.neo4j.rest.domain.DatabaseLocator;
import org.neo4j.webadmin.rest.BackupService;
import org.neo4j.webadmin.rest.ConfigService;
import org.neo4j.webadmin.rest.ConsoleService;
import org.neo4j.webadmin.rest.ExportService;
import org.neo4j.webadmin.rest.ImportService;
import org.neo4j.webadmin.rest.JmxService;
import org.neo4j.webadmin.rest.LifecycleService;
import org.neo4j.webadmin.rest.MonitorService;

public class ServerRootRepresentation extends RootRepresentation
{

    public enum Mode
    {
        EMBEDDED
    }

    private Mode mode;

    public ServerRootRepresentation( URI baseUri, Mode mode )
    {
        super( baseUri );
        this.mode = mode;
    }

    public Object serialize()
    {
        Map<String, Object> def = new HashMap<String, Object>();
        Map<String, Object> services = new HashMap<String, Object>();

        GraphDatabaseService currentDb;
        try
        {
            currentDb = DatabaseLocator.getGraphDatabase();
            if ( currentDb instanceof EmbeddedGraphDatabase )
            {

                services.put( "backup", baseUri + BackupService.ROOT_PATH );
                services.put( "config", baseUri + ConfigService.ROOT_PATH );
                services.put( "importing", baseUri + ImportService.ROOT_PATH );
                services.put( "exporting", baseUri + ExportService.ROOT_PATH );
                services.put( "console", baseUri + ConsoleService.ROOT_PATH );
                services.put( "jmx", baseUri + JmxService.ROOT_PATH );
                services.put( "lifecycle", baseUri + LifecycleService.ROOT_PATH );
                services.put( "monitor", baseUri + MonitorService.ROOT_PATH );

            }
            else if ( currentDb instanceof RemoteGraphDatabase )
            {
                // services.put( "backup", baseUri + BackupService.ROOT_PATH );
                services.put( "importing", baseUri + ImportService.ROOT_PATH );
                services.put( "config", baseUri + ConfigService.ROOT_PATH );
                services.put( "exporting", baseUri + ExportService.ROOT_PATH );
                services.put( "console", baseUri + ConsoleService.ROOT_PATH );
                // services.put( "monitor", baseUri + MonitorService.ROOT_PATH
                // );
            }
        }
        catch ( DatabaseBlockedException e )
        {
            services.put( "lifecycle", baseUri + LifecycleService.ROOT_PATH );
        }

        def.put( "services", services );
        return def;
    }
}
