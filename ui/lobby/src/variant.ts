const variantConfirms = {
  'frisian': "This is a Frisian Draughts game!\n\nPieces can also be captured horizontally and vertically.",
};

function storageKey(key) {
  return 'lobby.variant.' + key;
}

export default function(variant: string) {
  return Object.keys(variantConfirms).every(function(key) {
    const v = variantConfirms[key]
      if (variant === key && !window.lidraughts.storage.get(storageKey(key))) {
        const c = confirm(v);
        if (c) window.lidraughts.storage.set(storageKey(key), '1');
        return c;
      } else return true;
  })
}
