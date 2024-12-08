# todo

 com/jumpmind/symmetric/console/impl/fk.java


# Update service
#
# DatabaseOverridable: false
# Tags: misc
update.service.class=com.jumpmind.symmetric.console.service.impl.ConsoleUpdateService

# Staging Manager
#
# DatabaseOverridable: false
# Tags: misc
staging.manager.class=com.jumpmind.symmetric.stage.EnhancedStagingManager

# Statistics Manager
#
# DatabaseOverridable: false
# Tags: misc
statistic.manager.class=com.jumpmind.symmetric.statistic.ThroughputStatisticManager

# A comma-seperated list of custom interceptors which wrap URI handlers.
#
# DatabaseOverridable: true
# Tags: remote status
server.engine.uri.interceptors=com.jumpmind.symmetric.console.remote.RemoteStatusInterceptor

# Custom http transport manager supporting remote status.
#
# DatabaseOverridable: true
# Tags: remote status
http.transport.manager.class=com.jumpmind.symmetric.console.remote.RemoteStatusHttpTransportManager

# questions

We can use `dbimport` on `src\main\resources\console-schema.xml` to generate the pro-related tables, but how we replace the variable such as  `$(monitor)` in  `com/jumpmind/symmetric/console/service/impl/MonitorServiceSqlMap.java`?

# build
gradlew build -Pvaadin.productionMode