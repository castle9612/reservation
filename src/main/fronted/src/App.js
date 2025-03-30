import './App.css';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Component } from 'react';
import 'bootstrap/dist/css/bootstrap.css';
import Login from './components/login.js';
import Signup from './components/signup.js';
import Main from './components/main.js';
import Notice from './components/notice.js';
import Noticeview from './components/noticeview.js';
import Noticewrite from './components/noticewrite.js';
import Reserve from './components/reserve.js';
import UserPage from './components/userPage.js';

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
              <Route path='/' element={<Login/>}/>
              <Route path='/signup' element={<Signup/>}/>
              <Route path='/main' element={<Main/>}/>
              <Route path='/notice' element={<Notice/>}/>
              <Route path='/noticeview' element={<Noticeview/>}/>
              <Route path='/noticewrite' element={<Noticewrite/>}/>
              <Route path='/reserve' element={<Reserve/>}/>
              <Route path='/userPage' element={<UserPage/>}/>
            </Routes>
        </BrowserRouter>
      </div>
    )
  }
}

export default App;