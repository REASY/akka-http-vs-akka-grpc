package benchmark

import io.gatling.core.Predef._
import io.gatling.http.Predef._

class BlockingHttp extends Simulation with Fixture with CommonFormatter {
  val flatBody: String = toJsonString(addEmployeeRequest)

  val nestedBody: String = toJsonString(addSweetsRequest)

  before {
    println("***** Blocking Akka HTTP simulation is about to begin! *****")
  }

  after {
    println("***** Blocking Akka HTTP simulation has ended! ******")
  }

  val httpConf = http.baseUrl("http://localhost:8080")

  val sc1 = scenario("Blocking")
    .exec(
      http("Blocking")
        .get("/blocking/42")
    )

  setUp(
    sc1.inject(constantUsersPerSec(blockingRate) during duration)
  ).protocols(httpConf)
}
