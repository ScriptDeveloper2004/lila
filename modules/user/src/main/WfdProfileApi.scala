package lidraughts.user

import reactivemongo.bson._
import scala.concurrent.duration._

import lidraughts.db.dsl._
import lidraughts.memo.Syncache
import User.{ BSONFields => F }

final class WfdProfileApi(coll: Coll)(implicit system: akka.actor.ActorSystem) {

  import WfdProfileApi._

  def sync(id: User.ID): Option[ProfileWFD] = cache sync id
  def async(id: User.ID): Fu[Option[ProfileWFD]] = cache async id

  def asyncMany = cache.asyncMany _

  def invalidate = cache invalidate _

  def preloadOne = cache preloadOne _
  def preloadMany = cache preloadMany _
  def preloadUser(user: User) = cache.setOneIfAbsent(user.id, user.profileWFD)

  private val cacheName = "user.wfd"

  private val cache = new Syncache[User.ID, Option[ProfileWFD]](
    name = cacheName,
    compute = id => coll.find($id(id), projection).uno[ProfileWFD],
    default = _ => ProfileWFD.default.some,
    strategy = Syncache.WaitAfterUptime(10 millis),
    expireAfter = Syncache.ExpireAfterAccess(15 minutes),
    logger = logger branch "WfdProfileApi"
  )

}

private object WfdProfileApi {

  implicit val wfdProfileBSONReader = new BSONDocumentReader[ProfileWFD] {

    private implicit def profileWFDHandler = ProfileWFD.profileWFDBSONHandler

    def read(doc: BSONDocument) =
      doc.getAs[ProfileWFD](F.profileWFD) getOrElse ProfileWFD.default

  }

  val projection = $doc(F.profileWFD -> true)
}