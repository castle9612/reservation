import React from 'react';
import '../css/main.css';
import Rabon from './image/라본.jpg';
import Rabonlogo from './image/라본로고2.jpg';
import Rabonletter from './image/라본로고3.jpg';
import Container from 'react-bootstrap/Container';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import NavDropdown from 'react-bootstrap/NavDropdown';
import { BsCalendarFill, BsFillBellFill, BsArrowRightShort } from "react-icons/bs";

function Main() {

    const GoNotice=()=>{
        window.location.href='/notice'
    }

    const GoReserve=()=>{
        window.location.href='/reserve'
    }

  return (
    <div id="reservation_main">
        <Navbar expand="lg" className="bg-body-tertiary">
          <Container>
            <Navbar.Brand href="/">
                <img src={Rabonlogo} id="Rabonlogo" alt=""/>
            </Navbar.Brand>
            <img src={Rabonletter} id="Rabonletter" alt=""/>
            <Navbar.Toggle aria-controls="basic-navbar-nav" />
            <Navbar.Collapse id="basic-navbar-nav">
              <Nav className="me-auto">
                <Nav.Link href="/notice">공지사항</Nav.Link>
                <NavDropdown title="회원정보" id="basic-nav-dropdown">
                  <NavDropdown.Item href="#action/3.1">정보수정</NavDropdown.Item>
                  <NavDropdown.Item href="/reserve">
                    예약정보
                  </NavDropdown.Item>
                  <NavDropdown.Item href="#action/3.3">예약취소</NavDropdown.Item>
                  <NavDropdown.Divider />
                  <NavDropdown.Item href="/login">
                    로그인
                  </NavDropdown.Item>
                </NavDropdown>
              </Nav>
            </Navbar.Collapse>
          </Container>
        </Navbar>
        <div className="contents">
            <img src={Rabon} id="Rabon" alt=""/>
            <div className="main_contents">
                <div className="goToNotice" onClick={GoNotice}>
                    <label>공지사항</label><BsArrowRightShort size={50} color="#231C14"/><br/>
                    <BsFillBellFill id="bsbell" size={150} color="#231C14"/>
                </div>
                <div className="goToReserve" onClick={GoReserve}>
                    <label>예약</label><BsArrowRightShort size={50} color="#231C14"/><br/>
                    <BsCalendarFill id="bscal" size={150} color="#231C14"/>
                </div>
                <div className="wayTocome">
                    <label>찾아오는 길</label><br/>
                    <div className="picture">
                    </div>
                    <div className="info">
                    </div>
                </div>
            </div>
        </div>
    </div>
  );
}

export default Main;