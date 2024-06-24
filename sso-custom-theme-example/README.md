# Example of RH-SSO 7.2 configuration to deploy custom RH-SSO theme to RH-SSO OpenShift pod

This example illustrates custom *standalone-openshift.xml* configuration to deploy custom RH-SSO theme, provided as module into RH-SSO pod running on OpenShift.

The *modules/org/keycloak/example/themes/main/keycloak-example-themes.jar* JAR archive was created from upstream [themes example](https://github.com/keycloak/keycloak/tree/master/examples/themes) by running the following commands (after checkout):

```
$ mvn clean package
```

within the *keycloak/examples/themes* directory.

Perform the following steps to customize the login theme for the RH-SSO 7.2 server to the *sunrise* one:

1. Create a new project:
```
$ oc new-project sso-app-demo
```
2. Add the view role to the default service account. This enables the service account to view all the resources in the sso-app-demo namespace, which is necessary for managing the cluster.
```
$ oc policy add-role-to-user view system:serviceaccount:$(oc project -q):default
```
3. Checkout the repository and change the directory to the *sso-custom-theme-example* subdirectory:
```
$ git clone https://github.com/jboss-openshift/openshift-examples.git && cd openshift-examples/sso-custom-theme-example/
```
4. Create new OpenShift binary type build, specifying *redhat-sso72-openshift:1.1* as the image stream to use:
```
$ oc new-build --binary=true --name=sso-custom-theme-demo --image-stream=redhat-sso72-openshift:1.1
--> Found image 7dc08f4 (7 days old) in image stream "openshift/redhat-sso72-openshift" under tag "1.1" for "redhat-sso72-openshift:1.1"

    Red Hat SSO 7.2
    ---------------
    Platform for running Red Hat SSO

    Tags: sso, sso7, keycloak

    * A source build using binary input will be created
      * The resulting image will be pushed to image stream "sso-custom-theme-demo:latest"
      * A binary build was created, use 'start-build --from-dir' to trigger a new build

--> Creating resources with label build=sso-custom-theme-demo ...
    imagestream "sso-custom-theme-demo" created
    buildconfig "sso-custom-theme-demo" created
--> Success
```
5. Start the OpenShift binary type build using the *--from-dir* parameter to point to *sso-custom-theme-example* subdirectory:
```
$ oc start-build sso-custom-theme-demo --from-dir=./ --follow
Uploading directory "." as binary input for the build ...
build "sso-custom-theme-demo-1" started
Receiving source from STDIN as archive ...
Copying all war artifacts from /tmp/src directory into /opt/eap/standalone/deployments for later deployment...
Copying all ear artifacts from /tmp/src directory into /opt/eap/standalone/deployments for later deployment...
Copying all rar artifacts from /tmp/src directory into /opt/eap/standalone/deployments for later deployment...
Copying all jar artifacts from /tmp/src directory into /opt/eap/standalone/deployments for later deployment...
Copying all war artifacts from /tmp/src/deployments directory into /opt/eap/standalone/deployments for later deployment...
Copying all ear artifacts from /tmp/src/deployments directory into /opt/eap/standalone/deployments for later deployment...
Copying all rar artifacts from /tmp/src/deployments directory into /opt/eap/standalone/deployments for later deployment...
Copying all jar artifacts from /tmp/src/deployments directory into /opt/eap/standalone/deployments for later deployment...
Copying config files from project...
'/tmp/src/configuration/standalone-openshift.xml' -> '/opt/eap/standalone/configuration/standalone-openshift.xml'
'/tmp/src/configuration/standalone-openshift.xml.orig' -> '/opt/eap/standalone/configuration/standalone-openshift.xml.orig'
Copying modules from project...
'/tmp/src/modules/org' -> '/opt/eap/modules/org'
'/tmp/src/modules/org/keycloak' -> '/opt/eap/modules/org/keycloak'
'/tmp/src/modules/org/keycloak/example' -> '/opt/eap/modules/org/keycloak/example'
'/tmp/src/modules/org/keycloak/example/themes' -> '/opt/eap/modules/org/keycloak/example/themes'
'/tmp/src/modules/org/keycloak/example/themes/main' -> '/opt/eap/modules/org/keycloak/example/themes/main'
'/tmp/src/modules/org/keycloak/example/themes/main/keycloak-example-themes.jar' -> '/opt/eap/modules/org/keycloak/example/themes/main/keycloak-example-themes.jar'
'/tmp/src/modules/org/keycloak/example/themes/main/module.xml' -> '/opt/eap/modules/org/keycloak/example/themes/main/module.xml'
Pushing image docker-registry.default.svc:5000/sso-app-demo/sso-custom-theme-demo:latest ...
Pushed 0/8 layers, 5% complete
Pushed 1/8 layers, 12% complete
Pushed 2/8 layers, 36% complete
Pushed 3/8 layers, 56% complete
Pushed 4/8 layers, 68% complete
Pushed 5/8 layers, 97% complete
Pushed 6/8 layers, 98% complete
Pushed 7/8 layers, 98% complete
Pushed 8/8 layers, 100% complete
Push successful
```
6. Create a new application specifying the customized *sso-custom-theme-demo* image. Also specify couple of environment variables, when deploying it:
```
$ oc new-app sso-custom-theme-demo \
 -e X509\_CA\_BUNDLE="/var/run/secrets/kubernetes.io/serviceaccount/service-ca.crt" \
 -e SSO\_ADMIN\_USERNAME="admin" \
 -e SSO\_ADMIN\_PASSWORD="redhat" \
 -e SSO\_REALM="demorealm" \
--> Found image 380f2c2 (7 minutes old) in image stream "sso-app-demo/sso-custom-theme-demo" under tag "latest" for "sso-custom-theme-demo"

    sso-app-demo/sso-custom-theme-demo-1:0b35f6a5
    ---------------------------------------------
    Platform for running Red Hat SSO

    Tags: sso, sso7, keycloak

    * This image will be deployed in deployment config "sso-custom-theme-demo"
    * Ports 8080/tcp, 8443/tcp, 8778/tcp will be load balanced by service "sso-custom-theme-demo"
      * Other containers can access this service through the hostname "sso-custom-theme-demo"

--> Creating resources ...
    deploymentconfig "sso-custom-theme-demo" created
    service "sso-custom-theme-demo" created
--> Success
    Run 'oc status' to view your app.

```
7. Expose the *sso-custom-theme-demo* service:
```
$ oc expose svc sso-custom-theme-demo
route "sso-custom-theme-demo" exposed
```
8. Navigate to *http://sso-custom-theme-demo-sso-app-demo.openshift.example.com/* to access the administration console of the RH-SSO 7.2 server
9. Log in as RH-SSO 7.2 server administrator using the *SSO_ADMIN_USERNAME* and *SSO_ADMIN_PASSWORD* credentials above.
10. In the realm section of the left sidebar, select 'Master' realm.
11. Click the *Themes* tab. In the *Login Theme* drop-down menu select *sunrise*. Click *Save*.
12. Log out from RH-SSO 7.2 administrator console.
13. Notice how the background image for RH-SSO 7.2 administrator console changed to the motive of Sun rise.

Advanced Topics:

Explore what additional themes are available in the *keycloak-example-themes.jar* module that got deployed, and how these themes can be used.
