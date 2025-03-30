import React from 'react';
import '../css/notice.css';
import { useState } from "react";
import Rabonlogo from './image/라본로고2.jpg';
import Rabonletter from './image/라본로고3.jpg';
import Container from 'react-bootstrap/Container';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import NavDropdown from 'react-bootstrap/NavDropdown';

function Notice() {
  const noticesData = [
    { id: 1, title: "공지사항 1", date: "2025-03-30" },
    { id: 2, title: "공지사항 2", date: "2025-03-29" },
    { id: 3, title: "공지사항 3", date: "2025-03-28" },
  ];

  const [notices, setNotices] = useState(noticesData);
  const [search, setSearch] = useState("");

  const filteredNotices = notices.filter((notice) =>
      notice.title.includes(search)
  );

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
      <div className="contents_notice">
        <h3>공지사항</h3>
        <table className="w-full border-collapse border border-gray-300">
          <thead>
            <tr className="noticetable_header">
              <th className="border p-2">번호</th>
              <th className="border p-2">제목</th>
              <th className="border p-2">작성 날짜</th>
            </tr>
          </thead>
          <tbody>
          {filteredNotices.map((notice) => (
              <tr key={notice.id} className="border">
                <td className="border p-2">{notice.id}</td>
                <td className="border p-2">{notice.title}</td>
                <td className="border p-2">{notice.date}</td>
              </tr>
          ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default Notice;