import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class AccountRouterListSpec extends WordSpec with Matchers with ScalatestRouteTest with AccountMocks {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  private val doneAccount =
    Account("1", "Buy Milk", "The cat is thirsty", done=true)

  private val pendingAccount =
    Account("2", "Buy Eggs", "Ran out of eggs", done=false)

  private val accounts = Seq(doneAccount, pendingAccount)

  "AccountRouter" should {

    "return all the accounts" in {
      val repository = new InMemoryAccountRepository(accounts)
      val router = new AccountRouter(repository)

      Get("/accounts") ~> router.route ~> check {
        status shouldBe StatusCodes.OK
        val response = responseAs[Seq[Account]]
        response shouldBe accounts
      }
    }

    "return all the done accounts" in {
      val repository = new InMemoryAccountRepository(accounts)
      val router = new AccountRouter(repository)

      Get("/accounts/done") ~> router.route ~> check {
        status shouldBe StatusCodes.OK
        val response = responseAs[Seq[Account]]
        response shouldBe Seq(doneAccount)
      }
    }


    "return all the pending accounts" in {
      val repository = new InMemoryAccountRepository(accounts)
      val router = new AccountRouter(repository)

      Get("/accounts/pending") ~> router.route ~> check {
        status shouldBe StatusCodes.OK
        val response = responseAs[Seq[Account]]
        response shouldBe Seq(pendingAccount)
      }
    }

    "handle the repository failure in the accounts route" in {
      val repository = new FailingRepository
      val router = new AccountRouter(repository)

      Get("/accounts") ~> router.route ~> check {
        status shouldBe ApiError.generic.statusCode
        val resp = responseAs[String]
        resp shouldBe ApiError.generic.message
      }
    }

    "handle the repository failure in the pending accounts route" in {
      val repository = new FailingRepository
      val router = new AccountRouter(repository)

      Get("/accounts/pending") ~> router.route ~> check {
        status shouldBe ApiError.generic.statusCode
        val resp = responseAs[String]
        resp shouldBe ApiError.generic.message
      }
    }

    "handle the repository failure in the done accounts route" in {
      val repository = new FailingRepository
      val router = new AccountRouter(repository)

      Get("/accounts/done") ~> router.route ~> check {
        status shouldBe ApiError.generic.statusCode
        val resp = responseAs[String]
        resp shouldBe ApiError.generic.message
      }
    }


  }

}
