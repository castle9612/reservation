import { useState } from 'react';

const initialForm = {
  userId: '',
  password: '',
  userName: '',
  userEmail: '',
  phoneNumber: '',
  birthdate: '',
  gender: '',
  maritalStatus: false,
  privacyConsent: true
};

export default function SignupPage({ onSubmit }) {
  const [form, setForm] = useState(initialForm);

  return (
    <section className="form-panel">
      <h3>회원가입</h3>
      <form
        className="form-grid two-column"
        onSubmit={(e) => {
          e.preventDefault();
          void onSubmit(form);
        }}
      >
        <input placeholder="아이디" value={form.userId} onChange={(e) => setForm({ ...form, userId: e.target.value })} required />
        <input type="password" placeholder="비밀번호" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required />
        <input placeholder="이름" value={form.userName} onChange={(e) => setForm({ ...form, userName: e.target.value })} required />
        <input type="email" placeholder="이메일" value={form.userEmail} onChange={(e) => setForm({ ...form, userEmail: e.target.value })} required />
        <input placeholder="전화번호" value={form.phoneNumber} onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })} required />
        <input placeholder="생년월일" value={form.birthdate} onChange={(e) => setForm({ ...form, birthdate: e.target.value })} />
        <select value={form.gender} onChange={(e) => setForm({ ...form, gender: e.target.value })}>
          <option value="">성별 선택</option>
          <option value="남성">남성</option>
          <option value="여성">여성</option>
        </select>
        <select value={String(form.maritalStatus)} onChange={(e) => setForm({ ...form, maritalStatus: e.target.value === 'true' })}>
          <option value="false">미혼</option>
          <option value="true">기혼</option>
        </select>
        <label className="check-row full-span">
          <input type="checkbox" checked={form.privacyConsent} onChange={(e) => setForm({ ...form, privacyConsent: e.target.checked })} />
          개인정보 수집 동의
        </label>
        <button type="submit" className="full-span">회원가입</button>
      </form>
    </section>
  );
}
