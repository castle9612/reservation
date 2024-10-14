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
                    <div className="Gender">
                        <label className="gender">성별 :</label>
                        <input
                            type="radio"
                            name="gender"
                            value="male"
                            id="male"
                        />
                        <label htmlFor="male">남성</label>

                        <input
                            type="radio"
                            name="gender"
                            value="female"
                            id="female"
                        />
                        <label htmlFor="female">여성</label>
                    </div><br/>
                    <div className="CheckBox">
                        <input type="checkbox" className="checkBox"/>
                        <span className="checkBoxSpan">개인정보 동의</span>
                    </div><br/>
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