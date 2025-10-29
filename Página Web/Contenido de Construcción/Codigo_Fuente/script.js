/* ====== VinylShop Frontend (HTML+CSS+JS) ======
   - LocalStorage for users and carts
   - Email sending via EmailJS (optional)
   - Basic recommendation by genre
=============================================== */

(() => {
  // ---------- Config (rellena con tus keys EmailJS si quieres enviar correos reales) ----------
  const EMAILJS_SERVICE_ID = "YOUR_SERVICE_ID";     // ej.: 'service_xxx'
  const EMAILJS_TEMPLATE_ID = "YOUR_TEMPLATE_ID";   // ej.: 'template_xxx'
  const EMAILJS_PUBLIC_KEY = "YOUR_PUBLIC_KEY";     // ej.: 'user_xxx'
  // Si no se quiere configurar el EmailJS, deja las cadenas vacías y el envío será simulado.
  // -------------------------------------------------------------------------------------------

  // Inicializa el EmailJs si esta inicializado
  if (window.emailjs && EMAILJS_PUBLIC_KEY) {
    try { emailjs.init(EMAILJS_PUBLIC_KEY); } catch (e) {}
  }

  // ---------- DATA: productos iniciales ----------
  const PRODUCTS = [
    {
      id: "vinyl-01",
      title: "Beethoven — Sinfonía No.9 (Ed. Clásica)",
      artist: "L. van Beethoven",
      price: 850.00,
      genre: "Clásica",
      description: "Edición vinil remasterizada. 180g. Incluye libreto.",
      coverColor: "#2b2b2b"
    },
    {
      id: "vinyl-02",
      title: "Debussy — Préludes (Colección)",
      artist: "C. Debussy",
      price: 720.00,
      genre: "Clásica",
      description: "Selección de preludios. Edición de coleccionista.",
      coverColor: "#23384a"
    }
  ];

  // ---------- Utilidades ----------
  const $ = sel => document.querySelector(sel);
  const $$ = sel => Array.from(document.querySelectorAll(sel));
  const formatMoney = n => {
    // calcular moneda con precisión (digito por digito)
    const rounded = Math.round(n * 100); // centavos
    const pesos = Math.floor(rounded / 100);
    const cents = Math.abs(rounded % 100).toString().padStart(2, "0");
    return `$${pesos}.${cents}`;
  };

  // ---------- Estado ----------
  let currentUser = null; // { name, email }
  const cartKey = email => `mv_cart_${email}`;
  const userKey = "mv_user";

  // ---------- DOM refs ----------
  const productGrid = $("#productGrid");
  const cartCount = $("#cartCount");
  const cartPanel = $("#cartPanel");
  const cartItemsEl = $("#cartItems");
  const cartTotalEl = $("#cartTotal");
  const cartBtn = $("#cartBtn");
  const checkoutBtn = $("#checkoutBtn");
  const loginBtn = $("#loginBtn");
  const authModal = $("#authModal");
  const cartModalClose = $("#closeCart");
  const authClose = $("#closeAuth");
  const loginFormEl = $("#loginForm");
  const registerFormEl = $("#registerForm");
  const tabLogin = $("#tabLogin");
  const tabRegister = $("#tabRegister");
  const recommendModal = $("#recommendModal");
  const recommendContent = $("#recommendationContent");
  const goToProductsBtn = $("#goToProducts");

  // ---------- Init ----------
  document.getElementById("year").textContent = new Date().getFullYear();
  renderProducts();
  loadUserFromStorage();
  updateCartUI();

  // ---------- Render products ----------
  function renderProducts() {
    productGrid.innerHTML = "";
    PRODUCTS.forEach(p => {
      const card = document.createElement("article");
      card.className = "product-card";
      card.innerHTML = `
        <div class="album-art" style="background: linear-gradient(180deg, ${shade(p.coverColor, -12)}, ${shade(p.coverColor, -35)});">
          <div style="text-align:center;padding:0.5rem">
            <div style="font-size:0.9rem">${shortTitle(p.title)}</div>
            <div style="font-size:0.7rem;margin-top:6px">${p.artist}</div>
          </div>
        </div>
        <div class="product-info">
          <div class="product-title">${p.title}</div>
          <div class="product-meta">${p.artist} · ${p.genre}</div>
          <div style="display:flex;justify-content:space-between;align-items:center;margin-top:8px">
            <div class="price">${formatMoney(p.price)}</div>
            <div class="product-actions">
              <button class="btn" data-action="add" data-id="${p.id}">Añadir</button>
            </div>
          </div>
          <p style="margin-top:8px;font-size:0.86rem;color:#b9b09b">${p.description}</p>
        </div>
      `;
      productGrid.appendChild(card);
    });

    // attach events for add buttons
    $$(".product-card button[data-action='add']").forEach(btn => {
      btn.addEventListener("click", e => {
        const id = e.currentTarget.dataset.id;
        addToCart(id, 1);
      });
    });
  }

  // ---------- Cart functions ----------
  function getCartForCurrentUser() {
    if (!currentUser || !currentUser.email) return [];
    try {
      const raw = localStorage.getItem(cartKey(currentUser.email));
      return raw ? JSON.parse(raw) : [];
    } catch (e) {
      return [];
    }
  }
  function saveCartForCurrentUser(items) {
    if (!currentUser || !currentUser.email) return;
    localStorage.setItem(cartKey(currentUser.email), JSON.stringify(items));
  }

  function addToCart(productId, qty = 1) {
    if (!currentUser) {
      openAuthModal();
      return alert("Debes iniciar sesión antes de añadir al carrito.");
    }
    const items = getCartForCurrentUser();
    const p = PRODUCTS.find(x => x.id === productId);
    if (!p) return;
    const existing = items.find(i => i.id === productId);
    if (existing) existing.qty += qty;
    else items.push({ id: p.id, title: p.title, price: p.price, qty, genre: p.genre });
    saveCartForCurrentUser(items);
    updateCartUI();
  }

  function removeFromCart(productId) {
    if (!currentUser) return;
    let items = getCartForCurrentUser();
    items = items.filter(i => i.id !== productId);
    saveCartForCurrentUser(items);
    updateCartUI();
  }

  function changeQty(productId, newQty) {
    if (!currentUser) return;
    const items = getCartForCurrentUser();
    const it = items.find(i => i.id === productId);
    if (!it) return;
    it.qty = Math.max(0, Math.floor(newQty));
    const filtered = items.filter(i => i.qty > 0);
    saveCartForCurrentUser(filtered);
    updateCartUI();
  }

  function cartTotal(items) {
    // calculate total digit-by-digit: sum of (price * qty) in cents
    let totalCents = 0;
    items.forEach(it => {
      const cents = Math.round(it.price * 100);
      totalCents += cents * it.qty;
    });
    return totalCents / 100;
  }

  function updateCartUI() {
    const items = currentUser ? getCartForCurrentUser() : [];
    const count = items.reduce((s, i) => s + i.qty, 0);
    cartCount.textContent = String(count);
    // render items into panel
    cartItemsEl.innerHTML = "";
    if (items.length === 0) {
      cartItemsEl.innerHTML = `<p style="color:#9b9486">Tu carrito está vacío.</p>`;
    } else {
      items.forEach(it => {
        const div = document.createElement("div");
        div.className = "cart-item";
        div.innerHTML = `
          <div class="art" style="background: linear-gradient(180deg, ${shade("#333", -6)}, ${shade("#111", -24)});"></div>
          <div class="meta">
            <div style="display:flex;justify-content:space-between;align-items:center">
              <div style="font-weight:700;color:#f7f3eb">${it.title}</div>
              <div style="font-weight:700;color:var(--accent)">${formatMoney(it.price)}</div>
            </div>
            <div style="display:flex;gap:0.4rem;margin-top:6px;align-items:center">
              <label style="font-size:0.85rem">Cant.
                <input type="number" min="1" value="${it.qty}" data-action="qty" data-id="${it.id}" style="width:64px;padding:4px;border-radius:6px;background:#0b0b0b;color:var(--muted);border:1px solid rgba(255,255,255,0.03)" />
              </label>
              <button data-action="remove" data-id="${it.id}" class="btn" style="padding:0.4rem 0.7rem">Eliminar</button>
            </div>
          </div>
        `;
        cartItemsEl.appendChild(div);
      });
    }

    // attach events to qty inputs and remove buttons
    $$("#cartItems input[data-action='qty']").forEach(inp => {
      inp.addEventListener("change", e => {
        const id = e.currentTarget.dataset.id;
        const v = Number(e.currentTarget.value || 0);
        if (v <= 0) {
          if (!confirm("¿Eliminar este producto del carrito?")) {
            updateCartUI(); // reset
            return;
          } else removeFromCart(id);
        } else changeQty(id, v);
      });
    });
    $$("#cartItems button[data-action='remove']").forEach(b => {
      b.addEventListener("click", e => {
        const id = e.currentTarget.dataset.id;
        if (confirm("¿Quitar producto?")) removeFromCart(id);
      });
    });

    const total = cartTotal(items);
    cartTotalEl.textContent = formatMoney(total);
  }

  // ---------- Auth ----------
  function loadUserFromStorage() {
    try {
      const raw = localStorage.getItem(userKey);
      if (raw) {
        currentUser = JSON.parse(raw);
        onUserChanged();
      }
    } catch (e) { currentUser = null; }
  }
  function saveUserToStorage() {
    if (currentUser) localStorage.setItem(userKey, JSON.stringify(currentUser));
    else localStorage.removeItem(userKey);
  }

  async function checkSession() {
    try {
      const res = await fetch('api/current_user.php', { credentials: 'same-origin' });
      const data = await res.json();
      if (data && data.user) {
        currentUser = data.user;
        saveUserToStorage(); // optional: keep local copy to persist UI quickly
      } else {
        currentUser = null;
        saveUserToStorage();
      }
      onUserChanged();
    } catch (err) {
      console.error('Sesion check error:', err);
      }
  }

  async function registerUser(name, email, password) {
    try {
      const res = await fetch('api/register.php', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify({ name, email, password })
      });
      const data = await res.json();
      if (!data.ok) {
        alert(data.msg || 'Error al registrar');
        return false;
      }
      currentUser = data.user;
      saveUserToStorage();
      return true;
    } catch (e) {
      console.error(e);
      alert('Error de conexión al registrar');
      return false;
    }
  }

  async function loginUser(email, password) {
    try {
      const res = await fetch('api/login.php', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'same-origin',
        body: JSON.stringify({ email, password })
      });
      const data = await res.json();
      if (!data.ok) {
        return false;
      }
      currentUser = data.user;
      saveUserToStorage();
      return true;
    } catch (e) {
      console.error(e);
      return false;
    }
  }

  async function logoutUser() {
    try {
      await fetch('api/logout.php', { method: 'POST', credentials: 'same-origin' });
    } catch (e) { /* ignore */ }
    currentUser = null;
    saveUserToStorage();
    updateCartUI();
    loginBtn.textContent = "Iniciar sesión";
  }

  function onUserChanged() {
    if (currentUser) {
      loginBtn.textContent = `Hola, ${currentUser.name}`;
    } else {
      loginBtn.textContent = "Iniciar sesión";
      // close cart if open
      cartPanel.classList.add("hidden");
    }
    updateCartUI();
  }

  // ---------- Checkout & Email (EmailJS) ----------
  async function checkoutAndSendTicket() {
    if (!currentUser || !currentUser.email) {
      alert("Debes iniciar sesión para pagar.");
      openAuthModal();
      return;
    }
    const items = getCartForCurrentUser();
    if (!items.length) {
      alert("Tu carrito está vacío.");
      return;
    }
    const total = cartTotal(items);
    // Prepare ticket HTML / texto
    const itemsHtml = items.map(it => `<li>${escapeHtml(it.title)} — ${it.qty} × ${formatMoney(it.price)}</li>`).join("");
    const plainText = `Ticket VinylShop\nCliente: ${currentUser.name} <${currentUser.email}>\n\nItems:\n${items.map(it => `- ${it.title} x${it.qty} ${formatMoney(it.price)}`).join("\n")}\n\nTotal: ${formatMoney(total)}`;

    // Generate recommendation based on genres purchased
    const recommendation = generateRecommendation(items);

    // EmailJS send (if configured)
    const templateParams = {
      to_name: currentUser.name,
      to_email: currentUser.email,
      message_html: `<p>Gracias por tu compra. Aquí tu ticket:</p><ul>${itemsHtml}</ul><p><strong>Total: ${formatMoney(total)}</strong></p><p>Recomendación: ${escapeHtml(recommendation.title || "—")}</p>`,
      message_plain: plainText,
      recommendation: recommendation.title || ""
    };

    if (EMAILJS_SERVICE_ID && EMAILJS_TEMPLATE_ID && EMAILJS_PUBLIC_KEY && window.emailjs) {
      try {
        await emailjs.send(EMAILJS_SERVICE_ID, EMAILJS_TEMPLATE_ID, templateParams);
        alert("Ticket enviado por correo.");
      } catch (err) {
        console.error("EmailJS error:", err);
        alert("No se pudo enviar el correo (ver codigo).");
      }
    } else {
      // Simulate: muestra modal de confirmación y guarda la compra en historial local
      alert("Simulación: ticket PREPARADO. Si configuras EmailJS, el correo se enviará realmente.");
      console.log("TICKET (simulado):", templateParams);
    }

    // Guardar historial breve
    savePurchaseHistory(currentUser.email, items, total, recommendation);
    // vaciar carrito
    saveCartForCurrentUser([]);
    updateCartUI();
    // mostrar recomendación en modal
    showRecommendationModal(recommendation);
  }

  // ---------- Recommendations ----------
  function generateRecommendation(items) {
    // Basico: encuentra el género más frecuente y recomienda un producto diferente
    const genreCount = {};
    items.forEach(it => {
      genreCount[it.genre] = (genreCount[it.genre] || 0) + it.qty;
    });
    const topGenre = Object.keys(genreCount).sort((a,b)=>genreCount[b]-genreCount[a])[0];
    // find a product not in cart and same genre (or first different)
    const boughtIds = new Set(items.map(i=>i.id));
    let candidate = PRODUCTS.find(p => p.genre === topGenre && !boughtIds.has(p.id));
    if (!candidate) candidate = PRODUCTS.find(p => !boughtIds.has(p.id));
    if (!candidate) return { title: "Gracias por tu compra", reason: "No hay más productos para recomendar" };
    return { id: candidate.id, title: candidate.title, reason: `Basado en tu interés en ${topGenre}` };
  }

  function showRecommendationModal(reco) {
    recommendContent.innerHTML = `
      <div class="recommendation-card">
        <div style="width:72px;height:72px;border-radius:8px;background:linear-gradient(180deg,#222,#111);display:flex;align-items:center;justify-content:center">
          <span style="font-size:0.75rem;text-align:center">${shortTitle(reco.title)}</span>
        </div>
        <div>
          <div class="reco-title">${reco.title}</div>
          <div style="font-size:0.9rem;color:#b9b09b">${reco.reason || ""}</div>
        </div>
      </div>
    `;
    recommendModal.classList.remove("hidden");
    // go to product when button clicked
    goToProductsBtn.onclick = () => {
      recommendModal.classList.add("hidden");
      // scroll to product grid and highlight if exists
      const idx = PRODUCTS.findIndex(p => p.id === reco.id);
      if (idx >= 0) {
        // highlight briefly
        const card = productGrid.children[idx];
        card.style.transition = "box-shadow 250ms";
        card.style.boxShadow = "0 6px 40px rgba(185,139,61,0.18)";
        card.scrollIntoView({ behavior: "smooth", block: "center" });
        setTimeout(()=> card.style.boxShadow = "", 1500);
      }
    };
  }

  // ---------- Purchase history ----------
  function savePurchaseHistory(email, items, total, recommendation) {
    const key = `mv_history_${email}`;
    const raw = localStorage.getItem(key);
    const arr = raw ? JSON.parse(raw) : [];
    arr.push({
      date: new Date().toISOString(),
      items, total, recommendation
    });
    localStorage.setItem(key, JSON.stringify(arr));
  }

  // ---------- Helpers ----------
  function escapeHtml(s){ return (s+"").replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":"&#39;"}[c])); }
  function shortTitle(t){ return t.split(" ").slice(0,3).join(" "); }
  // shade helper (simple)
  function shade(hex, percent){
    // accepts #rrggbb
    try {
      const c = hex.replace("#","");
      const num = parseInt(c,16);
      let r = (num >> 16) + Math.round(percent/2);
      let g = (num >> 8 & 0x00FF) + Math.round(percent/2);
      let b = (num & 0x0000FF) + Math.round(percent/2);
      r = Math.max(0,Math.min(255,r));
      g = Math.max(0,Math.min(255,g));
      b = Math.max(0,Math.min(255,b));
      return "#" + ( (1<<24) + (r<<16) + (g<<8) + b ).toString(16).slice(1);
    } catch (e) { return hex; }
  }

  // ---------- UI Controls ----------
  // toggle nav on small screens
  $("#navToggle").addEventListener("click", () => {
    $("#navList").classList.toggle("show");
  });

  // open auth modal
  loginBtn.addEventListener("click", (e) => {
    if (currentUser) {
      // show small menu: logout option and profile
      if (confirm("¿Cerrar sesión?")) {
        logoutUser();
      }
      return;
    }
    openAuthModal();
  });

  function openAuthModal() {
    authModal.classList.remove("hidden");
    loginFormEl.classList.remove("hidden");
    registerFormEl.classList.add("hidden");
    tabLogin.classList.add("active");
    tabRegister.classList.remove("active");
  }
  authClose.addEventListener("click", ()=> authModal.classList.add("hidden"));
  tabLogin.addEventListener("click", ()=> {
    tabLogin.classList.add("active");
    tabRegister.classList.remove("active");
    loginFormEl.classList.remove("hidden");
    registerFormEl.classList.add("hidden");
  });
  tabRegister.addEventListener("click", ()=> {
    tabRegister.classList.add("active");
    tabLogin.classList.remove("active");
    registerFormEl.classList.remove("hidden");
    loginFormEl.classList.add("hidden");
  });

  // register action
  $("#doRegister").addEventListener("click", ()=> {
    const name = $("#regName").value.trim();
    const email = $("#regEmail").value.trim();
    const pwd = $("#regPassword").value;
    if (!name || !email || !pwd) return alert("Completa todos los campos.");
    if (registerUser(name,email,pwd)) {
      alert("Cuenta creada. Bienvenido/a " + name);
      authModal.classList.add("hidden");
      onUserChanged();
    }
  });

  // login action
  $("#doLogin").addEventListener("click", ()=> {
    const email = $("#loginEmail").value.trim();
    const pwd = $("#loginPassword").value;
    if (!email || !pwd) return alert("Completa correo y contraseña.");
    if (!loginUser(email,pwd)) return alert("Credenciales incorrectas.");
    alert("Sesión iniciada.");
    authModal.classList.add("hidden");
    onUserChanged();
  });

  // cart panel toggles
  cartBtn.addEventListener("click", (e) => {
    e.preventDefault();
    if (!currentUser) {
      openAuthModal();
      return;
    }
    cartPanel.classList.toggle("hidden");
    updateCartUI();
  });
  cartModalClose.addEventListener("click", ()=> cartPanel.classList.add("hidden"));

  // checkout
  checkoutBtn.addEventListener("click", ()=> {
    if (!confirm("Confirmar compra y enviar ticket por correo?")) return;
    checkoutAndSendTicket();
  });

  // recommendation modal close
  $("#closeRecommend").addEventListener("click", ()=> recommendModal.classList.add("hidden"));

  // Click outside modals to close (simple)
  window.addEventListener("click", e => {
    if (e.target === authModal) authModal.classList.add("hidden");
    if (e.target === recommendModal) recommendModal.classList.add("hidden");
  });


  onUserChanged();

  
  window.VinylShop = {
    PRODUCTS, getCartForCurrentUser, addToCart, removeFromCart, checkoutAndSendTicket
  };

})();
