package org.neo4j.webadmin.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.rest.domain.Representation;

/**
 * Represents current status of the backup sub system.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 * 
 */
public class BackupStatusRepresentation implements Representation
{
    public enum CurrentAction
    {
        IDLE,
        BACKING_UP,
        CREATING_FOUNDATION,
        WAITING_FOR_FOUNDATION
    }

    protected CurrentAction currentAction;
    protected Date started;
    protected Date eta;

    public BackupStatusRepresentation()
    {
        this( CurrentAction.IDLE, null, null );
    }

    public BackupStatusRepresentation( CurrentAction currentAction,
            Date started, Date eta )
    {
        this.currentAction = currentAction;
        this.started = started;
        this.eta = eta;
    }

    public Object serialize()
    {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put( "current_action", currentAction );
        map.put( "started", started != null ? started.getTime() : 0 );
        map.put( "eta", eta != null ? eta.getTime() : 0 );
        return map;
    }

}
