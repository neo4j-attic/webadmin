morpheus.provide("morpheus.components.server.backup.init");

$.require( "components/morpheus.server.backup/js/StatusTracker.js" );

/**
 * A component that handles setting up and triggering online backups.
 * 
 * Init module.
 */
morpheus.components.server.backup.init = (function($, undefined) {
    
    var me = {};
    
    me.basePage = $("<div></div>");
    me.ui = {};
    
    me.uiLoaded  = false;
    me.serverChanged = false;
    me.server = null;
    
    me.currentBackupPath = "";
    
    me.visible = false;
    
    //
    // PUBLIC
    //
    
    me.public = {
            
            getPage :  function() {
                return me.basePage;
            },
            
            pageChanged : function(ev) {
                
                if(ev.data === "morpheus.server.backup") {
                    
                    me.visible = true;
                    
                    if( me.uiLoaded === false ) {
                        me.uiLoaded = true;
                        me.basePage.setTemplateURL("components/morpheus.server.backup/templates/index.tp");
                    	me.loadBackupPath();
                        me.render();
                    } else if( me.serverChanged ) {
                    	me.serverChanged = false;
                    	me.loadBackupPath();
                        me.render();
                        me.trackStatus();
                    }
                    
                } else {
                    me.visible = false;
                }
            },
            
            serverChanged : function(ev) {
                
                me.server = ev.data.server;
                me.currentBackupPath = "";
                
                if( me.visible === true ) {

                	// Load current backup path
                	me.loadBackupPath();
                    me.render();
                    me.trackStatus();
                    
                } else {
                	me.serverChanged = true;
                }
                
            },
            
            init : function() {
            	
            }
            
    };
    
    // 
    // PRIVATE
    //
    
    me.render = function() {
    	
        me.basePage.processTemplate({
            server : me.server
        });
        me.updateUiBackupPath();
        
    };
    
    me.loadBackupPath = function() {
    	morpheus.components.server.config.get("general.backup.path", function(data) {
    		me.currentBackupPath = data.value;
    		me.updateUiBackupPath();
    	}); 
    };
    
    me.updateUiBackupPath = function() {
    	$('.mor_backup_path').val(me.currentBackupPath);
		$('button.mor_backup_setpathbutton').attr('disabled', 'disabled');
		
		if(me.currentBackupPath.length > 0) {
			$('button.mor_backup_triggerbutton').removeAttr('disabled');
		} else {
			$('button.mor_backup_triggerbutton').attr('disabled', 'disabled');
		}
    };
    
    /**
     * This is a callback for StatusTracker, called every time status changes.
     */
    me.statusChanged = function(data) {
    	if( data.current_action === "WAITING_FOR_FOUNDATION" ) {
    		
    		$('p.mor_backup_status').html("");
    		$('button.mor_backup_triggerbutton').attr('disabled', 'disabled');
    		$('div.mor_backup_foundationbox').show();
    		
    		// No more polling, please.
    		return false;
    	} else if ( data.current_action === "BACKING_UP" ) {
    		
    		$('p.mor_backup_status').html("Performing backup..");
    		$('button.mor_backup_triggerbutton').attr('disabled', 'disabled');
    		$('div.mor_backup_foundationbox').hide();
    		
    		// Keep polling, please.
    		return true;
    	} else if ( data.current_action === "CREATING_FOUNDATION" ) {
    		
    		$('p.mor_backup_status').html("Copying files and preparing your database for online backups..");
    		$('button.mor_backup_triggerbutton').attr('disabled', 'disabled');
    		$('div.mor_backup_foundationbox').hide();
    		
    		// Keep polling, please.
    		return true;
    	}
    	
    	$('p.mor_backup_status').html("");
		$('button.mor_backup_triggerbutton').removeAttr('disabled');
		$('div.mor_backup_foundationbox').hide();
    	
    	return false;
    };
    
    /**
     * Create a statustracker and trigger it to check status of the backup process.
     */
    me.trackStatus = function() {
    	var statusTracker = morpheus.components.server.backup.StatusTracker(me.server, me.statusChanged);
		
		setTimeout((function(statusTracker) {
			return function() {
				statusTracker.run();
			};
		})(statusTracker),400);
    };
    
    //
    // CONSTRUCT
    //
    
    $('input.mor_backup_path').live('change',function(ev) {
    	 var el = $(ev.target);
    	 if( el.val().trim() === me.currentBackupPath ) {
    		 $('button.mor_backup_setpathbutton').attr('disabled', 'disabled');
    		 
    		 if( me.currentBackupPath.length > 0) {
    			 $('button.mor_backup_triggerbutton').removeAttr('disabled');
    		 }
    		 
    	 } else {
    		 $('button.mor_backup_setpathbutton').removeAttr('disabled');
    		 $('button.mor_backup_triggerbutton').attr('disabled', 'disabled');
    	 }
    });
    
    $('button.mor_backup_setpathbutton').live('click',function(ev) {
    	$('input.mor_backup_path').attr('disabled', 'disabled');
    	
    	morpheus.components.server.config.set("general.backup.path", $('input.mor_backup_path').val(), function(result) {
    		$('input.mor_backup_path').removeAttr('disabled');
    		
    		if ( result === true ) {
    			me.currentBackupPath = $('input.mor_backup_path').val();
    			me.updateUiBackupPath();
    		} 
    	});
    	
    });
    
    $('button.mor_backup_triggerbutton').live('click', function(ev) {
    	
    	$('div.mor_backup_foundationbox').hide();
    	
    	me.server.admin.post("backup/trigger", function(data) {
    		$('p.mor_backup_status').html("Checking status..");
    		
    		me.trackStatus();
    	});
    	
    });
    
    $('button.mor_backup_foundation_triggerbutton').live('click', function(ev) {
    	
    	$('div.mor_backup_foundationbox').hide();
    	
    	me.server.admin.post("backup/triggerfoundation", function(data) {
    		$('p.mor_backup_status').html("Checking status..");
    		
    		me.trackStatus();
    	});
    	
    });

    
    return me.public;
    
})(jQuery);

//
// REGISTER STUFF
//

morpheus.ui.addPage("morpheus.server.backup",morpheus.components.server.backup.init);
morpheus.ui.mainmenu.add("Backups","morpheus.server.backup", null, "server");

morpheus.event.bind("morpheus.init", morpheus.components.server.backup.init.init);
morpheus.event.bind("morpheus.ui.page.changed", morpheus.components.server.backup.init.pageChanged);
morpheus.event.bind("morpheus.server.changed",  morpheus.components.server.backup.init.serverChanged);