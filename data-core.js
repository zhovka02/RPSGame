// Global data container. data-1.js … data-4.js append places via DATA.places.push(...).
// No start address or private coordinates live here; the start point is set on-device only.
window.DATA = {
  mapCenter: { lat: 52.52, lon: 13.405 },
  places: []
};
