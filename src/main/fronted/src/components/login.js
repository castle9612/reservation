import React from 'react';
import { useState } from "react";
import '../css/login.css';

function Login() {
    const [userID, setUserID] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");

    const handleSubmit = async (e) => {
        e.preventDefault(); // 기본 폼 제출 방지

        const loginData = {
            user_id: userID,
            password: password,
        };

        try {
            const response = await fetch("http://localhost:8080/api/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(loginData),
            });

            if (response.ok) {
                const data = await response.json();
                alert("로그인 성공!");
                localStorage.setItem("user", JSON.stringify(data)); // 로그인 정보를 저장
                window.location.href = "/home"; // 로그인 성공 후 홈으로 이동
            } else {
                setError("아이디 또는 비밀번호가 올바르지 않습니다.");
            }
        } catch (error) {
            console.error("로그인 요청 실패:", error);
            setError("서버 오류가 발생했습니다. 다시 시도해주세요.");
        }
    };

    return (
        <div className="App">
            <div className="Loginheader"/>
            <div className="Loginbody">
                <form className="Login">
                    <h2>로그인</h2>
                    <input
                        type="text"
                        name="userID"
                        id="userID"
                        placeholder="아이디"
                        value={userID}
                        onChange={(e) => setUserID(e.target.value)}
                        required
                    /><br/>
                    <input
                        type="password"
                        name="password"
                        id="password"
                        placeholder="비밀번호"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        required
                    /><br/>
                    <a href="/signup">회원가입</a><br/>
                    <button type="submit" className="Login_btn">
                        <span>로그인</span>
                    </button>
                    {error && <p style={{ color: "red" }}>{error}</p>}
                </form>
            </div>
            <div className="Loginfooter"/>
        </div>
    );
}

export default Login;