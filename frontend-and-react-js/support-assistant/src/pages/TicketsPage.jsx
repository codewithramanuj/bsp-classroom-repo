import React, { useEffect, useState } from 'react'
import TicketList from '../components/TicketList'
import TicketForm from '../components/TicketForm'
import { BrowserRouter, Route, Routes } from 'react-router'

function TicketsPage() {

  const API_URL = 'http://localhost:8080/api/v1/tickets'

  const [tickets, setTickets] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  async function loadTickets() {
    try {
      const response = await fetch(API_URL)
      if (!response.ok) {
        throw new Error(`The server answered ` + response.status)
      }
      const data = await response.json()
      setTickets(data)
    } catch (err) {
      setError('Could not load the tickets. ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  async function handleCreate(newTicket) {
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(newTicket),
    })
    if (!response.ok) {
      const problem = await response.json()
      throw new Error(problem.message || 'The server answered ' + response.status)
    }
    loadTickets()
  }


  async function deleteTicket(id) {
    const response = await fetch(`${API_URL}/${id}`, {
      method: 'DELETE'
    })
    if (response.status != 204) {
      throw new Error('The server answered ' + response.status)
    }
  }

  useEffect(async () => {
    function load() {
      loadTickets()
    }
    load()
  }
    , [])

  function handleDelete(id) {
    if (confirm(`Delete Ticket wiyh ID : ${id}?`)) {
      deleteTicket(id)
      loadTickets()
    }
  }

  if (loading) return <p>Loading tickets...</p>
  if (error) return <p>{error}</p>

  return (
    <>
    <section>
     <Routes>
       <Route path="" element={<TicketList tickets={tickets} onDelete={handleDelete} />}/>
        <Route path="/tickets/new" element={<TicketForm handleCreate={handleCreate} />}/>
      </Routes>
    </section>
      {/* <section>
        <TicketForm handleCreate={handleCreate} />
      </section>
      <section>
        <h2>My Tickets</h2>
        <p>{tickets.length} tickets</p>
        <TicketList tickets={tickets} onDelete={handleDelete} />
      </section> */}
    </>
  )


}

export default TicketsPage