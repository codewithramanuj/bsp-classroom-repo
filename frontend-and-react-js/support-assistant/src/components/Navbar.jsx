import React from 'react'
import { NavLink } from 'react-router'

function Navbar() {
  return (
   <nav className="navbar">
      <strong>Support Assistant</strong>
      <div>
        {/* "end" keeps this link from staying active on /tickets/new */}
        <NavLink to="/tickets" end>Tickets</NavLink>
        <NavLink to="/tickets/new">New Ticket</NavLink>
        <NavLink to="/login">Login</NavLink>
      </div>
    </nav>
  )
}

export default Navbar