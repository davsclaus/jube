## Troubleshooting

There are a few different ways to try figure out whats going on if things don't behave as you expect:

### Are your environment variables set up?

A common cause of issues; particularly with maven and command line tools is not [setting up the environment variables](getStarted.html#setting-environment-variables) correctly in your shell.

You need to ensure any Fabric8, OpenShift or Kuberentes tools are pointing at your Jube installation:

    export KUBERNETES_MASTER=http://localhost:8585/
    export FABRIC8_CONSOLE=http://localhost:8585/hawtio/

### Checking whats happening

To check on what happens after each command you can use the web console at [http://localhost:8585/hawtio/](http://localhost:8585/hawtio/) using these tabs:

 * [Pods tab](http://localhost:8585/hawtio/kubernetes/pods) views all the available [pods](pods.html) in your kubernetes environment
 * [Replication Controllers tab](http://localhost:8585/hawtio/kubernetes/replicationControllers) views all the available [replication controllers](replicationControllers.html) in your kubernetes environment
 * [Services tab](http://localhost:8585/hawtio/kubernetes/services) views all the available [services](services.html) in your kubernetes environment

Or you could view the JSON directly in your browser:

* [http://localhost:8585/kubernetes/api/v1/pods](http://localhost:8585/kubernetes/api/v1/pods) for the pods
* [http://localhost:8585/kubernetes/api/v1/replicationControllers](http://localhost:8585/kubernetes/api/v1/replicationControllers) for replication controllers
* [http://localhost:8585/kubernetes/api/v1/services](http://localhost:8585/kubernetes/api/v1/services) for the services

Or you can use the [Fabric8 Forge Addon commands](http://fabric8.io/v2/forge.html):

    kubernetes-pod-list
    kubernetes-replication-controller-list
    kubernetes-service-list

### Looking at the log files

If you look in the **/processes** folder inside your jube installation you should see a folder for each pod and inside each of those folders you should see some useful. The files depend on the kind of container being used.

#### For Java containers:

* **logs/cmd.out** shows the standard output of the image zip command along with the environment variables passed into the process
* **logs/out.out** standard output of the pod
* **logs/err.log** standard error of the pod

#### For Karaf based containers:

* **data/logs/karaf.log** the logs of the container