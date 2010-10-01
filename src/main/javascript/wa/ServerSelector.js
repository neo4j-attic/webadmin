
/**
 * Handles the server picking UI, adding and removing servers.
 * 
 * If only one server is available, this will automatically set that server as the current one.
 */
wa.ServerSelector = (function($, undefined) {
	
	var me = {};
	
	// 
	// PRIVATE
	//
	
	me.reload = function() {

		var servers = wa.Servers.getServers();
		var currentServer = wa.Servers.getCurrentServer();
		
		if( currentServer == null ) {
		    for( var key in servers ) {
		        $.bbq.pushState( {"s":key} );
		        break;
		    }
		}
		
		var list = $("#mor_servers ul.mor_servers_list");
		list.empty();
		
		for(var i = 0, l=servers.length; i < l; i++ ) {
			var extraClasses = "";
			if( currentServer && servers[i] === currentServer ) {
				extraClasses = " current";
			}
			list.append('<li class="mor_servers_server"><a class="'+extraClasses+'" href="#p=morpheus.monitor&s='+servers[i].getName()+'">'+servers[i].getName()+'</a></li>');
	    }

		//list.append('<li class="mor_servers_add"><a class="mor_servers_add_button" href="#">+</a></li>');
	};
	
	//
	// CONSTRUCT
	//
	
	// Keep track of when servers are available
    if( wa.Servers.isLoaded() ) {
    	me.reload();
    } else {
        wa.bind( "servers.loaded", function(ev) { me.reload(); });
    }

    wa.bind("servers.current.changed",  function() { me.reload(); });
    wa.bind("servers.changed",  function() { me.reload(); });
    
    $("a.mor_servers_add_button").live("click",function(ev){
    	ev.preventDefault();
    	wa.ui.Dialog.showUsingTemplate("Add new server","templates/addserver.tp");
    });
    
    $("#mor_servers_add_save_button").live("click", function(ev) {
    	ev.preventDefault();
    	var name = $("#mor_servers_add_name").val();
    	var url = $("#mor_servers_add_url").val();
    	
    	if( name.length < 1 ) {
    		$("#mor_servers_add_name").addClass("error");
    	} else {
    		$("#mor_servers_add_name").removeClass("error");
    	}
    	
    	if( url.length < 1 ) {
    		$("#mor_servers_add_url").addClass("error");
    	} else {
    		$("#mor_servers_add_url").removeClass("error");
    	}
    	
    	if( url.length > 0 && name.length > 0 ) {
    		wa.ui.Dialog.close();
    		morpheus.neo4jHandler.addServer(name, url);
    		morpheus.neo4jHandler.save();
    	}
    });
	
	return {};
	
})(jQuery);