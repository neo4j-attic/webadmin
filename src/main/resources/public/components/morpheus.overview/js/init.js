morpheus.provide("morpheus.components.overview");

morpheus.components.overview = (function($, undefined) {
	
	var me = {};
	
	// 
	// PRIVATE
	//
	
	me.basePage = $("<div></div>");
	me.ui = {};
	me.initiated = false;
	me.servers   = false;
	
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
	        // Wait until system is initiated to make sure all components are on-board
	        me.servers = servers;
	        return;
	    }
	    
	    for(var i = 0, l=servers.length; i < l; i++ ) {
	        me.ui.serverList.append( morpheus.components.Lifecycle(servers[i], "components/morpheus.overview/templates/server.tp").getWidget() );
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
			getPage : me.getPage
	};
	
	return me.api;
	
})(jQuery);

//
// REGISTER STUFF
//

morpheus.ui.addPage("morpheus.overview",morpheus.components.overview);
morpheus.ui.mainmenu.addItem("Overview","morpheus.overview");

morpheus.event.bind("morpheus.init", morpheus.components.overview.init );
