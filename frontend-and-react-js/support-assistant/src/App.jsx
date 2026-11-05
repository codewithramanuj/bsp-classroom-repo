import { useState } from 'react'
import heroImg from './assets/hero.png'
import reactLogo from './assets/react.svg'
import viteLogo from './assets/vite.svg'
import './App.css'
import Navbar from './components/Navbar'
import Footer from './components/Footer'
import TicketsPage from './pages/TicketsPage'
import Header from './components/Header'

function App() {

  

  return (
    <>

    {/* <Header location={'Delhi'}/> */}

     <div className="app">
      <Navbar />
      <main className="content">
        <TicketsPage />
      </main>
      <Footer />
    </div>
     
    </>

  )
}

export default App
