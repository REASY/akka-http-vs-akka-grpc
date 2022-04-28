package benchmark.grpc

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import benchmark.common.services.ServiceExampleImpl
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object AkkaGrpcServer {

  def main(args: Array[String]): Unit = {
    // Important: enable HTTP/2 in ActorSystem's config
    // We do it here programmatically, but you can also set it in the application.conf
    val conf = ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on")
      .withFallback(ConfigFactory.defaultApplication())

    // Akka boot up code
    implicit val system: ActorSystem = ActorSystem("GrpcServer", conf)
    implicit val mat: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    new AkkaGrpcServer("127.0.0.1", 8081, new ServiceExampleImpl()).run()
    println("Press `CTRL+C` to stop...")
  }
}

class AkkaGrpcServer(host: String, port: Int, svc: ServiceExampleImpl)
                    (implicit val sys: ActorSystem, mat: ActorMaterializer, ec: ExecutionContext){
  def run(): Future[Http.ServerBinding] = {
    // Create service handler
    val service: HttpRequest => Future[HttpResponse] =
      ServiceExampleHandler(svc)

    // Bind service handler server
    val bound = Http().newServerAt(host, port)
      .bind(service)

    // report successful binding
    bound.foreach { binding =>
      println(s"Akka-gRPC server bound to: ${binding.localAddress}")
    }
    bound
  }
}