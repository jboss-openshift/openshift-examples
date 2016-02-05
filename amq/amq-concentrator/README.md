# AMQ Concentrator Topology Example #

This example shows how to connect two separate networks of brokers, where the
first network serves as a funnel for the second, which serves as a concentrator.

This example uses custom configuration to connect the networks together.

## Firing it Up ##

### Create the Concentrator ###

Create an application using the `amq62-basic` template with the following
parameters:

 * `APPLICATION_NAME=concentrator`

For example:

    $ oc process amq62-basic -v APPLICATION_NAME=concentrator,MQ_USERNAME=concentrator,MQ_PASSWORD=12345 -n openshift | oc create -f -

This should create a single broker to serve as the concentrator.  Verify that
there is a *concentrator-amq-tcp* service.  This is the service the funnels will
use.

### Create the Funnel(s) ###

Create an application using the [amq62-funnel-s2i.json](amq62-funnel-s2i.json) file located in this
project.  (This template adds an `ImageStream` and `BuildConfig` to the
`amq62-basic` template and configures additional environment variables on the
A-MQ container for connecting to the concentrator.)  When processing the
template, you'll need to specify the following parameters:

 * `CONCENTRATOR_SERVICE`: OpenWire (tcp) service name for the concentrator (e.g. *concentrator-amq-tcp*)
 * `CONCENTRATOR_USERNAME`: User name for the concentrator (e.g. *concentrator*)
 * `CONCENTRATOR_PASSWORD`: Password for the concentrator (e.g. *12345*)

If you created the *concentrator* as documented above, create the first funnel:

    $ oc process -f amq62-funnel-s2i.json -v APPLICATION_NAME=funnel,CONCENTRATOR_SERVICE=concentrator-amq-tcp,CONCENTRATOR_USERNAME=concentrator,CONCENTRATOR_PASSWORD=12345 | oc create -f -

This should create a single broker to serve as the funnel.  Verify that the
funnel is connected to the concentrator.  You should see something like this
in the logs:

    INFO | Network connection between vm://funnel-amq-1-uh01x#4 and tcp:///10.1.2.8:61616@57441 (concentrator-amq-1-5ks7g) has been established.

Now scale up the funnel:

    $ oc scale dc funnel-amq --replicas=4

You should now have a mesh of four brokers feeding into the concentrator.

## Cleaning Up ##

To clean up:

 * The funnel:

        $ oc delete all -l application=funnel

 * The concentrator:

        $ oc delete all -l application=concentrator

## Customizations ##

The concentrator broker's user needs to be configured on the funnel broker or
the funnels will fail to connect to the concentrator.  This project includes a
customized [.sti/bin/run](.sti/bin/run) script which adds the concentrator user
to the `conf/openshift-users.properties` file.

The funnel broker also contains a custom openshift-activemq.xml file which
adds a `networkConnector` that connects to the concentrator brokers.
