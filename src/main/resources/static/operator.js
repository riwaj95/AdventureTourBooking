const SESSION_KEY = 'operatorSession';
const createForm = document.querySelector('#createTourForm');
const operatorTourList = document.querySelector('#operatorTourList');
const operatorStatus = document.querySelector('#operatorStatus');
const createStatus = document.querySelector('#createStatus');
const greeting = document.querySelector('#operatorGreeting');
const logoutButton = document.querySelector('#operatorLogout');

const priceFormatter = new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    maximumFractionDigits: 2,
});

const session = loadSession();

function loadSession() {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) {
        redirectHome();
        return null;
    }

    try {
        const parsed = JSON.parse(raw);
        if (!parsed?.authHeader) {
            throw new Error('Missing credentials');
        }
        return parsed;
    } catch (error) {
        console.error('Unable to load session', error);
        localStorage.removeItem(SESSION_KEY);
        redirectHome();
        return null;
    }
}

function redirectHome() {
    window.location.href = '/#allTours';
}

function authHeaders() {
    return {
        Authorization: session.authHeader,
    };
}

function jsonHeaders() {
    return {
        ...authHeaders(),
        'Content-Type': 'application/json',
    };
}

async function requestLogout(headers = {}) {
    try {
        await fetch('/api/auth/logout', {
            method: 'POST',
            headers,
            credentials: 'same-origin',
        });
    } catch (error) {
        console.warn('Logout request failed', error);
    }
}

function formatPrice(value) {
    if (value === null || value === undefined) {
        return 'Custom pricing';
    }
    const numeric = Number(value);
    if (Number.isNaN(numeric)) {
        return value;
    }
    return priceFormatter.format(numeric);
}

function toDateInputValue(value) {
    if (!value) {
        return '';
    }
    const date = new Date(value);
    const tzOffset = date.getTimezoneOffset();
    const local = new Date(date.getTime() - tzOffset * 60000);
    return local.toISOString().slice(0, 16);
}

function buildPayloadFromForm(formData) {
    const availableFromValue = formData.get('availableFrom');
    return {
        title: formData.get('title')?.trim(),
        description: formData.get('description')?.trim() || null,
        price: Number(formData.get('price')),
        location: formData.get('location')?.trim(),
        maxCapacity: Number(formData.get('maxCapacity')),
        availableFrom: availableFromValue ? new Date(availableFromValue).toISOString() : null,
        durationHour: Number(formData.get('durationHour')),
    };
}

function showStatus(element, message, type = 'info') {
    if (!element) return;
    element.textContent = message;
    element.className = 'status';
    if (type === 'success') {
        element.classList.add('success');
    } else if (type === 'error') {
        element.classList.add('error');
    }
}

async function loadTours() {
    showStatus(operatorStatus, 'Loading your tours…');
    try {
        const response = await fetch(`/api/tours/operators/${session.id}`, {
            headers: authHeaders(),
        });
        if (response.status === 401 || response.status === 403) {
            throw new Error('unauthorized');
        }
        if (!response.ok) {
            throw new Error('Unable to load tours.');
        }
        const tours = await response.json();
        renderTours(tours);
        if (!tours.length) {
            showStatus(operatorStatus, 'No tours yet. Use the form above to add your first adventure.', 'info');
        } else {
            showStatus(operatorStatus, `You have ${tours.length} tour${tours.length === 1 ? '' : 's'}.`, 'success');
        }
    } catch (error) {
        if (error.message === 'unauthorized') {
            localStorage.removeItem(SESSION_KEY);
            redirectHome();
            return;
        }
        showStatus(operatorStatus, error.message, 'error');
    }
}

function renderTours(tours = []) {
    operatorTourList.innerHTML = '';
    if (!tours.length) {
        return;
    }
    tours.forEach((tour) => {
        const card = document.createElement('article');
        card.className = 'operator-tour-card';

        const heading = document.createElement('h3');
        heading.textContent = tour.title || 'Untitled tour';
        card.appendChild(heading);

        const meta = document.createElement('p');
        meta.className = 'helper-text';
        meta.textContent = `${tour.location || 'Location TBA'} · ${formatPrice(tour.price)} · Capacity ${tour.maxCapacity || 'Flexible'}`;
        card.appendChild(meta);

        const form = document.createElement('form');
        form.className = 'tour-form';

        form.appendChild(createLabeledInput('Title', {
            name: 'title',
            required: true,
            value: tour.title || '',
        }));
        form.appendChild(createLabeledTextarea('Description', {
            name: 'description',
            value: tour.description || '',
        }));

        const gridOne = document.createElement('div');
        gridOne.className = 'form-grid';
        gridOne.appendChild(
            createLabeledInput('Price (USD)', {
                name: 'price',
                type: 'number',
                step: '0.01',
                min: '0',
                required: true,
                value: tour.price ?? '',
            })
        );
        gridOne.appendChild(
            createLabeledInput('Max capacity', {
                name: 'maxCapacity',
                type: 'number',
                min: '1',
                required: true,
                value: tour.maxCapacity ?? '',
            })
        );
        form.appendChild(gridOne);

        const gridTwo = document.createElement('div');
        gridTwo.className = 'form-grid';
        gridTwo.appendChild(
            createLabeledInput('Location', {
                name: 'location',
                required: true,
                value: tour.location || '',
            })
        );
        gridTwo.appendChild(
            createLabeledInput('Duration (hours)', {
                name: 'durationHour',
                type: 'number',
                min: '1',
                required: true,
                value: tour.durationHours ?? '',
            })
        );
        form.appendChild(gridTwo);

        form.appendChild(
            createLabeledInput('Available from', {
                name: 'availableFrom',
                type: 'datetime-local',
                value: toDateInputValue(tour.availableFrom),
            })
        );

        const buttonRow = document.createElement('div');
        buttonRow.className = 'button-row';
        const saveButton = document.createElement('button');
        saveButton.type = 'submit';
        saveButton.className = 'primary';
        saveButton.textContent = 'Update tour';
        const deleteButton = document.createElement('button');
        deleteButton.type = 'button';
        deleteButton.className = 'secondary danger';
        deleteButton.textContent = 'Delete';
        buttonRow.append(saveButton, deleteButton);
        form.appendChild(buttonRow);

        const statusLine = document.createElement('p');
        statusLine.className = 'status';
        form.appendChild(statusLine);

        form.addEventListener('submit', (event) => {
            event.preventDefault();
            handleUpdateTour(tour.id, new FormData(form), statusLine);
        });

        deleteButton.addEventListener('click', () => {
            const confirmed = confirm(`Delete ${tour.title || 'this tour'}? This cannot be undone.`);
            if (confirmed) {
                handleDeleteTour(tour.id);
            }
        });

        card.appendChild(form);
        operatorTourList.appendChild(card);
    });
}

function createLabeledInput(labelText, options) {
    const label = document.createElement('label');
    const textNode = document.createElement('span');
    textNode.textContent = labelText;
    label.appendChild(textNode);
    const input = document.createElement('input');
    input.name = options.name;
    input.type = options.type || 'text';
    if (options.value !== undefined && options.value !== null) {
        input.value = options.value;
    }
    if (options.required) {
        input.required = true;
    }
    if (options.step) {
        input.step = options.step;
    }
    if (options.min) {
        input.min = options.min;
    }
    label.appendChild(input);
    return label;
}

function createLabeledTextarea(labelText, options) {
    const label = document.createElement('label');
    const textNode = document.createElement('span');
    textNode.textContent = labelText;
    label.appendChild(textNode);
    const textarea = document.createElement('textarea');
    textarea.name = options.name;
    textarea.rows = options.rows || 3;
    textarea.value = options.value || '';
    label.appendChild(textarea);
    return label;
}

async function handleCreateTour(event) {
    event.preventDefault();
    showStatus(createStatus, 'Creating tour…');
    try {
        const payload = buildPayloadFromForm(new FormData(createForm));
        const response = await fetch('/api/tours', {
            method: 'POST',
            headers: jsonHeaders(),
            body: JSON.stringify(payload),
        });
        if (response.status === 401 || response.status === 403) {
            throw new Error('unauthorized');
        }
        if (!response.ok) {
            throw new Error('Unable to create tour. Ensure all fields are valid.');
        }
        showStatus(createStatus, 'Tour created successfully.', 'success');
        createForm.reset();
        await loadTours();
    } catch (error) {
        if (error.message === 'unauthorized') {
            localStorage.removeItem(SESSION_KEY);
            redirectHome();
            return;
        }
        showStatus(createStatus, error.message, 'error');
    }
}

async function handleUpdateTour(id, formData, statusElement) {
    showStatus(statusElement, 'Saving changes…');
    try {
        const payload = buildPayloadFromForm(formData);
        const response = await fetch(`/api/tours/${id}`, {
            method: 'PUT',
            headers: jsonHeaders(),
            body: JSON.stringify(payload),
        });
        if (response.status === 401 || response.status === 403) {
            throw new Error('unauthorized');
        }
        if (!response.ok) {
            throw new Error('Unable to update tour.');
        }
        showStatus(statusElement, 'Tour updated.', 'success');
        await loadTours();
    } catch (error) {
        if (error.message === 'unauthorized') {
            localStorage.removeItem(SESSION_KEY);
            redirectHome();
            return;
        }
        showStatus(statusElement, error.message, 'error');
    }
}

async function handleDeleteTour(id) {
    showStatus(operatorStatus, 'Deleting tour…');
    try {
        const response = await fetch(`/api/tours/${id}`, {
            method: 'DELETE',
            headers: authHeaders(),
        });
        if (response.status === 401 || response.status === 403) {
            throw new Error('unauthorized');
        }
        if (!response.ok) {
            throw new Error('Unable to delete tour.');
        }
        showStatus(operatorStatus, 'Tour deleted.', 'success');
        await loadTours();
    } catch (error) {
        if (error.message === 'unauthorized') {
            localStorage.removeItem(SESSION_KEY);
            redirectHome();
            return;
        }
        showStatus(operatorStatus, error.message, 'error');
    }
}

if (session) {
    greeting.textContent = `Signed in as ${session.name}`;
    logoutButton.addEventListener('click', async () => {
        await requestLogout(authHeaders());
        localStorage.removeItem(SESSION_KEY);
        redirectHome();
    });
    createForm.addEventListener('submit', handleCreateTour);
    loadTours();
}
