package lidraughts.user

case class ProfileWFD(
    firstName: Option[String] = None,
    lastName: Option[String] = None
) {

  def nonEmptyRealName = List(ne(firstName), ne(lastName)).flatten match {
    case Nil => none
    case names => (names mkString " ").some
  }

  private def ne(str: Option[String]) = str.filter(_.nonEmpty)
}

object ProfileWFD {

  val default = ProfileWFD()

  import reactivemongo.bson.Macros
  private[user] val profileWFDBSONHandler = Macros.handler[ProfileWFD]
}
