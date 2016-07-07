#Application Templates
This project contains OpenShift v3 all-in-one, demo application templates which support Red Hat SSO/Keycloak and SSO-enabled JEE applications

##Structure

All-in-one example (SSO/Keycloak + various EAP applications utilizing SSO) in ../demos:
 * sso-demo-secret.json: Secret containing full SSO/Keycloak configuration used for import. Required for the all-in-one template
 * sso-all-in-one-demo-config.json: SSO configuration exported by the SSO Server used to create sso-demo-secret.json. Non-functional, for information only.
 * sso70-all-in-one-demo.json: All-in-one SSO/Keycloak template backed by Postgresql with integrated EAP-based example applications
 * sso70-all-in-one-demo-persistent.json: All-in-one SSO/Keycloak template backed by persistent Postgresql with integrated EAP-based example applications

Templates are configured with the following basic parameters:
 * HOSTNAME_HTTP/HOSTNAME_HTTPS: Hostnames of the SSO-enabled JEE application (defaults to helloworld.cloudapps.example.com, secure-helloworld.cloudapps.example.com)
 * SSO_HOSTNAME_HTTP/SSO_HOSTNAME_HTTPS: Hostnames of the SSO server (defaults to sso.cloudapps.example.com, secure-sso.cloudapps.example.com)

##Username/Password
For SSO/Keycloak User created in SSO/Server in All-in-One: demouser/demopass

##All-in-One Example

NOTE: The all-in-one templates assume a domain of cloudapps.example.com. The templates will need to be modified (HOSTNAME_HTTP(S), SSO_HOSTNAME_HTTP(S)) when using another domain

Create Secrets, SSO/Keycloak Server, and SSO/Keycloak-enabled EAP in user (e.g. "myproject") project/namespace:

```
$ oc create -n myproject -f sso-demo-secret.json
$ oc process -f sso70-all-in-one-demo.json | oc create -n myproject -f -
```

After executing the above, you should be able to access the SSO/Keycloak-enabled applications at http://helloworld-myproject.hostname/app-context and https://secure-helloworld-myproject.hostname/app-context where app-context is app-jsp, app-profile-jsp, app-profile-saml-jsp, or service depending on the example application. 

