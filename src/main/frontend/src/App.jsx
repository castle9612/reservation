import { useEffect, useMemo, useState } from 'react';

const heroImages = [
  'https://images.pexels.com/photos/5240677/pexels-photo-5240677.jpeg?auto=compress&cs=tinysrgb&w=1400',
  'https://images.pexels.com/photos/6663369/pexels-photo-6663369.jpeg?auto=compress&cs=tinysrgb&w=1400',
  'https://images.pexels.com/photos/3757942/pexels-photo-3757942.jpeg?auto=compress&cs=tinysrgb&w=1400',
];

const adminLinks = [
  ['/admin/dashboard', '대시보드'],
  ['/admin/users', '회원 관리'],
  ['/reservations/admin', '예약 관리'],
  ['/courses/new', '코스 관리'],
  ['/staff/new', '스태프 관리'],
  ['/admin/announcements/new', '공지 관리'],
];

const loginInit = { userId: '', password: '' };
const signupInit = {
  userId: '',
  password: '',
  userName: '',
  userEmail: '',
  phoneNumber: '',
  birthdate: '',
  gender: '여성',
  maritalStatus: false,
  privacyConsent: false,
};
const guestInit = { courseId: '', reservationDateTime: '', name: '', phoneNumber: '' };
const memberInit = { courseId: '', reservationDateTime: '' };
const myPageInit = { userId: '', userName: '', userEmail: '', phoneNumber: '', password: '', packageCount: 0, memo: '' };
const reviewInit = { reviewerName: '', rating: 5, content: '', images: [] };

function App() {
  const [view, setView] = useState('home');
  const [loading, setLoading] = useState(true);
  const [csrf, setCsrf] = useState(null);
  const [notice, setNotice] = useState(null);
  const [auth, setAuth] = useState({ authenticated: false, userId: null, role: null });
  const [courses, setCourses] = useState([]);
  const [announcements, setAnnouncements] = useState([]);
  const [staff, setStaff] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [reviewSummary, setReviewSummary] = useState({ averageRating: 0, totalReviews: 0, ratingCounts: {}, recentImages: [] });
  const [myReservations, setMyReservations] = useState([]);
  const [guestReservations, setGuestReservations] = useState([]);
  const [myPage, setMyPage] = useState(myPageInit);
  const [searchPhone, setSearchPhone] = useState('');
  const [loginForm, setLoginForm] = useState(loginInit);
  const [signupForm, setSignupForm] = useState(signupInit);
  const [guestForm, setGuestForm] = useState(guestInit);
  const [memberForm, setMemberForm] = useState(memberInit);
  const [reviewForm, setReviewForm] = useState(reviewInit);
  const [editingReviewId, setEditingReviewId] = useState(null);

  const isAdmin = auth.role === 'ROLE_ADMIN';
  const heroImage = useMemo(() => heroImages[new Date().getDate() % heroImages.length], []);

  useEffect(() => {
    bootstrap();
  }, []);

  async function bootstrap() {
    setLoading(true);
    try {
      const [token, me, courseData, announcementData, staffData, reviewData] = await Promise.all([
        fetchCsrf(),
        api('/api/auth/me'),
        api('/api/courses'),
        api('/api/public/announcements'),
        api('/api/public/staff'),
        api('/api/reviews'),
      ]);

      setCsrf(token);
      setAuth(me || { authenticated: false, userId: null, role: null });
      setCourses(courseData || []);
      setAnnouncements(announcementData || []);
      setStaff(staffData || []);
      applyReviewState(reviewData);

      if (me?.authenticated) {
        await Promise.all([loadMyReservations(), loadMyPage()]);
      }
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '화면을 불러오지 못했습니다.' });
    } finally {
      setLoading(false);
    }
  }

  async function fetchCsrf() {
    const response = await fetch('/api/auth/csrf', {
      credentials: 'include',
      headers: { Accept: 'application/json' },
    });
    const payload = await parseResponse(response);
    const token = payload?.data || null;
    setCsrf(token);
    return token;
  }

  async function api(url, options = {}) {
    const { method = 'GET', headers = {}, body, useCsrf = false } = options;
    let token = csrf;
    if (useCsrf && !token) token = await fetchCsrf();

    const response = await fetch(url, {
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

    const payload = await parseResponse(response);
    if (!response.ok || payload?.success === false) {
      throw new Error(payload?.message || '서버 처리 중 오류가 발생했습니다.');
    }

    return payload?.data ?? null;
  }

  async function parseResponse(response) {
    const contentType = response.headers.get('content-type') || '';
    if (contentType.includes('application/json')) return response.json();
    const text = await response.text();
    return text ? { success: response.ok, message: text } : null;
  }

  async function loadMyReservations() {
    setMyReservations((await api('/api/reservations/me')) || []);
  }

  async function loadMyPage() {
    setMyPage((await api('/api/mypage')) || myPageInit);
  }

  async function refreshReviews() {
    applyReviewState(await api('/api/reviews'));
  }

  function applyReviewState(data) {
    setReviews(data?.reviews || []);
    setReviewSummary(data?.summary || { averageRating: 0, totalReviews: 0, ratingCounts: {}, recentImages: [] });
  }

  function update(setter) {
    return ({ target }) => setter((prev) => ({
      ...prev,
      [target.name]: target.type === 'checkbox' ? target.checked : target.value,
    }));
  }

  function jump(next) {
    setView(next);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function nextSlot() {
    const date = new Date();
    date.setMinutes(date.getMinutes() + 60);
    date.setSeconds(0, 0);
    date.setMinutes(date.getMinutes() + (30 - (date.getMinutes() % 30)) % 30);
    return date.toISOString().slice(0, 16);
  }

  function asset(path) {
    if (!path) return '';
    if (path.startsWith('http://') || path.startsWith('https://') || path.startsWith('/')) return path;
    return `/uploads/${path}`;
  }

  function formatDate(value) {
    if (!value) return '-';
    return new Intl.DateTimeFormat('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(new Date(value));
  }

  function formatMoney(value) {
    return `${new Intl.NumberFormat('ko-KR').format(Number(value || 0))}원`;
  }

  async function submitLogin(event) {
    event.preventDefault();
    try {
      await fetchCsrf();
      const form = new URLSearchParams({ userId: loginForm.userId, password: loginForm.password });
      await api('/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
        body: form,
        useCsrf: true,
      });
      await fetchCsrf();
      const me = await api('/api/auth/me');
      setAuth(me || { authenticated: false, userId: null, role: null });
      setLoginForm(loginInit);
      await Promise.all([loadMyReservations(), loadMyPage(), refreshReviews()]);
      setNotice({ type: 'success', text: '로그인되었습니다.' });
      jump('home');
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '로그인에 실패했습니다.' });
    }
  }

  async function submitSignup(event) {
    event.preventDefault();
    try {
      await api('/api/auth/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...signupForm, phoneNumber: signupForm.phoneNumber.replaceAll(/[^0-9]/g, '') }),
      });
      setSignupForm(signupInit);
      setNotice({ type: 'success', text: '회원가입이 완료되었습니다.' });
      jump('login');
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '회원가입에 실패했습니다.' });
    }
  }

  async function submitGuestReservation(event) {
    event.preventDefault();
    try {
      await api('/api/reservations/guest', {
        method: 'POST',
        useCsrf: true,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...guestForm,
          courseId: Number(guestForm.courseId),
          phoneNumber: guestForm.phoneNumber.replaceAll(/[^0-9]/g, ''),
        }),
      });
      setGuestForm({ ...guestInit, reservationDateTime: nextSlot() });
      setNotice({ type: 'success', text: '비회원 예약이 완료되었습니다.' });
      jump('guest-search');
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '비회원 예약에 실패했습니다.' });
    }
  }

  async function submitMemberReservation(event) {
    event.preventDefault();
    try {
      await api('/api/reservations/member', {
        method: 'POST',
        useCsrf: true,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...memberForm, courseId: Number(memberForm.courseId) }),
      });
      setMemberForm({ ...memberInit, reservationDateTime: nextSlot() });
      await loadMyReservations();
      setNotice({ type: 'success', text: '회원 예약이 완료되었습니다.' });
      jump('my-reservations');
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '회원 예약에 실패했습니다.' });
    }
  }

  async function searchGuestReservation(event) {
    event.preventDefault();
    try {
      const data = (await api(`/api/reservations/search?phoneNumber=${encodeURIComponent(searchPhone.replaceAll(/[^0-9]/g, ''))}`)) || [];
      setGuestReservations(data);
      setNotice({ type: 'info', text: data.length ? '예약 내역을 확인했습니다.' : '조회된 예약이 없습니다.' });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '비회원 예약 조회에 실패했습니다.' });
    }
  }

  async function submitMyPage(event) {
    event.preventDefault();
    try {
      await api('/api/mypage', {
        method: 'PUT',
        useCsrf: true,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(myPage),
      });
      await loadMyPage();
      setNotice({ type: 'success', text: '내 정보가 저장되었습니다.' });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '내 정보 저장에 실패했습니다.' });
    }
  }

  async function submitReview(event) {
    event.preventDefault();
    try {
      const formData = new FormData();
      if (!auth.authenticated) formData.append('reviewerName', reviewForm.reviewerName);
      formData.append('rating', String(reviewForm.rating));
      formData.append('content', reviewForm.content);
      Array.from(reviewForm.images || []).forEach((file) => formData.append('images', file));

      if (editingReviewId) {
        await api(`/api/reviews/${editingReviewId}`, { method: 'PUT', body: formData, useCsrf: true });
      } else {
        await api('/api/reviews', { method: 'POST', body: formData, useCsrf: true });
      }

      setReviewForm(reviewInit);
      setEditingReviewId(null);
      await refreshReviews();
      setNotice({ type: 'success', text: editingReviewId ? '리뷰가 수정되었습니다.' : '리뷰가 등록되었습니다.' });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '리뷰 저장에 실패했습니다.' });
    }
  }

  async function deleteReview(reviewId) {
    try {
      await api(`/api/reviews/${reviewId}`, { method: 'DELETE', useCsrf: true });
      await refreshReviews();
      if (editingReviewId === reviewId) {
        setReviewForm(reviewInit);
        setEditingReviewId(null);
      }
      setNotice({ type: 'success', text: '리뷰가 삭제되었습니다.' });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '리뷰 삭제에 실패했습니다.' });
    }
  }

  function startEditReview(review) {
    setEditingReviewId(review.id);
    setReviewForm({ reviewerName: review.reviewerName, rating: review.rating, content: review.content, images: [] });
    jump('reviews');
  }

  async function logout() {
    try {
      await api('/api/auth/logout', { method: 'POST', useCsrf: true });
      setAuth({ authenticated: false, userId: null, role: null });
      setMyPage(myPageInit);
      setMyReservations([]);
      setNotice({ type: 'success', text: '로그아웃되었습니다.' });
      jump('home');
      await fetchCsrf();
      await refreshReviews();
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '로그아웃에 실패했습니다.' });
    }
  }

  function renderNotice(targetViews) {
    if (!notice) return null;
    if (targetViews && !targetViews.includes(view)) return null;
    if (notice.type === 'success') return null;
    return <div className={`notice ${notice.type} inline`}>{notice.text}</div>;
  }

  if (loading) {
    return <div className="loading">차분한 회복 공간을 준비하고 있습니다.</div>;
  }

  return (
    <div className="site-shell">
      <header className="site-header">
        <div className="header-row">
          <div className="brand-block">
            <span className="brand-kicker">CALM THERAPY</span>
            <strong>평온한 회복의 예약 라운지</strong>
          </div>

          <nav className="header-nav">
            <button className={`nav-link ${view === 'home' ? 'active' : ''}`} onClick={() => jump('home')}>홈</button>
            <button className={`nav-link ${view === 'courses' ? 'active' : ''}`} onClick={() => jump('courses')}>프로그램</button>
            <button className={`nav-link ${view === 'announcements' ? 'active' : ''}`} onClick={() => jump('announcements')}>공지</button>
            <button className={`nav-link ${view === 'staff' ? 'active' : ''}`} onClick={() => jump('staff')}>스태프</button>
            <button className={`nav-link ${view === 'reviews' ? 'active' : ''}`} onClick={() => jump('reviews')}>리뷰</button>

            <div className={`nav-dropdown ${['guest-booking', 'guest-search', 'member-booking', 'my-reservations'].includes(view) ? 'active' : ''}`}>
              <span className="nav-link nav-label">예약</span>
              <div className="dropdown-menu">
                {!auth.authenticated && (
                  <>
                    <button onClick={() => jump('guest-booking')}>비회원 예약</button>
                    <button onClick={() => jump('guest-search')}>비회원 예약 확인</button>
                  </>
                )}
                {auth.authenticated && (
                  <>
                    <button onClick={() => jump('member-booking')}>회원 예약</button>
                    <button onClick={() => jump('my-reservations')}>내 예약 상태</button>
                  </>
                )}
              </div>
            </div>

            {auth.authenticated && <button className={`nav-link ${view === 'mypage' ? 'active' : ''}`} onClick={() => jump('mypage')}>마이페이지</button>}
            {isAdmin && <button className={`nav-link ${view === 'admin' ? 'active' : ''}`} onClick={() => jump('admin')}>관리자</button>}
          </nav>

          <div className="header-actions">
            {!auth.authenticated && <button className="button secondary" onClick={() => jump('login')}>로그인</button>}
            {!auth.authenticated && <button className="button" onClick={() => jump('guest-booking')}>예약하기</button>}
            {auth.authenticated && <button className="button secondary" onClick={() => jump('mypage')}>{myPage.userName || auth.userId} 님</button>}
            {auth.authenticated && <button className="button" onClick={logout}>로그아웃</button>}
          </div>
        </div>

        {view === 'home' ? (
          <div className="home-hero">
            <img src={heroImage} alt="테라피 메인 이미지" />
            <div className="home-hero-copy">
              <h1>편안한 회복, 가벼운 시작</h1>
              <p>예약과 리뷰, 회원 관리까지 하나의 차분한 흐름으로 이어지도록 구성했습니다.</p>
              <button className="button" onClick={() => jump(auth.authenticated ? 'member-booking' : 'guest-booking')}>지금 예약하기</button>
            </div>
          </div>
        ) : null}
      </header>

      <main className="page-stack">
        {view === 'home' && (
          <section className="panel split">
            <div>
              <p className="eyebrow">웰니스 스테이</p>
              <h2>예약 흐름은 단순하게, 관리 경험은 더 차분하게</h2>
              <p>메인만 큰 비주얼을 유지하고, 다른 페이지는 입력과 확인에 집중할 수 있도록 정리했습니다.</p>
            </div>
            <div className="feature-grid">
              <article><h3>프로그램 안내</h3><p>원하는 코스를 고르면 바로 예약으로 이어집니다.</p></article>
              <article><h3>고객 리뷰</h3><p>총 평점과 최근 이미지, 수정과 삭제 기능까지 함께 제공합니다.</p></article>
              <article><h3>마이페이지</h3><p>회원 정보 수정과 예약 상태 확인을 한곳에서 처리할 수 있습니다.</p></article>
            </div>
            {renderNotice(['home'])}
          </section>
        )}

        {view === 'courses' && (
          <section className="panel">
            <div className="card-grid">
              {courses.map((course) => (
                <article className="soft-card" key={course.id}>
                  <h3>{course.name}</h3>
                  <p>{course.durationMinutes}분 코스</p>
                  <p>회원가 {formatMoney(course.memberPrice)}</p>
                  <p>비회원가 {formatMoney(course.nonMemberPrice)}</p>
                  <button
                    className="button secondary"
                    onClick={() => {
                      const slot = nextSlot();
                      setGuestForm((prev) => ({ ...prev, courseId: String(course.id), reservationDateTime: slot }));
                      setMemberForm((prev) => ({ ...prev, courseId: String(course.id), reservationDateTime: slot }));
                      jump(auth.authenticated ? 'member-booking' : 'guest-booking');
                    }}
                  >
                    예약 선택
                  </button>
                </article>
              ))}
            </div>
          </section>
        )}

        {view === 'announcements' && (
          <section className="panel">
            <div className="list-grid">
              {announcements.map((item) => (
                <article className="list-card" key={item.id}>
                  <div className="list-head">
                    <h3>{item.title}</h3>
                    <span>{formatDate(item.createdAt)}</span>
                  </div>
                  <div className="html-content" dangerouslySetInnerHTML={{ __html: item.content || '<p>등록된 내용이 없습니다.</p>' }} />
                </article>
              ))}
            </div>
          </section>
        )}

        {view === 'staff' && (
          <section className="panel">
            <div className="card-grid">
              {staff.map((member) => (
                <article className="soft-card staff-card" key={member.id}>
                  <img src={asset(member.profilePicture) || heroImages[2]} alt={member.name} />
                  <h3>{member.name}</h3>
                  <p>차분한 응대와 안정적인 리듬으로 케어를 진행합니다.</p>
                </article>
              ))}
            </div>
          </section>
        )}

        {view === 'reviews' && (
          <section className="panel">
            <div className="review-summary">
              <div className="rating-panel">
                <strong>{Number(reviewSummary.averageRating || 0).toFixed(1)}</strong>
                <span>총 {reviewSummary.totalReviews}개의 리뷰</span>
                {[5, 4, 3, 2, 1].map((score) => (
                  <div className="rating-row" key={score}>
                    <label>{score}점</label>
                    <progress max={Math.max(reviewSummary.totalReviews || 1, 1)} value={reviewSummary.ratingCounts?.[score] || 0} />
                    <span>{reviewSummary.ratingCounts?.[score] || 0}</span>
                  </div>
                ))}
              </div>

              <div className="review-gallery">
                {(reviewSummary.recentImages || []).map((image, index) => (
                  <img key={`${image}-${index}`} src={asset(image)} alt={`리뷰 이미지 ${index + 1}`} />
                ))}
              </div>
            </div>

            <div className="review-layout">
              <form className="form-card" onSubmit={submitReview}>
                <h3>{editingReviewId ? '리뷰 수정' : '리뷰 작성'}</h3>
                {!auth.authenticated && (
                  <label>
                    <span>작성자 이름</span>
                    <input name="reviewerName" value={reviewForm.reviewerName} onChange={update(setReviewForm)} required />
                  </label>
                )}
                {auth.authenticated && <div className="readonly-box">{myPage.userName || auth.userId} 이름으로 리뷰가 등록됩니다.</div>}
                <label>
                  <span>별점</span>
                  <select name="rating" value={reviewForm.rating} onChange={update(setReviewForm)}>
                    {[5, 4, 3, 2, 1].map((score) => <option key={score} value={score}>{score}점</option>)}
                  </select>
                </label>
                <label>
                  <span>리뷰 내용</span>
                  <textarea name="content" value={reviewForm.content} onChange={update(setReviewForm)} rows="6" required />
                </label>
                <label>
                  <span>이미지 첨부</span>
                  <input type="file" multiple accept="image/*" onChange={(event) => setReviewForm((prev) => ({ ...prev, images: event.target.files || [] }))} />
                </label>
                <div className="button-row">
                  <button className="button" type="submit">{editingReviewId ? '수정 저장' : '리뷰 등록'}</button>
                  {editingReviewId && <button className="button secondary" type="button" onClick={() => { setReviewForm(reviewInit); setEditingReviewId(null); }}>수정 취소</button>}
                </div>
                {renderNotice(['reviews'])}
              </form>

              <div className="list-grid">
                {reviews.map((review) => (
                  <article className="list-card" key={review.id}>
                    <div className="list-head">
                      <h3>{review.reviewerName}</h3>
                      <span>{formatDate(review.createdAt)}</span>
                    </div>
                    <strong className="review-stars">{'★'.repeat(review.rating)}{'☆'.repeat(5 - review.rating)}</strong>
                    <p>{review.content}</p>
                    <div className="review-images">
                      {(review.imagePaths || []).map((image, index) => (
                        <img key={`${review.id}-${index}`} src={asset(image)} alt={`${review.reviewerName} 리뷰`} />
                      ))}
                    </div>
                    {(review.editable || review.deletable) && (
                      <div className="button-row">
                        {review.editable && <button className="button secondary" onClick={() => startEditReview(review)}>수정</button>}
                        {review.deletable && <button className="button secondary" onClick={() => deleteReview(review.id)}>삭제</button>}
                      </div>
                    )}
                  </article>
                ))}
              </div>
            </div>
          </section>
        )}

        {view === 'guest-booking' && (
          <section className="panel form-panel">
            <form className="form-card" onSubmit={submitGuestReservation}>
              <h2>비회원 전용 예약</h2>
              <label><span>코스</span><select name="courseId" value={guestForm.courseId} onChange={update(setGuestForm)} required><option value="">코스를 선택해 주세요</option>{courses.map((course) => <option key={course.id} value={course.id}>{course.name}</option>)}</select></label>
              <label><span>예약 일시</span><input type="datetime-local" name="reservationDateTime" value={guestForm.reservationDateTime} onChange={update(setGuestForm)} required /></label>
              <label><span>이름</span><input name="name" value={guestForm.name} onChange={update(setGuestForm)} required /></label>
              <label><span>전화번호</span><input name="phoneNumber" value={guestForm.phoneNumber} onChange={update(setGuestForm)} required /></label>
              <button className="button" type="submit">비회원 예약하기</button>
              {renderNotice(['guest-booking'])}
            </form>
          </section>
        )}

        {view === 'guest-search' && (
          <section className="panel">
            <form className="inline-form" onSubmit={searchGuestReservation}>
              <input value={searchPhone} onChange={(event) => setSearchPhone(event.target.value)} placeholder="예약 시 사용한 전화번호" required />
              <button className="button" type="submit">예약 확인</button>
            </form>
            {renderNotice(['guest-search'])}
            <div className="list-grid">
              {guestReservations.map((item) => (
                <article className="list-card" key={item.id}>
                  <div className="list-head">
                    <h3>{item.name || '비회원 예약'}</h3>
                    <span>{formatDate(item.reservationDateTime)}</span>
                  </div>
                  <p>코스 ID {item.courseId} / {item.status || '예약 완료'}</p>
                </article>
              ))}
            </div>
          </section>
        )}

        {view === 'member-booking' && (
          <section className="panel form-panel">
            <form className="form-card" onSubmit={submitMemberReservation}>
              <h2>회원 전용 예약</h2>
              <label><span>코스</span><select name="courseId" value={memberForm.courseId} onChange={update(setMemberForm)} required><option value="">코스를 선택해 주세요</option>{courses.map((course) => <option key={course.id} value={course.id}>{course.name}</option>)}</select></label>
              <label><span>예약 일시</span><input type="datetime-local" name="reservationDateTime" value={memberForm.reservationDateTime} onChange={update(setMemberForm)} required /></label>
              <button className="button" type="submit">회원 예약하기</button>
              {renderNotice(['member-booking'])}
            </form>
          </section>
        )}

        {view === 'my-reservations' && (
          <section className="panel">
            {renderNotice(['my-reservations'])}
            <div className="list-grid">
              {myReservations.map((item) => (
                <article className="list-card" key={item.id}>
                  <div className="list-head">
                    <h3>{myPage.userName || auth.userId}</h3>
                    <span>{formatDate(item.reservationDateTime)}</span>
                  </div>
                  <p>코스 ID {item.courseId} / {item.status || '예약 완료'}</p>
                </article>
              ))}
            </div>
          </section>
        )}

        {view === 'mypage' && (
          <section className="panel form-panel">
            <form className="form-card" onSubmit={submitMyPage}>
              <label><span>아이디</span><input value={myPage.userId || ''} readOnly /></label>
              <label><span>이름</span><input name="userName" value={myPage.userName || ''} onChange={update(setMyPage)} required /></label>
              <label><span>이메일</span><input name="userEmail" value={myPage.userEmail || ''} onChange={update(setMyPage)} required /></label>
              <label><span>전화번호</span><input name="phoneNumber" value={myPage.phoneNumber || ''} onChange={update(setMyPage)} required /></label>
              <label><span>새 비밀번호</span><input type="password" name="password" value={myPage.password || ''} onChange={update(setMyPage)} /></label>
              <button className="button" type="submit">내 정보 저장</button>
              {renderNotice(['mypage'])}
            </form>
          </section>
        )}

        {view === 'login' && (
          <section className="panel form-panel">
            <form className="form-card" onSubmit={submitLogin}>
              <h2>회원 로그인</h2>
              <label><span>아이디</span><input name="userId" value={loginForm.userId} onChange={update(setLoginForm)} required /></label>
              <label><span>비밀번호</span><input type="password" name="password" value={loginForm.password} onChange={update(setLoginForm)} required /></label>
              <button className="button" type="submit">로그인</button>
              {renderNotice(['login'])}
            </form>
          </section>
        )}

        {view === 'signup' && (
          <section className="panel form-panel">
            <form className="form-card" onSubmit={submitSignup}>
              <h2>멤버십 가입</h2>
              <div className="two-column">
                <label><span>아이디</span><input name="userId" value={signupForm.userId} onChange={update(setSignupForm)} required /></label>
                <label><span>비밀번호</span><input type="password" name="password" value={signupForm.password} onChange={update(setSignupForm)} required /></label>
              </div>
              <div className="two-column">
                <label><span>이름</span><input name="userName" value={signupForm.userName} onChange={update(setSignupForm)} required /></label>
                <label><span>이메일</span><input name="userEmail" value={signupForm.userEmail} onChange={update(setSignupForm)} required /></label>
              </div>
              <div className="two-column">
                <label><span>전화번호</span><input name="phoneNumber" value={signupForm.phoneNumber} onChange={update(setSignupForm)} required /></label>
                <label><span>생년월일</span><input type="date" name="birthdate" value={signupForm.birthdate} onChange={update(setSignupForm)} required /></label>
              </div>
              <div className="two-column">
                <label><span>성별</span><select name="gender" value={signupForm.gender} onChange={update(setSignupForm)}><option value="여성">여성</option><option value="남성">남성</option></select></label>
                <label><span>결혼 여부</span><select name="maritalStatus" value={signupForm.maritalStatus ? 'true' : 'false'} onChange={(event) => setSignupForm((prev) => ({ ...prev, maritalStatus: event.target.value === 'true' }))}><option value="false">미혼</option><option value="true">기혼</option></select></label>
              </div>
              <label className="consent-row"><input type="checkbox" name="privacyConsent" checked={signupForm.privacyConsent} onChange={update(setSignupForm)} required /><span>개인정보 수집 및 이용에 동의합니다.</span></label>
              <button className="button" type="submit">회원가입</button>
              {renderNotice(['signup'])}
            </form>
          </section>
        )}

        {view === 'admin' && isAdmin && (
          <section className="panel">
            <div className="card-grid">
              {adminLinks.map(([href, label]) => (
                <a className="soft-card admin-link" key={href} href={href}>
                  <h3>{label}</h3>
                  <p>기존 관리자 화면으로 이동해 수정과 삭제를 진행할 수 있습니다.</p>
                </a>
              ))}
            </div>
          </section>
        )}
      </main>
    </div>
  );
}

export default App;
