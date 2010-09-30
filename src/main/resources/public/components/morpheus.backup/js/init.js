morpheus.provide("morpheus.components.backup.init");

/**
 * A component that handles setting up and triggering online backups.
 * 
 * Init module.
 */
morpheus.components.backup.init = (function($, undefined) {
    
    var me = {};
    
    me.basePage = $("<div></div>");
    me.ui = {};
    
    me.uiLoaded  = false;
    me.serverChanged = false;
    me.schedule = null;
    
    me.currentBackupPath = "";
    
    me.prevAction = null;
    
    me.visible = false;
    
    //
    // PUBLIC
    //
    
    me.api = {
            
            getPage :  function() {
                return me.basePage;
            },
            
            pageChanged : function(ev) {
                
                if(ev.data === "morpheus.backup") {
                    
                    me.visible = true;
                    
                    if( me.uiLoaded === false ) {
                        me.uiLoaded = true;
                        me.basePage.setTemplateURL("components/morpheus.backup/templates/index.tp");
                        me.render();
                    	me.loadBackupData();
                    } else if( me.serverChanged ) {
                    	me.serverChanged = false;
                        me.render();
                    	me.loadBackupData();
                    }
                    
                } else {
                    me.visible = false;
                }
            },
            
            serverChanged : function(ev) {
                
                me.currentBackupPath = "";
                
                if( me.visible === true ) {

                	// Load current backup path
                	me.loadBackupData();
                    me.render();
                    
                } else {
                	me.serverChanged = true;
                }
                
            },
            
            init : function() {
                me.loadBackupData();
            }
            
    };
    
    // 
    // PRIVATE
    //
    
    function serverManager() {
        return morpheus.Servers.getCurrentServer().manage;
    }
    
    me.render = function() {
    	
        me.basePage.processTemplate({
            server : me.server
        });
        
        delete(me.ui.jobList);
    };
    
    me.loadBackupData = function() {
    	
        var server = morpheus.Servers.getCurrentServer();
        
        if( server ) { 
        	server.manage.config.getProperty("general.backup.path", function(prop) {
        		me.currentBackupPath = prop.value;
        		me.updateUiBackupPath();
        	});
        	
        	updateJobs();
        }
    };
    
    me.updateUiBackupPath = function() {
    	$('.mor_backup_path').val(me.currentBackupPath);
		$('button.mor_backup_setpathbutton').attr('disabled', 'disabled');
		
		me.hideStatus();
		
		if(me.currentBackupPath.length > 0) {
			$('button.mor_backup_triggerbutton').removeAttr('disabled');
		} else {
			$('button.mor_backup_triggerbutton').attr('disabled', 'disabled');
		}
    };
    
    me.updateBackupJobUi = function(data) {
    	
    	if( ! me.ui.jobList ) {
    		me.ui.jobList = $("ul.mor_backup_job_list");
    		me.ui.jobList.setTemplateURL("components/morpheus.backup/templates/schedule.tp")
    	}
    	
    	var data = morpheus.components.backup.parseJobData(data);
    	
    	me.ui.jobList.processTemplate({
    		jobs : data.jobList
    	});
    	
    };
    
    /**
     * Show a simple status message. This will hide the big backup
     * foundation-message.
     */
    me.showStatus = function(message) {
    	$('p.mor_backup_status').html(message);
		$('p.mor_backup_status').show();
		$('div.mor_backup_foundationbox').hide();
    };
    
    /**
     * Hide all status messages.
     */
    me.hideStatus = function() {
    	$('p.mor_backup_status').hide();
		$('div.mor_backup_foundationbox').hide();
    };

    function updateJobs() {
        if( me.visible ) {
            var server = morpheus.Servers.getCurrentServer();
            if(server && server.manage.backup.available) {
                server.manage.backup.getJobs(me.updateBackupJobUi);
            }
        }
    }
    
    //
    // CONSTRUCT
    //
    
    $('input.mor_backup_path').live('keyup',function(ev) {
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
    	
    	var server = morpheus.Servers.getCurrentServer();
        
        server.manage.config.setProperty("general.backup.path", $('input.mor_backup_path').val(), function(result) {
            $('input.mor_backup_path').removeAttr('disabled');
            
            me.currentBackupPath = $('input.mor_backup_path').val();
            me.updateUiBackupPath();
            
        });
    	
    });
    
    $('button.mor_backup_triggerbutton').live('click', function(ev) {
    	
        var server = morpheus.Servers.getCurrentServer();
        
        me.showStatus("Performing backup..");
        $('button.mor_backup_triggerbutton').attr('disabled', 'disabled');
        
        server.manage.backup.triggerManual(function(data) {
            if(data !== false) {
                me.showStatus("Backup successful!");
                $('button.mor_backup_triggerbutton').removeAttr('disabled');
            } else {
                $('p.mor_backup_status').hide();
                $('button.mor_backup_triggerbutton').attr('disabled', 'disabled');
                $('div.mor_backup_foundationbox').show();
            }
        });
        
    });
    
    $('button.mor_backup_foundation_triggerbutton').live('click', function(ev) {
    	
    	if( confirm("This will DESTROY any files in '" + me.currentBackupPath + "', are you sure?") ) {
	    	
    	    var server = morpheus.Servers.getCurrentServer();
    	    
    	    me.showStatus("Creating foundation..");
            
            server.manage.backup.triggerManualFoundation(function(data) {
                me.showStatus("Foundation successful! Your database is now ready for online backups.");
                $('button.mor_backup_triggerbutton').removeAttr('disabled');            
                
            });
    	}
    	
    });
    
    $('button.mor_backup_add_job').live('click',function(ev) {
    	
    	morpheus.ui.Dialog.showUsingTemplate("New backup job","components/morpheus.backup/templates/job.tp");
    	
    });
    
    $('button.mor_job_dialog_save').live('click', function(ev){
    	
    	// Validate form
    	var validators = [
		    { field:'input.mor_job_dialog_name',
		      validator : 'not_empty' },
		    { field:'input.mor_job_dialog_path',
	    	  validator : 'not_empty' },
	    	{ field:'input.mor_job_dialog_cronexp',
	    	  validator : 'not_empty' }
    	];
    	
    	
    	if( morpheus.forms.validateFields(validators) ) {
    		
    		var name = $('input.mor_job_dialog_name').val();
			var path = $('input.mor_job_dialog_path').val();
			var cron = $('input.mor_job_dialog_cronexp').val();
			var autoFoundation = $('input.mor_job_dialog_auto-foundation:checked').length > 0;
    		
    		var id = $("input.mor_job_dialog_id").val();
    		
    		id = id.length > 0 ? id : null;
    		
    		// Save job
    		
    		var server = morpheus.Servers.getCurrentServer();
    		
    		server.manage.backup.setJob(
    		    {
    		        'name': name,
    		        'backupPath':path,
    		        'cronExpression':cron,
    		        'autoFoundation': autoFoundation,
    		        'id':id
    		    },
				function(){
    		        server.manage.backup.getJobs(me.updateBackupJobUi);
				}
    		);
    		
    		morpheus.ui.Dialog.close();
    	}
    	
    });
    
    $('button.mor_backup_job_edit').live('click', function(ev){
        var server = morpheus.Servers.getCurrentServer();
    	server.manage.backup.getJob($(ev.target).closest("li.mor_backup_job").find(".mor_backup_job_id_value").val(), function(job){
    	    neo4j.log(job);
    		morpheus.ui.Dialog.showUsingTemplate("Edit backup job","components/morpheus.backup/templates/job.tp", job);
    	});
    });
    
    
    $('button.mor_backup_job_delete').live('click', function(ev){
    	if( confirm("Are you sure you want to delete the backup job?") ) {
    	    var server = morpheus.Servers.getCurrentServer();
            server.manage.backup.deleteJob($(ev.target).closest("li.mor_backup_job").find(".mor_backup_job_id_value").val(), updateJobs);
    	}
    });
    
    $('button.mor_backup_job_create_foundation').live('click', function(ev){
    	var id = $(ev.target).closest("li.mor_backup_job").find(".mor_backup_job_id_value").val();
    	var server = morpheus.Servers.getCurrentServer();
        server.manage.backup.getJob(id, function(job){
    		if(job && confirm("This will delete any current backup in " + job.backupPath +". Are you sure?")) {
    			
    			$(".mor_backup_job_info").html("Creating foundation..");
    			$(".mor_backup_job_info").show();
    			
    			server.manage.backup.triggerJobFoundation(job.id, 
    				function(data) {
    					$(".mor_backup_job_info").html("Successfully created foundation.");
    					setTimeout(function() {
    						$(".mor_backup_job_info").hide();
    					}, 2000);
    				});
    			
    		}
    	});
    });
    
    // Update job info at regular intervals
    
    setInterval(updateJobs, 5000);
    
    return me.api;
    
})(jQuery);

//
// REGISTER STUFF
//

morpheus.ui.Pages.add("morpheus.backup",morpheus.components.backup.init);
morpheus.ui.MainMenu.add({ label : "Backup", pageKey:"morpheus.backup", index:4, requiredServices:['backup'], perspectives:['server']});

neo4j.events.bind("morpheus.init", morpheus.components.backup.init.init);
neo4j.events.bind("morpheus.ui.page.changed", morpheus.components.backup.init.pageChanged);
neo4j.events.bind("morpheus.servers.current.changed", morpheus.components.backup.init.serverChanged);