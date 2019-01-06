
# Akka HTTP vs Akka gRPC
The aim of this project is to show a brief performance comparison [Akka HTTP](https://doc.akka.io/docs/akka-http/current/) and [Akka gRPC](https://developer.lightbend.com/docs/akka-grpc/current/). 

The structure of this repo is as follows:
	-  `servers` folder contains code related to servers
	- `benchmark` folder contains load testing code
It is splitted in this way so dependencies do not interfere with each other. For load-testing I use [Gatling](https://gatling.io/) with [Gatling-gRPC](https://github.com/phiSgr/gatling-grpc) plugin to support gRPC.

## How to run servers
### Akka HTTP Server
To run the Akka HTTP server execute the following:
```sh
cd servers && sbt 'project akka-http' run
```
When it has started you will see:
```
[info] Running (fork) benchmark.http.AkkaHttpServer
[info] Press `CTRL+C` to stop...
[info] Akka-HTTP server bound to: /127.0.0.1:8080
```

### Akka gRPC Server
To run the Akka gRPC server execute the following:
```sh
cd servers && sbt 'project akka-grpc' run
```
When it has started you will see:
```
[info] Running (fork) benchmark.grpc.AkkaGrpcServer
[info] Press `CTRL+C` to stop...
[info] Akka-gRPC server bound to: /127.0.0.1:8081
```

## How to run load test
Before running the load tests either Akka HTTP Server or Akka gRPC Server must be running first. 
### Load test against Akka HTTP
Execute the following to run  it against Akka HTTP. `Blocking` here and further is about **server side code** having block operation (e.g., `Thread.sleep`, `Await.result` and etc):
```sh
cd benchmark && sbt 'gatling:testOnly benchmark.NonBlockingHttp'
```

### Load test against Akka gRPC
Execute the following to run  it against Akka gRPC.
```sh
cd benchmark && sbt 'gatling:testOnly benchmark.NonBlockingGrpc'
```