# Akka HTTP vs Akka gRPC
The aim of this project is to show a brief performance comparison [Akka HTTP v10.1.5](https://doc.akka.io/docs/akka-http/current/) and [Akka gRPC v0.4.2](https://developer.lightbend.com/docs/akka-grpc/current/).

Originally I tried to use  [Gatling](https://gatling.io/) with [Gatling-gRPC](https://github.com/phiSgr/gatling-grpc) plugin to support gRPC, but it could not make it to  keep high number of requests. So that's why I decided to switch to [wrk2](https://github.com/giltene/wrk2) to load test HTTP and [ghz](https://github.com/bojand/ghz) to load test gRPC.

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
Execute the following to run  it against Akka HTTP.
```sh
wrk -t1 -c100 -d300s -R14000 --latency http://127.0.0.1:8080/employees/42
```

### Load test against Akka gRPC
Execute the following to run  it against Akka gRPC.
```sh
ghz -insecure -proto ./schema.proto -call benchmark.grpc.ServiceExample.getEmployee -c 100 -z 300s -d '{"employeeId": 1}' 127.0.0.1:8081
```

## Results on local machine
My machine:
-   OS: Microsoft Windows  10 x64 [Version 10.0.17134.472]
-   CPU: AMD Ryzen 7 2700X Eight-Core Processor i7 3.7 GHz
-   Memory: DDR4-3200 GHz 16 GB
-   JVM: Java HotSpot(TM) 64-Bit Server VM (build 25.191-b12, mixed mode)
### HTTP
```
wrk -t1 -c100 -d300s -R14000 --latency http://127.0.0.1:8080/employees/42
Running 5m test @ http://127.0.0.1:8080/employees/42
  1 threads and 100 connections
  Thread calibration: mean lat.: 1.604ms, rate sampling interval: 10ms
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.59ms  799.39us  34.66ms   75.78%
    Req/Sec    14.75k     1.21k   52.30k    84.76%
  Latency Distribution (HdrHistogram - Recorded Latency)
 50.000%    1.48ms
 75.000%    2.08ms
 90.000%    2.50ms
 99.000%    3.18ms
 99.900%    7.16ms
 99.990%   24.19ms
 99.999%   32.86ms
100.000%   34.69ms
#[Mean    =        1.589, StdDeviation   =        0.799]
#[Max     =       34.656, Total count    =      4052985]
#[Buckets =           27, SubBuckets     =         2048]
----------------------------------------------------------
  4196552 requests in 5.00m, 832.45MB read
Requests/sec:  13988.51
Transfer/sec:      2.77MB
```

### gRPC
```
ghz -insecure -proto ./schema.proto -call benchmark.grpc.ServiceExample.getEmployee -c 100 -z 300s -d '{"employeeId": 1}' 127.0.0.1:8081

Summary:
  Count:        4253496
  Total:        300003.01 ms
  Slowest:      82.51 ms
  Fastest:      0.42 ms
  Average:      3.48 ms
  Requests/sec: 14178.18
Latency distribution:
  10% in 3.07 ms
  25% in 3.28 ms
  50% in 3.43 ms
  75% in 3.57 ms
  90% in 3.75 ms
  95% in 3.95 ms
  99% in 5.09 ms
Status code distribution:
  [OK]   4253496 responses
```
### Table
```
| Benchmark   | Total number of requests | Req/s    | Max,ms | Avg, ms | 50th pct | 75th pct | 90th pct | 99th pct |
|-------------|--------------------------|----------|--------|---------|----------|----------|----------|----------|
| Akka HTTP	  | 4196552                  | 13988.51 | 34.656 | 1.589   | 1.48     | 2.08     | 2.50     | 3.18     |
| Akka gRPC	  | 4253496                  | 14178.18 | 82.51	 | 3.48    | 3.43     | 3.57     | 3.75     | 5.09     |

```

### Why `Akka gRPC` performing worst than `Akka HTTP`?
I run both load-tests again with [Java Flight Recorder](https://docs.oracle.com/javacomponents/jmc-5-4/jfr-runtime-guide/about.htm#JFRUH170) turned-on to get some understanding what can be an issue. Here is the link to [jfr files](https://drive.google.com/open?id=110L63Vv8hy7nV8Qc7j6Y0Ql5l1SswhdW). To open them you have to have installed Oracle JDK and use [Java Mission Control](https://www.oracle.com/technetwork/java/javaseproducts/mission-control/index.html).  [GC logs for Akka HTTP server](https://gceasy.io:443/my-gc-report.jsp?p=c2hhcmVkLzIwMTkvMDEvMTUvLS1nY19ha2thLWh0dHBfMjAxOTAxMTUwOTUyMTcubG9nLS0xNi0yMS0yOQ==&channel=WEB) and [for Akka gRPC server](https://gceasy.io:443/my-gc-report.jsp?p=c2hhcmVkLzIwMTkvMDEvMTUvLS1nY19ha2thLWdycGNfMjAxOTAxMTUxMDAzMDIubG9nLS0xNi0yMi0yNQ==&channel=WEB). I guess the root cause might be GC, it triggers more often for `Akka gRPC` load test. But intuitively it seems that protobuf serialization/deserialzation should be faster and more GC-friendly. I found out that a lot of memory is consumed during protobuf deserialization:
Here is the call from ScalaPB: [GeneratedMessageCompanion.scala#L197](https://github.com/scalapb/ScalaPB/blob/master/scalapb-runtime/shared/src/main/scala/scalapb/GeneratedMessageCompanion.scala#L197)  to Google protobuf [CodedInputStream.java#L92](https://github.com/protocolbuffers/protobuf/blob/3.6.x/java/core/src/main/java/com/google/protobuf/CodedInputStream.java#L92) with `DEFAULT_BUFFER_SIZE`4Kbytes which allocate that array here [CodedInputStream.java#L2101](https://github.com/protocolbuffers/protobuf/blob/3.6.x/java/core/src/main/java/com/google/protobuf/CodedInputStream.java#L2101).