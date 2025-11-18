const tourList = document.querySelector('#tourList');
const loginToggle = document.querySelector('#loginToggle');
const loginPanel = document.querySelector('#loginPanel');
const loginForm = document.querySelector('#loginForm');
const loginStatus = document.querySelector('#loginStatus');
const loginEmail = document.querySelector('#loginEmail');
const tourDetailOverlay = document.querySelector('#tourDetailOverlay');
const tourDetailClose = document.querySelector('#tourDetailClose');
const logoutButton = document.querySelector('#logoutButton');
const operatorConsoleButton = document.querySelector('#operatorConsoleButton');
const operatorHeaderLogout = document.querySelector('#operatorHeaderLogout');
const detailTitle = document.querySelector('#tourDetailTitle');
const detailLocation = document.querySelector('#tourDetailLocation');
const detailDescription = document.querySelector('#tourDetailDescription');
const detailPrice = document.querySelector('#tourDetailPrice');
const detailCapacity = document.querySelector('#tourDetailCapacity');
const detailDuration = document.querySelector('#tourDetailDuration');
const detailActivity = document.querySelector('#tourDetailActivity');
const detailGear = document.querySelector('#tourDetailGear');
const detailHighlights = document.querySelector('#tourDetailHighlights');
const detailTip = document.querySelector('#tourDetailTip');
const OPERATOR_SESSION_KEY = 'operatorSession';
let currentUser = null;
let lastFocusedCard = null;

const demoTours = [
    {
        title: 'Misty Mountain Hike',
        location: 'Aspen, USA',
        description: 'Start your morning above the clouds with a gentle hike to a hidden alpine lake.',
        price: 129,
        maxCapacity: 14,
        durationHours: 5,
        activityLevel: 'Moderate',
        whatToBring: 'Layers, daypack & 2L of water',
        highlights: [
            'Sunrise departure over the valley',
            'Private guide with altitude tips',
            'Picnic breakfast beside the lake',
        ],
        guideTip: 'Pack a lightweight rain shell—alpine weather changes quickly.',
    },
    {
        title: 'Rainforest River Kayak',
        location: 'Leticia, Colombia',
        description: 'Glide through lush mangroves while spotting parrots, sloths and river dolphins.',
        price: 189,
        maxCapacity: 10,
        durationHours: 4,
        activityLevel: 'Active',
        whatToBring: 'Quick-dry clothing & reef-safe sunscreen',
        highlights: [
            'Floating breakfast basket',
            'Guided wildlife spotting breaks',
            'Fresh açai tasting at a riverside village',
        ],
        guideTip: 'Keep cameras in a dry bag—guides will help you capture the wildlife safely.',
    },
    {
        title: 'Volcanic Sunset Jeep Ride',
        location: 'Santorini, Greece',
        description: 'Bounce across black-sand trails before sharing a picnic on the caldera rim.',
        price: 159,
        maxCapacity: 12,
        durationHours: 3,
        activityLevel: 'Easy',
        whatToBring: 'Sunglasses & a light jacket',
        highlights: [
            'Hidden lava field photo stops',
            'Sunset mezze platter with local wine',
            'Night sky telescope viewing',
        ],
        guideTip: 'Dust kicks up on the trail—complimentary scarves are provided for comfort.',
    },
    {
        title: 'Nordic Fjord Cycling',
        location: 'Ålesund, Norway',
        description: 'Cycle quiet coastal roads and hop ferries between islands.',
        price: 210,
        maxCapacity: 8,
        durationHours: 6,
        activityLevel: 'Challenging',
        whatToBring: 'Thermal base layers & cycling gloves',
        highlights: [
            'Ferry ride across Geirangerfjord',
            'Local bakery lunch stop',
            'Waterfall-side photo session',
        ],
        guideTip: 'E-bikes available—just mention it in your booking notes if you prefer assistance.',
    },
    {
        title: 'Red Desert Stars',
        location: 'Merzouga, Morocco',
        description: 'An overnight camel caravan with astronomer-led stargazing in the quiet dunes.',
        price: 275,
        maxCapacity: 6,
        durationHours: 12,
        activityLevel: 'Leisurely',
        whatToBring: 'Closed-toe sandals & a reusable water bottle',
        highlights: [
            'Berber drumming around the campfire',
            'Traditional tagine dinner',
            'Guided constellation walk with telescope',
        ],
        guideTip: 'Nighttime temps dip low—camp provides blankets but socks keep toes cozy.',
    },
];

const priceFormatter = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 2,
});

const durationText = (hours) => {
    const numeric = Number(hours);
    if (!Number.isFinite(numeric)) {
        return 'Custom';
    }
    if (numeric >= 24 && Number.isInteger(numeric / 24)) {
        const days = numeric / 24;
        return days === 1 ? '1 day' : `${days} days`;
    }
    return `${numeric} hrs`;
};

const capacityText = (capacity) => (capacity ? `${capacity} guests` : 'Flexible');

const priceText = (price) => {
    const value = Number(price);
    if (!Number.isFinite(value)) {
        return price || 'Contact for pricing';
    }
    return priceFormatter.format(value);
};

function togglePanel(forceState) {
    const willShow = typeof forceState === 'boolean' ? forceState : loginPanel.classList.contains('hidden');
    loginPanel.classList.toggle('hidden', !willShow);
    if (willShow) {
        loginEmail.focus();
    }
}

function closePanelOnOutsideClick(event) {
    if (!loginPanel.contains(event.target) && event.target !== loginToggle) {
        loginPanel.classList.add('hidden');
    }
}

document.addEventListener('click', closePanelOnOutsideClick);
loginToggle.addEventListener('click', () => togglePanel());

function toggleOperatorShortcut(shouldShow) {
    if (operatorConsoleButton) {
        operatorConsoleButton.classList.toggle('hidden', !shouldShow);
    }
    if (operatorHeaderLogout) {
        operatorHeaderLogout.classList.toggle('hidden', !shouldShow);
    }
    if (loginToggle) {
        loginToggle.classList.toggle('hidden', shouldShow);
    }
    if (shouldShow && loginPanel) {
        loginPanel.classList.add('hidden');
    }
    if (logoutButton) {
        logoutButton.classList.toggle('hidden', shouldShow || !currentUser);
    }
}

function updateLoginButton() {
    const isOperator = currentUser?.role === 'OPERATOR';
    toggleOperatorShortcut(isOperator);

    if (!loginToggle) {
        return;
    }

    if (currentUser && !isOperator) {
        loginToggle.textContent = `Hi, ${currentUser.name}`;
    } else if (!currentUser) {
        loginToggle.textContent = 'Log in';
        if (loginPanel) {
            loginPanel.classList.add('hidden');
        }
    }
}

function persistOperatorSession(user, password) {
    if (user.role !== 'OPERATOR') {
        localStorage.removeItem(OPERATOR_SESSION_KEY);
        return user;
    }

    const authHeader = `Basic ${btoa(`${user.email}:${password}`)}`;
    const enrichedUser = { ...user, authHeader };
    localStorage.setItem(OPERATOR_SESSION_KEY, JSON.stringify(enrichedUser));
    return enrichedUser;
}

function restoreOperatorSession() {
    const storedValue = localStorage.getItem(OPERATOR_SESSION_KEY);
    if (!storedValue) {
        return;
    }

    try {
        const parsed = JSON.parse(storedValue);
        if (parsed && parsed.role === 'OPERATOR') {
            currentUser = parsed;
            updateLoginButton();
        } else {
            localStorage.removeItem(OPERATOR_SESSION_KEY);
        }
    } catch (error) {
        console.error('Unable to restore operator session', error);
        localStorage.removeItem(OPERATOR_SESSION_KEY);
    }
}

function handleLogout() {
    currentUser = null;
    localStorage.removeItem(OPERATOR_SESSION_KEY);
    loginForm.reset();
    loginStatus.textContent = 'Signed out.';
    loginStatus.className = 'status';
    updateLoginButton();
    togglePanel(false);
}

async function loadTours() {
    tourList.innerHTML = '<p class="state">Loading tours…</p>';
    try {
        const response = await fetch('/api/tours');
        if (!response.ok) {
            throw new Error('Unable to fetch tours.');
        }
        const tours = await response.json();
        renderTours(tours);
    } catch (error) {
        renderTours(demoTours);
        const notice = document.createElement('p');
        notice.className = 'state error';
        notice.textContent = `${error.message} Showing demo data instead.`;
        tourList.prepend(notice);
    }
}

function renderTours(tours) {
    if (!tours.length) {
        tourList.innerHTML = '<p class="state">No tours available yet.</p>';
        return;
    }

    const template = document.querySelector('#tourCardTemplate');
    const fragment = document.createDocumentFragment();

    tours.forEach((tour) => {
        const node = template.content.firstElementChild.cloneNode(true);
        node.querySelector('.tour-title').textContent = tour.title;
        node.setAttribute('aria-label', `View details for ${tour.title}`);
        node.querySelector('.tour-location').textContent = tour.location || 'Location TBA';
        node.querySelector('.tour-description').textContent = tour.description || 'Stay tuned for more details.';

        node.querySelector('.tour-price').textContent = priceText(tour.price);
        node.querySelector('.tour-capacity').textContent = capacityText(tour.maxCapacity);
        node.querySelector('.tour-duration').textContent = durationText(tour.durationHours);

        const handleOpenDetail = () => openTourDetail(tour, node);
        node.addEventListener('click', handleOpenDetail);
        node.addEventListener('keydown', (event) => {
            if (event.key === 'Enter' || event.key === ' ') {
                event.preventDefault();
                handleOpenDetail();
            }
        });
        fragment.appendChild(node);
    });

    tourList.innerHTML = '';
    tourList.appendChild(fragment);
}

function renderHighlights(items = []) {
    detailHighlights.innerHTML = '';
    const list = items.length ? items : ['More highlight info coming soon.'];
    list.forEach((item) => {
        const li = document.createElement('li');
        li.textContent = item;
        detailHighlights.appendChild(li);
    });
}

function setChip(element, text) {
    const hasText = Boolean(text);
    element.textContent = text || '';
    element.classList.toggle('hidden', !hasText);
}

function openTourDetail(tour, triggerElement) {
    lastFocusedCard = triggerElement || null;
    detailTitle.textContent = tour.title;
    detailLocation.textContent = tour.location || 'Location TBA';
    detailDescription.textContent = tour.description || 'Stay tuned for more details.';
    detailPrice.textContent = priceText(tour.price);
    detailCapacity.textContent = capacityText(tour.maxCapacity);
    detailDuration.textContent = durationText(tour.durationHours);
    setChip(detailActivity, tour.activityLevel ? `${tour.activityLevel} intensity` : 'All levels welcome');
    setChip(detailGear, tour.whatToBring ? `Bring ${tour.whatToBring}` : 'Gear provided');
    renderHighlights(Array.isArray(tour.highlights) ? tour.highlights : []);
    detailTip.textContent = tour.guideTip || 'Message the operator for tailored advice.';

    tourDetailOverlay.classList.remove('hidden');
    document.body.classList.add('modal-open');
    tourDetailClose.focus();
}

function closeTourDetail() {
    tourDetailOverlay.classList.add('hidden');
    document.body.classList.remove('modal-open');
    if (lastFocusedCard) {
        lastFocusedCard.focus();
    }
}

async function handleLogin(event) {
    event.preventDefault();
    loginStatus.textContent = 'Signing in…';
    loginStatus.className = 'status';

    const formData = new FormData(loginForm);
    const payload = {
        email: formData.get('email'),
        password: formData.get('password'),
    };

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload),
        });

        if (!response.ok) {
            throw new Error('Login failed. Double-check the demo credentials.');
        }

        const authenticatedUser = await response.json();
        currentUser = persistOperatorSession(authenticatedUser, payload.password);
        const isOperator = currentUser.role === 'OPERATOR';
        loginStatus.textContent = isOperator
            ? `Signed in as ${currentUser.name}. Redirecting to operator tools…`
            : `Signed in as ${currentUser.name}`;
        loginStatus.classList.add('success');
        updateLoginButton();
        setTimeout(() => {
            togglePanel(false);
            if (isOperator) {
                window.location.href = '/operator.html';
            }
        }, isOperator ? 800 : 900);
    } catch (error) {
        loginStatus.textContent = error.message;
        loginStatus.classList.add('error');
        currentUser = null;
        updateLoginButton();
        localStorage.removeItem(OPERATOR_SESSION_KEY);
    }
}

loginForm.addEventListener('submit', handleLogin);
if (logoutButton) {
    logoutButton.addEventListener('click', handleLogout);
}
if (operatorConsoleButton) {
    operatorConsoleButton.addEventListener('click', () => {
        window.location.href = '/operator.html';
    });
}
if (operatorHeaderLogout) {
    operatorHeaderLogout.addEventListener('click', () => {
        handleLogout();
        window.location.hash = 'allTours';
    });
}
tourDetailClose.addEventListener('click', closeTourDetail);
tourDetailOverlay.addEventListener('click', (event) => {
    if (event.target === tourDetailOverlay) {
        closeTourDetail();
    }
});
document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && !tourDetailOverlay.classList.contains('hidden')) {
        closeTourDetail();
    }
});
restoreOperatorSession();
loadTours();
