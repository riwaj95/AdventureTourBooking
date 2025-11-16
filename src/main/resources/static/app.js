const tourList = document.querySelector('#tourList');
const loginToggle = document.querySelector('#loginToggle');
const loginPanel = document.querySelector('#loginPanel');
const loginForm = document.querySelector('#loginForm');
const loginStatus = document.querySelector('#loginStatus');
const loginEmail = document.querySelector('#loginEmail');
let currentUser = null;

const demoTours = [
    {
        title: 'Misty Mountain Hike',
        location: 'Aspen, USA',
        description: 'Start your morning above the clouds with a gentle hike to a hidden alpine lake.',
        price: 129,
        maxCapacity: 14,
        durationHours: 5,
    },
    {
        title: 'Rainforest River Kayak',
        location: 'Leticia, Colombia',
        description: 'Glide through lush mangroves while spotting parrots, sloths and river dolphins.',
        price: 189,
        maxCapacity: 10,
        durationHours: 4,
    },
    {
        title: 'Volcanic Sunset Jeep Ride',
        location: 'Santorini, Greece',
        description: 'Bounce across black-sand trails before sharing a picnic on the caldera rim.',
        price: 159,
        maxCapacity: 12,
        durationHours: 3,
    },
    {
        title: 'Nordic Fjord Cycling',
        location: 'Ålesund, Norway',
        description: 'Cycle quiet coastal roads and hop ferries between islands.',
        price: 210,
        maxCapacity: 8,
        durationHours: 6,
    },
    {
        title: 'Red Desert Stars',
        location: 'Merzouga, Morocco',
        description: 'An overnight camel caravan with astronomer-led stargazing in the quiet dunes.',
        price: 275,
        maxCapacity: 6,
        durationHours: 12,
    },
];

const priceFormatter = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 2,
});

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

function updateLoginButton() {
    loginToggle.textContent = currentUser ? `Hi, ${currentUser.name}` : 'Log in';
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
        node.querySelector('.tour-location').textContent = tour.location || 'Location TBA';
        node.querySelector('.tour-description').textContent = tour.description || 'Stay tuned for more details.';

        const priceValue = Number(tour.price);
        node.querySelector('.tour-price').textContent = Number.isFinite(priceValue)
            ? priceFormatter.format(priceValue)
            : tour.price;
        node.querySelector('.tour-capacity').textContent = tour.maxCapacity ? `${tour.maxCapacity} guests` : 'Flexible';
        node.querySelector('.tour-duration').textContent = tour.durationHours ? `${tour.durationHours} hrs` : 'Custom';
        fragment.appendChild(node);
    });

    tourList.innerHTML = '';
    tourList.appendChild(fragment);
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

        currentUser = await response.json();
        loginStatus.textContent = `Signed in as ${currentUser.name}`;
        loginStatus.classList.add('success');
        updateLoginButton();
        setTimeout(() => togglePanel(false), 900);
    } catch (error) {
        loginStatus.textContent = error.message;
        loginStatus.classList.add('error');
        currentUser = null;
        updateLoginButton();
    }
}

loginForm.addEventListener('submit', handleLogin);
loadTours();
