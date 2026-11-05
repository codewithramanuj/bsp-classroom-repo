import React from 'react'

function TicketCard({ticket, deleteTicket}) {
{}

  return (
     <div className="card">
      <h3>Ticket {ticket.id}</h3>
      <p>{ticket.title}</p>
      <button onClick={()=>deleteTicket(ticket.id)}>Delete</button>
    </div>
  )
}

export default TicketCard