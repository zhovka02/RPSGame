/* Berlin Trip Explorer — client logic.
   Renders the map, the place list, search/filters/favorites, the "surprise"
   pick and the detail bottom sheet. Photos are pulled live from Wikimedia
   Commons and lazy-loaded. The start point is read from localStorage or the
   browser geolocation API only; it is never stored in the repository. */
(function () {
  "use strict";

  var DATA = window.DATA || { mapCenter: { lat: 52.52, lon: 13.405 }, places: [] };

  // Stable display order: nearest first. Each place gets a fixed number shared
  // by its card and its map marker.
  var places = DATA.places.slice().sort(function (a, b) { return a.driveMin - b.driveMin; });
  places.forEach(function (p, i) { p._n = i + 1; });

  var FAV_KEY = "berlin_trip_favs";
  var START_KEY = "berlin_trip_start";

  var favs = loadFavs();
  var startPoint = loadStart();       // { lat, lon, label } | null
  var activeFilter = "all";
  var query = "";
  var selectedId = null;

  var photoCache = {};                // id -> Promise<[{thumb, href, credit}]>

  var CATEGORY_EMOJI = {
    hike: "🥾", explore: "🏚️", water: "🌊", wildlife: "🦬",
    unique: "🏜️", culture: "🏛️", view: "🔭", city: "🏙️", infra: "⚙️"
  };

  // ---- small helpers -------------------------------------------------------

  function $(sel) { return document.querySelector(sel); }
  function el(id) { return document.getElementById(id); }

  function esc(s) {
    return String(s == null ? "" : s)
      .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  function stripHtml(html) {
    var d = document.createElement("div");
    d.innerHTML = html || "";
    return (d.textContent || "").trim();
  }

  function loadFavs() {
    try { return new Set(JSON.parse(localStorage.getItem(FAV_KEY) || "[]")); }
    catch (e) { return new Set(); }
  }
  function saveFavs() {
    try { localStorage.setItem(FAV_KEY, JSON.stringify(Array.from(favs))); }
    catch (e) { /* private mode: ignore */ }
  }
  function loadStart() {
    try {
      var v = JSON.parse(localStorage.getItem(START_KEY) || "null");
      return v && typeof v.lat === "number" && typeof v.lon === "number" ? v : null;
    } catch (e) { return null; }
  }
  function saveStart(v) {
    startPoint = v;
    try {
      if (v) localStorage.setItem(START_KEY, JSON.stringify(v));
      else localStorage.removeItem(START_KEY);
    } catch (e) { /* ignore */ }
  }

  // ---- maps deep links -----------------------------------------------------
  // Built at runtime from public coordinates. If the user set a start point it
  // is added as the route origin, but it stays on the device only.

  function googleUrl(p) {
    var base = "https://www.google.com/maps/dir/?api=1&travelmode=driving&destination=" +
      p.lat + "," + p.lon;
    return startPoint ? base + "&origin=" + startPoint.lat + "," + startPoint.lon : base;
  }
  function appleUrl(p) {
    var base = "https://maps.apple.com/?dirflg=d&daddr=" + p.lat + "," + p.lon;
    return startPoint ? base + "&saddr=" + startPoint.lat + "," + startPoint.lon : base;
  }

  // ---- Wikimedia Commons photos -------------------------------------------

  var SKIP_RE = /\.(svg|pdf|ogg|ogv|webm|tif|tiff|gif)$/i;
  var SKIP_WORDS = /(logo|icon|karte|\bmap\b|wappen|coat of arms|seal|flag|locator|diagram|panorama_map)/i;

  function commonsQuery(term, limit) {
    var url = "https://commons.wikimedia.org/w/api.php?action=query&format=json&origin=*" +
      "&generator=search&gsrnamespace=6&gsrlimit=" + limit +
      "&gsrsearch=" + encodeURIComponent(term) +
      "&prop=imageinfo&iiprop=url|extmetadata&iiurlwidth=900";
    return fetch(url).then(function (r) { return r.json(); }).then(function (json) {
      var pages = json && json.query && json.query.pages ? json.query.pages : {};
      var out = [];
      Object.keys(pages).forEach(function (k) {
        var pg = pages[k];
        var title = pg.title || "";
        if (SKIP_RE.test(title) || SKIP_WORDS.test(title)) return;
        var ii = pg.imageinfo && pg.imageinfo[0];
        if (!ii || !ii.thumburl) return;
        var meta = ii.extmetadata || {};
        var author = meta.Artist ? stripHtml(meta.Artist.value) : "";
        var lic = meta.LicenseShortName ? stripHtml(meta.LicenseShortName.value) : "Wikimedia Commons";
        out.push({
          index: pg.index || 999,
          thumb: ii.thumburl,
          href: ii.descriptionurl || ("https://commons.wikimedia.org/wiki/" + encodeURIComponent(title)),
          credit: (author ? author + " · " : "") + lic
        });
      });
      out.sort(function (a, b) { return a.index - b.index; });
      return out;
    }).catch(function () { return []; });
  }

  function fetchPhotos(p) {
    if (photoCache[p.id]) return photoCache[p.id];
    var promise = commonsQuery(p.commons, 14).then(function (list) {
      if (list.length >= 3 || !p.wiki) return list;
      // Not enough hits: widen the search with the article title.
      return commonsQuery(p.wiki, 14).then(function (more) {
        var seen = {}, merged = [];
        list.concat(more).forEach(function (it) {
          if (seen[it.thumb]) return;
          seen[it.thumb] = 1; merged.push(it);
        });
        return merged;
      });
    }).then(function (list) { return list.slice(0, 6); });
    photoCache[p.id] = promise;
    return promise;
  }

  // ---- card list -----------------------------------------------------------

  var cardsEl = el("cards");
  var countEl = el("count");

  function visiblePlaces() {
    var q = query.trim().toLowerCase();
    return places.filter(function (p) {
      if (!matchesFilter(p)) return false;
      if (!q) return true;
      var hay = [p.name, p.short, p.destination, (p.tags || []).join(" "),
        p.unique, p.hike, p.descLong.join(" ")].join(" ").toLowerCase();
      return hay.indexOf(q) !== -1;
    });
  }

  function matchesFilter(p) {
    switch (activeFilter) {
      case "all": return true;
      case "under60": return p.driveMin < 60;
      case "60to90": return p.driveMin >= 60 && p.driveMin <= 90;
      case "over90": return p.driveMin > 90;
      case "wow": return p.wow >= 9;
      case "hike": return p.category === "hike";
      case "explore": return p.category === "explore";
      case "infra": return p.category === "infra";
      case "water": return p.category === "water";
      case "city": return p.category === "city" || p.category === "culture";
      case "favorites": return favs.has(p.id);
      default: return true;
    }
  }

  function cardHtml(p) {
    var emoji = CATEGORY_EMOJI[p.category] || "🌲";
    var meta = esc(p.drive) + " в пути · " + esc(p.walk) + " · " + esc(p.difficulty);
    var tags = (p.tags || []).slice(0, 4).map(function (t) {
      return '<span class="tag">' + esc(t) + "</span>";
    }).join("");
    return '' +
      '<article class="card" data-id="' + p.id + '">' +
        '<div class="photoWrap" data-photo="' + p.id + '">' +
          '<div class="photoFallback">' + emoji + "</div>" +
          '<img class="photo" alt="' + esc(p.name) + '" decoding="async">' +
          '<a class="photoCredit" target="_blank" rel="noopener"></a>' +
        "</div>" +
        '<div class="cardBody">' +
          '<div class="cardTop">' +
            '<div class="num">' + p._n + "</div>" +
            "<div><h3>" + esc(p.name) + "</h3>" +
              '<div class="meta">' + meta + "</div></div>" +
            '<button class="fav' + (favs.has(p.id) ? " on" : "") +
              '" data-fav="' + p.id + '" aria-label="В избранное">' +
              (favs.has(p.id) ? "★" : "☆") + "</button>" +
          "</div>" +
          '<p class="short">' + esc(p.short) + "</p>" +
          '<div class="special">✨ ' + esc(p.unique) + "</div>" +
          '<div class="tags">' + tags + "</div>" +
          '<div class="actions">' +
            '<button class="detailBtn" data-detail="' + p.id + '">Подробно</button>' +
            '<a class="apple" target="_blank" rel="noopener" href="' + esc(appleUrl(p)) + '">Apple Maps</a>' +
            '<a class="google" target="_blank" rel="noopener" href="' + esc(googleUrl(p)) + '">Google Maps</a>' +
          "</div>" +
        "</div>" +
      "</article>";
  }

  function renderCards() {
    var list = visiblePlaces();
    countEl.textContent = list.length + " из " + places.length;
    if (!list.length) {
      cardsEl.innerHTML = '<div class="empty">Ничего не найдено. Измените поиск или фильтр.</div>';
      return;
    }
    cardsEl.innerHTML = list.map(cardHtml).join("");
    list.forEach(function (p) {
      var wrap = cardsEl.querySelector('[data-photo="' + p.id + '"]');
      if (wrap) photoObserver.observe(wrap);
    });
  }

  // Lazy card photo: fetch only when the card scrolls into view.
  var photoObserver = new IntersectionObserver(function (entries) {
    entries.forEach(function (e) {
      if (!e.isIntersecting) return;
      var wrap = e.target;
      photoObserver.unobserve(wrap);
      var id = +wrap.getAttribute("data-photo");
      var p = places.find(function (x) { return x.id === id; });
      if (!p) return;
      fetchPhotos(p).then(function (photos) {
        if (!photos.length) return;
        var img = wrap.querySelector("img.photo");
        var credit = wrap.querySelector(".photoCredit");
        img.onload = function () { img.classList.add("loaded"); };
        img.src = photos[0].thumb;
        credit.textContent = photos[0].credit;
        credit.href = photos[0].href;
        credit.classList.add("show");
      });
    });
  }, { rootMargin: "300px" });

  // ---- detail bottom sheet -------------------------------------------------

  var sheetBack = el("sheetBack");
  var sheetContent = el("sheetContent");

  function factHtml(label, value) {
    return '<div class="fact"><b>' + esc(label) + "</b>" + esc(value) + "</div>";
  }
  function sectionHtml(label, value, cls) {
    return '<div class="section' + (cls ? " " + cls : "") + '"><b>' + esc(label) +
      "</b><p>" + esc(value) + "</p></div>";
  }

  function openSheet(p) {
    selectedId = p.id;
    highlightMarker(p.id);

    var longText = p.descLong.map(function (t) { return "<p>" + esc(t) + "</p>"; }).join("");
    var nearby = (p.nearby || []).map(function (n) { return "<li>" + esc(n) + "</li>"; }).join("");

    sheetContent.innerHTML = '' +
      "<h2>" + esc(p.name) + "</h2>" +
      '<div class="sheetMeta">' + esc(p.drive) + " в пути · " + esc(p.walk) +
        " · " + esc(p.activity) + " · сложность: " + esc(p.difficulty) + "</div>" +
      '<div class="gallery" id="gallery"><div class="galleryLoading">Загружаю фотографии из Wikimedia…</div></div>' +
      '<div class="galleryHint" id="galleryHint"></div>' +
      '<div class="longText">' + longText + "</div>" +
      '<div class="factsGrid">' +
        factHtml("Время в пути", p.drive) +
        factHtml("Пешая часть", p.walk) +
        factHtml("Активность", p.activity) +
        factHtml("Сложность", p.difficulty) +
      "</div>" +
      '<div class="section routeBox"><b>Поход и маршрут</b><p>' + esc(p.hike) +
        "</p><p>" + esc(p.route) + "</p></div>" +
      sectionHtml("Чем уникально", p.unique) +
      sectionHtml("Парковка", p.parking) +
      sectionHtml("Лучшее время", p.season) +
      '<div class="section nearbyBox"><b>Что рядом</b><ul class="highlightList">' + nearby + "</ul></div>" +
      '<div class="section warn"><b>Важно знать</b><p>' + esc(p.warning) + "</p></div>" +
      '<div class="section"><a class="official" target="_blank" rel="noopener" href="' +
        esc(p.official) + '">Официальный сайт места →</a></div>' +
      '<div class="sheetActions">' +
        '<a class="apple" target="_blank" rel="noopener" href="' + esc(appleUrl(p)) + '">Маршрут в Apple Maps</a>' +
        '<a class="google" target="_blank" rel="noopener" href="' + esc(googleUrl(p)) + '">Маршрут в Google Maps</a>' +
      "</div>";

    sheetBack.classList.add("open");
    sheetBack.setAttribute("aria-hidden", "false");
    sheetContent.parentElement.scrollTop = 0;

    loadGallery(p);
  }

  function loadGallery(p) {
    fetchPhotos(p).then(function (photos) {
      var g = el("gallery");
      var hint = el("galleryHint");
      if (selectedId !== p.id || !g) return;
      if (!photos.length) {
        g.innerHTML = '<div class="galleryLoading">Фотографии не найдены. Откройте официальный сайт места ниже.</div>';
        return;
      }
      g.innerHTML = photos.map(function (ph) {
        return '<div class="galleryItem">' +
          '<img loading="lazy" decoding="async" src="' + esc(ph.thumb) + '" alt="' + esc(p.name) + '">' +
          '<a class="galleryCredit" target="_blank" rel="noopener" href="' + esc(ph.href) + '">' +
            esc(ph.credit) + "</a></div>";
      }).join("");
      if (hint) {
        hint.innerHTML = "Фото: " + photos.length +
          " — источник Wikipedia / Wikimedia Commons, лицензии указаны на снимках. Проведите пальцем, чтобы листать.";
      }
    });
  }

  function closeSheet() {
    sheetBack.classList.remove("open");
    sheetBack.setAttribute("aria-hidden", "true");
    if (selectedId != null) { selectedId = null; highlightMarker(null); }
  }

  // ---- Leaflet map ---------------------------------------------------------

  var map = null;
  var markers = {};      // id -> marker
  var originMarker = null;

  function numberIcon(p, selected) {
    var cls = "numberMarker" + (p.wow >= 9 ? " wow" : "") + (selected ? " selected" : "");
    return L.divIcon({
      className: "", html: '<div class="' + cls + '">' + p._n + "</div>",
      iconSize: [32, 32], iconAnchor: [16, 16], popupAnchor: [0, -14]
    });
  }

  function highlightMarker(id) {
    Object.keys(markers).forEach(function (k) {
      var p = places.find(function (x) { return x.id === +k; });
      markers[k].setIcon(numberIcon(p, +k === id));
    });
  }

  function initMap() {
    if (typeof L === "undefined") { el("mapError").classList.add("show"); return; }
    try {
      map = L.map("map", { zoomControl: true, attributionControl: true })
        .setView([DATA.mapCenter.lat, DATA.mapCenter.lon], 8);
      L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
        maxZoom: 19,
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
      }).addTo(map);

      places.forEach(function (p) {
        var m = L.marker([p.lat, p.lon], { icon: numberIcon(p, false) }).addTo(map);
        m.bindPopup(
          '<div class="popupTitle">' + p._n + ". " + esc(p.name) + "</div>" +
          '<div class="popupMeta">' + esc(p.drive) + " · " + esc(p.walk) + "</div>" +
          '<a class="popupBtn" href="#" data-popup="' + p.id + '">Открыть описание</a>'
        );
        m.on("click", function () { openSheet(p); });
        markers[p.id] = m;
      });

      // Fit to all markers with a little padding.
      var group = L.featureGroup(Object.keys(markers).map(function (k) { return markers[k]; }));
      map.fitBounds(group.getBounds().pad(0.15));

      if (startPoint) setOriginMarker(startPoint, false);
    } catch (e) {
      el("mapError").classList.add("show");
    }
  }

  function setOriginMarker(pt, fly) {
    if (!map) return;
    if (originMarker) map.removeLayer(originMarker);
    originMarker = L.marker([pt.lat, pt.lon], {
      icon: L.divIcon({ className: "", html: '<div class="originMarker">📍</div>', iconSize: [38, 38], iconAnchor: [19, 19] })
    }).addTo(map).bindPopup("Ваша стартовая точка (хранится только на этом устройстве)");
    if (fly) map.flyTo([pt.lat, pt.lon], 10);
  }

  function flyToPlace(p) {
    if (map) map.flyTo([p.lat, p.lon], 11, { duration: 0.6 });
  }

  // ---- start point (local only) -------------------------------------------

  var startStatus = el("startStatus");

  function updateStartStatus(msg) {
    var clearBtn = el("clearStart");
    // The leading text node holds the status message; the button and the
    // privacy note after it are preserved.
    if (msg && startStatus.firstChild) startStatus.firstChild.nodeValue = msg + " ";
    if (clearBtn) clearBtn.hidden = !startPoint;
  }

  function geocodeAndSave(address) {
    var url = "https://nominatim.openstreetmap.org/search?format=json&limit=1&q=" +
      encodeURIComponent(address);
    return fetch(url, { headers: { "Accept-Language": "ru" } })
      .then(function (r) { return r.json(); })
      .then(function (arr) {
        if (!arr || !arr.length) throw new Error("not found");
        var hit = arr[0];
        var pt = { lat: parseFloat(hit.lat), lon: parseFloat(hit.lon), label: address };
        saveStart(pt);
        setOriginMarker(pt, true);
        refreshMapLinks();
        return pt;
      });
  }

  // Re-render cards so Apple/Google links pick up the new origin.
  function refreshMapLinks() { renderCards(); }

  function useGeolocation() {
    if (!navigator.geolocation) {
      updateStartStatus("Геолокация недоступна в этом браузере.");
      return;
    }
    updateStartStatus("Определяю ваше местоположение…");
    navigator.geolocation.getCurrentPosition(function (pos) {
      var pt = { lat: pos.coords.latitude, lon: pos.coords.longitude, label: "Моя геопозиция" };
      saveStart(pt);
      setOriginMarker(pt, true);
      refreshMapLinks();
      updateStartStatus("Стартовая точка задана по вашей геопозиции.");
    }, function () {
      updateStartStatus("Не удалось получить геопозицию. Проверьте разрешения браузера.");
    }, { enableHighAccuracy: true, timeout: 10000 });
  }

  // ---- wiring --------------------------------------------------------------

  function bind() {
    // List / card interactions (event delegation).
    cardsEl.addEventListener("click", function (e) {
      var fav = e.target.closest("[data-fav]");
      if (fav) {
        var fid = +fav.getAttribute("data-fav");
        if (favs.has(fid)) favs.delete(fid); else favs.add(fid);
        saveFavs();
        fav.classList.toggle("on", favs.has(fid));
        fav.textContent = favs.has(fid) ? "★" : "☆";
        if (activeFilter === "favorites") renderCards();
        return;
      }
      var det = e.target.closest("[data-detail]");
      if (det) {
        var p = places.find(function (x) { return x.id === +det.getAttribute("data-detail"); });
        if (p) { openSheet(p); flyToPlace(p); }
        return;
      }
      if (e.target.closest("a")) return; // let map links open
      var card = e.target.closest(".card");
      if (card) {
        var cp = places.find(function (x) { return x.id === +card.getAttribute("data-id"); });
        if (cp) { openSheet(cp); flyToPlace(cp); }
      }
    });

    // Popup "open description" links.
    document.addEventListener("click", function (e) {
      var pop = e.target.closest("[data-popup]");
      if (pop) {
        e.preventDefault();
        var p = places.find(function (x) { return x.id === +pop.getAttribute("data-popup"); });
        if (p) openSheet(p);
      }
    });

    // Search.
    var searchEl = el("search");
    var t;
    searchEl.addEventListener("input", function () {
      clearTimeout(t);
      t = setTimeout(function () { query = searchEl.value; renderCards(); }, 120);
    });

    // Filter chips.
    el("chips").addEventListener("click", function (e) {
      var chip = e.target.closest(".chip");
      if (!chip) return;
      activeFilter = chip.getAttribute("data-filter");
      Array.prototype.forEach.call(el("chips").children, function (c) {
        c.classList.toggle("active", c === chip);
      });
      renderCards();
    });

    // Surprise me.
    el("surprise").addEventListener("click", function () {
      var list = visiblePlaces();
      if (!list.length) list = places;
      var p = list[Math.floor(Math.random() * list.length)];
      openSheet(p); flyToPlace(p);
    });

    // Sheet close.
    el("close").addEventListener("click", closeSheet);
    sheetBack.addEventListener("click", function (e) { if (e.target === sheetBack) closeSheet(); });
    document.addEventListener("keydown", function (e) { if (e.key === "Escape") closeSheet(); });

    // Start point controls.
    el("saveStart").addEventListener("click", function () {
      var v = el("startInput").value.trim();
      if (!v) { updateStartStatus("Введите адрес или название места."); return; }
      updateStartStatus("Ищу точку на карте…");
      geocodeAndSave(v).then(function () {
        el("startInput").value = "";
        updateStartStatus("Стартовая точка сохранена на этом устройстве.");
      }).catch(function () {
        updateStartStatus("Не удалось найти адрес. Уточните написание.");
      });
    });
    el("useGeo").addEventListener("click", useGeolocation);
    el("locate").addEventListener("click", useGeolocation);
    var clearBtn = el("clearStart");
    if (clearBtn) clearBtn.addEventListener("click", function () {
      saveStart(null);
      if (originMarker && map) { map.removeLayer(originMarker); originMarker = null; }
      refreshMapLinks();
      updateStartStatus("Сохранённая точка удалена.");
    });
  }

  // ---- boot ----------------------------------------------------------------

  renderCards();
  initMap();
  bind();
  if (startPoint) updateStartStatus("Стартовая точка задана (хранится только на этом устройстве).");
})();
