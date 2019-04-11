---
Level: Intermediate
Technologies: Teiid, Infinispan, Hot Rod, Remote Query, Materialization
Target Product: DV
Product Versions: DV 6.1+, JDG 6.5+
Source: https://github.com/teiid/teiid-quickstarts
---

JDG Remote-Cache Materialization Quickstart using JDG Hot Rod that supports Google Protocol Buffers for Serialization
================================

# What is it?

This quickstart demonstrates how Teiid can connect to a remote JBoss Data Grid (JDG) to use as a caching data source to improve query performance. 


# Quick Start requirements

-  If you have not done so, please review the System Requirements [../README.md](../README.md) 


1.  Teiid Server Prerequistes

* JBoss application server to run Teiid
* The Teiid Jboss distribution kit
* The dynamicvdb-datafederation quickstart [../../dynamicvdb-datafederation/README.md] needs to be installed.

2.  JDG Server Prerequistes

* JDG 6.5 server kit installed (used as the remote server)
* JDG 6.5 HotRod modules kit (used by Teiid to access the remote cache)
	> NOTE: You can obtain JDG kit distributions on Red Hat's Customer Portal at https://access.redhat.com/jbossnetwork/restricted/listSoftware.html


# JDG setup

1.  Setup JDG Server
	
-  Install the JDG server
-  Configure caches based on the [./JDG_SERVER_README.md]


2.  Starting JDG Server

-  It is assumed that you will be running both servers on the same box, there start the JDG server by adding the following command line argument:
	*  -Djboss.socket.binding.port-offset=100

Example:   ./standalone.sh -Djboss.socket.binding.port-offset=100

For the purpose of this quick start, it assumes running both servers on the same machine and is expecting the JDG server to have its ports incremented.  The
port adjustment has been made in the jdg.properties on the client side to match the above offset.

#  [PreRequistes] Dynamicvdb-datafederation quickstart

*  Install dynamicvdb-datafederation quickstart [../../dynamicvdb-datafederation/README.md]


# Teiid jdg-remote-cache-materialization quickstart Setup

1.  build the jdg-remote-cache-materialization quickstart

-  run  mvn -s ./settings.xml clean install

-  After building the quickstart, the jdg-remote-cache-materialization-pojos-jboss-as7-dist.zip should be found in the target directory.
-  This zip will be used later, where it is deployed to the Teiid server.


# Setup Teiid Server
AliasCacheName
1. shutdown jbossas server, if not already.

2. deploy pojo Module  
	-	take the jdg-remote-cache-pojos-jboss-as7-dist.zip and unzip at <jbossas-dir>

3. Install the JBoss Data Grid version of the hot rod client modules kit for EAP into <jbossas-dir>/modules/ of your Teiid/EAP instance.
   See Red Hat:   http://access.redhat.com  to obtain the kit.


4. setup the infinispan resource adapter 
           
*  configure for materialization
	-	open the file: {jbossas.server.dir}/docs/teiid/datasources/infinispan/infinispan-remote-query-materialize-annotations-dsl-ds.xml
	-	copy and paste the resource-adapter section it into the server configuration, under the section:

        <subsystem xmlns="urn:jboss:domain:resource-adapters:1.1">
            <resource-adapters>

5.  Edit the module.xml for org.jboss.teiid.resource-adapter.infinispan.dsl resource adapter to add the pojo dependency

example:  <module name="com.client.quickstart.addressbook.pojos"  optional="true"  export="true" />

6. Start the server

	To start the server, open a command line and navigate to the "bin" directory under the root directory of the JBoss server and run:
	
	For Linux:   ./standalone.sh	
	for Windows: standalone.bat

	If Teiid isn't configured in the default configuration, append the following arguments to the command to specify the configuration
		
	-c {configuration.file} 
	
	Example: -c standalone-teiid.xml 


7. Install the infinispan-cache-dsl translator

	-	cd to the ${JBOSS_HOME}/bin directory
	-	execute:  ./jboss-cli.sh --connect --file=../docs/teiid/datasources/infinispan/add-infinispan-cache-dsl-translator.cli 
	
	
8. deploy the VDB

	- copy files jdg-remote-cache-mat-vdb.xml and jdg-remote-cache-mat-vdb.xml.dodeploy to {jbossas.server.dir}/standalone/deployments	


# Query Demonstrations

==== Using the simpleclient example ====

1) Change your working directory to "${quickstart.install.dir}/simpleclient"

2) Use the simpleclient example to run the following queries:

Example:   mvn exec:java -Dvdb="PeopleMat" -Dsql="select name, id, email from PersonMatModel.PersonMatView"  -Dusername="teiidUser" -Dpassword="pwd"


or 


Use a sql tool, like SQuirreL, to connect and issue following example query:

-  connect:  jdbc:teiid:PeopleMat@mm://localhost:31000

1.  Query for reading from the materialized cache

*  select name, id, email from PersonMatModel.PersonMatView


2.  To test the materialization process, do the following:

*  perform a delete from the Customer table 
	-	delete from Customer where SSN = 'CST01002'
	
	Customer:  Joseph Smith,19980002,Joseph.Smith@email.com  is removed

*  wait 2 min's because the refresh rate is set at 1 min.
*  perform query from #1 above, and should see row missing for SSN = CST01002

