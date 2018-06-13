# Example on how to install custom RH-SSO adapters to EAP 6.4 or EAP 7.1 Middleware images

## About
This example shows how to install custom RH-SSO adapters to EAP 6.4 or EAP 7.1 Middleware images. The adapters to be installed are expected to be specified as the value of *_SSO_ADAPTERS_OVERRIDES_* environment variable, which can have one of the following three values:

* **latest** or **LATEST**

	In this case, the latest available version of RH-SSO SAML and Wildfly adapters will be installed,

* **3.4.3.Final-redhat-2**

	In this case, the specified *_3.4.3.Final-redhat-2_* version of RH-SSO SAML and Wildfly adapters will be installed, or

* **https://maven.repository.redhat.com/ga/org/keycloak/keycloak-wildfly-adapter-dist/3.4.8.Final-redhat-6/keycloak-wildfly-adapter-dist-3.4.8.Final-redhat-6.zip,https://maven.repository.redhat.com/ga/org/keycloak/keycloak-saml-wildfly-adapter-dist/3.4.8.Final-redhat-6/keycloak-saml-wildfly-adapter-dist-3.4.8.Final-redhat-6.zip**

	In this case the RH-SSO SAML and Wildfly adapters from particular Zip archives, specified as a comma-separated list of URLs will be retrieved and installed.

**Important: The versions of both the SAML and Wildfly RH-SSO adapters to be installed need to match!!!**

## How it works

The example [overrides the default form of the *_assemble_* S2I script](https://docs.openshift.com/container-platform/3.3/dev_guide/builds.html#override-builder-image-scripts), as provided by the EAP 6.4 or EAP 7.1 Middleware images to define new [*_install_custom_rh_sso_adapters()_*](https://github.com/iankko/openshift-examples/blob/eap-s2i-custom-rh-sso-adapters/eap-s2i-install-custom-rh-sso-adapters/s2i/assemble#L213) routine, which is retrieving the specified RH-SSO adapters, and installing them.

## How to use

### Prerequisite:

Perform the following steps to utilize this change:

* Deploy [RH-SSO for OpenShift image](https://access.redhat.com/documentation/en-us/red_hat_single_sign-on/7.2/html-single/red_hat_single_sign-on_for_openshift/#requirements_and_deploying_link_xl_href_introduction_introduction_xml_passthrough_templates_passthrough_tls_termination_link_rh_sso_templates). These steps will produce RH-SSO server pod running in the *_sso-app-demo_* OpenShift namespace/project.

### Using the change one-time way (against specific buildConfig):

To use the custom *_assemble_* S2I builder image script one-time way (only against the specific buildConfig) perform the following steps:

* Deploy [an application on top of EAP 6.4 or EAP 7.1 Middleware images, secured against the RH-SSO server](https://access.redhat.com/documentation/en-us/red_hat_single_sign-on/7.2/html-single/red_hat_single_sign-on_for_openshift/#deploy_binary_build_of_eap_6_4_7_0_jsp_service_invocation_application_and_secure_it_using_red_hat_single_sign_on) created in previous step. These steps will produce the application pod running in the *_eap-app-demo_* OpenShift namespace/project.
* In the *_eap-app-demo_* namespace edit the buildConfig definition to define desired form of the *_SSO_ADAPTERS_OVERRIDES_* environment variable:

  * Determine the name of the buildConfig in question:
    ```
    $ oc get bc -o name
    buildconfigs/eap-app
    ```
  * Patch the definition by supplying custom location of the S2I build scripts to use
    ```
    $ oc patch bc/eap-app --type=json \
    -p '[{"op": "add", "path": "/spec/strategy/sourceStrategy/scripts", "value": "https://raw.githubusercontent.com/iankko/openshift-examples/eap-s2i-custom-rh-sso-adapters/eap-s2i-install-custom-rh-sso-adapters/s2i"}]'
    ```
  * Define the *_SSO_ADAPTERS_OVERRIDES_* variable with expected value:
    ```
    $ oc set env bc/eap-app -e SSO_ADAPTERS_OVERRIDES="3.4.3.Final-redhat-2"
    ```
  * (Optional) Verify the *_SSO_ADAPTERS_OVERRIDES_* definition:
    ```
    $ oc env bc/eap-app --list
    ```
  * Start new build and wait for the build to finish:
    ```
    $ oc start-build bc/eap-app --follow
    ```
  * Deploy the application from the latest build:
    ```
    $ oc rollout latest dc/eap-app
    ```
  * Verify the version of the RH-SSO adapters installed in the pod:
    ```
    $ oc rsh eap-app-56-gzm4g find / -name *.jar 2>/dev/null | grep keycloak | head -2
    /opt/eap/modules/system/add-ons/keycloak/org/keycloak/keycloak-common/main/keycloak-common-3.4.3.Final-redhat-2.jar
    /opt/eap/modules/system/add-ons/keycloak/org/keycloak/keycloak-adapter-spi/main/keycloak-undertow-adapter-spi-3.4.3.Final-redhat-2.jar
    ```

### Modifying the default form of *_eap64-sso-s2i_* and *_eap71-sso-s2i_* application templates

Alternatively, it is possible directly to modify the default definition of the *_eap64-sso-s2i_* and *_eap71-sso-s2i_* application templates to achieve the override RH-SSO adapters functionality to be available for each deployment provisioned from now on from these two templates. If this is desired, perform the following steps:

#### Create new namespace. Grant view role to the default service account

  * Create a new *_eap-app-adapters-install-demo_* namespace:
    ```
    $ oc new-project eap-app-adapters-install-demo
    ```
  * Grant the *_view_* role to the *_default_* service account:
    ```
    $ oc policy add-role-to-user view system:serviceaccount:$(oc project -q):default
    ```

#### Generate SSL and JGroups keystore for the EAP template

  * The EAP template requires an [SSL keystore and a JGroups keystore](https://access.redhat.com/documentation/en-us/red_hat_jboss_middleware_for_openshift/3/html-single/red_hat_jboss_sso_for_openshift/#Configuring-Keystores). his example uses **keytool**, a package included with the Java Development Kit, to generate self-signed certificates for these keystores.

    * Generate a secure key for the SSL keystore (this example uses **password** as password for the keystore).
      ```
      $ keytool -genkeypair \
      -dname "CN=secure-eap-app-eap-app-adapters-install-demo.openshift.example.com" \
      -alias https \
      -storetype JKS \
      -keystore eapkeystore.jks
      ```
    * Generate a secure key for the JGroups keystore (this example uses **password** as password for the keystore).
      ```
      $ keytool -genseckey \
      -alias jgroups \
      -storetype JCEKS \
      -keystore eapjgroups.jceks
      ```
    * Generate the EAP 6.4 / 7.0 for OpenShift secrets with the SSL and JGroup keystore files.
      ```
      $ oc create secret generic eap-ssl-secret --from-file=eapkeystore.jks
      ```

      ```
      $ oc create secret generic eap-jgroup-secret --from-file=eapjgroups.jceks
      ```
    * Add the EAP application secret to the [default](https://docs.openshift.com/container-platform/latest/dev_guide/service_accounts.html#default-service-accounts-and-roles) service account.
      ```
      $ oc secrets link default eap-ssl-secret eap-jgroup-secret
      ```

#### Create modified *_eap71-sso-s2i-adapters-install_* application template

  **Important**: To be able to import the modified *_eap71-sso-s2i-adapters-install_* application template into the *_openshift_* namespace below, the user account in question needs to have cluster administrator privileges or the user needs to have project administrator access to the global *_openshift_* project. On your master host(s), login as follows:
  ```
  $ oc login -u system:admin
  ```

  * Export the original form of the *_eap71-sso-s2i_* application template:
    ```
    $ oc export template/eap71-sso-s2i -n openshift -o json > /tmp/eap71-sso-s2i-adapters-install.json
    ```
  * Modify its name to *_eap71-sso-s2i-adapters-install_* so it does not collide with the original when imported back in the next step:
    ```
    $ sed -i 's/eap71-sso-s2i/eap71-sso-s2i-adapters-install/g' /tmp/eap71-sso-s2i-adapters-install.json
    ```
  * Import the customized *_eap71-sso-s2i-adapters-install_* template back to the *_openshift_* namespace:
    ```
    $ oc create -f /tmp/eap71-sso-s2i-adapters-install.json -n openshift
    ```
  * Define the *_strategy/sourceStrategy/scripts_* field of the template to point to custom location of the S2I image builder scripts:
    ```
    oc patch template/eap71-sso-s2i-adapters-install -n openshift --type=json \
    -p '[{"op": "add", "path": "/objects/6/spec/strategy/sourceStrategy/scripts", "value": "https://raw.githubusercontent.com/iankko/openshift-examples/eap-s2i-custom-rh-sso-adapters/eap-s2i-install-custom-rh-sso-adapters/s2i"}]'
    ``` 
   **Note:** The BuildConfig is defined as the 7-th element of the *_objects_* array in the template. That's the reason we use *_6_* index to reference it in the previous command (indexing starts from zero).

  * Edit the template to require the new *_SSO_ADAPTERS_OVERRIDES_* environment variable, defaulting to the **latest** value. Append this variable as the last one into the existing list of template parameters:
    ```
    $ oc patch template/eap71-sso-s2i-adapters-install -n openshift --type=json \
    -p '[{"op": "add", "path": "/parameters/-", "value": {"name": "SSO_ADAPTERS_OVERRIDES", "displayName": "RH-SSO adapters overrides", "description": "Override the default version of the installed RH-SSO adapters", "value": "latest", "required": true}}]'
    ```
  * Pass the provided value of the *_SSO_ADAPTERS_OVERRIDES_* parameter (user input) to the particular buildConfig:
    ```
    $ oc patch template/eap71-sso-s2i-adapters-install -n openshift --type=json \
    -p '[{"op": "add", "path": "/objects/6/spec/strategy/sourceStrategy/env/-", "value": {"name": "SSO_ADAPTERS_OVERRIDES", "value": "${SSO_ADAPTERS_OVERRIDES}"}}]'
    ```

#### Deploy the application using the modified *_eap71-sso-s2i-adapters-install_* template

  * Finally, deploy the application from the modified *_eap71-sso-s2i-adapters-install_* template, using the *_3.4.6.Final-redhat-1_* as the version of the RH-SSO adapters:

   **Important: Be sure to modify the values of the *_SSO_REALM_*, *_SSO_USERNAME_*, *_SSO_PASSWORD_*, and *_SSO_PUBLIC_KEY_* variables in the following command, as appropriate for your environment.**

   **Note:** For simplicity, the following command uses the same secret to configure all of the following:
    * HTTPS service,
    * RH-SSO SAML client, and
    * RH-SSO truststore.

   **In production environment it is recommended to use different secrets per each of these aspects.** (in other words different secret for HTTPS service configuration, another one for RH-SSO SAML client configuration etc.).

  ```
  $ oc new-app --template=eap71-sso-s2i-adapters-install \
  -p SSO_ADAPTERS_OVERRIDES="3.4.6.Final-redhat-1" \
  -p HOSTNAME_HTTP="eap-app-eap-app-adapters-install-demo.openshift.example.com" \
  -p HOSTNAME_HTTPS="secure-eap-app-eap-app-adapters-install-demo.openshift.example.com" \
  -p SSO_URL="https://secure-sso-sso-app-demo.openshift.example.com/auth" \
  -p SSO_SERVICE_URL="https://secure-sso.sso-app-demo.svc:8443/auth" \
  -p SSO_REALM="demorealm" \
  -p SSO_USERNAME="demouser" \
  -p SSO_PASSWORD="demopassword" \
  -p SSO_PUBLIC_KEY="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA4zWb+NSohaR5vz7MAydmvV3tRE3NTnLYUeVITZ9BimCtvZJcP8HkUPfs9MjYr7/KnTTxRyMSAkgmUO4L4CBG88TNbEOuhKpJmXCvxW0wQfNRUG+5M4WMJ3McezhKqoxVRDF+iQ33I5hwbL1nnhKEL6TDuSPRA6/gOFVECMcG9/yydTe+mKlrhe5tBIX9EXe2u1tdy4VZVgj5QEmSWmADJj/EXWKYlE1WuTKIeYMXilHKAsMtVhfDKOedSZPnWUyFB2C2jXbOIK5Gohj/SqVcXCsHPO5cAMGJASL+x0/dIx4ZEMpbHiqs2Y3xazH3VmWnKowU0BKHKicMkUogCMMEoQIDAQAB" \
  -p HTTPS_SECRET="eap-ssl-secret" \
  -p HTTPS_KEYSTORE="eapkeystore.jks" \
  -p HTTPS_NAME="https" \
  -p HTTPS_PASSWORD="password" \
  -p JGROUPS_ENCRYPT_SECRET="eap-jgroup-secret" \
  -p JGROUPS_ENCRYPT_KEYSTORE="eapjgroups.jceks" \
  -p JGROUPS_ENCRYPT_NAME="jgroups" \
  -p JGROUPS_ENCRYPT_PASSWORD="password" \
  -p SSO_SAML_KEYSTORE_SECRET="eap-ssl-secret" \
  -p SSO_SAML_KEYSTORE="eapkeystore.jks" \
  -p SSO_SAML_CERTIFICATE_NAME="https" \
  -p SSO_SAML_KEYSTORE_PASSWORD="password" \
  -p SSO_TRUSTSTORE_SECRET="eap-ssl-secret" \
  -p SSO_TRUSTSTORE="eapkeystore.jks" \
  -p SSO_TRUSTSTORE_PASSWORD="password"
  ```

  * Identify the build and wait for it to finish:
    ```
    $ oc get pods
    NAME              READY     STATUS    RESTARTS   AGE
    eap-app-1-build   1/1       Running   0          9s
    ```

    ```
    $ oc logs eap-app-1-build --follow
    ...
    Pushing image docker-registry.default.svc:5000/eap-app-adapters-install-demo/eap-app:latest ...
    Pushed 4/7 layers, 57% complete
    Pushed 5/7 layers, 87% complete
    Pushed 6/7 layers, 97% complete
    Pushed 7/7 layers, 100% complete
    Push successful
    ```
  *  Determine the name of the EAP application pod:
    ```
    $ oc get pods
    NAME               READY     STATUS      RESTARTS   AGE
    eap-app-1-56lh6    0/1       Running     0          16s
    eap-app-1-build    0/1       Completed   0          9m
    eap-app-1-deploy   1/1       Running     0          17s
    ``` 
  * Verify the version of the RH-SSO adapters installed in the pod:
    ```
    $ oc rsh eap-app-1-56lh6 find / -name *.jar 2>/dev/null | grep keycloak | tail -4
    /opt/eap/modules/system/add-ons/keycloak/org/keycloak/keycloak-undertow-adapter/main/keycloak-undertow-adapter-3.4.6.Final-redhat-1.jar
    /opt/eap/modules/system/add-ons/keycloak/org/keycloak/keycloak-wildfly-subsystem/main/keycloak-wildfly-subsystem-3.4.6.Final-redhat-1.jar
    /opt/eap/modules/system/add-ons/keycloak/org/keycloak/keycloak-adapter-core/main/keycloak-adapter-core-3.4.6.Final-redhat-1.jar
    /opt/eap/modules/system/add-ons/keycloak/org/keycloak/keycloak-core/main/keycloak-core-3.4.6.Final-redhat-1.jar
    ```
  * (Optionally) Verify the clients have been properly registered:
    ```
    $ oc logs eap-app-1-56lh6 | grep -P 'Registered .* client'
    INFO Registered openid-connect client for module app-jsp in realm demorealm on "http://eap-app-eap-app-adapters-install-demo.openshift.example.com/app-jsp/*","https://secure-eap-app-eap-app-adapters-install-demo.openshift.example.com/app-jsp/*"
    INFO Registered openid-connect client for module app-profile-jsp in realm demorealm on "http://eap-app-eap-app-adapters-install-demo.openshift.example.com/app-profile-jsp/*","https://secure-eap-app-eap-app-adapters-install-demo.openshift.example.com/app-profile-jsp/*"
    INFO Registered openid-connect client for module service in realm demorealm on "http://eap-app-eap-app-adapters-install-demo.openshift.example.com/service/*","https://secure-eap-app-eap-app-adapters-install-demo.openshift.example.com/service/*"
    INFO Registered saml client for module app-profile-saml in realm demorealm on "http://eap-app-eap-app-adapters-install-demo.openshift.example.com/app-profile-saml/*","https://secure-eap-app-eap-app-adapters-install-demo.openshift.example.com/app-profile-saml/*"
    ``` 

## What are the changes

**Note:** Per suggestion in the [Overriding Builder Image Scripts](https://docs.openshift.com/container-platform/3.3/dev_guide/builds.html#override-builder-image-scripts) section of the OpenShift documentation the scripts path specified in the *_strategy/sourceStrategy/scripts_* field:

> will have run, assemble, and save-artifacts appended to it. If any or all scripts are found they will be used in place of the same named script(s) provided in the image.

Since the default form of [*_assemble_*](https://github.com/jboss-openshift/cct_module/blob/master/os-eap-s2i/added/s2i/assemble) S2I script, as shipped with the EAP 6.4 and EAP 7.1 Middleware images also imports:

* [The code from *_common.sh_* shell script](https://github.com/jboss-openshift/cct_module/blob/master/os-eap-s2i/added/s2i/assemble#L8), and
* [The code from *_scl-enable-maven_* script](https://github.com/jboss-openshift/cct_module/blob/master/os-eap-s2i/added/s2i/assemble#L9)

the definition of the *_assemble.orig_* file, present in this repository has been modified with the expanded (full) form of these two scripts taken from the respective definitions of:
* [*_common.sh_*](https://github.com/jboss-openshift/cct_module/blob/master/s2i-common/common.sh)
* [*_scl-enable-maven_*](https://github.com/jboss-openshift/cct_module/blob/master/jboss-maven/added/s2i/scl-enable-maven)

via the following two commands:

 ```
 $ sed -i \
 -e '/. \$(dirname \$0)\/common.sh/r common.sh' \
 -e '/. \$(dirname \$0)\/common.sh/d' assemble.orig
 ```

 ```
 $ sed -i \
 -e '/source \/usr\/local\/s2i\/scl-enable-maven/r scl-enable-maven' \
 -e '/source \/usr\/local\/s2i\/scl-enable-maven/d' assemble.orig
 ```

**Without these changes**, the *_assemble_* script appended to the *_scripts_* path during processing of builder image scripts reported *_No such file or directory: common.sh_* error message.

Clone the repository, and issue:

```
$ diff assemble.orig assemble
```

to see the actual implementation of the *_install_custom_rh_sso_adapters()_* routine and call to it (the changes between modified *_assemble_* script and its expanded form in the *_assemble.orig_* script).
