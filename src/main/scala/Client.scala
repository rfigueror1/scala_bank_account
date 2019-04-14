import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer

import scala.concurrent.Future
import scala.util.{ Failure, Success }

object Client {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val bid = "-100"
    val user = "RICK"
    val typ = "Loan"

    def make_request(user:String, bid:String, typeof:String, request1:String){
      if(request1 == "make transaction"){
        val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://localhost:8080/?bid=${bid}&user=${user}&typ=${typeof}"))
        responseFuture
          .onComplete {
            case Success(res) => println(res)
            case Failure(_) => sys.error("something wrong")
          }
      }
      if(request1 == "check balance"){
        val responseFuture1: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://localhost:8080/balance?user=${user}"))
        responseFuture1
          .onComplete {
            case Success(res) => println(res)
            case Failure(_) => sys.error("something wrong")
          }
      }

      if(request1 == "check outstanding debts"){
        val responseFuture1: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://localhost:8080/balance?user=${user}"))
        responseFuture1
          .onComplete {
            case Success(res) => println(res)
            case Failure(_) => sys.error("something wrong")
          }
      }

    }

    make_request(user, bid, "loan", "make transaction")

  }
}
