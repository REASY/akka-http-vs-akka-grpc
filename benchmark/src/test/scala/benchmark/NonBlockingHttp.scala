package benchmark

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class NonBlockingHttp extends Simulation with Fixture with CommonFormatter {
  val flatBody: String = toJsonString(addEmployeeRequest)

  val nestedBody: String = toJsonString(addSweetsRequest)

  before {
    println("***** Non-blocking Akka HTTP simulation is about to begin! *****")
  }

  after {
    println("***** Non-blocking Akka HTTP simulation has ended! ******")
  }

  val httpConf = http.baseUrl("http://localhost:8080")

  val sc1 = scenario("Add flat type")
    .exec(
      http("Add flat type")
        .post("/employees")
        .body(StringBody(flatBody)).asJson
    )

  val sc2 = scenario("Get flat type")
    .exec(
      http("Get flat type")
        .get("/employees/42")
    )

  val sc3 = scenario("Add nested type")
    .exec(
      http("Add nested type")
        .post("/sweets")
        .body(StringBody(nestedBody)).asJson
    )

  val sc4 = scenario("Get nested type")
    .exec(
      http("Get nested type")
        .get("/sweets")
    )

  setUp(
    sc1.inject(constantUsersPerSec(nonBlockingRate) during duration),
    sc2.inject(constantUsersPerSec(nonBlockingRate) during duration),
    sc3.inject(constantUsersPerSec(nonBlockingRate) during duration),
    sc4.inject(constantUsersPerSec(nonBlockingRate) during duration)
  ).protocols(httpConf)
}
