import React from 'react';
import '../css/reserve.css';
import Rabonlogo from './image/라본로고2.jpg';
import Rabonletter from './image/라본로고3.jpg';
import Container from 'react-bootstrap/Container';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import NavDropdown from 'react-bootstrap/NavDropdown';

function Reserve() {
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
        <div class="contents">

        </div>
    </div>
  );
}

export default Reserve;