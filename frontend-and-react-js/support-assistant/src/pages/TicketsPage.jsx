import React, { useEffect, useState } from 'react'
import TicketList from '../components/TicketList'
import TicketForm from '../components/TicketForm'
import NotFoundPage from './NotFoundPage'
import { Route, Routes, useNavigate } from 'react-router'

function TicketsPage() {

  const API_URL = 'http://localhost:8080/api/v1/tickets'
  const navigate = useNavigate()

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
      setError('')
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
    await loadTickets()
    navigate('/tickets') // back to the list after saving
  }


  async function deleteTicket(id) {
    const response = await fetch(`${API_URL}/${id}`, {
      method: 'DELETE'
    })
    if (response.status != 204) {
      throw new Error('The server answered ' + response.status)
    }
  }

  // The function given to useEffect must not be async: React calls what it
  // returns when the page is left, and a Promise cannot be called.
  useEffect(() => {
    loadTickets()
  }, [])

  function handleDelete(id) {
    if (confirm(`Delete Ticket wiyh ID : ${id}?`)) {
      deleteTicket(id)
      loadTickets()
    }
  }

  // The list shows its own loading and error messages, so the other
  // routes (such as the form) still work when the tickets cannot be loaded.
  let listView = <TicketList tickets={tickets} onDelete={handleDelete} />
  if (loading) listView = <p>Loading tickets...</p>
  if (error) listView = <p>{error}</p>

  return (
    <section>
      <Routes>
        {/* these paths are relative to /tickets/* in App.jsx */}
        <Route index element={listView} />
        <Route path="new" element={<TicketForm handleCreate={handleCreate} />} />
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </section>
  )
}

export default TicketsPage