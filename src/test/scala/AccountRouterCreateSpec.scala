import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class AccountRouterCreateSpec extends WordSpec with Matchers with ScalatestRouteTest with AccountMocks {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  val testCreateAccount = CreateAccount(
    "Test account",
    "Test description",
    false
  )

  "A AccountRouter" should {
    "create a account with valid data" in {
      val repository = new InMemoryAccountRepository()
      val router = new AccountRouter(repository)
      Post("/accounts", testCreateAccount) ~> router.route ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[Account]
        resp.title shouldBe testCreateAccount.title
        resp.description shouldBe testCreateAccount.description
      }
    }


      "not create a account with invalid data" in {
        val repository = new FailingRepository
        val router = new AccountRouter(repository)
        Post("/accounts", testCreateAccount.copy(title = "")) ~> router.route ~> check {
          status shouldBe ApiError.emptyTitleField.statusCode
          val resp = responseAs[String]
          resp shouldBe ApiError.emptyTitleField.message
        }
      }

    "handle repository failure when creating accounts" in {
      val repository = new FailingRepository
      val router = new AccountRouter(repository)
      Post("/accounts", testCreateAccount) ~> router.route ~> check {
        status shouldBe ApiError.generic.statusCode
        val resp = responseAs[String]
        resp shouldBe ApiError.generic.message
      }
    }

  }


}
