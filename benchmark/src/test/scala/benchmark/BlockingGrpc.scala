package benchmark

import benchmark.grpc.schema._
import io.gatling.core.Predef.{stringToExpression => _, _}
import io.grpc.ManagedChannelBuilder

class BlockingGrpc extends Simulation with com.github.phisgr.gatling.grpc.GrpcDsl with Fixture {
  before {
    println("***** Non-blocking GRPC simulation is about to begin! *****")
  }

  after {
    println("***** Non-blocking GRPC simulation has ended! ******")
  }

  val grpcConf = grpc(ManagedChannelBuilder.forAddress("localhost", 8081).usePlaintext())

  val sc1 = scenario("Blocking")
    .exec(setUpGrpc)
    .exec(
      grpc("Blocking")
        .rpc(ServiceExampleGrpc.METHOD_BLOCKING)
        .payload(GetEmployeeRequest(1))
    )
  setUp(
    sc1.inject(constantUsersPerSec(blockingRate) during duration),
  ).protocols(grpcConf)
}
