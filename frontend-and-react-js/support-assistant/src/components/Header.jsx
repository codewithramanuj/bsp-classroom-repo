import React, { useState } from 'react'

function Header({location}) {

    let [counter, setCounter] = useState(0);

    function increment(){
        setCounter(counter+1);
        console.log(counter);
        
    }

    function reset(){
        setCounter(0)
    }

  return (
    <>
    <div>Hello from {location}</div>
    {counter}
    <button onClick={increment}>Increment Counter</button>
    <button onClick={reset}>Reset Counter</button>
    </>
  )
}

export default Header