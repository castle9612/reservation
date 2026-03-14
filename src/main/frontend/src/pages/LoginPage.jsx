import { useState } from 'react';

export default function LoginPage({ onSubmit }) {
  const [form, setForm] = useState({ userId: '', password: '' });

  return (
    <section className="form-panel">
      <h3>로그인</h3>
      <form
        className="form-grid"
        onSubmit={(e) => {
          e.preventDefault();
          void onSubmit(form);
        }}
      >
        <input
          placeholder="아이디"
          value={form.userId}
          onChange={(e) => setForm({ ...form, userId: e.target.value })}
          required
        />
        <input
          type="password"
          placeholder="비밀번호"
          value={form.password}
          onChange={(e) => setForm({ ...form, password: e.target.value })}
          required
        />
        <button type="submit">로그인</button>
      </form>
    </section>
  );
}
