// Configuration
const API_BASE_URL = 'http://82.123.126.71:8080';
let authToken = localStorage.getItem('authToken');

// DOM Elements
const loginForm = document.getElementById('login-form');
const partnersSection = document.getElementById('partners-section');
const announcementsSection = document.getElementById('announcements-section');
const partnersList = document.getElementById('partners-list');
const announcementsList = document.getElementById('announcements-list');

// Check authentication status on load
document.addEventListener('DOMContentLoaded', () => {
    if (authToken) {
        showAuthenticatedContent();
    }
});

// Login function
async function login() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch(`${API_BASE_URL}/auth/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        if (!response.ok) {
            throw new Error('Login failed');
        }

        const data = await response.json();
        authToken = data.token;
        localStorage.setItem('authToken', authToken);
        showAuthenticatedContent();
    } catch (error) {
        alert('Login failed. Please try again.');
        console.error('Login error:', error);
    }
}

// Show authenticated content
async function showAuthenticatedContent() {
    loginForm.classList.add('hidden');
    partnersSection.classList.remove('hidden');
    announcementsSection.classList.remove('hidden');
    
    await Promise.all([
        fetchPartners(),
        fetchAnnouncements()
    ]);
}

// Fetch partners
async function fetchPartners() {
    try {
        const response = await fetch(`${API_BASE_URL}/partners`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch partners');
        }

        const partners = await response.json();
        displayPartners(partners);
    } catch (error) {
        console.error('Error fetching partners:', error);
        partnersList.innerHTML = '<p class="error">Failed to load partners</p>';
    }
}

// Display partners
function displayPartners(partners) {
    partnersList.innerHTML = partners.map(partner => `
        <div class="card">
            <h3>${partner.name}</h3>
            <p>${partner.description}</p>
            <a href="${partner.websiteUrl}" target="_blank">Visit Website</a>
        </div>
    `).join('');
}

// Fetch announcements
async function fetchAnnouncements() {
    try {
        const response = await fetch(`${API_BASE_URL}/announcements`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch announcements');
        }

        const announcements = await response.json();
        displayAnnouncements(announcements);
    } catch (error) {
        console.error('Error fetching announcements:', error);
        announcementsList.innerHTML = '<p class="error">Failed to load announcements</p>';
    }
}

// Display announcements
function displayAnnouncements(announcements) {
    announcementsList.innerHTML = announcements.map(announcement => `
        <div class="card">
            <h3>${announcement.title}</h3>
            <p>${announcement.content}</p>
            <small>Posted by ${announcement.author} on ${new Date(announcement.createdAt).toLocaleDateString()}</small>
        </div>
    `).join('');
}

// Service Worker Registration
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('/service-worker.js')
            .then(registration => {
                console.log('ServiceWorker registration successful');
            })
            .catch(err => {
                console.log('ServiceWorker registration failed: ', err);
            });
    });
} 