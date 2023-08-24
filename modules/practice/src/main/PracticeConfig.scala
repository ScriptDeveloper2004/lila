package lidraughts.practice

import lidraughts.study.Study

case class PracticeConfig(
    sections: List[PracticeConfigSection]
) {

  def studyIds = sections.flatMap(_.studies.map(_.id)) map Study.Id.apply

  def translatedSection(id: String, lang: String) = sections.find(s => s.id == id && s.lang.contains(lang))
}

object PracticeConfig {

  val empty = PracticeConfig(Nil)

  def validate(config: PracticeConfig) =
    config.sections.filter(_.lang.isDefined) flatMap { secTrans =>
      config.sections.find(s => s.id == secTrans.id && s.lang.isEmpty) match {
        case Some(secBase) =>
          if (config.sections.indexOf(secTrans) < config.sections.indexOf(secBase))
            Some(s"Expected untranslated section ${secBase.id} before translation ${secTrans.lang.get}")
          else if (secBase.studies.size != secTrans.studies.size)
            Some(s"Expected ${secBase.studies.size} studies in ${secTrans.id} ${secTrans.lang.get}")
          else None
        case _ => Some(s"No untranslated section for ${secTrans.id} ${secTrans.lang.get}")
      }
    }
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
