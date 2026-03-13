import React, { useState } from 'react';
import '../css/login.css';

function Login() {
    const [formData, setFormData] = useState({
        userID: '',
        userPW: '',
    });

    const [errorMessage, setErrorMessage] = useState('');

    // 입력값 변경 핸들러
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value,
        });
    };

    // 폼 제출 핸들러
    const handleSubmit = async (e) => {
        e.preventDefault();
        setErrorMessage(''); // 오류 메시지 초기화

        try {
            const response = await fetch('http://localhost:8080/api/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    user_id: formData.userID,
                    password: formData.userPW,
                }),
            });

            if (response.ok) {
                const data = await response.json();
                alert(`로그인 성공! 사용자 이름: ${data.name}`);
                // 성공 후 필요한 처리 (예: 페이지 이동, 토큰 저장)
            } else if (response.status === 401) {
                setErrorMessage('아이디 또는 비밀번호가 잘못되었습니다.');
            } else {
                setErrorMessage('로그인 실패. 다시 시도해주세요.');
            }
        } catch (error) {
            console.error('Error:', error);
            setErrorMessage('서버 오류가 발생했습니다.');
        }
    };

    return (
        <div className="App">
            <div className="Loginheader" />
            <div className="Loginbody">
                <form className="Login" onSubmit={handleSubmit}>
                    <h2>로그인</h2>
                    {errorMessage && <p className="error">{errorMessage}</p>}
                    <input
                        type="text"
                        name="userID"
                        placeholder="아이디"
                        value={formData.userID}
                        onChange={handleChange}
                    /><br />
                    <input
                        type="password"
                        name="userPW"
                        placeholder="비밀번호"
                        value={formData.userPW}
                        onChange={handleChange}
                    /><br />
                    <a href="/signup">회원가입</a><br />
                    <button type="submit" className="Login_btn">
                        <span>로그인</span>
                    </button>
                </form>
            </div>
            <div className="Loginfooter" />
        </div>
    );
}

export default Login;
