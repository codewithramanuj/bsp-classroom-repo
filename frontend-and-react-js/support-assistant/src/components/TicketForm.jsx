import React, { useState } from 'react'

function TicketForm({handleCreate}) {

    const [title, setTitle] = useState('')
    const [description, setDescription] = useState('')
    const [priority, setPriority] = useState('MEDIUM')

    function handleSubmit(event) {
        event.preventDefault()
        const newTicket = { title, description, priority }
        console.log(newTicket)
        handleCreate(newTicket)
    }




    return (

        <form onSubmit={handleSubmit}>
            <h2>Raise a Ticket</h2>

            <div>
                <label htmlFor="title">Title</label>
                <br />
                <input
                    id="title"
                    value={title}
                    onChange={(event) => setTitle(event.target.value)}
                    required
                />
            </div>

            <div>
                <label htmlFor="description">Description</label>
                <br />
                <textarea
                    id="description"
                    value={description}
                    onChange={(event) => setDescription(event.target.value)}
                />
            </div>

            <div>
                <label htmlFor="priority">Priority</label>
                <br />
                <select
                    id="priority"
                    value={priority}
                    onChange={(event) => setPriority(event.target.value)}
                >
                    <option value="HIGH">HIGH</option>
                    <option value="MEDIUM">MEDIUM</option>
                    <option value="LOW">LOW</option>
                </select>
                
            </div>
            <div>
                <button type='submit'>Add Ticket</button>
            </div>

            <p>Preview: {title} | {description} | {priority}</p>
        </form>

    )
}

export default TicketForm