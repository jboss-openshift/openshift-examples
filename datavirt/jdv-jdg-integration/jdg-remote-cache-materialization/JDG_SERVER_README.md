remote-query: Query JDG remotely through Hotrod
================================================

What is this?
-----------

This describes how to configure the JDG Server and the remote caches that will be used in this quick start.


System requirements
-------------------

All you need to build this project is Java 6.0 (Java SDK 1.6) or newer, Maven 3.0 or newer.

The application this project produces is designed to be run on JBoss Data Grid 6.x

 
Configure Maven
---------------

If you have not yet done so, you must [Configure Maven](../../README.md#configure-maven) before testing the quickstarts.


Configure JDG
-------------

1. Obtain JDG server distribution on Red Hat's Customer Portal at https://access.redhat.com/jbossnetwork/restricted/listSoftware.html

2. Install a JDBC driver into JDG (since JDG includes H2 by default, this step may be skipped for the scope of this example). More information can be found in the DataSource Management chapter of the [Administration and Configuration Guide for JBoss Enterprise Application Platform](https://access.redhat.com/site/documentation/JBoss_Enterprise_Application_Platform) . _NOTE: JDG does not support deploying applications so one cannot install it as a deployment._

3. This Quickstart uses JDBC to store the cache. To permit this, it's necessary to alter JDG configuration file (`JDG_HOME/standalone/configuration/standalone.xml`) to contain the following definitions:
   
* Datasource subsystem definition:

    
        <subsystem xmlns="urn:jboss:domain:datasources:1.2">
            <!-- Define this Datasource with jndi name  java:jboss/datasources/ExampleDS -->
            <datasources>
                <datasource jndi-name="java:jboss/datasources/ExampleDS" pool-name="ExampleDS" enabled="true" use-java-context="true">
                    <!-- The connection URL uses H2 Database Engine with in-memory database called test -->
                    <connection-url>jdbc:h2:mem:test;DB_CLOSE_DELAY=-1</connection-url>
                    <!-- JDBC driver name -->
                    <driver>h2</driver>
                    <!-- Credentials -->
                    <security>
                        <user-name>sa</user-name>
                        <password>sa</password>
                    </security>
                </datasource>
                <!-- Define the JDBC driver called 'h2' -->
                <drivers>
                    <driver name="h2" module="com.h2database.h2">
                        <xa-datasource-class>org.h2.jdbcx.JdbcDataSource</xa-datasource-class>
                    </driver>
                </drivers>
            </datasources>
        </subsystem>

* Infinispan subsystem definition:

This subsytem  will completely replace what's currently defined in the standalone.xml.

        <subsystem xmlns="urn:infinispan:server:core:6.4" default-cache-container="local">
            <cache-container name="local" default-cache="default" statistics="true">
                <local-cache name="default" start="EAGER">
                    <locking isolation="NONE" acquire-timeout="30000" concurrency-level="1000" striping="false"/>
                    <transaction mode="NONE"/>
                </local-cache>
                <local-cache name="memcachedCache" start="EAGER">
                    <locking isolation="NONE" acquire-timeout="30000" concurrency-level="1000" striping="false"/>
                    <transaction mode="NONE"/>
                </local-cache>
                <local-cache name="namedCache" start="EAGER"/>
                
                <!-- Add a local cache named 'addressbook_indexed' -->
                <local-cache name="addressbook_indexed" start="EAGER">

                    <!-- Define the locking isolation of this cache -->
                    <locking
                        acquire-timeout="20000"
                        concurrency-level="500"
                        striping="false" />
                        
                    <!-- Enable indexing using the RAM Lucene directory provider -->
                    <indexing index="ALL">
                        <property name="default.directory_provider">ram</property>
                    </indexing>
                    
                    <!-- Define the JdbcBinaryCacheStores to point to the ExampleDS previously defined -->
                    <string-keyed-jdbc-store datasource="java:jboss/datasources/ExampleDS" passivation="false" preload="false" purge="false">

                        <!-- specifies information about database table/column names and data types -->
                        <string-keyed-table prefix="JDG">
                            <id-column name="id" type="VARCHAR"/>
                            <data-column name="datum" type="BINARY"/>
                            <timestamp-column name="version" type="BIGINT"/>
                        </string-keyed-table>
                    </string-keyed-jdbc-store>
                </local-cache>
                <!-- End of 'addressbook_indexed' cache definition -->

                <!-- Add a local cache named 'addressbook' which is not indexed -->
                <local-cache name="addressbook" start="EAGER">

                    <!-- Define the locking isolation of this cache -->
                    <locking
                        acquire-timeout="20000"
                        concurrency-level="500"
                        striping="false" />

                    <!-- Define the JdbcBinaryCacheStores to point to the ExampleDS previously defined -->
                    <string-keyed-jdbc-store datasource="java:jboss/datasources/ExampleDS" passivation="false" preload="false" purge="false">

                        <!-- specifies information about database table/column names and data types -->
                        <string-keyed-table prefix="JDG">
                            <id-column name="id" type="VARCHAR"/>
                            <data-column name="datum" type="BINARY"/>
                            <timestamp-column name="version" type="BIGINT"/>
                        </string-keyed-table>
                    </string-keyed-jdbc-store>
                </local-cache>
               <local-cache name="addressbook_indexed_mat" start="EAGER">

                    <!-- Define the locking isolation of this cache -->
                    <locking
                        acquire-timeout="20000"
                        concurrency-level="500"
                        striping="false" />
                        
                    <!-- Enable indexing using the RAM Lucene directory provider -->
                    <indexing index="ALL">
                        <property name="default.directory_provider">ram</property>
                    </indexing>
                    
                    <!-- Define the JdbcBinaryCacheStores to point to the ExampleDS previously defined -->
                    <string-keyed-jdbc-store datasource="java:jboss/datasources/ExampleDS" passivation="false" preload="false" purge="false">

                        <!-- specifies information about database table/column names and data types -->
                        <string-keyed-table prefix="JDG">
                            <id-column name="id" type="VARCHAR"/>
                            <data-column name="datum" type="BINARY"/>
                            <timestamp-column name="version" type="BIGINT"/>
                        </string-keyed-table>
                    </string-keyed-jdbc-store>
                </local-cache>   
              <local-cache name="aliasCache" start="EAGER">

                    <!-- Define the locking isolation of this cache -->
                    <locking
                        acquire-timeout="20000"
                        concurrency-level="500"
                        striping="false" />

                    <!-- Define the JdbcBinaryCacheStores to point to the ExampleDS previously defined -->
                    <string-keyed-jdbc-store datasource="java:jboss/datasources/ExampleDS" passivation="false" preload="false" purge="false">

                    </string-keyed-jdbc-store>
                </local-cache>                                    
                <!-- End of 'addressbook' cache definition -->

            </cache-container>
            <cache-container name="security"/>

        </subsystem>

Start JDG
---------

1. Open a command line and navigate to the root of the JDG directory.
2. The following shows the command line to start the server with the web profile:

        For Linux:   $JDG_HOME/bin/standalone.sh
        For Windows: %JDG_HOME%\bin\standalone.bat




