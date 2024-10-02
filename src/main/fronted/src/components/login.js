import React from 'react';
import '../css/login.css';

function Login() {

    return (
        <div className="App">
            <div className="Loginheader"/>
            <div className="Loginbody">
                <form className="Login">
                    <h2>로그인</h2>
                    <input
                        type="text"
                        name="userID"
                        placeholder="아이디"
                    /><br/>
                    <input
                        type="password"
                        name="userPW"
                        placeholder="비밀번호"
                    /><br/>
                    <a href="/signup">회원가입</a><br/>
                    <button type="submit" className="Login_btn">
                        <span>로그인</span>
                    </button>
                </form>
            </div>
            <div className="Loginfooter"/>
        </div>
    );
}

export default Login;