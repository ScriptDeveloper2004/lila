var m = require('mithril');
var util = require('./util');
var ceval = require('./ceval');
var status = require('game/status');

var boardContent = m('div.cg-board-wrap', m('div.cg-board'));

function miniPairing(ctrl) {
  return function(pairing) {
    var game = pairing.game;
    var player = pairing.player;
    var result = pairing.game.status >= status.ids.aborted ? (
      pairing.winnerColor === 'white' ? (ctrl.pref.draughtsResult ? '2-0' : '1-0')
      : (pairing.winnerColor === 'black' ? (ctrl.pref.draughtsResult ? '0-2' : '0-1')
      : (ctrl.pref.draughtsResult ? '1-1' : '½-½'))
    ) : '*';
    return m('a', {
      class: ctrl.evals !== undefined ? 'gauge_displayed' : '',
      href: '/' + game.id + '/' + game.orient
    }, [
      m('span', {
        class: 'mini-board live-' + game.id + ' parse-fen is2d',
        'data-color': game.orient,
        'data-fen': game.fen,
        'data-lastmove': game.lastMove,
        config: function(el, isUpdate) {
          if (!isUpdate) lidraughts.parseFen($(el));
        }
      }, boardContent),
      m('span', {
        class: 'vstext' + (ctrl.data.host.gameId === game.id ? ' host' : '')
      }, [
        m('span.vstext__pl', [
          util.playerVariant(ctrl, player).name,
          m('br'),
          result
        ]),
        m('div.vstext__op', [
          player.username,
          m('br'),
          player.title ? player.title + ' ' : '',
          player.officialRating ? ('FMJD ' + player.officialRating) : player.rating
        ])
      ]),
      ctrl.evals !== undefined ? ceval.renderGauge(pairing, ctrl.evals) : null
    ]);
  };
}

module.exports = function(ctrl) {
  return m('div.game-list.now-playing.box__pad', ctrl.data.pairings.map(miniPairing(ctrl)));
};
