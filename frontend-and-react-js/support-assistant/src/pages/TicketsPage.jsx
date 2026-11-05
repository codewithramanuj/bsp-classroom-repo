import React, { useEffect, useState } from 'react'
import TicketList from '../components/TicketList'

function TicketsPage() {

  const API_URL = 'http://localhost:8080/api/v1/tickets'

  const [tickets, setTickets] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  async function loadTickets() {
    try {
      const response = await fetch(API_URL)
      if (!response.ok) {
        throw new Error('The server answered ' + response.status)
      }
      const data = await response.json()
      setTickets(data)
    } catch (err) {
      setError('Could not load the tickets. ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  async function deleteTicket(id) {
    const response = await fetch(API_URL + '/' + id, {
      method: 'DELETE'
    })
     if (!response.ok) {
        throw new Error('The server answered ' + response.status)
      }
      loadTickets()
  }

  useEffect(loadTickets, [])

  function handleDelete(id) {
    deleteTicket(id)
  }

  if (loading) return <p>Loading tickets...</p>
  if (error) return <p>{error}</p>

  return (
    <section>
      <h2>My Tickets</h2>
      <p>{tickets.length} tickets</p>
      <TicketList tickets={tickets} onDelete={handleDelete}/>
    </section>
  )


}

export default TicketsPage