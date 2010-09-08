morpheus.provide("morpheus.components.server.backup.Schedule");

morpheus.components.server.backup.Schedule = function(server) {
	
	var me = {};
	
	me.server = server;
	me.data = null;
	
	me.public = {
		
		setJob : function( name, path, cron, autoFoundation, callback ) {
			
			var entity = {
					name:name,
	                cronExpression:cron,
	                autoFoundation:autoFoundation,
	                backupPath:path
	        };
			
			var callback = typeof(callback) === "function" ? callback : function(){};
		
			me.server.admin.put("backup/job", entity ,
                
                function(data) {
					callback(true);
				}, function(error) {
					callback(false);
				}
			);
		
		},
		
		getJobs : function(cb) {
			me.server.admin.get("backup/job", 
				function(data) {
				
					me.data = data;
					cb(data);
				
				},
				
				function(error) {
					
					me.data = { jobList : [] };
					cb( me.data );
					
				}
			);
		},
		
		getJob : function(name, cb) {
			
			if( me.data === null ) {
				me.public.getJobs(function() {
					me.public.getJob(name, cb);
				});
			} else {
				
				for(var index in me.data.jobList ) {
					if( me.data.jobList[index].name === name ) {
						return cb(me.data.jobList[index]);
					}
				}
				
				return cb(null);
				
			}
			
		},
		
		deleteJob : function(name, callback ) {
			
			var callback = typeof(callback) === "function" ? callback : function(){};
			
			me.server.admin.del("backup/job/" + name, 
                
                function(data) {
					callback(true);
				}, function(error) {
					callback(false);
				}
			);
			
		}
	};
	
	return me.public;
	
};