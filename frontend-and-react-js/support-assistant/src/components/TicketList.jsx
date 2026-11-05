import React, { useState } from 'react'
import TicketCard from '../components/TicketCard';

function TicketList({tickets, onDelete}) {


  return (
   <div>
     {tickets.map((ticket) => (
              <TicketCard key={ticket.id} ticket={ticket} deleteTicket={onDelete} />
    ))}

    </div>
  )
}

export default TicketList