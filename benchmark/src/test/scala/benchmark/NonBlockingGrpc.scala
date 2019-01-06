package benchmark

import benchmark.grpc.schema._
import io.gatling.core.Predef.{stringToExpression => _, _}
import io.grpc.ManagedChannelBuilder

class NonBlockingGrpc extends Simulation with com.github.phisgr.gatling.grpc.GrpcDsl with Fixture {
  before {
    println("***** Non-blocking GRPC simulation is about to begin! *****")
  }

  after {
    println("***** Non-blocking GRPC simulation has ended! ******")
  }

  val grpcConf = grpc(ManagedChannelBuilder.forAddress("localhost", 8081).usePlaintext())

  val sc1 = scenario("Add flat type")
    .exec(setUpGrpc)
    .exec(
      grpc("Add flat type")
        .rpc(ServiceExampleGrpc.METHOD_ADD_EMPLOYEE)
        .payload(addEmployeeRequest)
    )
  val sc2 = scenario("Get flat type")
    .exec(setUpGrpc)
    .exec(
      grpc("Get flat type")
        .rpc(ServiceExampleGrpc.METHOD_GET_EMPLOYEE)
        .payload(GetEmployeeRequest(1))
    )

  val sc3 = scenario("Add nested type")
    .exec(setUpGrpc)
    .exec(
      grpc("Add nested type")
        .rpc(ServiceExampleGrpc.METHOD_ADD_SWEETS)
        .payload(addSweetsRequest)
    )

  val sc4 = scenario("Get nested type")
    .exec(setUpGrpc)
    .exec(
      grpc("Get nested type")
        .rpc(ServiceExampleGrpc.METHOD_GET_ALL_SWEETS)
        .payload(new Empty())
    )

  setUp(
    sc1.inject(constantUsersPerSec(nonBlockingRate) during duration),
    sc2.inject(constantUsersPerSec(nonBlockingRate) during duration),
    sc3.inject(constantUsersPerSec(nonBlockingRate) during duration),
    sc4.inject(constantUsersPerSec(nonBlockingRate) during duration),
  ).protocols(grpcConf)
}
