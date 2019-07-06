var m = require('mithril');
var Draughtsground = require('draughtsground').Draughtsground;
var util = require('draughtsground/util');

module.exports = function(ctrl) {
  return m('div.cg-board-wrap', {
    config: function(el, isUpdate) {
      if (isUpdate) return;
      ctrl.draughtsground = Draughtsground(el, makeConfig(ctrl));
      bindEvents(el, ctrl);
    }
  });
}

function bindEvents(el, ctrl) {
  var handler = onMouseEvent(ctrl);
  ['touchstart', 'touchmove', 'mousedown', 'mousemove', 'contextmenu'].forEach(function(ev) {
    el.addEventListener(ev, handler)
  });
}

function isLeftButton(e) {
  return e.buttons === 1 || e.button === 1;
}

function isLeftClick(e) {
  return isLeftButton(e) && !e.ctrlKey;
}

function isRightClick(e) {
  return util.isRightButton(e) || (e.ctrlKey && isLeftButton(e));
}

var downKey;
var lastKey;
var placeDelete;

function onMouseEvent(ctrl) {
  return function(e) {
    var sel = ctrl.vm.selected();

    if (isLeftClick(e) || e.type === 'touchstart' || e.type === 'touchmove') {
      if (
        sel === 'pointer' ||
          (
            ctrl.draughtsground &&
              ctrl.draughtsground.state.draggable.current &&
              ctrl.draughtsground.state.draggable.current.newPiece
          )
      ) return;
      var key = ctrl.draughtsground.getKeyAtDomPos(util.eventPosition(e));
      if (!key) return;
      if (e.type === 'mousedown' || e.type === 'touchstart') {
        downKey = key;
      }
      if (sel === 'trash') {
        deleteOrHidePiece(ctrl, key, e);
      } else {
        var existingPiece = ctrl.draughtsground.state.pieces[key];
        var piece = {};
        piece.color = sel[0];
        piece.role = sel[1];

        if (
          (e.type === 'mousedown' || e.type === 'touchstart') &&
            existingPiece &&
            piece.color === existingPiece.color &&
            piece.role === existingPiece.role
        ) {
          deleteOrHidePiece(ctrl, key, e);

          placeDelete = true;

          var endEvents = {mousedown: 'mouseup', touchstart: 'touchend'};

          document.addEventListener(endEvents[e.type], function() {
            placeDelete = false;
          }, {once: true});
        } else if (
          !placeDelete &&
            (e.type === 'mousedown' || e.type === 'touchstart' || key !== lastKey)
        ) {
          var pieces = {};
          pieces[key] = piece;
          ctrl.draughtsground.setPieces(pieces);
          ctrl.onChange();
          ctrl.draughtsground.cancelMove();
        }
      }
      lastKey = key;
    } else if (isRightClick(e)) {
      if (sel !== 'pointer') {
        ctrl.draughtsground.state.drawable.current = undefined;
        ctrl.draughtsground.state.drawable.shapes = [];

        if (
          e.type === 'contextmenu' &&
            ['pointer', 'trash'].indexOf(sel) === -1 && sel.length >= 2
        ) {
          ctrl.draughtsground.cancelMove();
          sel[0] = util.opposite(sel[0]);
          m.redraw();
        }
      }
    }
  };
}

function deleteOrHidePiece(ctrl, key, e) {
  if (e.type === 'touchstart') {
    if (ctrl.draughtsground.state.pieces[key]) {
      ctrl.draughtsground.cancelMove();
      e.srcElement.style.display = 'none';
    }

    document.addEventListener('touchend', function() {
      deletePiece(ctrl, key);
    }, {once: true});
  } else if (e.type === 'mousedown' || key !== downKey) {
    deletePiece(ctrl, key);
  }
}

function deletePiece(ctrl, key) {
  var pieces = {};
  pieces[key] = false;
  ctrl.draughtsground.setPieces(pieces);
  ctrl.onChange();
}

function makeConfig(ctrl) {
  return {
    fen: ctrl.cfg.fen,
    orientation: ctrl.options.orientation || 'white',
    coordinates: ctrl.embed ? 0 : ctrl.cfg.coords,
    addPieceZIndex: ctrl.cfg.is3d,
    movable: {
      free: true,
      color: 'both',
      dropOff: 'trash'
    },
    animation: {
      duration: ctrl.cfg.animation.duration
    },
    premovable: {
      enabled: false
    },
    drawable: {
      enabled: true
    },
    draggable: {
      showGhost: true,
      distance: 0,
      autoDistance: false,
      deleteOnDropOff: true
    },
    selectable: {
      enabled: false
    },
    highlight: {
      lastMove: false
    },
    events: {
      change: ctrl.onChange.bind(ctrl)
    },
    disableContextMenu: true
  };
}
