package org.neo4j.webadmin.domain;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.webadmin.rest.MonitorService;

public class MonitorServiceRepresentation extends RootRepresentation
{
    public MonitorServiceRepresentation( URI baseUri )
    {
        super( baseUri );
        this.baseUri = this.baseUri + MonitorService.ROOT_PATH;
    }

    public Object serialize()
    {
        Map<String, Object> def = new HashMap<String, Object>();
        Map<String, Object> resources = new HashMap<String, Object>();

        resources.put( "latest_data", baseUri + MonitorService.DATA_PATH );
        resources.put( "data_from", baseUri + MonitorService.DATA_FROM_PATH );
        resources.put( "data_period", baseUri + MonitorService.DATA_SPAN_PATH );

        def.put( "resources", resources );
        return def;
    }
}
