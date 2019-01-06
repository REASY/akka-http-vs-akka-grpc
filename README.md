
# Akka HTTP vs Akka gRPC
The aim of this project is to show a brief performance comparison [Akka HTTP v10.1.5](https://doc.akka.io/docs/akka-http/current/) and [Akka gRPC v0.4.2](https://developer.lightbend.com/docs/akka-grpc/current/). 

The structure of this repo is as follows:
 - [servers](servers) folder contains code related to servers
 - [benchmark](benchmark) folder contains load testing code

It is splitted in this way so dependencies do not interfere with each other. For load-testing I use [Gatling](https://gatling.io/) with [Gatling-gRPC](https://github.com/phiSgr/gatling-grpc) plugin to support gRPC. Please, keep in mind that `.proto` file [schema.proto](servers/common/src/main/protobuf/schema.proto) and [schema.proto](benchmark/src/main/protobuf/schema.proto) must be in sync.

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
Before running the load tests either Akka HTTP Server or Akka gRPC Server must be running first. Scenario for [HTTP load test](benchmark/src/test/scala/benchmark/NonBlockingHttp.scala), for [gRPC load test](benchmark/src/test/scala/benchmark/NonBlockingGrpc.scala). Load test duration is `10 minutes`, request rate is constant - `120` requests per second.
### Load test against Akka HTTP
Execute the following to run  it against Akka HTTP. `Blocking` here and further is about [server side code](https://github.com/REASY/akka-http-vs-akka-grpc/blob/master/servers/common/src/main/scala/benchmark/common/services/ServiceExampleImpl.scala#L55) which is blocking operation (e.g., `Thread.sleep`, `Await.result` and etc):
```sh
cd benchmark && sbt 'gatling:testOnly benchmark.NonBlockingHttp'
```

### Load test against Akka gRPC
Execute the following to run  it against Akka gRPC.
```sh
cd benchmark && sbt 'gatling:testOnly benchmark.NonBlockingGrpc'
```

## Results on local machine
My machine:
-   OS: Microsoft Windows  10 x64 [Version 10.0.17134.472]
-   CPU: AMD Ryzen 7 2700X Eight-Core Processor i7 3.7 GHz
-   Memory: DDR4-3200 GHz 16 GB
-   JVM: Java HotSpot(TM) 64-Bit Server VM (build 25.191-b12, mixed mode)

### [Non-Blocking HTTP](http://htmlpreview.github.io/?https://github.com/REASY/akka-http-vs-akka-grpc/blob/master/benchmark/results/gatling/http/index.html)
|Request          | Total | OK    | KO | KO % | Req/s | Min | 50th pct | 75th pct | 95th pct | 99th pct | Max | Mean | Std dev |
|-----------------|-------|-------|----|------|-------|-----|----------|----------|----------|----------|-----|------|---------|
| Add flat type	  | 72000 | 72000 | 0  | 0%	  | 119.8 | 0   |  1       | 1        | 2        | 3        | 12  | 1    | 1       |
| Get flat type	  | 72000 | 72000 | 0  | 0%	  | 119.8 | 0   |  1       | 1        | 2        | 3        | 11  | 1    | 1       |
| Add nested type | 72000 | 72000 | 0  | 0%	  | 119.8 | 0   |  1       | 1        | 2        | 3        | 12  | 1    | 1       |
| Get nested type | 72000 | 72000 | 0  | 0%	  | 119.8 | 0   |  1       | 2        | 2        | 3        | 8   | 1    | 1       |


### [Non-Blocking gRPC](http://htmlpreview.github.io/?https://github.com/REASY/akka-http-vs-akka-grpc/blob/master/benchmark/results/gatling/grpc/index.html)
|Request          | Total | OK    | KO | KO % | Req/s | Min | 50th pct | 75th pct | 95th pct | 99th pct | Max  | Mean | Std dev |
|-----------------|-------|-------|----|------|-------|-----|----------|----------|----------|----------|------|------|---------|
| Add flat type	  | 72000 | 72000 | 0  | 0%	  | 120	  | 1   |  1	   | 2        | 3        | 7        | 941  | 2    | 18      |
| Get flat type	  | 72000 | 72000 | 0  | 0%	  | 120	  | 0   |  1	   | 2        | 3        | 7        | 974  | 2    | 22      |
| Add nested type | 72000 | 72000 | 0  | 0%	  | 120	  | 1   |  1	   | 2        | 3        | 7        | 934  | 2    | 22      |
| Get nested type | 72000 | 72000 | 0  | 0%	  | 120	  | 1   |  1	   | 2        | 3        | 7        | 917  | 2    | 22      |

### Why `Akka gRPC` performing worst than `Akka HTTP`?
I run both load-tests again with [Java Flight Recorder](https://docs.oracle.com/javacomponents/jmc-5-4/jfr-runtime-guide/about.htm#JFRUH170) turned-on to get some understanding what can be an issue. Here is the link to [jfr files](https://drive.google.com/open?id=1RUcjsRUwTcBDqLDWqimxc6K0MVbtuDSL). To open them you have to have installed Oracle JDK and use [Java Mission Control](https://www.oracle.com/technetwork/java/javaseproducts/mission-control/index.html). I guess the root cause might be GC, it triggers more often for `Akka gRPC` load test (Allocation rate is 1.652 times higher). But intuitively it seems that protobuf serialization/deserialzation should be faster and more GC-friendly. Though [Akka-gRPC](https://github.com/akka/akka-grpc#project-status) team says it's production-ready.

### CPU usage
![CPU usage](benchmark/results/jmc/CPU.PNG)

### Threads
![Memory usage](benchmark/results/jmc/Threads.PNG)

### Memory usage
![Memory usage](benchmark/results/jmc/Memory.PNG)

### Allocations
![Memory usage](benchmark/results/jmc/Allocations.PNG)
