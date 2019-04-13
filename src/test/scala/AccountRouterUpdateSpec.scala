import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}

class AccountRouterUpdateSpec extends WordSpec with Matchers with ScalatestRouteTest with AccountMocks {
  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  val accountId = UUID.randomUUID().toString
  val testAccount = Account(
    accountId,
    "Original title",
    "Original description",
    done = false
  )

  val testUpdateAccount = UpdateAccount(
    Some("Test account "),
    None,
    Some(true)
  )

  "A AccountRouter" should {

    "update a account with valid data" in {
      val repository = new InMemoryAccountRepository(Seq(testAccount))
      val router = new AccountRouter(repository)

      Put(s"/accounts/$accountId", testUpdateAccount) ~> router.route ~> check {
        status shouldBe StatusCodes.OK
        val resp = responseAs[Account]
        resp.title shouldBe testUpdateAccount.title.get
        resp.description shouldBe testAccount.description
        resp.done shouldBe testUpdateAccount.done.get
      }
    }

    "return not found with non existing account" in {
      val repository = new InMemoryAccountRepository(Seq(testAccount))
      val router = new AccountRouter(repository)

      Put(s"/accounts/1", testUpdateAccount) ~> router.route ~> check {
        status shouldBe ApiError.accountNotFound("1").statusCode
        val resp = responseAs[String]
        resp shouldBe ApiError.accountNotFound(id = "1").message
      }
    }

    "not update a account with valid data" in {
      val repository = new InMemoryAccountRepository(Seq(testAccount))
      val router = new AccountRouter(repository)

      Put(s"/accounts/$accountId", testUpdateAccount.copy(title = Some(""))) ~> router.route ~> check {
        status shouldBe ApiError.emptyTitleField.statusCode
        val resp = responseAs[String]
        resp shouldBe ApiError.emptyTitleField.message
      }
    }


    "handle repository failures when updating accounts" in {
      val repository = new FailingRepository
      val router = new AccountRouter(repository)

      Put(s"/accounts/$accountId", testUpdateAccount) ~> router.route ~> check {
        status shouldBe ApiError.generic.statusCode
        val resp = responseAs[String]
        resp shouldBe ApiError.generic.message
      }
    }

  }


}
