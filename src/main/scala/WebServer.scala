import java.text.SimpleDateFormat
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

object WebServer {

  case class Transaction(userId: String, offer: Int, typ:String, date:String)
  case object GetTransactions
  case class GetBalance(userId:String)
  case class GetPrincipal(userId:String)
  case class GetBankStatement(userId:String, date1:String, date2:String)
  case class Transactions(transactions: List[Transaction])
  case class Principal(userId: String, principal:String, date: String)
  case class Balance(userId:String, amout:String)
  case class Principals(principals: List[Principal])

  class Banks extends Actor with ActorLogging {
    var transactions = List.empty[Transaction]
    var outstanding_debt = List.empty[Principal]
    def receive = {
      case transaction @ Transaction(userId, offer, typ, date) =>
        val temp_tran = transactions.filter(o => o.userId == userId).map(x => x.offer).sum.toString
        val now = Calendar.getInstance()
        if(temp_tran.toInt < 0) {
          outstanding_debt = outstanding_debt :+ Principal(userId, temp_tran, now.toString)
        }
        println(temp_tran.toString+"balance"+outstanding_debt.map(o => o.principal.toInt).sum)
        transactions = transactions :+ transaction
        log.info(s"Transaction complete: $userId, $offer, $typ, $date")
      case GetTransactions => sender() ! Transactions(transactions)
      case GetBalance(userId) =>
        sender() ! Balance(userId, transactions.filter(o => o.userId == userId).map(x => x.offer).sum.toString)
      case GetBankStatement(userId, date1, date2) => sender() ! Transactions(transactions.filter(o => o.userId == userId).
        filter(x => format.parse(x.date).after(format.parse(date1))).
        filter(x => format.parse(x.date).before(format.parse(date2))))
      case GetPrincipal(userId) => sender() ! Principals(outstanding_debt.filter(x => x.userId==userId))
      case _ => log.info("Invalid message")
    }
  }

  // these are from spray-json
  val format = new SimpleDateFormat("yyyy-MM-dd")

  implicit val principalFormat = jsonFormat3(Principal)
  implicit val bidFormat = jsonFormat4(Transaction)
  implicit val bidsFormat = jsonFormat1(Transactions)
  implicit val balanceFormat = jsonFormat2(Balance)
  implicit val principalsFormat = jsonFormat1(Principals)

  def main(args: Array[String]) {
    implicit val system = ActorSystem()
    implicit val materializer = ActorMaterializer()
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.dispatcher

    val bank = system.actorOf(Props[Banks], "bank")

    val route =
      pathSingleSlash {
        put {
          parameter("bid".as[Int], "user", "typ") { (bid, user, typ) =>
            // place a bid, fire-and-forget
            val now = Calendar.getInstance()
            val date = now.getTime
            val date1 = format.format(date)
            bank ! Transaction(user, bid, typ, date1)
            complete((StatusCodes.Accepted, "bid placed"))
          }
        } ~
          get {
            implicit val timeout: Timeout = 5.seconds
            val transactions1: Future[Transactions] = (bank ? GetTransactions).mapTo[Transactions]
            complete(transactions1)
          }
      } ~path("balance"){
        get {
          parameter("user") { userId =>
            implicit val timeout: Timeout = 5.seconds
            // query the actor for the current auction state
            val balance: Future[Balance] = (bank ? GetBalance(userId)).mapTo[Balance]
            complete(balance)
            }
          }
        }~path("outstandingPrincipal"){
        get {
          parameter("user") { userId =>
            implicit val timeout: Timeout = 5.seconds
            // query the actor for the current auction state
            val outstanding: Future[Principals] = (bank ? GetPrincipal(userId)).mapTo[Principals]
            complete(outstanding)
          }
        }
      }~path("BankStatements"){
        get {
          parameter("user", "date1", "date2") { (userId, date1, date2) =>
            implicit val timeout: Timeout = 5.seconds
            // query the actor for the current auction state
            val statement: Future[Transactions] = (bank ? GetBankStatement(userId, date1, date2)).mapTo[Transactions]
            complete(statement)
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