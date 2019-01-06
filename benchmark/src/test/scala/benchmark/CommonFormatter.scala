package benchmark

import benchmark.grpc.schema._
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

trait CommonFormatter {
  import io.circe.syntax._

  implicit val employeeDecoder: Encoder[Employee] = deriveEncoder[Employee]
  implicit val addEmployeeRequestDecoder: Encoder[AddEmployeeRequest] = deriveEncoder[AddEmployeeRequest]

  implicit val toppingDecoder: Encoder[Topping] = deriveEncoder[Topping]
  implicit val batterDecoder: Encoder[Batter] = deriveEncoder[Batter]
  implicit val sweetDecoder: Encoder[Sweet] = deriveEncoder[Sweet]
  implicit val sweetsDecoder: Encoder[Sweets] = deriveEncoder[Sweets]
  implicit val addSweetsRequestDecoder: Encoder[AddSweetsRequest] = deriveEncoder[AddSweetsRequest]

  def toJsonString[T](obj: T)(implicit enc: Encoder[T]) = obj.asJson.toString()
}