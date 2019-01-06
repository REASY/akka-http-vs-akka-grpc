package benchmark.common.services

import java.util.concurrent.atomic.AtomicInteger

import benchmark.grpc._

import scala.concurrent.Future

class ServiceExampleImpl extends ServiceExample{
  private val nextId: AtomicInteger = new AtomicInteger(0)

  override def addEmployee(in: AddEmployeeRequest): Future[AddEmployeeResponse] = {
    Future.successful(AddEmployeeResponse(employeeId = nextId.getAndIncrement()))
  }

  override def getEmployee(in: GetEmployeeRequest): Future[Employee] = {
    Future.successful(Employee(firstName = "Test", lastName = "Test",
      age = 28, height = 182.5f, flag = true))
  }

  override def addSweets(in: AddSweetsRequest): Future[AddSweetsResponse] = {
    Future.successful(AddSweetsResponse(status = 42))
  }

  override def getAllSweets(in: Empty): Future[Sweets] = {
    val sweet1 = Sweet(id = "0001", `type` = "donut", name = "Cake", ppu = 0.55,
      batters = Seq(Batter("1001", "Regular"), Batter("1002", "Chocolate"), Batter("1003", "Blueberry"),
        Batter("1004", "Devil's Food")),
      topping = Seq(		Topping("5001", "None"),
        Topping("5002", "Glazed"),
        Topping("5005", "Sugar"),
        Topping("5007", "Powdered Sugar"),
        Topping("5006", "Chocolate with Sprinkles"),
        Topping("5003", "Chocolate"),
        Topping("5004", "Maple")))

    val sweet2 = Sweet(id = "0002", `type` = "donut", name = "Raised", ppu = 0.55,
      batters = Seq(Batter("1001", "Regular")),
      topping = Seq(Topping("5001", "None"),
        Topping("5002", "Glazed"),
        Topping("5003", "Chocolate"),
        Topping("5004", "Maple")))

    val sweet3 = Sweet(id = "0003", `type` = "donut", name = "Old Fashioned", ppu = 0.55,
      batters = Seq(Batter("1001", "Regular"), Batter("1002", "Chocolate")),
      topping = Seq(Topping("5001", "None"),
        Topping("5002", "Glazed"),
        Topping("5003", "Chocolate"),
        Topping("5004", "Maple")))

    Future.successful(Sweets(sweets = Seq(sweet1, sweet2, sweet3)))
  }

  override def blocking(in: GetEmployeeRequest): Future[Employee] = {
    Thread.sleep(20)
    getEmployee(in)
  }
}