package lidraughts.tree

import play.api.libs.json._

import draughts.format.pdn.{ Glyph, Glyphs }
import draughts.format.{ Uci, UciCharPair }
import draughts.opening.FullOpening
import draughts.Pos

import draughts.Centis

sealed trait Node {
  def ply: Int
  def fen: String
  // None when not computed yet
  def dests: Option[Map[Pos, List[Pos]]]
  def captureLength: Option[Int]
  def drops: Option[List[Pos]]
  def eval: Option[Eval]
  def shapes: Node.Shapes
  def comments: Node.Comments
  def gamebook: Option[Node.Gamebook]
  def glyphs: Glyphs
  def children: List[Branch]
  def opening: Option[FullOpening]
  def comp: Boolean // generated by a computer analysis
  def addChild(branch: Branch): Node
  def dropFirstChild: Node
  def clock: Option[Centis]

  // implementation dependent
  def idOption: Option[UciCharPair]
  def moveOption: Option[Uci.WithSan]

  // who's color plays next
  def color = draughts.Color(ply % 2 == 0)

  def mainlineNodeList: List[Node] =
    dropFirstChild :: children.headOption.fold(List.empty[Node])(_.mainlineNodeList)
}

case class Root(
    ply: Int,
    fen: String,
    // None when not computed yet
    dests: Option[Map[Pos, List[Pos]]] = None,
    captureLength: Option[Int] = None,
    drops: Option[List[Pos]] = None,
    eval: Option[Eval] = None,
    shapes: Node.Shapes = Node.Shapes(Nil),
    comments: Node.Comments = Node.Comments(Nil),
    gamebook: Option[Node.Gamebook] = None,
    glyphs: Glyphs = Glyphs.empty,
    children: List[Branch] = Nil,
    opening: Option[FullOpening] = None,
    clock: Option[Centis] = None // clock state at game start, assumed same for both players
) extends Node {

  def idOption = None
  def moveOption = None
  def comp = false

  def addChild(branch: Branch) = copy(children = children :+ branch)
  def prependChild(branch: Branch) = copy(children = branch :: children)
  def dropFirstChild = copy(children = if (children.isEmpty) children else children.tail)
}

case class Branch(
    id: UciCharPair,
    ply: Int,
    move: Uci.WithSan,
    fen: String,
    // None when not computed yet
    dests: Option[Map[Pos, List[Pos]]] = None,
    captureLength: Option[Int] = None,
    drops: Option[List[Pos]] = None,
    eval: Option[Eval] = None,
    shapes: Node.Shapes = Node.Shapes(Nil),
    comments: Node.Comments = Node.Comments(Nil),
    gamebook: Option[Node.Gamebook] = None,
    glyphs: Glyphs = Glyphs.empty,
    children: List[Branch] = Nil,
    opening: Option[FullOpening] = None,
    comp: Boolean = false,
    clock: Option[Centis] = None // clock state after the move is played, and the increment applied
) extends Node {

  def idOption = Some(id)
  def moveOption = Some(move)

  def addChild(branch: Branch) = copy(children = children :+ branch)
  def prependChild(branch: Branch) = copy(children = branch :: children)
  def dropFirstChild = copy(children = if (children.isEmpty) children else children.tail)

  def setComp = copy(comp = true)
}

object Node {

  sealed trait Shape
  object Shape {
    type ID = String
    type Brush = String
    case class Circle(brush: Brush, orig: Pos) extends Shape
    case class Arrow(brush: Brush, orig: Pos, dest: Pos) extends Shape
  }
  case class Shapes(value: List[Shape]) extends AnyVal {
    def list = value
    def ++(shapes: Shapes) = Shapes {
      (value ::: shapes.value).distinct
    }
  }
  object Shapes {
    val empty = Shapes(Nil)
  }

  case class Comment(id: Comment.Id, text: Comment.Text, by: Comment.Author) {
    def removeMeta = text.removeMeta map { t =>
      copy(text = t)
    }
  }
  object Comment {
    case class Id(value: String) extends AnyVal
    object Id {
      def make = Id(scala.util.Random.alphanumeric take 4 mkString)
    }
    private val metaReg = """\[%[^\]]+\]""".r
    case class Text(value: String) extends AnyVal {
      def removeMeta: Option[Text] = {
        val v = metaReg.replaceAllIn(value, "").trim
        if (v.nonEmpty) Some(Text(v)) else None
      }
    }
    sealed trait Author
    object Author {
      case class User(id: String, titleName: String) extends Author
      case class External(name: String) extends Author
      case object Lidraughts extends Author
      case object Unknown extends Author
    }
    def sanitize(text: String) = Text {
      text.trim.take(4000)
        .replaceAll("""\r\n""", "\n") // these 3 lines dedup white spaces and new lines
        .replaceAll("""(?m)(^ *| +(?= |$))""", "")
        .replaceAll("""(?m)^$([\n]+?)(^$[\n]+?^)+""", "$1")
        .replaceAll("\\{|\\}", "") // {} are reserved in PDN comments
    }
  }
  case class Comments(value: List[Comment]) extends AnyVal {
    def list = value
    def findBy(author: Comment.Author) = list.find(_.by == author)
    def set(comment: Comment) = Comments {
      if (list.exists(_.by == comment.by)) list.map {
        case c if c.by == comment.by => c.copy(text = comment.text)
        case c => c
      }
      else list :+ comment
    }
    def delete(commentId: Comment.Id) = Comments {
      value.filterNot(_.id == commentId)
    }
    def +(comment: Comment) = Comments(comment :: value)
    def ++(comments: Comments) = Comments(value ::: comments.value)

    def filterEmpty = Comments(value.filter(_.text.value.nonEmpty))
  }
  object Comments {
    val empty = Comments(Nil)
  }

  case class Gamebook(deviation: Option[String], hint: Option[String]) {
    private def trimOrNone(txt: Option[String]) = txt.map(_.trim).filter(_.nonEmpty)
    def cleanUp = copy(
      deviation = trimOrNone(deviation),
      hint = trimOrNone(hint)
    )
    def nonEmpty = deviation.nonEmpty || hint.nonEmpty
  }

  implicit val openingWriter: OWrites[draughts.opening.FullOpening] = OWrites { o =>
    Json.obj(
      "eco" -> o.eco,
      "name" -> o.name
    )
  }

  private implicit val posWrites: Writes[Pos] = Writes[Pos] { p =>
    JsString(p.key)
  }
  private implicit val shapeCircleWrites = Json.writes[Shape.Circle]
  private implicit val shapeArrowWrites = Json.writes[Shape.Arrow]
  implicit val shapeWrites: Writes[Shape] = Writes[Shape] {
    case s: Shape.Circle => shapeCircleWrites writes s
    case s: Shape.Arrow => shapeArrowWrites writes s
  }
  implicit val shapesWrites: Writes[Node.Shapes] = Writes[Node.Shapes] { s =>
    JsArray(s.list.map(shapeWrites.writes))
  }
  implicit val glyphWriter: Writes[Glyph] = Json.writes[Glyph]
  implicit val glyphsWriter: Writes[Glyphs] = Writes[Glyphs] { gs =>
    Json.toJson(gs.toList)
  }

  implicit val clockWrites: Writes[Centis] = Writes { clock =>
    JsNumber(clock.centis)
  }
  implicit val commentIdWrites: Writes[Comment.Id] = Writes { id =>
    JsString(id.value)
  }
  implicit val commentTextWrites: Writes[Comment.Text] = Writes { text =>
    JsString(text.value)
  }
  implicit val commentAuthorWrites: Writes[Comment.Author] = Writes[Comment.Author] {
    case Comment.Author.User(id, name) => Json.obj("id" -> id, "name" -> name)
    case Comment.Author.External(name) => JsString(s"${name.trim}")
    case Comment.Author.Lidraughts => JsString("lidraughts")
    case Comment.Author.Unknown => JsNull
  }
  implicit val commentWriter = Json.writes[Node.Comment]
  private implicit val commentsWriter: Writes[Node.Comments] = Writes[Node.Comments] { s =>
    JsArray(s.list.map(commentWriter.writes))
  }
  implicit val gamebookWriter = Json.writes[Node.Gamebook]
  import Eval.JsonHandlers.evalWrites

  @inline implicit def toPimpedJsObject(jo: JsObject) = new lidraughts.base.PimpedJsObject(jo)

  def makeNodeJsonWriter(alwaysChildren: Boolean): Writes[Node] = Writes { node =>
    import node._
    try {
      val comments = node.comments.list.flatMap(_.removeMeta)
      Json.obj(
        "ply" -> ply,
        "fen" -> fen
      ).add("id", idOption.map(_.toString))
        .add("uci", moveOption.map(_.uci.uci))
        .add("san", moveOption.map(_.san))
        .add("eval", eval.filterNot(_.isEmpty))
        .add("comments", if (comments.nonEmpty) Some(comments) else None)
        .add("gamebook", gamebook)
        .add("glyphs", glyphs.nonEmpty)
        .add("shapes", if (shapes.list.nonEmpty) Some(shapes.list) else None)
        .add("opening", opening)
        .add("dests", dests.map(dst => {
          var destStr = dst.map {
            case (orig, dests) => s"${orig.piotr}${dests.map(_.piotr).mkString}"
          }.mkString(" ")
          captureLength match {
            case Some(capts) => "#" + capts.toString + " " + destStr
            case _ => destStr
          }
        }))
        .add("drops", drops.map { drops =>
          JsString(drops.map(_.key).mkString)
        })
        .add("clock", clock)
        .add("comp", comp)
        .add("children", if (alwaysChildren || children.nonEmpty) Some(children) else None)
    } catch {
      case e: StackOverflowError =>
        e.printStackTrace
        sys error s"### StackOverflowError ### in tree.makeNodeJsonWriter($alwaysChildren)"
    }
  }

  implicit val defaultNodeJsonWriter: Writes[Node] =
    makeNodeJsonWriter(alwaysChildren = true)

  val minimalNodeJsonWriter: Writes[Node] =
    makeNodeJsonWriter(alwaysChildren = false)

  val partitionTreeJsonWriter: Writes[Node] = Writes { node =>
    JsArray {
      node.mainlineNodeList.map(minimalNodeJsonWriter.writes)
    }
  }

}
