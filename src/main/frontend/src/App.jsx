import { useEffect, useState } from 'react';
import SiteHeader from './components/SiteHeader';
import StatusPanel from './components/StatusPanel';
import HomePage from './pages/HomePage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';

const tabs = ['home', 'login', 'signup'];

export default function App() {
  const [tab, setTab] = useState('home');
  const [csrfToken, setCsrfToken] = useState('');
  const [auth, setAuth] = useState({ authenticated: false, userId: null, role: null });
  const [summary, setSummary] = useState({ announcements: [], staff: [] });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    void bootstrap();
  }, []);

  async function bootstrap() {
    setError('');
    try {
      const [csrfRes, meRes, summaryRes] = await Promise.all([
        fetch('/api/auth/csrf', { credentials: 'include' }),
        fetch('/api/auth/me', { credentials: 'include' }),
        fetch('/api/public/summary', { credentials: 'include' })
      ]);

      const csrfJson = await csrfRes.json();
      const meJson = await meRes.json();
      const summaryJson = await summaryRes.json();

      setCsrfToken(csrfJson?.data?.token ?? '');
      setAuth(meJson?.data ?? { authenticated: false, userId: null, role: null });
      setSummary(summaryJson?.data ?? { announcements: [], staff: [] });
    } catch (e) {
      setError('초기 데이터를 불러오지 못했다. 연결이 안 맞으면 늘 이런 식이다.');
    }
  }

  async function handleLogin(form) {
    setMessage('');
    setError('');

    const body = new URLSearchParams({
      userId: form.userId,
      password: form.password
    }).toString();

    const response = await fetch('/login', {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
        'X-Requested-With': 'XMLHttpRequest',
        'X-CSRF-TOKEN': csrfToken,
        'Accept': 'application/json'
      },
      body
    });

    const json = await response.json().catch(() => ({ success: false, message: '로그인 응답 해석 실패' }));
    if (!response.ok || !json.success) {
      setError(json.message || '로그인 실패');
      return false;
    }

    setMessage(json.message || '로그인되었습니다.');
    await bootstrap();
    setTab('home');
    return true;
  }

  async function handleSignup(form) {
    setMessage('');
    setError('');

    const response = await fetch('/api/auth/signup', {
      method: 'POST',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json',
        'X-CSRF-TOKEN': csrfToken,
        'Accept': 'application/json'
      },
      body: JSON.stringify(form)
    });

    const json = await response.json().catch(() => ({ success: false, message: '회원가입 응답 해석 실패' }));
    if (!response.ok || !json.success) {
      setError(json.message || '회원가입 실패');
      return false;
    }

    setMessage(json.message || '회원가입이 완료되었습니다.');
    setTab('login');
    return true;
  }

  async function handleLogout() {
    setMessage('');
    setError('');

    const response = await fetch('/api/auth/logout', {
      method: 'POST',
      credentials: 'include',
      headers: {
        'X-Requested-With': 'XMLHttpRequest',
        'X-CSRF-TOKEN': csrfToken,
        'Accept': 'application/json'
      }
    });

    const json = await response.json().catch(() => ({ success: false, message: '로그아웃 응답 해석 실패' }));
    if (!response.ok || !json.success) {
      setError(json.message || '로그아웃 실패');
      return;
    }

    setMessage(json.message || '로그아웃되었습니다.');
    await bootstrap();
  }

  return (
    <div className="shell">
      <SiteHeader
        tab={tab}
        tabs={tabs}
        setTab={setTab}
        authenticated={auth.authenticated}
        onLogout={handleLogout}
      />

      <section className="hero">
        <div>
          <p className="eyebrow">CALM THERAPY RESERVATION</p>
          <h1>차분한 예약 흐름</h1>
          <p>테스트용 프런트도 최소한 실제 백엔드 필드와 엔드포인트는 맞춰야 한다. 그런 기본기부터 다시 맞췄다.</p>
        </div>
        <StatusPanel auth={auth} csrfToken={csrfToken} />
      </section>

      {message ? <div className="alert success">{message}</div> : null}
      {error ? <div className="alert error">{error}</div> : null}

      {tab === 'home' && <HomePage summary={summary} />}
      {tab === 'login' && <LoginPage onSubmit={handleLogin} />}
      {tab === 'signup' && <SignupPage onSubmit={handleSignup} />}
    </div>
  );
}
