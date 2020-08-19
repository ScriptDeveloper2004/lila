import { h } from 'snabbdom';
import LobbyController from '../ctrl';

function timer(pov) {
  const date = Date.now() + pov.secondsLeft * 1000;
  return h('time.timeago', {
    hook: {
      insert(vnode) {
        (vnode.elm as HTMLElement).setAttribute('datetime', '' + date);
      }
    }
  }, window.lidraughts.timeago.format(date));
}

export default function(ctrl: LobbyController) {
  return h('div.now-playing',
    ctrl.data.nowPlaying.map(pov =>
      h('a.' + pov.variant.key, {
        key: `${pov.gameId}${pov.lastMove}`,
        attrs: { href: '/' + pov.fullId }
      }, [
        h('span.mini-board.cg-wrap.is2d.is' + pov.variant.board.key, {
          attrs: {
            'data-state': `${pov.fen}|${pov.variant.board.size[0]}x${pov.variant.board.size[1]}|${pov.color}|${pov.lastMove}`
          },
          hook: {
            insert(vnode) {
              window.lidraughts.miniBoard.init(vnode.elm as HTMLElement);
            }
          }
        }),
        h('span.meta', [
          pov.opponent.ai ? ctrl.trans('aiNameLevelAiLevel', 'Scan', pov.opponent.ai) : pov.opponent.username,
          h('span.indicator',
            pov.isMyTurn ?
            (pov.secondsLeft ? timer(pov) : [ctrl.trans.noarg('yourTurn')]) :
            h('span', '\xa0')) // &nbsp;
        ])
      ])
    ));
}
