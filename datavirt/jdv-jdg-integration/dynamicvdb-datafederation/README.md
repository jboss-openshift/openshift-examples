---
Level: Basic
Technologies: Teiid, Dynamic VDB, Native Queries, VDB reuse, reading data from JDBC, delimited file and Excel File
Target Product: DV
Product Versions: DV 6.0+
Source: https://github.com/teiid/teiid-quickstarts
---

Dynamicvdb-datafederation is the 'Hello World' example for Teiid.
================================

## VDB: 

* Portfolio   -  source models, view's, native query


## What is it?

This quickstart demonstrates how to define a dynamic vdb to enable data federation across
multiple data sources (i.e., relational and text file).   This will demonstrate the
following: 

-  how to federate data from a relational data source, a text file-based data source and an EXCEL File
-  how to define a view using DDL
-  how to define a translator override to support native queries
-  how to define a second vdb that reuses (extends) another vdb


This example uses the H2 database, which is referenced as the "accounts-ds" data source in the server, 
but the creation SQL can be adapted to another database if you choose.

Note:  this example provides the base setup for which other quick starts depend upon.


## System requirements

If you have not done so, please review the System Requirements [../README.md](../README.md)

## Setup

1)  Start the server

	To start the server, open a command line and navigate to the "bin" directory under the root directory of the JBoss server and run:
	
	For Linux:   ./standalone.sh	
	for Windows: standalone.bat

	If Teiid isn't configured in the default configuration, append the following arguments to the command to specify the configuration
		
	-c {configuration.file}  
	
	Example: -c standalone-teiid.xml 
		
2)  Copy teiid support files
	
- Copy the "teiidfiles" directory to the $JBOSS_HOME/ directory

	The src/teiidfiles directory should contain:
	(1) customer-schema.sql
	(2) customer-schema-drop.sql
	(3) data/marketdata-price.txt
	(4) data/marketdata-price1.txt
	
when complete, you should see $JBOSS_HOME/teiidfiles

3) Setup the h2 datasource and file resource adapter

-  run the following CLI script

	-	cd to the $JBOSS_HOME/bin directory
	-	execute:  ./jboss-cli.sh --connect --file={path}/dynamicvdb-datafederation/src/scripts/setup.cli 

4)  Teiid Deployment:

Copy (deploy) the following VDB related files to the $JBOSS_HOME/standalone/deployments directory

	* Portfolio VDB
    	- src/vdb/portfolio-vdb.xml
     	- src/vdb/portfolio-vdb.xml.dodeploy


You should see the server log indicate the VDB is active with a message like:  TEIID40003 VDB Portfolio.1 is set to ACTIVE

5)  Open the admin console to make sure the VDB is deployed

	*  open a brower to http://localhost:9990/console 	

6)  See "Query Demonstrations" below to demonstrate data federation.

## Query Demonstrations

==== Using the simpleclient example ====

1) Change your working directory to "${quickstart.install.dir}/simpleclient"

2) Use the simpleclient example to run the following queries:

Example:   mvn exec:java -Dvdb="portfolio" -Dsql="example query" -Dusername="xx" -Dpassword="xx"

## Examples:

###  Source and Federated Queries

> NOTE:  For the following examples,  use the vdb:  Portfolio


*  Example a  - queries the relational source

	select * from product


*  Example b  - queries the text file-based source

	select stock.* from (call MarketData.getTextFiles('*.txt')) f, TEXTTABLE(f.file COLUMNS symbol string, price bigdecimal HEADER) stock


*  Example c  - performs a join between the relational source and the text file-based source.  The files returned from the getTextFiles procedure are passed to the TEXTTABLE table function (via the nested table correlated reference f.file).  The TEXTTABLE function expects a 
text file with a HEADER containing entries for at least symbol and price columns. 

	select product.symbol, stock.price, company_name from product, (call MarketData.getTextFiles('*.txt')) f, TEXTTABLE(f.file COLUMNS symbol string, price bigdecimal HEADER) stock where product.symbol=stock.symbol


*  Example d  -  queries the EXCEL file to retrieve other personal holdings valuations

	select * from OtherHoldings.PersonalHoldings

### Native Query

> NOTE:  For the following examples,  use the vdb:  Portfolio


*  Example a  - Issue query that contains a NATIVE sql call that will be directly issued against the H2 database.  This is useful if the function isn't supported by the translator (check the documentation for the types of translators that support NATIVE sql).   Note that the translator override in the vdb xml enabling support for native queries has to be set.

 	select x.* FROM (call native('select Shares_Count, MONTHNAME(Purchase_Date) from Holdings')) w, ARRAYTABLE(w.tuple COLUMNS "Shares_Count" integer, "MonthPurchased" string ) AS x


