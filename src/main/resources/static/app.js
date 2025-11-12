const STORAGE_KEYS = {
    USER: "atb:user",
    TOURS: "atb:tours",
    BOOKINGS: "atb:bookings",
};

const createId = () => (typeof crypto !== "undefined" && crypto.randomUUID ? crypto.randomUUID() : `${Date.now()}-${Math.random().toString(16).slice(2)}`);

const defaultTours = [
    {
        id: createId(),
        name: "Sunset Kayak Adventure",
        location: "Vancouver, Canada",
        price: 120,
        capacity: 12,
        description: "Paddle along the coastline at golden hour with a certified guide and warm beverages included.",
        createdAt: Date.now(),
    },
    {
        id: createId(),
        name: "Sahara Stargazing Trek",
        location: "Merzouga, Morocco",
        price: 240,
        capacity: 8,
        description: "Ride across the dunes on camelback before camping under the clearest night skies imaginable.",
        createdAt: Date.now(),
    },
    {
        id: createId(),
        name: "Patagonia Glacier Hike",
        location: "El Calafate, Argentina",
        price: 320,
        capacity: 10,
        description: "Strap on crampons for a guided exploration of the Perito Moreno glacier and its ice caves.",
        createdAt: Date.now(),
    },
];

function loadState(key, fallback) {
    try {
        const raw = localStorage.getItem(key);
        return raw ? JSON.parse(raw) : fallback;
    } catch (error) {
        console.error("Failed to parse state", error);
        return fallback;
    }
}

function saveState(key, value) {
    localStorage.setItem(key, JSON.stringify(value));
}

const state = {
    user: loadState(STORAGE_KEYS.USER, null),
    tours: loadState(STORAGE_KEYS.TOURS, defaultTours),
    bookings: loadState(STORAGE_KEYS.BOOKINGS, []),
};

const elements = {
    loginView: document.querySelector("#loginView"),
    travellerView: document.querySelector("#travellerView"),
    operatorView: document.querySelector("#operatorView"),
    loginForm: document.querySelector("#loginForm"),
    createTourForm: document.querySelector("#createTourForm"),
    tourList: document.querySelector("#tourList"),
    operatorTours: document.querySelector("#operatorTours"),
    bookingTable: document.querySelector("#bookingTable"),
    bookingEmptyState: document.querySelector("#bookingEmptyState"),
    bookingTableBody: document.querySelector("#bookingTable tbody"),
    userBadge: document.querySelector("#userBadge"),
    logoutButton: document.querySelector("#logoutButton"),
};

function setView(role) {
    elements.loginView.classList.toggle("hidden", Boolean(role));
    elements.travellerView.classList.toggle("hidden", role !== "traveller");
    elements.operatorView.classList.toggle("hidden", role !== "operator");
    elements.logoutButton.classList.toggle("hidden", !role);
    elements.userBadge.classList.toggle("hidden", !role);
}

function renderUserBadge() {
    if (!state.user) return;
    elements.userBadge.textContent = `${state.user.name} · ${state.user.role}`;
}

function sortTours(tours) {
    return [...tours].sort((a, b) => b.createdAt - a.createdAt);
}

function renderTours() {
    elements.tourList.innerHTML = "";
    const template = document.querySelector("#tourCardTemplate");

    sortTours(state.tours).forEach((tour) => {
        const card = template.content.firstElementChild.cloneNode(true);
        card.querySelector(".tour-card__title").textContent = tour.name;
        card.querySelector(".tour-card__location").textContent = tour.location;
        card.querySelector(".tour-card__description").textContent = tour.description || "";
        card.querySelector(".price").textContent = `$${tour.price} / person`;
        card.querySelector(".capacity").textContent = `${tour.capacity} spots`;

        const bookButton = card.querySelector("button.primary");
        const bookingForm = card.querySelector(".booking-form");
        const cancelButton = card.querySelector(".booking-form .cancel");

        bookButton.addEventListener("click", () => {
            bookingForm.classList.toggle("hidden", false);
            bookButton.classList.add("hidden");
        });

        cancelButton.addEventListener("click", () => {
            bookingForm.reset();
            bookingForm.classList.add("hidden");
            bookButton.classList.remove("hidden");
        });

        bookingForm.addEventListener("submit", (event) => {
            event.preventDefault();
            const formData = new FormData(bookingForm);
            const booking = {
                id: createId(),
                travellerName: state.user.name,
                tourId: tour.id,
                date: formData.get("date"),
                groupSize: Number(formData.get("groupSize")),
                status: "pending",
                createdAt: Date.now(),
            };

            state.bookings.push(booking);
            saveState(STORAGE_KEYS.BOOKINGS, state.bookings);
            renderBookings();

            bookingForm.reset();
            bookingForm.classList.add("hidden");
            bookButton.classList.remove("hidden");
        });

        elements.tourList.appendChild(card);
    });
}

function renderOperatorTours() {
    elements.operatorTours.innerHTML = "";
    sortTours(state.tours).forEach((tour) => {
        const item = document.createElement("li");
        item.innerHTML = `
            <strong>${tour.name}</strong>
            <span>${tour.location}</span>
            <span>$${tour.price} · ${tour.capacity} spots</span>
        `;
        elements.operatorTours.appendChild(item);
    });
}

function renderBookings() {
    const hasBookings = state.bookings.length > 0;
    elements.bookingEmptyState.classList.toggle("hidden", hasBookings);
    elements.bookingTable.classList.toggle("hidden", !hasBookings);

    if (!hasBookings) {
        elements.bookingTableBody.innerHTML = "";
        return;
    }

    elements.bookingTableBody.innerHTML = "";
    const tourMap = Object.fromEntries(state.tours.map((tour) => [tour.id, tour]));

    state.bookings
        .slice()
        .sort((a, b) => b.createdAt - a.createdAt)
        .forEach((booking) => {
            const row = document.createElement("tr");
            row.innerHTML = `
                <td>${booking.travellerName}</td>
                <td>${tourMap[booking.tourId]?.name ?? "Unknown"}</td>
                <td>${booking.date}</td>
                <td>${booking.groupSize}</td>
                <td><span class="badge" data-status="${booking.status}">${booking.status}</span></td>
                <td class="actions"></td>
            `;

            const actionsCell = row.querySelector(".actions");
            [
                { label: "Confirm", status: "confirmed" },
                { label: "Cancel", status: "cancelled" },
                { label: "Reset", status: "pending" },
            ].forEach(({ label, status }) => {
                const button = document.createElement("button");
                button.textContent = label;
                button.className = "ghost";
                button.addEventListener("click", () => updateBookingStatus(booking.id, status));
                actionsCell.appendChild(button);
            });

            elements.bookingTableBody.appendChild(row);
        });
}

function updateBookingStatus(id, status) {
    const booking = state.bookings.find((item) => item.id === id);
    if (!booking) return;
    booking.status = status;
    saveState(STORAGE_KEYS.BOOKINGS, state.bookings);
    renderBookings();
}

function handleLogin(event) {
    event.preventDefault();
    const formData = new FormData(elements.loginForm);
    const name = formData.get("username")?.trim();
    const role = formData.get("role");

    if (!name) {
        alert("Please enter your display name");
        return;
    }

    state.user = { name, role };
    saveState(STORAGE_KEYS.USER, state.user);

    setView(role);
    renderUserBadge();
    renderTours();
    renderOperatorTours();
    renderBookings();
}

function handleLogout() {
    state.user = null;
    saveState(STORAGE_KEYS.USER, state.user);
    setView(null);
}

function handleCreateTour(event) {
    event.preventDefault();
    const formData = new FormData(elements.createTourForm);
    const tour = {
        id: createId(),
        name: (formData.get("name") || "").trim(),
        location: (formData.get("location") || "").trim(),
        price: Number(formData.get("price")),
        capacity: Number(formData.get("capacity")),
        description: (formData.get("description") || "").trim(),
        createdAt: Date.now(),
    };

    if (!tour.name || !tour.location || !tour.price || !tour.capacity) {
        alert("Please fill in all required fields");
        return;
    }

    state.tours.push(tour);
    saveState(STORAGE_KEYS.TOURS, state.tours);
    elements.createTourForm.reset();
    renderTours();
    renderOperatorTours();
}

function hydrateFromStorage() {
    if (state.tours.length === 0) {
        state.tours = defaultTours;
        saveState(STORAGE_KEYS.TOURS, state.tours);
    }

    if (state.user) {
        setView(state.user.role);
        renderUserBadge();
    }

    if (state.user?.role === "traveller") {
        renderTours();
    }

    if (state.user?.role === "operator") {
        renderTours();
        renderOperatorTours();
        renderBookings();
    }
}

document.addEventListener("DOMContentLoaded", () => {
    hydrateFromStorage();
    elements.loginForm.addEventListener("submit", handleLogin);
    elements.logoutButton.addEventListener("click", handleLogout);
    elements.createTourForm.addEventListener("submit", handleCreateTour);
});
