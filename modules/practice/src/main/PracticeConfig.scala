package lidraughts.practice

import lidraughts.study.Study

case class PracticeConfig(
    sections: List[PracticeConfigSection]
) {

  def studyIds = sections.flatMap(_.studies.map(_.id)) map Study.Id.apply

  def sectionTrans(id: String, lang: String) = sections.find(s => s.id == id && s.lang.contains(lang))
}

object PracticeConfig {
  val empty = PracticeConfig(Nil)
}

case class PracticeConfigSection(
    id: String,
    lang: Option[String],
    name: String,
    studies: List[PracticeConfigStudy]
)

case class PracticeConfigStudy(
    id: String, // study ID
    name: String,
    desc: String
)
