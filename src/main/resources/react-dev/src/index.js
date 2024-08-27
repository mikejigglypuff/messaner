import React from 'react';
import ReactDOM from 'react-dom/client';
import './static/css/index.css';
import App from './static/js/App.js';
import logo from './static/media/logo.svg';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
