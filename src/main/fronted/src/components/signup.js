import React, { useState } from 'react';
import '../css/signup.css';

function Signup() {
    // State 변수로 form 데이터 관리
    const [formData, setFormData] = useState({
        userName: '',
        userId: '',
        userPw: '',
        phone: '',
        gender: '',
        agree: false,
    });

    // 입력값 변경 핸들러
    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setFormData({
            ...formData,
            [name]: type === 'checkbox' ? checked : value,
        });
    };

    // 폼 제출 핸들러
    const handleSubmit = async (e) => {
        e.preventDefault();

        // 개인정보 동의 체크 여부 확인
        if (!formData.agree) {
            alert('개인정보 동의를 체크해주세요.');
            return;
        }

        try {
            const response = await fetch('http://localhost:8080/api/signup', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    name: formData.userName,
                    id: formData.userId,
                    password: formData.userPw,
                    phone: formData.phone,
                    gender: formData.gender,
                }),
            });

            if (response.ok) {
                alert('회원가입 성공!');
            } else {
                alert('회원가입 실패. 다시 시도해주세요.');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('서버 오류가 발생했습니다.');
        }
    };

    return (
        <div className="App">
            <div className="header" />
            <div className="body">
                <form className="SignupForm" onSubmit={handleSubmit}>
                    <h2>회원가입</h2>
                    <input
                        type="text"
                        name="userName"
                        placeholder="이름"
                        value={formData.userName}
                        onChange={handleChange}
                    /><br />
                    <input
                        type="text"
                        name="userId"
                        placeholder="아이디"
                        value={formData.userId}
                        onChange={handleChange}
                    /><br />
                    <input
                        type="password"
                        name="userPw"
                        placeholder="비밀번호"
                        value={formData.userPw}
                        onChange={handleChange}
                    /><br />
                    <input
                        type="text"
                        name="phone"
                        placeholder="전화번호"
                        value={formData.phone}
                        onChange={handleChange}
                    /><br />
                    <div className="Gender">
                        <label className="gender">성별 :</label>
                        <input
                            type="radio"
                            name="gender"
                            value="male"
                            id="male"
                            checked={formData.gender === 'male'}
                            onChange={handleChange}
                        />
                        <label htmlFor="male">남성</label>

                        <input
                            type="radio"
                            name="gender"
                            value="female"
                            id="female"
                            checked={formData.gender === 'female'}
                            onChange={handleChange}
                        />
                        <label htmlFor="female">여성</label>
                    </div><br />
                    <div className="CheckBox">
                        <input
                            type="checkbox"
                            name="agree"
                            className="checkBox"
                            checked={formData.agree}
                            onChange={handleChange}
                        />
                        <span className="checkBoxSpan">개인정보 동의</span>
                    </div><br />
                    <button type="submit" className="signup_btn">
                        <span>회원가입</span>
                    </button>
                </form>
            </div>
            <div className="Signupfooter" />
        </div>
    );
}

export default Signup;
