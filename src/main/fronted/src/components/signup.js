import React from 'react';
import '../css/signup.css';

function Signup() {

    return (
        <div className="App">
            <div className="header"/>
            <div className="body">
                <form className= "SignupForm">
                    <h2>회원가입</h2>
                    <input
                        type="text"
                        name="userName"
                        placeholder="이름"
                    /><br/>
                    <input
                        type="text"
                        name="userId"
                        placeholder="아이디"
                    /><br/>
                    <input
                        type="password"
                        name="userPw"
                        placeholder="비밀번호"
                    /><br/>
                    <input
                        type="text"
                        name="userId"
                        placeholder="전화번호"
                    /><br/>
                    <input type="checkbox" className="CheckBox"/>
                    <span className="checkBoxSpan">개인정보 동의</span><br/>
                    <button type="submit" className="signup_btn">
                        <span>회원가입</span>
                    </button>
                </form>
            </div>
            <div className="Signupfooter"/>
        </div>
    );
}

export default Signup;