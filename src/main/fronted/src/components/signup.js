import React from 'react';
import { useState } from "react";
import '../css/signup.css';

function Signup() {
    const [formData, setFormData] = useState({
        userID: "",
        password: "",
        userName: "",
        userEmail: "",
        phoneNumber: "",
        gender: "",
        birthdate: "",
        privacyConsent: false,
    });

    const [error, setError] = useState("");

    // 입력값 변경
    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData({
            ...formData,
            [name]: type === "checkbox" ? checked : value,
        });
    };

    // 폼 제출
    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!formData.privacyConsent) {
            setError("개인정보 동의가 필요합니다.");
            return;
        }

        try {
            const response = await fetch("http://localhost:8080/api/signup", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(formData),
            });

            if (response.ok) {
                alert("회원가입 성공!");
                window.location.href = "/login";
            } else {
                const errorMessage = await response.text();
                setError(errorMessage || "회원가입 실패");
            }
        } catch (error) {
            console.error("회원가입 요청 실패:", error);
            setError("서버 오류가 발생했습니다. 다시 시도해주세요.");
        }
    };

    return (
        <div className="App">
            <div className="header"/>
            <div className="body">
                <form className="SignupForm" onSubmit={handleSubmit}>
                    <h2>회원가입</h2>
                    {error && <p style={{ color: "red" }}>{error}</p>}
                    <input
                        type="text"
                        name="userID"
                        placeholder="아이디"
                        value={formData.userID}
                        onChange={handleChange}
                        required
                    /><br/>
                    <input
                        type="password"
                        name="password"
                        placeholder="비밀번호"
                        value={formData.password}
                        onChange={handleChange}
                        required
                    /><br/>
                    <input
                        type="text"
                        name="userName"
                        placeholder="이름"
                        value={formData.userName}
                        onChange={handleChange}
                        required
                    /><br/>
                    <input
                        type="email"
                        name="userEmail"
                        placeholder="이메일"
                        value={formData.userEmail}
                        onChange={handleChange}
                        required
                    /><br/>
                    <input
                        type="text"
                        name="phoneNumber"
                        placeholder="전화번호"
                        value={formData.phoneNumber}
                        onChange={handleChange}
                        required
                    /><br/>
                    <input
                        type="date"
                        name="birthdate"
                        value={formData.birthdate}
                        onChange={handleChange}
                    /><br/>
                    <div className="Gender">
                        <label className="gender">성별 :</label>
                        <input
                            type="radio"
                            name="gender"
                            value="male"
                            id="male"
                            checked={formData.gender === "male"}
                            onChange={handleChange}
                        />
                        <label htmlFor="male">남성</label>
                        <input
                            type="radio"
                            name="gender"
                            value="female"
                            id="female"
                            checked={formData.gender === "female"}
                            onChange={handleChange}
                        />
                        <label htmlFor="female">여성</label>
                    </div><br/>
                    <div className="CheckBox">
                        <input
                            type="checkbox"
                            name="privacyConsent"
                            checked={formData.privacyConsent}
                            onChange={handleChange}
                        />
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