package controllers

import lidraughts.app._
import views._

object TournamentCrud extends LidraughtsController {

  private def env = Env.tournament
  private def crud = env.crudApi
  private def teamApi = Env.team.api

  def index(page: Int) = Secure(_.ManageTournament) { implicit ctx => me =>
    crud.paginator(page) map { paginator =>
      html.tournament.crud.index(paginator)
    }
  }

  def edit(id: String) = Secure(_.ManageTournament) { implicit ctx => me =>
    OptionOk(crud one id) { tour =>
      html.tournament.crud.edit(tour, crud editForm tour)
    }
  }

  def update(id: String) = SecureBody(_.ManageTournament) { implicit ctx => me =>
    OptionFuResult(crud one id) { tour =>
      implicit val req = ctx.body
      crud.editForm(tour).bindFromRequest.fold(
        err => BadRequest(html.tournament.crud.edit(tour, err)).fuccess,
        data => {
          teamsFromConditions(data) flatMap { teams =>
            crud.update(tour, data, teams) inject Redirect(routes.TournamentCrud.edit(id))
          }
        }
      )
    }
  }

  def form = Secure(_.ManageTournament) { implicit ctx => me =>
    Ok(html.tournament.crud.create(crud.createForm)).fuccess
  }

  def create = SecureBody(_.ManageTournament) { implicit ctx => me =>
    implicit val req = ctx.body
    crud.createForm.bindFromRequest.fold(
      err => BadRequest(html.tournament.crud.create(err)).fuccess,
      data => {
        teamsFromConditions(data) flatMap { teams =>
          crud.create(data, me, teams) map { tour =>
            Redirect {
              if (tour.isTeamBattle) routes.Tournament.teamBattleEdit(tour.id)
              else routes.TournamentCrud.edit(tour.id)
            }
          }
        }
      }
    )
  }

  def clone(id: String) = Secure(_.ManageTournament) { implicit ctx => me =>
    OptionFuResult(crud one id) { old =>
      val tour = crud clone old
      Ok(html.tournament.crud.create(crud editForm tour)).fuccess
    }
  }

  private def teamsFromConditions(data: lidraughts.tournament.crud.CrudForm.Data): Fu[List[lidraughts.hub.lightTeam.LightTeam]] =
    data.conditions.teamMember.flatMap(_.teamId).??(teamApi.light).map(team => ~team.map(List(_)))
}
