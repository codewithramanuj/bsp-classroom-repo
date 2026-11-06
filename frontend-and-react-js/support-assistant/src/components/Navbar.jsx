import React from 'react'
import { NavLink } from 'react-router'

function Navbar() {
  return (
   <nav className="navbar">
      <strong>Support Assistant</strong>
      <div>
        <NavLink to="/tickets">Tickets</NavLink>
        <NavLink to="/tickets/new">New Ticket</NavLink>
        <NavLink to="/login">Login</NavLink>
      </div>
    </nav>
  )
}

export default Navbar