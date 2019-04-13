trait Validator[T]{
 def validate(t: T): Option[ApiError]
}

object CreateAccountValidator extends Validator[CreateAccount] {
  def validate(createAccount: CreateAccount): Option[ApiError] = {
    if(createAccount.title.isEmpty)
      Some(ApiError.emptyTitleField)
    else
      None
  }
}

object UpdateAccountValidator extends Validator[UpdateAccount]{
  override def validate(updateAccount: UpdateAccount): Option[ApiError] =
    if(updateAccount.title.exists(_.isEmpty))
      Some(ApiError.emptyTitleField)
    else
      None
}
