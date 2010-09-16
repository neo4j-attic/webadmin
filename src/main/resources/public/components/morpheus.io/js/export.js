morpheus.provide("morpheus.components.io.export");

/**
 * Handles exporting the database.
 */
morpheus.components.io.export = (function($, undefined) {
	
	var me = {};
	
	//
	// PUBLIC
	//
	
	me.public = {};
	
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
		
		morpheus.neo4jHandler.currentServer().admin.post("export",function(data){
			$(".mor_io_export_button_wrap").show();
			$(".mor_io_export_progress_wrap").hide();
			
			var url = data.url;
			window.open(url,'Neo4j export download','');
			
		}, function(error){
			$(".mor_io_export_button_wrap").show();
			$(".mor_io_export_progress_wrap").hide();
			$(".mor_io_export_error_wrap").show();
			$(".mor_io_export_error_wrap").html("Export failed, please see the server logs.");
		});
		
	});
	
	return me.public;
	
})(jQuery);