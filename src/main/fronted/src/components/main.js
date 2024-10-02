import React from 'react';
import '../css/main.css';
import Rabon from './image/라본.jpg';
import Rabonlogo from './image/라본로고2.jpg';
import Rabonletter from './image/라본로고3.jpg';
import Container from 'react-bootstrap/Container';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import NavDropdown from 'react-bootstrap/NavDropdown';
import Card from 'react-bootstrap/Card';
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import Button from 'react-bootstrap/Button';

function Main() {
  return (
    <div id="reservation_main">
        <Navbar expand="lg" className="bg-body-tertiary">
          <Container>
            <Navbar.Brand href="#home">
                <img src={Rabonlogo} id="Rabonlogo" alt=""/>
            </Navbar.Brand>
            <Navbar.Toggle aria-controls="basic-navbar-nav" />
            <Navbar.Collapse id="basic-navbar-nav">
              <Nav className="me-auto">
                <img src={Rabonletter} id="Rabonletter" alt=""/>
                <Nav.Link href="#link">공지사항</Nav.Link>
                <NavDropdown title="회원정보" id="basic-nav-dropdown">
                  <NavDropdown.Item href="#action/3.1">정보수정</NavDropdown.Item>
                  <NavDropdown.Item href="#action/3.2">
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
        <div class="contents">
            <img src={Rabon} id="Rabon" alt=""/>
            <div class="main_contents">
            </div>
        </div>
    </div>
  );
}

export default Main;