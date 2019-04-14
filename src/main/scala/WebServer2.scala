import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import spray.json.DefaultJsonProtocol._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn
import java.util.{Calendar, Date}
import scala.math.Ordering.Implicits._
import java.time.LocalDate
implicit def localDateOrderer: Ordering[LocalDate] = Ordering.by(d => d.toEpochDay)


object WebServer2 {

  case class Transaction(userId: String, offer: Int, typ:String, date: LocalDate)
  case object GetTransactions
  case class GetBalance(userId:String)
  case class GetBankStatement(userId:String, date1:String, date2:String)
  case class Transactions(transactions: List[Transaction])

  class Banks extends Actor with ActorLogging {
    var transactions = List.empty[Transaction]
    def receive = {
      case transaction @ Transaction(userId, offer, typ, date) =>
        transactions = transactions :+ transaction
        log.info(s"Transaction complete: $userId, $offer, $typ, $date")
      case GetTransactions => sender() ! Transactions(transactions)
      case GetBalance(userId) => sender() ! transactions.filter(o => o.userId == userId).map(x => x.offer).sum.toString
      case GetBankStatement(userId, date1, date2) => sender() ! transactions.filter(o => o.userId == userId).filter(x => x.date>LocalDate.parse(date1)).filter(x => x.date < LocalDate.parse(date2))
      case _ => log.info("Invalid message")
    }
  }

  // these are from spray-json
  implicit val bidFormat = jsonFormat4(Transaction)
  implicit val bidsFormat = jsonFormat1(Transactions)

  def main(args: Array[String]) {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val bank = system.actorOf(Props[Banks], "bank")

    val route =
      path("bank") {
        put {
          parameter("bid".as[Int], "user", "typ") { (bid, user, typ) =>
            // place a bid, fire-and-forget
            val now = Calendar.getInstance()
            bank ! Transaction(user, bid, typ, LocalDate.now())
            complete((StatusCodes.Accepted, "bid placed"))
          }
        } ~
          get {
            implicit val timeout: Timeout = 5.seconds
            // query the actor for the current auction state
            val transactions: Future[Transactions] = (bank ? GetTransactions).mapTo[Transactions]
            complete(transactions)
          } ~
          get {
            parameter("user".as[String]) { (user) =>
              // get the balance of the queried user or account
              implicit val timeout: Timeout = 5.seconds
              // query the actor for the current auction state
              val transactions: Future[String] = (bank ? GetBalance(user)).mapTo[String]
              complete(transactions)
            }
          }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done

  }
}