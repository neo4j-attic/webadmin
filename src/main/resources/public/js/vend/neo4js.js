var neo4j=neo4j||{};
neo4j.services=neo4j.services||{};
neo4j.cachedFunction=function(f,g,h){var c=null;
var d=null;
var e=h||false;
var i=false;
var a=[];
return function b(){var j=arguments[g];
if(i){j.apply(d,c)
}else{if(a.length==0){arguments[g]=function(){d=this;
c=arguments;
i=true;
for(var k in a){a[k].apply(d,c)
}a=[];
if(e){setTimeout(function(){i=false
},e)
}};
f.apply(this,arguments)
}a.push(j)
}}
};
neo4j.log=function(){if(window.console&&typeof(window.console.log)==="function"){console.log.apply(this,arguments)
}};
neo4j.proxy=function(b,a){return jQuery.proxy(b,a)
};
neo4j.Events=function(a){this.uniqueNamespaceCount=0;
this.handlers={};
this.context=a||{}
};
neo4j.Events.prototype.createUniqueNamespace=function(){return"uniq#"+(this.uniqueNamespaceCount++)
};
neo4j.Events.prototype.bind=function(a,b){if(typeof(this.handlers[a])==="undefined"){this.handlers[a]=[]
}this.handlers[a].push(b)
};
neo4j.Events.prototype.trigger=function(b,d){if(typeof(this.handlers[b])!=="undefined"){var d=d||{};
var e=this.handlers[b];
var c=$.extend({key:b,data:d},this.context);
for(var a=0,f=e.length;
a<f;
a++){setTimeout((function(g){return function(){try{g(c)
}catch(h){neo4j.log("Event handler for event "+b+" threw exception.",h)
}}
})(e[a]),0)
}}};
neo4j.events=new neo4j.Events();
neo4j.jqueryWebProvider={ajax:function(e,b,c,d,a){if(typeof(c)==="function"){a=d;
d=c;
c=null
}setTimeout((function(j,g,h,i,f){if(h===null||h==="null"){h=""
}else{h=JSON.stringify(h)
}return function(){if(neo4j.Web.isCrossDomain(g)&&window.XDomainRequest){if(typeof(f)==="function"){f("Cross-domain requests are available in IE, but are not yet implemented in neo4js.")
}}else{$.ajax({url:g,type:j,data:h,processData:false,success:i,contentType:"application/json",error:function(l){try{if(l.status===200){return i(null)
}}catch(m){}if(typeof(f)==="function"){try{var k=JSON.parse(l.responseText);
f(k,l)
}catch(m){f({},l)
}}else{neo4j.log(l)
}},dataType:"json",beforeSend:function(k){return k
}})
}}
})(e,b,c,d,a),0)
}};
neo4j.Web=function(){var a=neo4j.jqueryWebProvider;
return{get:function(c,d,e,b){return neo4j.Web.ajax("GET",c,d,e,b)
},post:function(c,d,e,b){return neo4j.Web.ajax("POST",c,d,e,b)
},put:function(c,d,e,b){return neo4j.Web.ajax("PUT",c,d,e,b)
},del:function(c,d,e,b){return neo4j.Web.ajax("DELETE",c,d,e,b)
},ajax:function(f,c,d,e,b){return a.ajax(f,c,d,e,b)
},isCrossDomain:function(c){var b=c.indexOf("://");
if(b===-1||b>7){return false
}else{return c.substring(b+3).split("/",1)[0]!==window.location.host
}},setWebProvider:function(b){a=b
},replace:function(b,c){for(var d in c){b=b.replace("{"+d+"}",c[d])
}return b
}}
}();
neo4j.Service=function(){};
neo4j.Service.resourceFactory=function(d){var f=d.urlArgs||[];
var c=f.length;
var g=d.callbackWrap?d.callbackWrap:function(h,i){i(h)
};
var e=d.wrap?d.wrap:function(i,h){i.apply(this,h)
};
var b=d.errorHandler?d.errorHandler:function(i,h){i({message:"An error occurred, please see attached error object.",error:h})
};
var a=function(){g=neo4j.proxy(g,this);
b=neo4j.proxy(b,this);
if(c>0){var k={};
for(var j=0;
j<c;
j++){k[f[j]]=arguments[j]
}var h=neo4j.Web.replace(this.resources[d.resource],k)
}else{var h=this.resources[d.resource]
}var l=null;
var m=function(){};
if(arguments.length>c){if(typeof(arguments[arguments.length-1])==="function"){m=arguments[arguments.length-1]
}if((arguments.length-1)>c){l=arguments[arguments.length-2]
}}if(l!==null){neo4j.Web.ajax(d.method,h,l,function(i){g(i,m)
},function(i){b(m,i)
})
}else{neo4j.Web.ajax(d.method,h,function(i){g(i,m)
},function(i){b(m,i)
})
}};
return function(){this.serviceMethodPreflight(function(){e.call(this,neo4j.proxy(a,this),arguments)
},arguments)
}
};
neo4j.Service.prototype.initialized=false;
neo4j.Service.prototype.available=null;
neo4j.Service.prototype.resources=null;
neo4j.Service.prototype.__init__=function(a){this.callsWaiting=[];
this.loadServiceDefinition=neo4j.cachedFunction(this.loadServiceDefinition,0);
this.events=new neo4j.Events();
this.bind=neo4j.proxy(this.events.bind,this.events);
this.trigger=neo4j.proxy(this.events.trigger,this.events);
this.db=a;
this.db.bind("services.loaded",neo4j.proxy(function(){if(!this.initialized){this.setNotAvailable()
}},this))
};
neo4j.Service.prototype.handleWaitingCalls=function(){for(var b=0,a=this.callsWaiting.length;
b<a;
b++){try{this.serviceMethodPreflight(this.callsWaiting[b].method,this.callsWaiting[b].args)
}catch(c){neo4j.log(c)
}}};
neo4j.Service.prototype.loadServiceDefinition=function(a){this.get("/",neo4j.proxy(function(b){this.resources=b.resources;
this.trigger("service.definition.loaded",b);
a(b)
},this))
};
neo4j.Service.prototype.makeAvailable=function(a){this.initialized=true;
this.available=true;
this.url=a;
this.handleWaitingCalls()
};
neo4j.Service.prototype.setNotAvailable=function(){this.initialized=true;
this.available=false;
this.handleWaitingCalls()
};
neo4j.Service.prototype.get=function(c,b,d,a){neo4j.Web.get(this.url+c,b,d,a)
};
neo4j.Service.prototype.del=function(c,b,d,a){neo4j.Web.del(this.url+c,b,d,a)
};
neo4j.Service.prototype.post=function(c,b,d,a){neo4j.Web.post(this.url+c,b,d,a)
};
neo4j.Service.prototype.put=function(c,b,d,a){neo4j.Web.put(this.url+c,b,d,a)
};
neo4j.Service.prototype.serviceMethodPreflight=function(b,a){if(this.available===false){throw new Error("The service you are accessing is not available for this server.")
}else{if(!this.initialized){this.callsWaiting.push({method:b,args:a});
return
}}a=a||[];
if(this.resources!==null){b.apply(this,a)
}else{this.loadServiceDefinition(neo4j.proxy(function(){b.apply(this,a)
},this))
}};
neo4j.GraphDatabaseHeartbeat=function(a){this.db=a;
this.monitor=a.manage.monitor;
this.listeners={};
this.idCounter=0;
this.listenerCounter=0;
this.timespan={year:1000*60*60*24*365,month:1000*60*60*24*31,week:1000*60*60*24*7,day:1000*60*60*24,hours:1000*60*60*6,minutes:1000*60*35};
this.startTimestamp=(new Date()).getTime()-this.timespan.year;
this.endTimestamp=this.startTimestamp+1;
this.timestamps=[];
this.data={};
this.isPolling=false;
this.processMonitorData=neo4j.proxy(this.processMonitorData,this);
this.beat=neo4j.proxy(this.beat,this);
setInterval(this.beat,2000)
};
neo4j.GraphDatabaseHeartbeat.prototype.addListener=function(a){this.listenerCounter++;
this.listeners[this.idCounter++]=a;
return this.idCounter
};
neo4j.GraphDatabaseHeartbeat.prototype.removeListener=function(b){var c=false;
if(typeof(b)==="function"){for(var a in this.listeners){if(this.listeners[a]===b){delete this.listeners[a];
c;
break
}}}else{if(this.listeners[b]){delete this.listeners[b];
c=true
}}if(c){this.listenerCounter--
}};
neo4j.GraphDatabaseHeartbeat.prototype.getCachedData=function(){return{timestamps:this.timestamps,data:this.data,endTimestamp:this.endTimestamp,startTimestamp:this.startTimestamp}
};
neo4j.GraphDatabaseHeartbeat.prototype.beat=function(){if(this.listenerCounter>0&&!this.isPolling){this.isPolling=true;
this.monitor.getDataFrom(this.endTimestamp,this.processMonitorData)
}};
neo4j.GraphDatabaseHeartbeat.prototype.processMonitorData=function(d){this.isPolling=false;
if(d&&!d.error){var a=this.findDataBoundaries(d);
if(a.dataEnd>=0){this.endTimestamp=d.timestamps[a.dataEnd];
var e=d.timestamps.splice(a.dataStart,a.dataEnd-a.dataStart);
this.timestamps=this.timestamps.concat(e);
var c={};
for(var b in d.data){c[b]=d.data[b].splice(a.dataStart,a.dataEnd-a.dataStart);
if(typeof(this.data[b])==="undefined"){this.data[b]=[]
}this.data[b]=this.data[b].concat(c[b])
}var f={server:this.server,newData:{data:c,timestamps:e,end_time:this.endTimestamp,start_time:d.start_time},allData:this.getCachedData()};
this.callListeners(f)
}else{this.adjustRequestedTimespan()
}}};
neo4j.GraphDatabaseHeartbeat.prototype.adjustRequestedTimespan=function(a){var b=(new Date()).getTime()-this.endTimestamp;
if(b>=this.timespan.year){this.endTimestamp=(new Date()).getTime()-this.timespan.month;
this.beat()
}else{if(b>=this.timespan.month){this.endTimestamp=(new Date()).getTime()-this.timespan.week;
this.beat()
}else{if(b>=this.timespan.week){this.endTimestamp=(new Date()).getTime()-this.timespan.day;
this.beat()
}else{if(b>=this.timespan.day){this.endTimestamp=(new Date()).getTime()-this.timespan.hours;
this.beat()
}else{if(b>=this.timespan.day){this.endTimestamp=(new Date()).getTime()-this.timespan.minutes;
this.beat()
}}}}}};
neo4j.GraphDatabaseHeartbeat.prototype.findDataBoundaries=function(d){var c=this.getFirstKey(d);
var a=-1,b=-1;
if(c){for(a=d.timestamps.length-1;
a>=0;
a--){if(typeof(d.data[c][a])==="number"){break
}}for(b=0;
b<=a;
b++){if(typeof(d.data[c][b])==="number"){break
}}}return{dataStart:b,dataEnd:a}
};
neo4j.GraphDatabaseHeartbeat.prototype.callListeners=function(b){for(var a in this.listeners){setTimeout(function(c){return function(){c(b)
}
}(this.listeners[a]),0)
}};
neo4j.GraphDatabaseHeartbeat.prototype.getFirstKey=function(a){if(typeof(a)==="object"){for(var b in a.data){break
}}return b?b:null
};
neo4j.services.BackupService=function(a){this.__init__(a)
};
neo4j.services.BackupService.prototype=new neo4j.Service();
neo4j.services.BackupService.prototype.triggerManual=neo4j.Service.resourceFactory({resource:"trigger_manual",method:"POST",errorHandler:function(b,a){if(a.exception=="NoBackupFoundationException"){b(false)
}}});
neo4j.services.BackupService.prototype.triggerManualFoundation=neo4j.Service.resourceFactory({resource:"trigger_manual_foundation",method:"POST"});
neo4j.services.BackupService.prototype.getJobs=neo4j.Service.resourceFactory({resource:"jobs",method:"GET"});
neo4j.services.BackupService.prototype.getJob=function(b,a){this.getJobs(function(c){for(var d in c.jobList){if(c.jobList[d].id==b){a(c.jobList[d]);
return
}}a(null)
})
};
neo4j.services.BackupService.prototype.deleteJob=neo4j.Service.resourceFactory({resource:"job",method:"DELETE",urlArgs:["id"]});
neo4j.services.BackupService.prototype.triggerJobFoundation=neo4j.Service.resourceFactory({resource:"trigger_job_foundation",method:"POST",urlArgs:["id"]});
neo4j.services.BackupService.prototype.setJob=neo4j.Service.resourceFactory({resource:"jobs",method:"PUT"});
neo4j.services.ConfigService=function(a){this.__init__(a)
};
neo4j.services.ConfigService.prototype=new neo4j.Service();
neo4j.services.ConfigService.prototype.getProperties=neo4j.Service.resourceFactory({resource:"properties",method:"GET",wrap:function(c,a){var b=a[0];
c(function(f){var e={};
for(var d in f){e[f[d].key]=f[d]
}b(e)
})
}});
neo4j.services.ConfigService.prototype.getProperty=function(a,b){this.getProperties(function(c){for(var d in c){if(d===a){b(c[d]);
return
}}b(null)
})
};
neo4j.services.ConfigService.prototype.setProperties=neo4j.Service.resourceFactory({resource:"properties",method:"POST",wrap:function(e,a){var c=[];
var d;
for(var b in a[0]){d={key:b,value:a[0][b]};
c.push(d);
this.db.trigger("config.property.set",d)
}e(c,a[1])
}});
neo4j.services.ConfigService.prototype.setProperty=function(a,c,d){var b={};
b[a]=c;
this.setProperties(b,d)
};
neo4j.services.ImportService=function(a){this.__init__(a)
};
neo4j.services.ImportService.prototype=new neo4j.Service();
neo4j.services.ImportService.prototype.fromUrl=neo4j.Service.resourceFactory({resource:"import_from_url",method:"POST",wrap:function(b,a){b({url:a[0]},a[1])
}});
neo4j.services.ImportService.prototype.getUploadUrl=function(a){this.serviceMethodPreflight(function(b){b(this.resources.import_from_file)
},arguments)
};
neo4j.services.ExportService=function(a){this.__init__(a)
};
neo4j.services.ExportService.prototype=new neo4j.Service();
neo4j.services.ExportService.prototype.all=neo4j.Service.resourceFactory({resource:"export_all",method:"POST"});
neo4j.services.ConsoleService=function(a){this.__init__(a)
};
neo4j.services.ConsoleService.prototype=new neo4j.Service();
neo4j.services.ConsoleService.prototype.exec=neo4j.Service.resourceFactory({resource:"exec",method:"POST",wrap:function(b,a){b({command:a[0]},a[1])
}});
neo4j.services.JmxService=function(a){this.__init__(a);
this.kernelInstance=neo4j.cachedFunction(this.kernelInstance,0,2000)
};
neo4j.services.JmxService.prototype=new neo4j.Service();
neo4j.services.JmxService.prototype.getDomains=neo4j.Service.resourceFactory({resource:"domains",method:"GET"});
neo4j.services.JmxService.prototype.getDomain=neo4j.Service.resourceFactory({resource:"domain",method:"GET",urlArgs:["domain"]});
neo4j.services.JmxService.prototype.getBean=neo4j.Service.resourceFactory({resource:"bean",method:"GET",urlArgs:["domain","objectName"],wrap:function(c,a){if(a[0]==="neo4j"){var b=this;
this.kernelInstance(function(d){a[0]="org.neo4j";
a[1]=escape(d+",name="+a[1]);
c.apply(this,a)
})
}else{a[0]=escape(a[0]);
a[1]=escape(a[1]);
c.apply(this,a)
}}});
neo4j.services.JmxService.prototype.query=neo4j.Service.resourceFactory({resource:"query",method:"POST"});
neo4j.services.JmxService.prototype.kernelInstance=function(a){this.serviceMethodPreflight(function(c){var b=this.resources.kernelquery;
neo4j.Web.get(b,function(e){var d=e?e.split(":")[1].split(",")[0]:null;
c(d)
})
},[a])
};
neo4j.services.LifecycleService=function(a){this.__init__(a)
};
neo4j.services.LifecycleService.prototype=new neo4j.Service();
neo4j.services.LifecycleService.prototype.getStatus=neo4j.Service.resourceFactory({resource:"status",method:"GET"});
neo4j.services.LifecycleService.prototype.start=neo4j.Service.resourceFactory({resource:"start",method:"POST"});
neo4j.services.LifecycleService.prototype.stop=neo4j.Service.resourceFactory({resource:"stop",method:"POST"});
neo4j.services.LifecycleService.prototype.restart=neo4j.Service.resourceFactory({resource:"restart",method:"POST"});
neo4j.services.MonitorService=function(a){this.__init__(a)
};
neo4j.services.MonitorService.prototype=new neo4j.Service();
neo4j.services.MonitorService.prototype.getData=neo4j.Service.resourceFactory({resource:"latest_data",method:"GET"});
neo4j.services.MonitorService.prototype.getDataFrom=neo4j.Service.resourceFactory({resource:"data_from",method:"GET",urlArgs:["start"]});
neo4j.services.MonitorService.prototype.getDataBetween=neo4j.Service.resourceFactory({resource:"data_period",method:"GET",urlArgs:["start","stop"]});
neo4j.GraphDatabaseManager=function(a){this.db=a;
this.url=a.manageUrl;
this.backup=new neo4j.services.BackupService(a);
this.config=new neo4j.services.ConfigService(a);
this.importing=new neo4j.services.ImportService(a);
this.exporting=new neo4j.services.ExportService(a);
this.console=new neo4j.services.ConsoleService(a);
this.jmx=new neo4j.services.JmxService(a);
this.lifecycle=new neo4j.services.LifecycleService(a);
this.monitor=new neo4j.services.MonitorService(a);
this.discoverServices()
};
neo4j.GraphDatabaseManager.prototype.servicesLoaded=function(){return(this.services)?true:false
};
neo4j.GraphDatabaseManager.prototype.availableServices=function(){if(this.services){if(!this.serviceNames){this.serviceNames=[];
for(var a in this.services){this.serviceNames.push(a)
}}return this.serviceNames
}else{throw new Error("Service definition has not been loaded yet.")
}};
neo4j.GraphDatabaseManager.prototype.discoverServices=function(){neo4j.Web.get(this.url,neo4j.proxy(function(b){this.services=b.services;
for(var a in b.services){if(this[a]){this[a].makeAvailable(b.services[a])
}}this.db.trigger("services.loaded")
},this),neo4j.proxy(function(a){throw new Error("Unable to fetch service descriptions for server "+this.url)
},this))
};
neo4j.GraphDatabase=function(b,a){this.url=b;
this.manageUrl=a||null;
this.events=new neo4j.Events({db:this});
this.bind=neo4j.proxy(this.events.bind,this.events);
this.trigger=neo4j.proxy(this.events.trigger,this.events);
this.manage=new neo4j.GraphDatabaseManager(this);
this.heartbeat=new neo4j.GraphDatabaseHeartbeat(this)
};
neo4j.GraphDatabase.prototype.get=function(c,b,d,a){neo4j.Web.get(this.url+c,b,d,a)
};
neo4j.GraphDatabase.prototype.del=function(c,b,d,a){neo4j.Web.del(this.url+c,b,d,a)
};
neo4j.GraphDatabase.prototype.post=function(c,b,d,a){neo4j.Web.post(this.url+c,b,d,a)
};
neo4j.GraphDatabase.prototype.put=function(c,b,d,a){neo4j.Web.put(this.url+c,b,d,a)
};
neo4j.GraphDatabase.prototype.stripUrlBase=function(a){if(typeof(a)==="undefined"||a.indexOf("://")==-1){return a
}if(a.indexOf(this.url)===0){return a.substring(this.url.length)
}else{if(a.indexOf(this.manageUrl)===0){return a.substring(this.manageUrl.length)
}else{return a.substring(a.indexOf("/",8))
}}};
