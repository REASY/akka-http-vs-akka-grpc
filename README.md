# Akka HTTP vs Akka gRPC
The aim of this project is to show a brief performance comparison [Akka HTTP v10.2.9](https://doc.akka.io/docs/akka-http/current/) and [Akka gRPC v2.1.4](https://developer.lightbend.com/docs/akka-grpc/current/).

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
-   OS:Ubuntu 20.04.4 LTS, x64
-   CPU: Intel® Core™ i7-10750H CPU @ 2.60GHz × 12
-   Memory: SODIMM DDR4 64 Gbytes
-   JVM: Java 8, OpenJDK Runtime Environment (build 1.8.0_312-8u312-b07-0ubuntu1~20.04-b07)
### HTTP
```
wrk -t1 -c100 -d300s -R14000 --latency http://127.0.0.1:8080/employees/42
Running 5m test @ http://127.0.0.1:8080/employees/42
  1 threads and 100 connections
  Thread calibration: mean lat.: 5.824ms, rate sampling interval: 10ms
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     1.24ms    1.17ms  50.02ms   94.29%
    Req/Sec    14.77k     1.60k   45.44k    90.41%
  Latency Distribution (HdrHistogram - Recorded Latency)
 50.000%    1.09ms
 75.000%    1.47ms
 90.000%    1.99ms
 99.000%    4.52ms
 99.900%   15.52ms
 99.990%   35.29ms
 99.999%   44.35ms
100.000%   50.05ms

#[Mean    =        1.239, StdDeviation   =        1.168]
#[Max     =       50.016, Total count    =      4052987]
#[Buckets =           27, SubBuckets     =         2048]
----------------------------------------------------------
  4196567 requests in 5.00m, 832.45MB read
Requests/sec:  13988.55
Transfer/sec:      2.77MB

```

### gRPC
```
ghz --insecure --proto ./common/src/main/protobuf/schema.proto --call benchmark.grpc.ServiceExample.getEmployee --concurrency 100 --rps 14000 --duration 300s --data '{"employeeId": 1}' 127.0.0.1:8081
Summary:
  Count:	4199967
  Total:	300.00 s
  Slowest:	28.98 ms
  Fastest:	0.10 ms
  Average:	1.14 ms
  Requests/sec:	13999.85

Response time histogram:
  0.103  [1]      |
  2.991  [907901] |∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎∎
  5.878  [66877]  |∎∎∎
  8.766  [19167]  |∎
  11.654 [3071]   |
  14.542 [1338]   |
  17.430 [791]    |
  20.318 [527]    |
  23.205 [167]    |
  26.093 [109]    |
  28.981 [51]     |

Latency distribution:
  10 % in 0.15 ms 
  25 % in 0.17 ms 
  50 % in 0.20 ms 
  75 % in 0.43 ms 
  90 % in 2.78 ms 
  95 % in 4.40 ms 
  99 % in 7.74 ms 

Status code distribution:
  [OK]            4199937 responses   
  [Unavailable]   22 responses        
  [Canceled]      8 responses         

Error distribution:
  [22]   rpc error: code = Unavailable desc = transport is closing                  
  [8]    rpc error: code = Canceled desc = grpc: the client connection is closing   

```
### Table
```
| Benchmark   | Total requests | Req/s    | Max,ms | Avg, ms | 50th pct | 75th pct | 90th pct | 99th pct |
|-------------|----------------|----------|--------|---------|----------|----------|----------|----------|
| Akka HTTP   | 4196567        | 13988.55 | 50.02  | 1.24    | 1.09     | 1.47     | 1.99     | 4.52     |
| Akka gRPC   | 4199967        | 13999.85 | 28.98  | 1.14    | 0.20     | 0.43     | 2.78     | 7.74     |

```