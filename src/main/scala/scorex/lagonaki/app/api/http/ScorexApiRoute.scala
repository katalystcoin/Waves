package scorex.lagonaki.app.api.http

import akka.pattern.ask
import play.api.libs.json.Json
import scorex.api.http.{ApiRoute, CommonApiFunctions}
import scorex.lagonaki.app.LagonakiApplication
import scorex.lagonaki.app.settings.Constants
import scorex.lagonaki.network.BlockchainSyncer
import spray.routing.HttpService._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

case class ScorexApiRoute(application:LagonakiApplication) extends ApiRoute with CommonApiFunctions {

  override lazy val route =
    pathPrefix("scorex") {
      path("stop") {
        get {
          complete {
            Future(application.stopAll())
            Json.obj("stopped" -> true).toString()
          }
        }
      } ~ path("status") {
        get {
          onComplete {
            (application.blockchainSyncer ? BlockchainSyncer.GetStatus).map { status =>
              Json.obj("status" -> status.asInstanceOf[String]).toString()
            }
          } {
            case Success(value) => complete(value)
            case Failure(ex) => failWith(ex)
          }
        }
      } ~ path("version") {
        get(complete(Json.obj("version" -> Constants.AgentName).toString()))
      }
    }
}