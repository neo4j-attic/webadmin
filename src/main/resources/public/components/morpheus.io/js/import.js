morpheus.provide("morpheus.components.io.import");

/**
 * Import a graphml file by specifying a URL.
 */
morpheus.components.io.import= (function($, undefined) {
	
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
	
	$("button.mor_io_urlImport_button").live("click",function(ev) {
		ev.preventDefault();
		
		var url = $(".mor_io_urlImport_url").val();
		$(".mor_io_urlImport_url").val("");
		
		if( url.length > 0 ) {
			
			$(".mor_io_urlImport_error_wrap").hide();
			$(".mor_io_urlImport_button_wrap").hide();
			$(".mor_io_urlImport_progress_wrap").show();
			
			var request = {
				url : url
			};
			
			morpheus.neo4jHandler.currentServer().admin.post("import/url", request ,function(data){
				$(".mor_io_urlImport_button_wrap").show();
				$(".mor_io_urlImport_progress_wrap").hide();
			}, function(error){
				$(".mor_io_urlImport_button_wrap").show();
				$(".mor_io_urlImport_progress_wrap").hide();
				$(".mor_io_urlImport_error_wrap").show();
				$(".mor_io_urlImport_error_wrap").html("Import failed, please see the server logs.");
			});
		}
		
	});
	
	$("input.mor_io_fileImport_button").live("click",function(ev) { 
		
		// Set redirect to correct value right before submitting.
		$("input.mor_io_fileImport_redirect").val(location.href);
		
		$(".mor_io_fileImport_button_wrap").hide();
		$(".mor_io_fileImport_progress_wrap").show();
		
	});
	
	return me.public;
	
})(jQuery);