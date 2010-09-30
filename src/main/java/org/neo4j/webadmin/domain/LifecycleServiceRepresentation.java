package org.neo4j.webadmin.domain;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.webadmin.rest.LifecycleService;

public class LifecycleServiceRepresentation extends RootRepresentation
{
    public LifecycleServiceRepresentation( URI baseUri )
    {
        super( baseUri );
        this.baseUri = this.baseUri + LifecycleService.ROOT_PATH;
    }

    public Object serialize()
    {
        Map<String, Object> def = new HashMap<String, Object>();
        Map<String, Object> resources = new HashMap<String, Object>();

        resources.put( "status", baseUri + LifecycleService.STATUS_PATH );
        resources.put( "start", baseUri + LifecycleService.START_PATH );
        resources.put( "stop", baseUri + LifecycleService.STOP_PATH );
        resources.put( "restart", baseUri + LifecycleService.RESTART_PATH );

        def.put( "resources", resources );
        return def;
    }
}
