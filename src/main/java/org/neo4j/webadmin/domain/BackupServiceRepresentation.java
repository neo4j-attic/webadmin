package org.neo4j.webadmin.domain;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.webadmin.rest.BackupService;

public class BackupServiceRepresentation extends RootRepresentation
{
    public BackupServiceRepresentation( URI baseUri )
    {
        super( baseUri );
        this.baseUri = this.baseUri + BackupService.ROOT_PATH;
    }

    public Object serialize()
    {
        Map<String, Object> def = new HashMap<String, Object>();
        Map<String, Object> resources = new HashMap<String, Object>();

        resources.put( "trigger_manual", baseUri
                                         + BackupService.MANUAL_TRIGGER_PATH );
        resources.put( "trigger_manual_foundation",
                baseUri + BackupService.MANUAL_FOUNDATION_TRIGGER_PATH );
        resources.put( "jobs", baseUri + BackupService.JOBS_PATH );
        resources.put( "job", baseUri + BackupService.JOB_PATH );
        resources.put( "trigger_job_foundation",
                baseUri + BackupService.JOB_FOUNDATION_TRIGGER_PATH );

        def.put( "resources", resources );
        return def;
    }

}
