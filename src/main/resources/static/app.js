const tourList = document.querySelector('#tourList');
const loginToggle = document.querySelector('#loginToggle');
const loginPanel = document.querySelector('#loginPanel');
const loginForm = document.querySelector('#loginForm');
const loginStatus = document.querySelector('#loginStatus');
const loginEmail = document.querySelector('#loginEmail');
const detailOverlay = document.querySelector('#tourDetail');
const detailTitle = document.querySelector('#detailTitle');
const detailLocation = document.querySelector('#detailLocation');
const detailDescription = document.querySelector('#detailDescription');
const detailOperator = document.querySelector('#detailOperator');
const detailAvailability = document.querySelector('#detailAvailability');
const detailDuration = document.querySelector('#detailDuration');
const detailCapacity = document.querySelector('#detailCapacity');
const detailPrice = document.querySelector('#detailPrice');
const detailClose = document.querySelector('#detailClose');
const bookingForm = document.querySelector('#bookingForm');
const bookingPeople = document.querySelector('#bookingPeople');
const bookingDate = document.querySelector('#bookingDate');
const bookingTotal = document.querySelector('#bookingTotal');
const bookingStatus = document.querySelector('#bookingStatus');
const bookingHelper = document.querySelector('#bookingHelper');
const bookingSubmit = document.querySelector('#bookingSubmit');
let currentUser = null;
let authCredentials = null;
let selectedTour = null;

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
    updateBookingAvailability();
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
        node.querySelector('.tour-title').textContent = tour.title || 'Untitled tour';
        node.querySelector('.tour-location').textContent = tour.location || 'Location TBA';
        node.querySelector('.tour-description').textContent = tour.description || 'Stay tuned for more details.';

        const priceValue = Number(tour.price);
        node.querySelector('.tour-price').textContent = Number.isFinite(priceValue)
            ? priceFormatter.format(priceValue)
            : tour.price;
        node.querySelector('.tour-capacity').textContent = tour.maxCapacity ? `${tour.maxCapacity} guests` : 'Flexible';
        node.querySelector('.tour-duration').textContent = tour.durationHours ? `${tour.durationHours} hrs` : 'Custom';

        const detailButton = node.querySelector('.tour-button');
        detailButton.addEventListener('click', () => openTourDetail(tour));
        detailButton.setAttribute('aria-label', `View details for ${tour.title}`);
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
        authCredentials = {
            email: payload.email,
            password: payload.password,
        };
        loginStatus.textContent = `Signed in as ${currentUser.name}`;
        loginStatus.classList.add('success');
        updateLoginButton();
        setTimeout(() => togglePanel(false), 900);
    } catch (error) {
        loginStatus.textContent = error.message;
        loginStatus.classList.add('error');
        currentUser = null;
        authCredentials = null;
        updateLoginButton();
    }
}

loginForm.addEventListener('submit', handleLogin);
loadTours();

function openTourDetail(tour) {
    if (!detailOverlay) {
        return;
    }
    selectedTour = tour;
    detailTitle.textContent = tour.title || 'Untitled tour';
    detailLocation.textContent = tour.location || 'Location TBA';
    detailDescription.textContent = tour.description || 'Stay tuned for more details.';
    detailOperator.textContent = tour.operatorName ? `${tour.operatorName}` : 'Local guide';
    detailAvailability.textContent = formatDate(tour.availableFrom);
    detailDuration.textContent = tour.durationHours ? `${tour.durationHours} hrs` : 'Custom itinerary';
    detailCapacity.textContent = tour.maxCapacity ? `${tour.maxCapacity} guests` : 'Flexible group size';
    detailPrice.textContent = formatPrice(tour.price);
    prepareBookingForm(tour);
    setBookingStatus('');
    updateBookingAvailability();
    detailOverlay.classList.remove('hidden');
}

function closeTourDetail() {
    if (!detailOverlay) {
        return;
    }
    detailOverlay.classList.add('hidden');
    selectedTour = null;
    updateBookingAvailability();
}

function formatDate(value) {
    if (!value) {
        return 'Flexible start dates';
    }
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
        return 'Flexible start dates';
    }
    return date.toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' });
}

function formatPrice(value) {
    const numberValue = Number(value);
    return Number.isFinite(numberValue) ? priceFormatter.format(numberValue) : value || 'Contact for pricing';
}

function prepareBookingForm(tour) {
    if (!bookingForm) {
        return;
    }
    if (bookingPeople) {
        const maxGuests = tour.maxCapacity && Number.isFinite(Number(tour.maxCapacity)) ? Number(tour.maxCapacity) : '';
        bookingPeople.value = '1';
        bookingPeople.min = '1';
        bookingPeople.max = maxGuests || '';
    }

    if (bookingDate) {
        const defaultDate = pickDefaultBookingDate(tour.availableFrom);
        bookingDate.min = defaultDate;
        bookingDate.value = defaultDate;
    }
    updateBookingTotal();
}

function pickDefaultBookingDate(availableFrom) {
    const now = new Date();
    const parsed = availableFrom ? new Date(availableFrom) : null;
    const reference = parsed && !Number.isNaN(parsed.getTime()) && parsed > now ? parsed : now;
    const local = new Date(reference.getTime());
    local.setMinutes(local.getMinutes() - local.getTimezoneOffset());
    return local.toISOString().slice(0, 16);
}

function updateBookingTotal() {
    if (!bookingTotal) {
        return;
    }
    if (!selectedTour) {
        bookingTotal.textContent = '$0.00';
        return;
    }
    const priceValue = Number(selectedTour.price);
    const guests = bookingPeople ? Number(bookingPeople.value) || 0 : 0;
    const total = Number.isFinite(priceValue) ? priceValue * guests : NaN;
    bookingTotal.textContent = Number.isFinite(total) ? priceFormatter.format(total) : 'Contact us';
}

function updateBookingAvailability() {
    if (!bookingForm) {
        return;
    }
    const canBook = Boolean(selectedTour && selectedTour.id);
    const isCustomer = currentUser?.role === 'CUSTOMER';
    const shouldEnable = canBook && isCustomer;
    [bookingPeople, bookingDate, bookingSubmit].forEach((element) => {
        if (element) {
            element.disabled = !shouldEnable;
        }
    });

    if (!bookingHelper) {
        return;
    }

    if (!selectedTour) {
        bookingHelper.textContent = 'Select a tour to see the full details and booking form.';
    } else if (!canBook) {
        bookingHelper.textContent = 'Demo tours cannot be booked. Try again once the API is available.';
    } else if (!currentUser) {
        bookingHelper.textContent = 'Log in with the traveller account to book this tour.';
    } else if (!isCustomer) {
        bookingHelper.textContent = 'Only customer accounts can request bookings.';
    } else {
        bookingHelper.textContent = `You are booking as ${currentUser.name}.`;
    }
}

function setBookingStatus(message, state) {
    if (!bookingStatus) {
        return;
    }
    bookingStatus.textContent = message;
    bookingStatus.className = 'status';
    if (state) {
        bookingStatus.classList.add(state);
    }
}

function formatBookingDate(value) {
    if (!value) {
        return '';
    }
    return value.length === 16 ? `${value}:00` : value;
}

async function handleBooking(event) {
    event.preventDefault();
    if (!bookingPeople || !bookingDate || !bookingSubmit) {
        return;
    }

    if (!selectedTour) {
        setBookingStatus('Select a tour to book.', 'error');
        return;
    }

    if (!selectedTour.id) {
        setBookingStatus('This tour is demo-only and cannot be booked.', 'error');
        return;
    }

    if (!currentUser) {
        setBookingStatus('Log in as a customer to place a booking.', 'error');
        return;
    }

    if (currentUser.role !== 'CUSTOMER') {
        setBookingStatus('Bookings can only be made with a customer account.', 'error');
        return;
    }

    if (!authCredentials) {
        setBookingStatus('Please log in again to continue.', 'error');
        return;
    }

    const guests = Number(bookingPeople.value);
    if (!Number.isFinite(guests) || guests <= 0) {
        setBookingStatus('Enter how many guests are travelling.', 'error');
        return;
    }

    const bookingDateValue = bookingDate.value;
    if (!bookingDateValue) {
        setBookingStatus('Choose when you would like to start.', 'error');
        return;
    }

    const total = Number(selectedTour.price) * guests;
    const payload = {
        tourId: selectedTour.id,
        numberOfPeople: guests,
        totalPrice: Number.isFinite(total) ? total : selectedTour.price,
        bookingDate: formatBookingDate(bookingDateValue),
        status: 'PENDING',
    };

    bookingSubmit.disabled = true;
    setBookingStatus('Submitting booking request…');

    try {
        const response = await fetch('/api/bookings', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                Authorization: `Basic ${btoa(`${authCredentials.email}:${authCredentials.password}`)}`,
            },
            body: JSON.stringify(payload),
        });

        if (!response.ok) {
            throw new Error('Unable to book this tour. Please try again.');
        }

        setBookingStatus('Booking request sent! We will confirm shortly.', 'success');
    } catch (error) {
        setBookingStatus(error.message, 'error');
    } finally {
        bookingSubmit.disabled = false;
    }
}

if (bookingForm) {
    bookingForm.addEventListener('submit', handleBooking);
    if (bookingPeople) {
        bookingPeople.addEventListener('input', () => {
            updateBookingTotal();
            setBookingStatus('');
        });
    }
    if (bookingDate) {
        bookingDate.addEventListener('input', () => setBookingStatus(''));
    }
}

if (detailClose) {
    detailClose.addEventListener('click', closeTourDetail);
}

if (detailOverlay) {
    detailOverlay.addEventListener('click', (event) => {
        if (event.target === detailOverlay) {
            closeTourDetail();
        }
    });
}

document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape' && detailOverlay && !detailOverlay.classList.contains('hidden')) {
        closeTourDetail();
    }
});

updateBookingAvailability();
