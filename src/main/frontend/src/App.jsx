import { useEffect, useMemo, useState } from 'react';

const images = [
  'https://images.pexels.com/photos/6621465/pexels-photo-6621465.jpeg?auto=compress&cs=tinysrgb&w=1200',
  'https://images.pexels.com/photos/6663369/pexels-photo-6663369.jpeg?auto=compress&cs=tinysrgb&w=1200',
  'https://images.pexels.com/photos/19641820/pexels-photo-19641820.jpeg?auto=compress&cs=tinysrgb&w=1200',
];

const adminLinks = [
  ['/admin/dashboard', '대시보드'],
  ['/admin/users', '회원 관리'],
  ['/reservations/admin', '예약 관리'],
  ['/courses/new', '코스 등록'],
  ['/staff/new', '스태프 등록'],
  ['/admin/announcements/new', '공지 작성'],
];

const loginInit = { userId: '', password: '' };
const signupInit = {
  userId: '', password: '', userName: '', userEmail: '', phoneNumber: '',
  birthdate: '', gender: '남성', maritalStatus: false, privacyConsent: false,
};
const guestInit = { courseId: '', reservationDateTime: '', name: '', phoneNumber: '' };
const memberInit = { courseId: '', reservationDateTime: '' };

function App() {
  const [view, setView] = useState('home');
  const [csrf, setCsrf] = useState(null);
  const [loading, setLoading] = useState(true);
  const [notice, setNotice] = useState(null);
  const [auth, setAuth] = useState({ authenticated: false, username: null, role: null });
  const [summary, setSummary] = useState({ announcements: [], staff: [] });
  const [courses, setCourses] = useState([]);
  const [announcements, setAnnouncements] = useState([]);
  const [staff, setStaff] = useState([]);
  const [myReservations, setMyReservations] = useState([]);
  const [guestReservations, setGuestReservations] = useState([]);
  const [searchPhone, setSearchPhone] = useState('');
  const [loginForm, setLoginForm] = useState(loginInit);
  const [signupForm, setSignupForm] = useState(signupInit);
  const [guestForm, setGuestForm] = useState(guestInit);
  const [memberForm, setMemberForm] = useState(memberInit);

  const isAdmin = auth.role === 'ROLE_ADMIN';
  const heroImage = useMemo(() => images[new Date().getDate() % images.length], []);
  const nav = [
    ['home', '홈'], ['courses', '프로그램'], ['announcements', '공지'],
    ['staff', '테라피스트'], ['guest-booking', '비회원 예약'], ['guest-search', '비회원 조회'],
    ...(auth.authenticated ? [['member-booking', '회원 예약'], ['my-reservations', '내 예약']] : []),
    ...(isAdmin ? [['admin', '관리자']] : []),
    ...(!auth.authenticated ? [['login', '로그인'], ['signup', '회원가입']] : []),
  ];

  useEffect(() => { bootstrap(); }, []);

  async function bootstrap() {
    setLoading(true);
    try {
      const [token, me, sum, courseData, noticeData, staffData] = await Promise.all([
        fetchCsrf(), api('/api/auth/me'), api('/api/public/summary'), api('/api/courses'),
        api('/api/public/announcements'), api('/api/public/staff'),
      ]);
      setCsrf(token);
      setAuth(me || { authenticated: false, username: null, role: null });
      setSummary({ announcements: sum?.announcements || [], staff: sum?.staff || [] });
      setCourses(courseData || []);
      setAnnouncements(noticeData || []);
      setStaff(staffData || []);
      if (me?.authenticated) await refreshMyReservations();
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '초기 화면을 불러오지 못했습니다.' });
    } finally {
      setLoading(false);
    }
  }

  async function fetchCsrf() {
    const res = await fetch('/api/auth/csrf', { credentials: 'include', headers: { Accept: 'application/json' } });
    const payload = await parseResponse(res);
    const token = payload?.data || null;
    setCsrf(token);
    return token;
  }

  async function api(url, options = {}) {
    const { method = 'GET', body, headers = {}, useCsrf = false } = options;
    let token = csrf;
    if (useCsrf && !token) token = await fetchCsrf();
    const res = await fetch(url, {
      method,
      credentials: 'include',
      headers: {
        Accept: 'application/json',
        'X-Requested-With': 'XMLHttpRequest',
        ...(useCsrf && token?.headerName && token?.token ? { [token.headerName]: token.token } : {}),
        ...headers,
      },
      body,
    });
    const payload = await parseResponse(res);
    if (!res.ok || payload?.success === false) throw new Error(payload?.message || (res.status === 403 ? '접근 권한이 없습니다.' : '요청 처리에 실패했습니다.'));
    return payload?.data ?? null;
  }

  async function parseResponse(response) {
    const type = response.headers.get('content-type') || '';
    if (type.includes('application/json')) return response.json();
    const text = await response.text();
    return text ? { success: response.ok, message: text } : null;
  }

  async function refreshMyReservations() {
    setMyReservations((await api('/api/reservations/me')) || []);
  }

  function onChange(setter) {
    return ({ target }) => setter((prev) => ({ ...prev, [target.name]: target.type === 'checkbox' ? target.checked : target.value }));
  }

  function jump(next) {
    setView(next);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function nextSlot() {
    const d = new Date();
    d.setMinutes(d.getMinutes() + 60); d.setSeconds(0, 0);
    d.setMinutes(d.getMinutes() + (30 - (d.getMinutes() % 30)) % 30);
    return d.toISOString().slice(0, 16);
  }

  function money(v) { return `${new Intl.NumberFormat('ko-KR').format(Number(v || 0))}원`; }
  function dt(v) { return v ? new Intl.DateTimeFormat('ko-KR', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }).format(new Date(v)) : '-'; }
  function dd(v) { return v ? new Intl.DateTimeFormat('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' }).format(new Date(v)) : ''; }
  function normalizeAsset(path) {
    if (!path) return '';
    if (path.startsWith('http://') || path.startsWith('https://') || path.startsWith('/')) return path;
    return `/uploads/${path}`;
  }

  async function submitLogin(event) {
    event.preventDefault(); setNotice(null);
    try {
      await fetchCsrf();
      const form = new URLSearchParams({ userId: loginForm.userId, password: loginForm.password });
      await api('/login', { method: 'POST', body: form, headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' }, useCsrf: true });
      const me = await api('/api/auth/me');
      setAuth(me || { authenticated: false, username: null, role: null });
      setLoginForm(loginInit);
      await refreshMyReservations();
      setNotice({ type: 'success', text: '로그인되었습니다.' });
      jump(me?.role === 'ROLE_ADMIN' ? 'admin' : 'member-booking');
    } catch (error) { setNotice({ type: 'error', text: error.message || '로그인에 실패했습니다.' }); }
  }

  async function submitSignup(event) {
    event.preventDefault(); setNotice(null);
    try {
      await api('/api/auth/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...signupForm, phoneNumber: signupForm.phoneNumber.replaceAll(/[^0-9]/g, '') }),
      });
      setSignupForm(signupInit);
      setNotice({ type: 'success', text: '회원가입이 완료되었습니다. 바로 로그인해 주세요.' });
      jump('login');
    } catch (error) { setNotice({ type: 'error', text: error.message || '회원가입에 실패했습니다.' }); }
  }

  async function submitGuest(event) {
    event.preventDefault(); setNotice(null);
    try {
      await api('/api/reservations/guest', {
        method: 'POST', useCsrf: true, headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...guestForm, courseId: Number(guestForm.courseId), phoneNumber: guestForm.phoneNumber.replaceAll(/[^0-9]/g, '') }),
      });
      setGuestForm({ ...guestInit, reservationDateTime: nextSlot() });
      setNotice({ type: 'success', text: '비회원 예약이 접수되었습니다.' });
      jump('guest-search');
    } catch (error) { setNotice({ type: 'error', text: error.message || '비회원 예약에 실패했습니다.' }); }
  }

  async function submitMember(event) {
    event.preventDefault(); setNotice(null);
    try {
      await api('/api/reservations/member', {
        method: 'POST', useCsrf: true, headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...memberForm, courseId: Number(memberForm.courseId) }),
      });
      setMemberForm({ ...memberInit, reservationDateTime: nextSlot() });
      await refreshMyReservations();
      setNotice({ type: 'success', text: '회원 예약이 완료되었습니다.' });
      jump('my-reservations');
    } catch (error) { setNotice({ type: 'error', text: error.message || '회원 예약에 실패했습니다.' }); }
  }

  async function searchGuest(event) {
    event.preventDefault(); setNotice(null);
    try {
      const phoneNumber = searchPhone.replaceAll(/[^0-9]/g, '');
      const data = (await api(`/api/reservations/search?phoneNumber=${encodeURIComponent(phoneNumber)}`)) || [];
      setGuestReservations(data);
      setNotice({ type: 'info', text: data.length ? '조회된 예약을 확인해 주세요.' : '조회된 예약이 없습니다.' });
    } catch (error) { setNotice({ type: 'error', text: error.message || '예약 조회에 실패했습니다.' }); }
  }

  async function logout() {
    setNotice(null);
    try {
      await api('/api/auth/logout', { method: 'POST', useCsrf: true });
      setAuth({ authenticated: false, username: null, role: null });
      setMyReservations([]); setGuestReservations([]); setSearchPhone('');
      setNotice({ type: 'success', text: '로그아웃되었습니다.' });
      jump('home'); await fetchCsrf();
    } catch (error) { setNotice({ type: 'error', text: error.message || '로그아웃에 실패했습니다.' }); }
  }

  function pickCourse(id, memberOnly = false) {
    const value = String(id);
    if (memberOnly) { setMemberForm((p) => ({ ...p, courseId: value, reservationDateTime: nextSlot() })); jump('member-booking'); return; }
    setGuestForm((p) => ({ ...p, courseId: value, reservationDateTime: nextSlot() }));
    jump(auth.authenticated ? 'member-booking' : 'guest-booking');
  }

  if (loading) return <div className="loading">차분한 예약 공간을 준비하고 있습니다.</div>;

  return (
    <div className="app-shell">
      <header className="hero laon-hero">
        <div className="hero-copy">
          <p className="eyebrow">CALM STAY SPA</p>
          <h1>도심 속 웰니스 스테이, 호텔 라운지 같은 예약 경험</h1>
          <p className="hero-text">브랜드 헤더와 넓은 여백, 갤러리형 비주얼, 정제된 예약 흐름을 중심으로 호텔형 테라피 사이트 감도로 다시 다듬었습니다.</p>
          <div className="hero-stats"><div><strong>{courses.length}</strong><span>프로그램</span></div><div><strong>{staff.length}</strong><span>테라피스트</span></div><div><strong>{announcements.length}</strong><span>공지</span></div></div>
        </div>
        <div className="hero-gallery"><img className="gallery-main" src={heroImage} alt="호텔 스파 메인 이미지" /><div className="gallery-side"><img src={images[1]} alt="웰니스 공간" /><img src={images[2]} alt="테라피 프로그램" /></div></div>
        <aside className="hero-panel"><div className="status-card"><span className="status-label">{auth.authenticated ? '멤버 라운지' : '게스트 라운지'}</span><strong>{auth.authenticated ? `${auth.username} 님 환영합니다` : '예약 전 둘러보는 중'}</strong><p>{auth.authenticated ? (isAdmin ? '관리자 도구와 예약 메뉴를 함께 이용할 수 있습니다.' : '회원 전용 예약과 내 일정 확인을 이용할 수 있습니다.') : '비회원 예약과 공지 확인을 먼저 진행할 수 있습니다.'}</p></div><div className="quick-actions">{!auth.authenticated && <><button className="btn btn-secondary" onClick={() => jump('login')}>로그인</button><button className="btn btn-primary" onClick={() => jump('signup')}>회원가입</button></>}{auth.authenticated && <button className="btn btn-ghost" onClick={logout}>로그아웃</button>}{isAdmin && <button className="btn btn-primary" onClick={() => jump('admin')}>관리자 페이지</button>}{auth.authenticated && !isAdmin && <button className="btn btn-primary" onClick={() => jump('member-booking')}>회원 예약</button>}</div></aside>
      </header>

      <nav className="main-nav">{nav.map(([key, label]) => <button key={key} className={`nav-pill ${view === key ? 'active' : ''}`} onClick={() => jump(key)}>{label}</button>)}</nav>
      {notice && <div className={`notice-banner ${notice.type}`}>{notice.text}</div>}

      <main className="content-grid">
        {view === 'home' && <><section className="panel panel-wide intro-panel"><div className="section-head"><div><p className="eyebrow">Signature Stay</p><h2>호텔형 웰니스 흐름</h2></div></div><div className="intro-grid"><article><strong>Arrival Ritual</strong><p>첫 방문 고객도 코스 확인부터 예약까지 부드럽게 이어집니다.</p></article><article><strong>Private Treatment</strong><p>테라피스트, 공지, 예약 조회가 한 화면에서 정리됩니다.</p></article><article><strong>Member Lounge</strong><p>회원은 예약 관리, 관리자는 별도 운영 페이지로 바로 이동합니다.</p></article></div></section><section className="panel panel-wide editorial-panel"><div className="editorial-copy"><p className="eyebrow">Wellness Edit</p><h2>라운지형 헤더와 시그니처 카드 구성</h2><p>호텔 라온제나처럼 브랜드 소개와 안내 섹션이 선명하게 보이도록 첫 화면을 다시 정렬했습니다.</p><button className="btn btn-primary" onClick={() => jump('courses')}>프로그램 보기</button></div><div className="editorial-image"><img src={images[1]} alt="호텔 스파 스타일" /></div></section></>}

        {view === 'courses' && <section className="panel panel-wide"><div className="section-head"><div><p className="eyebrow">Programs</p><h2>체류형 프로그램</h2></div><p>원하는 코스를 고르면 바로 예약 입력으로 이어집니다.</p></div><div className="card-grid">{courses.map((course) => <article className="program-card" key={course.id}><span className="program-meta">{course.durationMinutes}분 프로그램</span><h3>{course.name}</h3><p>{course.staff?.name ? `${course.staff.name} 테라피스트 배정 가능` : '테라피스트 선택 가능'}</p><div className="price-row"><strong>회원 {money(course.memberPrice)}</strong><span>비회원 {money(course.nonMemberPrice)}</span></div><div className="card-actions"><button className="btn btn-secondary" onClick={() => pickCourse(course.id)}>바로 예약</button><button className="btn btn-ghost" onClick={() => pickCourse(course.id, true)}>회원 예약</button></div></article>)}</div></section>}

        {view === 'announcements' && <section className="panel panel-wide"><div className="section-head"><div><p className="eyebrow">Notice</p><h2>운영 안내</h2></div></div><div className="stack-list">{announcements.map((item) => <article className="stack-card" key={item.id}><div className="stack-card-head"><h3>{item.title}</h3><span>{dd(item.createdAt)}</span></div><div className="html-content" dangerouslySetInnerHTML={{ __html: item.content || '<p>등록된 안내 내용을 확인해 주세요.</p>' }} /></article>)}</div></section>}

        {view === 'staff' && <section className="panel panel-wide"><div className="section-head"><div><p className="eyebrow">Therapists</p><h2>테라피스트</h2></div></div><div className="staff-grid">{staff.map((member) => <article className="staff-card" key={member.id}><div className="staff-image"><img src={normalizeAsset(member.profilePicture) || images[2]} alt={member.name} /></div><div><h3>{member.name}</h3><p>안정적인 리듬과 프라이빗 케어를 중심으로 관리합니다.</p></div></article>)}</div></section>}

        {view === 'guest-booking' && <section className="panel panel-wide booking-layout"><div className="booking-copy"><p className="eyebrow">Guest Booking</p><h2>비회원 예약</h2><p>간단한 정보만으로 예약하고, 같은 번호로 다시 조회할 수 있습니다.</p><img src={images[0]} alt="게스트 예약 이미지" /></div><form className="form-panel" onSubmit={submitGuest}><label><span>프로그램</span><select name="courseId" value={guestForm.courseId} onChange={onChange(setGuestForm)} required><option value="">코스를 선택해 주세요</option>{courses.map((course) => <option key={course.id} value={course.id}>{course.name}</option>)}</select></label><div className="form-split"><label><span>예약 일시</span><input type="datetime-local" name="reservationDateTime" value={guestForm.reservationDateTime} onChange={onChange(setGuestForm)} required /></label><label><span>성함</span><input name="name" value={guestForm.name} onChange={onChange(setGuestForm)} required /></label></div><label><span>전화번호</span><input name="phoneNumber" value={guestForm.phoneNumber} onChange={onChange(setGuestForm)} placeholder="01012345678" required /></label><button className="btn btn-primary wide" type="submit">비회원 예약 접수</button></form></section>}

        {view === 'guest-search' && <section className="panel panel-wide"><div className="section-head"><div><p className="eyebrow">Lookup</p><h2>비회원 예약 조회</h2></div></div><form className="lookup-form" onSubmit={searchGuest}><input value={searchPhone} onChange={(e) => setSearchPhone(e.target.value)} placeholder="예약 시 입력한 전화번호" required /><button className="btn btn-primary" type="submit">조회하기</button></form><div className="stack-list">{guestReservations.map((item) => <article className="reservation-card" key={item.id}><strong>{item.name || '비회원 예약'}</strong><span>{dt(item.reservationDateTime)}</span><p>코스 ID {item.courseId} / 상태 {item.status || '접수 완료'}</p></article>)}</div></section>}

        {view === 'login' && <section className="panel auth-panel"><div className="section-head"><div><p className="eyebrow">Member Sign In</p><h2>로그인</h2></div></div><form className="form-panel compact" onSubmit={submitLogin}><label><span>아이디</span><input name="userId" value={loginForm.userId} onChange={onChange(setLoginForm)} required /></label><label><span>비밀번호</span><input type="password" name="password" value={loginForm.password} onChange={onChange(setLoginForm)} required /></label><button className="btn btn-primary wide" type="submit">로그인</button></form></section>}

        {view === 'signup' && <section className="panel panel-wide signup-layout"><div className="signup-visual"><img src={images[2]} alt="회원가입 안내" /><div className="signup-overlay"><p className="eyebrow">Membership</p><h2>회원 전용 일정 관리</h2><p>예약 기록과 재방문 흐름을 호텔 멤버십처럼 이어갈 수 있습니다.</p></div></div><form className="form-panel" onSubmit={submitSignup}><div className="section-head"><div><p className="eyebrow">Join</p><h2>회원가입</h2></div></div><div className="form-split"><label><span>아이디</span><input name="userId" value={signupForm.userId} onChange={onChange(setSignupForm)} required /></label><label><span>비밀번호</span><input type="password" name="password" value={signupForm.password} onChange={onChange(setSignupForm)} required /></label></div><div className="form-split"><label><span>이름</span><input name="userName" value={signupForm.userName} onChange={onChange(setSignupForm)} required /></label><label><span>이메일</span><input type="email" name="userEmail" value={signupForm.userEmail} onChange={onChange(setSignupForm)} required /></label></div><div className="form-split"><label><span>전화번호</span><input name="phoneNumber" value={signupForm.phoneNumber} onChange={onChange(setSignupForm)} placeholder="01012345678" required /></label><label><span>생년월일</span><input type="date" name="birthdate" value={signupForm.birthdate} onChange={onChange(setSignupForm)} required /></label></div><div className="form-split"><label><span>성별</span><select name="gender" value={signupForm.gender} onChange={onChange(setSignupForm)}><option value="남성">남성</option><option value="여성">여성</option></select></label><label><span>결혼 여부</span><select name="maritalStatus" value={signupForm.maritalStatus ? 'true' : 'false'} onChange={(e) => setSignupForm((p) => ({ ...p, maritalStatus: e.target.value === 'true' }))}><option value="false">미혼</option><option value="true">기혼</option></select></label></div><label className="consent-box"><input type="checkbox" name="privacyConsent" checked={signupForm.privacyConsent} onChange={onChange(setSignupForm)} required /><div><strong>개인정보 수집 및 이용 동의</strong><p>예약 확인과 방문 안내를 위한 최소 정보만 보관합니다.</p></div></label><button className="btn btn-primary wide" type="submit">회원가입 완료</button></form></section>}

        {view === 'member-booking' && <section className="panel panel-wide booking-layout"><div className="booking-copy"><p className="eyebrow">Member Booking</p><h2>회원 예약</h2><p>로그인한 회원은 더 빠르게 일정을 선택하고 내 예약으로 바로 이어집니다.</p><img src={images[1]} alt="회원 예약 이미지" /></div><form className="form-panel" onSubmit={submitMember}>{!auth.authenticated && <div className="notice-inline">회원 예약은 로그인 후 이용할 수 있습니다.</div>}<label><span>프로그램</span><select name="courseId" value={memberForm.courseId} onChange={onChange(setMemberForm)} required><option value="">코스를 선택해 주세요</option>{courses.map((course) => <option key={course.id} value={course.id}>{course.name}</option>)}</select></label><label><span>예약 일시</span><input type="datetime-local" name="reservationDateTime" value={memberForm.reservationDateTime} onChange={onChange(setMemberForm)} required /></label><button className="btn btn-primary wide" type="submit" disabled={!auth.authenticated}>회원 예약 완료</button></form></section>}

        {view === 'my-reservations' && <section className="panel panel-wide"><div className="section-head"><div><p className="eyebrow">My Schedule</p><h2>내 예약</h2></div><button className="btn btn-secondary" onClick={refreshMyReservations}>새로고침</button></div>{!auth.authenticated && <div className="notice-inline">로그인 후 예약 내역을 확인할 수 있습니다.</div>}<div className="stack-list">{myReservations.map((item) => <article className="reservation-card" key={item.id}><strong>{auth.username}</strong><span>{dt(item.reservationDateTime)}</span><p>코스 ID {item.courseId} / 상태 {item.status || '예약 완료'}</p></article>)}{auth.authenticated && myReservations.length === 0 && <p className="empty-state">아직 등록된 회원 예약이 없습니다.</p>}</div></section>}

        {view === 'admin' && isAdmin && <><section className="panel panel-wide admin-hero"><div><p className="eyebrow">Admin Lounge</p><h2>관리자 전용 운영 페이지</h2><p>운영자 계정은 기존 관리자 화면으로 바로 이동할 수 있습니다.</p></div><div className="admin-kpis"><article><strong>{summary.announcements.length}</strong><span>최근 공지</span></article><article><strong>{staff.length}</strong><span>등록 스태프</span></article><article><strong>{courses.length}</strong><span>운영 코스</span></article></div></section><section className="panel panel-wide"><div className="admin-links">{adminLinks.map(([href, label]) => <a className="admin-link-card" key={href} href={href}><strong>{label}</strong><span>기존 관리자 화면으로 이동</span></a>)}</div></section></>}
      </main>
    </div>
  );
}

export default App;
