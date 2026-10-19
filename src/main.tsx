// Mounts the root FoodOS React application into the browser document.
// Initializes React and imports the global stylesheet for the frontend entry point.

import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
