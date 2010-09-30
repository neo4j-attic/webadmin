morpheus.provide("morpheus.components.io.export");

/**
 * Handles exporting the database.
 */
morpheus.components.io.exporting = (function($, undefined) {
	
	var me = {};
	
	//
	// PRIVATE
	//
	
	// 
	// LISTEN TO THE WORLD
	//
	
	$("button.mor_io_export_button").live("click",function(ev) {
		ev.preventDefault();

		$(".mor_io_export_error_wrap").hide();
		$(".mor_io_export_button_wrap").hide();
		$(".mor_io_export_progress_wrap").show();
		
		var server = morpheus.Servers.getCurrentServer();
		server.manage.exporting.all(function(data){
			$(".mor_io_export_button_wrap").show();
			$(".mor_io_export_progress_wrap").hide();
			
			var url = data.url;
			window.open(url,'Neo4j export download','');
			
		});
		
	});
	
	return {};
	
})(jQuery);