import React from 'react';
import '../css/userPage.css';
import Rabonlogo from './image/라본로고2.jpg';
import Rabonletter from './image/라본로고3.jpg';
import Container from 'react-bootstrap/Container';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import NavDropdown from 'react-bootstrap/NavDropdown';

function UserPage() {
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
                  <NavDropdown.Item href="/userPage">마이페이지</NavDropdown.Item>
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
        <div className="contents_userPage">
            <h3>마이페이지</h3>
            <div className="myPage_contents">
                <div className="userid" id="cate">
                    <div className="cate_id">
                        <span>아이디</span>
                    </div>
                    <div className="user_id">
                    </div>
                </div>
                <div className="username" id="cate">
                    <div className="cate_name">
                        <span>이름</span>
                    </div>
                    <div className="user_name">
                    </div>
                </div>
                <div className="usercount" id="cate">
                    <div className="cate_count">
                        패키지 남은 횟수
                    </div>
                    <div className="user_count">
                    </div>
                </div>
                <div className="userphone" id="cate">
                    <div className="cate_phone">
                        전화번호
                    </div>
                    <div className="user_phone">
                    </div>
                </div>
                <div className="useremail">
                    <div className="cate_email">
                        이메일
                    </div>
                    <div className="user_email">
                    </div>
                </div>
            </div>
            <button id="EditUserInfo">
                정보수정
            </button>
        </div>
    </div>
  );
}

export default UserPage;