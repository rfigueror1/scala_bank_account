import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

import scala.concurrent.Await
import scala.util.{Failure, Success}

object Main extends App {

  val host = "0.0.0.0"
  val port = 9000

  implicit val system: ActorSystem = ActorSystem(name = "accountapi")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  import system.dispatcher

  val accountRepository = new InMemoryAccountRepository(Seq(
    Account("1", "Buy Eggs", "Ran out of eggs, buy a dozen", false),
    Account("2", "Buy Milk", "That cat is thirsty", true)
  ))
  val router = new AccountRouter(accountRepository)
  val server = new Server(router, host, port)

  val binding = server.bind()
  binding.onComplete {
    case Success(_) => println("Success!")
    case Failure(error) => println(s"Failed: ${error.getMessage}")
  }

  import scala.concurrent.duration._
  Await.result(binding, 3.seconds)
}
