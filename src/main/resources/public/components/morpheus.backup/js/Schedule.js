morpheus.provide("morpheus.components.backup.Schedule");

morpheus.components.backup.Schedule = function(server) {
	
	var me = {};
	
	me.server = server;
	me.data = null;
	
	me.public = {
		
		setJob : function( name, path, cron, autoFoundation, id, callback ) {
			
			var entity = {
					name:name,
	                cronExpression:cron,
	                autoFoundation:autoFoundation,
	                backupPath:path,
	                id : id
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
				
					if( data != null ) {
						me.data = data;
						
						for(var i = 0, l = data.jobList.length; i < l; i++) {
							
							// Write last-backup message (used by templates)
							var job = data.jobList[i];
							if( job.log.latestSuccess == null ) {
								job.readableLatestSuccess = "Never";
							} else {
								var now = new Date().getTime();
								var diff = (now - job.log.latestSuccess.timestamp) / 1000;
								
								var readableDiff = "";
								if( diff > 60 * 60 * 24 * 2 ) {
									// Over two days ago, only worry about days
									readableDiff = Math.floor(diff / (60 * 60 * 24)) + " days ago"; 
								} else if( diff > 60 * 60 * 24 ) {
									// Over one day
									readableDiff = "One day and " + Math.floor( (diff - 60 * 60 * 24) / (60 * 60)) + " hours ago";
								} else if( diff > 60 * 60 ) {
									// Over one hour
									readableDiff = Math.floor( diff / (60*60)) + " hours ago";
								} else if( diff > 60 ) {
									// Over one minute
									readableDiff = Math.floor( diff / 60) + " minutes ago";
								} else {
									// Less than a minute
									readableDiff = "Less than a minute ago"
								}
								
								job.readableLatestSuccess = readableDiff;
							}
							
							// Provide easy access to any blocking error (used by templates)
							if( job.log.entries.length > 0 && job.log.entries[0].type === "ERROR") {
								job.error = {
									message   : job.log.entries[0].message,
									timestamp : job.log.entries[0].timestamp,
									code      : job.log.entries[0].code
								};
							} else {
								job.error = false;
							}
						}
					}
					
					cb(me.data);
				
				},
				
				function(error) {
					
					me.data = { jobList : [] };
					cb( me.data );
					
				}
			);
		},
		
		getJob : function(id, cb) {
			
			if( me.data === null ) {
				me.public.getJobs(function() {
					me.public.getJob(id, cb);
				});
			} else {
				
				for(var index in me.data.jobList ) {
					if( me.data.jobList[index].id == id ) {
						return cb(me.data.jobList[index]);
					}
				}
				
				return cb(null);
				
			}
			
		},
		
		deleteJob : function(id, callback ) {
			
			var callback = typeof(callback) === "function" ? callback : function(){};
			
			me.server.admin.del("backup/job/" + id, 
                
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