package org.neo4j.webadmin.domain;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.webadmin.rest.JmxService;

public class JmxServiceRepresentation extends RootRepresentation
{
    public JmxServiceRepresentation( URI baseUri )
    {
        super( baseUri );
        this.baseUri = this.baseUri + JmxService.ROOT_PATH;
    }

    public Object serialize()
    {
        Map<String, Object> def = new HashMap<String, Object>();
        Map<String, Object> resources = new HashMap<String, Object>();

        resources.put( "domains", baseUri + JmxService.DOMAINS_PATH );
        resources.put( "domain", baseUri + JmxService.DOMAIN_PATH );
        resources.put( "bean", baseUri + JmxService.BEAN_PATH );
        resources.put( "query", baseUri + JmxService.QUERY_PATH );
        resources.put( "kernelquery", baseUri + JmxService.KERNEL_NAME_PATH );

        def.put( "resources", resources );
        return def;
    }
}
