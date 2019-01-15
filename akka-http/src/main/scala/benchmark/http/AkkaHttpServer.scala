package benchmark.http

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.DebuggingDirectives
import akka.stream.ActorMaterializer
import benchmark.common.services.ServiceExampleImpl
import benchmark.grpc._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Decoder

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object AkkaHttpServer {
  def main(args: Array[String]): Unit = {
    val port: Int = 8080

    // Akka boot up code
    implicit val system = ActorSystem()
    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    new AkkaHttpServer("localhost", port, new ServiceExampleImpl()).run()
    println("Press `CTRL+C` to stop...")
  }
}

class AkkaHttpServer(host: String, port: Int, svc: ServiceExampleImpl)
                    (implicit val system: ActorSystem, mat: ActorMaterializer, ec: ExecutionContext)
  extends FailFastCirceSupport {

  import io.circe.generic.auto._
  import io.circe.generic.semiauto._

  implicit val employeeDecoder: Decoder[Employee] = deriveDecoder[Employee]
  implicit val addEmployeeRequestDecoder: Decoder[AddEmployeeRequest] = deriveDecoder[AddEmployeeRequest]


  val route: Route = {
    path("employees") {
      post {
        entity(as[AddEmployeeRequest]) { request =>
          complete(svc.addEmployee(request))
        }
      }
    }
  } ~
  pathPrefix("employees" / IntNumber) { employeeId =>
    complete(svc.getEmployee(GetEmployeeRequest(employeeId)))
  } ~
  path("sweets") {
    post {
      entity(as[AddSweetsRequest]) { request =>
        complete(svc.addSweets(request))
      }
    } ~
    get {
      complete(svc.getAllSweets(new Empty))
    }
  } ~
  pathPrefix("blocking" / IntNumber) { employeeId =>
    complete(svc.blocking(GetEmployeeRequest(employeeId)))
  }

  val clientRouteLogged = DebuggingDirectives.logRequestResult("Client ReST", Logging.InfoLevel)(route)

  def run(): Future[Http.ServerBinding] = {
    // Bind service handler server
    val bound = Http().bindAndHandle(
      route,
      interface = host,
      port = port)

    // report successful binding
    bound.foreach { binding =>
      println(s"Akka-HTTP server bound to: ${binding.localAddress}")
    }
    bound
  }
}


