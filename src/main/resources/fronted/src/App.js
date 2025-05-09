import './App.css';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Component } from 'react';
import 'bootstrap/dist/css/bootstrap.css';
import Login from './components/login.js';
import Signup from './components/signup.js';
import Main from './components/main.js';
import Notice from './components/notice.js';
import Reserve from './components/reserve.js';

class App extends Component{
  constructor(props){
    super(props)
    this.state={

    }
  }

  render(){
    return(
      <div id='App'>
        <BrowserRouter>
            <Routes>
              <Route path='/' element={<Main/>}/>
              <Route path='/signup' element={<Signup/>}/>
              <Route path='/login' element={<Login/>}/>
              <Route path='/notice' element={<Notice/>}/>
              <Route path='/reserve' element={<Reserve/>}/>
            </Routes>
        </BrowserRouter>
      </div>
    )
  }
}

export default App;