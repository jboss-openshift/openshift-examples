# Custom module example

This example shows how to add a custom module to JBoss EAP. By default a PostgreSQL 9.2 driver is being used. This example shows how to switch to version 9.4.

When building the image you should see similar messages:

```
I0317 12:03:10.327088 18863 sti.go:357] Copying config files from project...
I0317 12:03:10.331538 18863 sti.go:357] '/home/jboss/source/configuration/standalone-openshift.xml' -> '/opt/eap/standalone/configuration/standalone-openshift.xml'
I0317 12:03:10.331587 18863 sti.go:357] Copying modules from project...
I0317 12:03:10.337968 18863 sti.go:357] '/home/jboss/source/modules/org/postgresql94' -> '/opt/eap/modules/org/postgresql94'
I0317 12:03:10.337997 18863 sti.go:357] '/home/jboss/source/modules/org/postgresql94/main' -> '/opt/eap/modules/org/postgresql94/main'
I0317 12:03:10.338002 18863 sti.go:357] '/home/jboss/source/modules/org/postgresql94/main/module.xml' -> '/opt/eap/modules/org/postgresql94/main/module.xml'
I0317 12:03:10.338007 18863 sti.go:357] '/home/jboss/source/modules/org/postgresql94/main/postgresql-9.4-1201.jdbc41.jar' -> '/opt/eap/modules/org/postgresql94/main/postgresql-9.4-1201.jdbc41.jar'
```

And when you boot you should see the correct driver being used:

```
12:04:09,467 INFO  [org.jboss.as.connector.subsystems.datasources] (ServerService Thread Pool -- 29) JBAS010404: Deploying non-JDBC-compliant driver class org.postgresql.Driver (version 9.4)
```
