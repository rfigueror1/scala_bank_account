case class Account(id: String, title: String, description: String, done: Boolean)
case class CreateAccount(title: String, description: String, done: Boolean)
case class UpdateAccount(title: Option[String], description: Option[String], done: Option[Boolean])