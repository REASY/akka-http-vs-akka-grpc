package benchmark.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.DebuggingDirectives
import benchmark.common.services.ServiceExampleImpl
import benchmark.grpc._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.{Decoder, Encoder}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object AkkaHttpServer {
  def main(args: Array[String]): Unit = {
    val port: Int = 8080

    // Akka boot up code
    implicit val system: ActorSystem = ActorSystem()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    new AkkaHttpServer("localhost", port, new ServiceExampleImpl()).run()
    println("Press `CTRL+C` to stop...")
  }
}

class AkkaHttpServer(host: String, port: Int, svc: ServiceExampleImpl)
                    (implicit val system: ActorSystem, ec: ExecutionContext)
  extends FailFastCirceSupport {

  import io.circe.generic.auto._

  implicit val employeeDecoder: Decoder[Employee] =
    Decoder.forProduct5("firstName", "lastName", "age", "height", "flag")(Employee.of)

  implicit val employeeEncoder: Encoder[Employee] =
    Encoder.forProduct5("firstName", "lastName", "age", "height", "flag")(e =>
      (e.firstName, e.lastName, e.age, e.height, e.flag))

  implicit val addEmployeeRequestDecoder: Decoder[AddEmployeeRequest] =
    Decoder.forProduct1("employee")(AddEmployeeRequest.of)

  implicit val AddEmployeeResponseEncoder: Encoder[AddEmployeeResponse] =
    Encoder.forProduct1("employeeId")(r => r.employeeId)

  val route: Route = {
    path("employees") {
      post {
        entity(as[AddEmployeeRequest]) { request =>
          onSuccess(svc.addEmployee(request)) { r =>
            complete(r)
          }
        }
      }
    }
  } ~
    pathPrefix("employees" / IntNumber) { employeeId =>
      onSuccess(svc.getEmployee(GetEmployeeRequest(employeeId))) { r =>
        complete(r)
      }
    } /* ~ FIXME Need to implement encoder/decoder for the remaining types
    path("sweets") {
      post {
        entity(as[AddSweetsRequest]) { request =>
          onSuccess(svc.addSweets(request)) { r =>
            complete(r)
          }
        }
      } ~
        get {
          onSuccess(svc.getAllSweets(new Empty)) { r =>
            complete(r)
          }
        }
    } ~
    pathPrefix("blocking" / IntNumber) { employeeId =>
      onSuccess(svc.blocking(GetEmployeeRequest(employeeId))) { r =>
        complete(r)
      }
    }*/

  def run(): Future[Http.ServerBinding] = {
    // Bind service handler server
    val bound = Http().newServerAt(interface = host, port = port)
      .bind(route)

    // report successful binding
    bound.foreach { binding =>
      println(s"Akka-HTTP server bound to: ${binding.localAddress}")
    }
    bound
  }
}


