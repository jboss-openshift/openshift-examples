# Cluster1

Small app that has 3 servlets:

#### Hi
It just prints something like `Served from node: machine-name/192.168.1.40`. Useful when you have a cluster and want to know which node has served you.

#### ShowHeaders
It just prints all request headers as well all request parameters. Useful for debugging.

#### StoreInSession
Accepts `POST` and `GET` requests and works like this:
-  You first `POST` to it, passing a value to the parameter named `key`.
-  Then you just `GET` the servlet. It will print the value previously stored in the `POST`.

The interesting use case of this is to `POST` into a node and `GET` from another, to test if session replication is working.

