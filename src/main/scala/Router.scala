import scala.util.{Failure, Success}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}

trait Router {

  def route: Route

}

class AccountRouter(accountRepository: AccountRepository) extends Router with Directives with AccountDirectives with ValidatorDirectives {

  import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
  import io.circe.generic.auto._

  override def route: Route = pathPrefix( pm = "accounts") {
    pathEndOrSingleSlash {
      get {
        handleWithGeneric(accountRepository.all()) { accounts =>
          complete(accounts)
        }
      } ~ post {
        entity(as[CreateAccount]) { createAccount =>
          validateWith(CreateAccountValidator)(createAccount) {
            handleWithGeneric(accountRepository.create(createAccount)) { accounts =>
              complete(accounts)
            }
          }
        }
      }
    }~ path(Segment) { id: String =>
        put {
         entity(as[UpdateAccount]) { updateAccount =>
           validateWith(UpdateAccountValidator)(updateAccount) {
             handle(accountRepository.update(id, updateAccount)) {
               case AccountRepository.AccountNotFound(_) =>
                 ApiError.accountNotFound(id)
               case _ =>
                 ApiError.generic
             } { account =>
               complete(account)
             }
           }
         }
        }
    } ~ path(pm = "done") {
      get {
        handleWithGeneric(accountRepository.done()) { accounts =>
          complete(accounts)
        }
      }
    } ~ path(pm = "pending") {
      get {
        handleWithGeneric(accountRepository.pending()) { accounts =>
          complete(accounts)
        }
      }
    }
  }
}
