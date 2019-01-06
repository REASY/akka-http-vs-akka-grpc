package benchmark

import benchmark.grpc.schema._

import scala.concurrent.duration._

trait Fixture {
  val duration: FiniteDuration = 10.minutes

  val nonBlockingRate: Int = 120

  val blockingRate: Int = 500

  val employee: Employee  = Employee(firstName = "Test", lastName = "Test",
    age = 28, height = 182.5f, flag = true)

  val addEmployeeRequest: AddEmployeeRequest = AddEmployeeRequest(employee = Some(employee))

  val sweets: Sweets = Sweets(
    sweets = Seq(
      Sweet(id = "0001", `type` = "donut", name = "Cake", ppu = 0.55,
      batters = Seq(Batter("1001", "Regular"), Batter("1002", "Chocolate"), Batter("1003", "Blueberry"),
        Batter("1004", "Devil's Food")),
      topping = Seq(		Topping("5001", "None"),
        Topping("5002", "Glazed"),
        Topping("5005", "Sugar"),
        Topping("5007", "Powdered Sugar"),
        Topping("5006", "Chocolate with Sprinkles"),
        Topping("5003", "Chocolate"),
        Topping("5004", "Maple"))),
      Sweet(id = "0002", `type` = "donut", name = "Raised", ppu = 0.55,
        batters = Seq(Batter("1001", "Regular")),
        topping = Seq(Topping("5001", "None"),
          Topping("5002", "Glazed"),
          Topping("5003", "Chocolate"),
          Topping("5004", "Maple"))),
      Sweet(id = "0003", `type` = "donut", name = "Old Fashioned", ppu = 0.55,
        batters = Seq(Batter("1001", "Regular"), Batter("1002", "Chocolate")),
        topping = Seq(Topping("5001", "None"),
          Topping("5002", "Glazed"),
          Topping("5003", "Chocolate"),
          Topping("5004", "Maple"))))
  )

  val addSweetsRequest: AddSweetsRequest = AddSweetsRequest(sweets.sweets)
}
