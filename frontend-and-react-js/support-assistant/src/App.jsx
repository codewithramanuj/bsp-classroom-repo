import { useState } from 'react'
import heroImg from './assets/hero.png'
import reactLogo from './assets/react.svg'
import viteLogo from './assets/vite.svg'
import './App.css'
import Navbar from './components/Navbar'
import Footer from './components/Footer'
import TicketsPage from './pages/TicketsPage'
import Header from './components/Header'
import { Navigate, Route, Routes } from 'react-router'
import LoginPage from './pages/LoginPage'
import NotFoundPage from './pages/NotFoundPage'

function App() {



  return (
    <>

    {/* <Header location={'Delhi'}/> */}

     <div className="app">
      <Navbar />
      <main className="content">
        <Routes>
          <Route path="/" element={<Navigate to="/tickets" replace />} />
          {/* "/*" lets the Routes inside TicketsPage handle /tickets/new and so on */}
          <Route path="/tickets/*" element={<TicketsPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </main>
      <Footer />
    </div>
     
    </>

  )
}

export default App
