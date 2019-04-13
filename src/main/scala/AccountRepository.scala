import java.util.UUID

import AccountRepository.AccountNotFound

import scala.concurrent.{ExecutionContext, Future}

trait AccountRepository {

  def all(): Future[Seq[Account]]
  def done(): Future[Seq[Account]]
  def pending(): Future[Seq[Account]]
  def create(createAccount: CreateAccount): Future[Account]
  def update(id: String, updateAccount: UpdateAccount): Future[Account]
}

object AccountRepository{
  final case class AccountNotFound(id: String) extends Exception(s"Account with id $id not found")
}


class InMemoryAccountRepository(initialAccounts: Seq[Account] = Seq.empty)(implicit ec: ExecutionContext) extends AccountRepository {

  private var accounts: Vector[Account] = initialAccounts.toVector

  override def all(): Future[Seq[Account]] = Future.successful(accounts)

  override def done(): Future[Seq[Account]] = Future.successful(accounts.filter(_.done))

  override def pending(): Future[Seq[Account]] = Future.successful(accounts.filterNot(_.done))

  override def create(createAccount: CreateAccount): Future[Account] = Future.successful {
    val account = Account(
      UUID.randomUUID().toString,
      createAccount.title,
      createAccount.description,
      false
    )
    accounts = accounts :+ account
    account
  }

  override def update(id: String, updateAccount: UpdateAccount): Future[Account] = {
    accounts.find(_.id == id) match {
      case Some (foundAccount) =>
        val newAccount = updateHelper(foundAccount, updateAccount)
        accounts = accounts.map(t => if (t.id == id) newAccount else t)
        Future.successful(newAccount)
      case None =>
        Future.failed(AccountNotFound(id))
    }
  }

  private def updateHelper(account: Account, updateAccount: UpdateAccount): Account ={
    val t1 = updateAccount.title.map(title => account.copy(title = title)).getOrElse(account)
    val t2 = updateAccount.description.map(description => t1.copy(description = description)).getOrElse(t1)
    updateAccount.done.map(done => t2.copy(done = done)).getOrElse(t2)
  }

}
