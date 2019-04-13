import scala.concurrent.Future

trait AccountMocks {
  class FailingRepository extends AccountRepository{

    override def all(): Future[Seq[Account]] = Future.failed(new Exception("Mocked exception"))

    override def done(): Future[Seq[Account]] = Future.failed(new Exception("Mocked exception"))

    override def pending(): Future[Seq[Account]] = Future.failed(new Exception("Mocked exception"))

    override def create(createAccount: CreateAccount): Future[Account] = Future.failed(new Exception("Mocked exception"))

    override def update(id: String, updateAccount: UpdateAccount): Future[Account] = Future.failed(new Exception("Mocked Exception"))
  }
}
