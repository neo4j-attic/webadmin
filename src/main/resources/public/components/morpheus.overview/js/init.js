morpheus.provide("morpheus.components.overview");

/**
 * TODO: This is planned for removal. Instead, there will be a list of servers presented on the dashboard page.
 */
morpheus.components.overview = (function($, undefined) {
	
	var me = {};
	
	// 
	// PRIVATE
	//
	
	me.basePage = $("<div></div>");
	me.ui = {};
	me.initiated = false;
	me.servers   = false;
	
	me.serverInstances = [];
	
	me.init = function() {
	    
	    if( me.servers !== false ) {
	        me.initServers(me.servers);
	    }
	    
	    me.initiated = true;
	    
	};
	
	me.getPage = function() {
		return me.basePage;
	};
	
	me.initServers = function(servers) {
	    if( me.initiated === false ) {
	        // Wait until system is initiated to make sure all components are
            // on-board
	        me.servers = servers;
	        return;
	    }
	    
	    for(var i = 0, l=servers.length; i < l; i++ ) {
	        var server = morpheus.components.Lifecycle(servers[i], "components/morpheus.overview/templates/server.tp");
	        me.serverInstances.push(server);
	        me.ui.serverList.append( server.getWidget() );
	    }
	};
	
	me.pageShown = function() {
	   
	    // Refresh server status
	    for(var i = 0, l=me.serverInstances.length; i < l; i++ ) {
	        me.serverInstances[i].check();
	    }
	    
	}; 
	
	//
	// CONSTRUCT
	//
	
	me.basePage.setTemplateURL("components/morpheus.overview/templates/overview.tp");
    me.basePage.processTemplate();
    
    me.ui.serverList = $('.mor_overview_server-list', me.basePage);
	
    // Keep track of when servers are available
    if( morpheus.neo4j && morpheus.neo4j.loaded ) {
        me.initServers( morpheus.neo4j.servers() );
    } else {
        morpheus.event.bind( "morpheus.servers.loaded", function(ev) {
            me.initServers(ev.data.servers);
        });
    }
    
	//
	// PUBLIC INTERFACE
	//
	
	me.api = {
			init : me.init,
			getPage : me.getPage,
			pageShown : me.pageShown
	};
	
	return me.api;
	
})(jQuery);

//
// REGISTER STUFF
//

morpheus.ui.addPage("morpheus.overview",morpheus.components.overview);
morpheus.ui.mainmenu.add("Servers","morpheus.overview");

morpheus.event.bind("morpheus.init", morpheus.components.overview.init );
morpheus.event.bind("morpheus.ui.page.changed", function(ev) {
    // Remove the current server from state if the overview page is shown (since
    // it shows several servers at a time).
    if(ev.data === "morpheus.overview") {
        $.bbq.removeState("s");
        morpheus.components.overview.pageShown();
    }
} );

