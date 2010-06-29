package org.neo4j.webadmin.domain;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.rest.domain.Representation;

/**
 * Represents current lifecycle status and any action that the user has requested.
 * 
 * @author Jacob Hansson <jacob@voltvoodoo.com>
 *
 */
public class LifecycleRepresentation implements Representation {

	public enum Status {RUNNING, STOPPED}
	public enum Action {STARTED, STOPPED, RESTARTED, NONE}
	
	protected Status currentStatus;
	protected Action actionPerformed;
	
	public LifecycleRepresentation(Status status) {
		this.currentStatus = status;
	}
	
	public LifecycleRepresentation(Status status, Action action) {
		this.actionPerformed = action;
		this.currentStatus = status;
	}
	
	public Object serialize() {
		Map<String, Object> map = new HashMap<String, Object>();
        map.put( "current_status",   currentStatus );
        if( actionPerformed != null ) {
        	map.put( "action_performed", actionPerformed );
        }
        return map;
	}

}
