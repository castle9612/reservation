import { useEffect, useRef, useState } from 'react';

const fallbackHeroImage = `${import.meta.env.BASE_URL}hero-default.jpg`;

const adminPages = [
  ['admin-dashboard', '대시보드'],
  ['admin-content', '메인 관리'],
  ['admin-users', '회원 관리'],
  ['admin-reservations', '예약 관리'],
  ['admin-courses', '프로그램 관리'],
  ['admin-staff', '스태프 관리'],
  ['admin-announcements', '공지 관리'],
];

const viewPathMap = {
  home: '/',
  courses: '/programs',
  announcements: '/announcements',
  staff: '/therapists',
  reviews: '/reviews',
  'guest-booking': '/reservation/guest',
  'guest-search': '/reservation/search',
  'member-booking': '/reservation/member',
  'my-reservations': '/reservation/my',
  mypage: '/mypage',
  login: '/login',
  signup: '/signup',
  admin: '/admin',
  'admin-dashboard': '/admin/dashboard',
  'admin-content': '/admin/content',
  'admin-users': '/admin/users',
  'admin-reservations': '/admin/reservations',
  'admin-courses': '/admin/courses',
  'admin-staff': '/admin/staff',
  'admin-announcements': '/admin/announcements',
};

function resolveViewFromPath(pathname) {
  const normalized = pathname.endsWith('/') && pathname !== '/' ? pathname.slice(0, -1) : pathname;
  const matchedEntry = Object.entries(viewPathMap).find(([, path]) => path === normalized);
  if (matchedEntry?.[0] === 'admin') return 'admin-dashboard';
  return matchedEntry?.[0] || 'home';
}

const loginInit = { userId: '', password: '' };
const signupInit = {
  userId: '',
  password: '',
  passwordConfirm: '',
  userName: '',
  userEmail: '',
  phoneNumber: '',
  birthdate: '',
  gender: '여성',
  maritalStatus: false,
  privacyConsent: false,
};
const signupCheckInit = {
  userId: { status: 'idle', message: '' },
  userEmail: { status: 'idle', message: '' },
  phoneNumber: { status: 'idle', message: '' },
};
const guestInit = { courseId: '', reservationDateTime: '', name: '', phoneNumber: '' };
const guestSearchInit = { name: '', phoneNumber: '' };
const memberInit = { courseId: '', reservationDateTime: '' };
const myPageInit = { userId: '', userName: '', userEmail: '', phoneNumber: '', password: '', packageCount: 0, memo: '' };
const reviewInit = { reviewerName: '', rating: 5, content: '', images: [] };
const defaultSiteContent = {
  brandName: import.meta.env.VITE_STORE_NAME || '라본 바디 테라피',
  heroEyebrow: '웰니스 스테이',
  heroTitle: '편안한 회복, 가벼운 시작',
  heroDescription: '차분한 테라피 시간으로 몸의 긴장을 내려놓고, 다시 일상을 시작할 수 있도록 돕습니다.',
  heroImagePath: '',
  storeName: import.meta.env.VITE_STORE_NAME || '라본 바디 테라피',
  storeAddress: import.meta.env.VITE_STORE_ADDRESS || '예약 전 위치와 이동 동선을 먼저 확인해 두시면 보다 편안하게 방문하실 수 있습니다.',
  storePhone: import.meta.env.VITE_STORE_PHONE || '',
  locationDescription: '차분한 테라피 시간으로 이어질 수 있도록 매장 위치를 한눈에 확인하실 수 있게 준비했습니다.',
};
const courseFormInit = { name: '', staffId: '', durationMinutes: 60, memberPrice: 0, nonMemberPrice: 0 };
const staffFormInit = { name: '', profilePicture: '', profileImage: null };
const announcementFormInit = { title: '', content: '', newAttachmentFiles: [], deletedAttachmentPaths: [] };
const reservationFormInit = { id: '', courseId: '', reservationDateTime: '', name: '', phoneNumber: '', status: 'PENDING' };
const userFormInit = { userId: '', name: '', email: '', phoneNumber: '', role: 'USER', gender: '', birthdate: '', maritalStatus: false, packageCount: 0, memo: '' };
const reservationStatusOptions = [
  ['PENDING', '예약 대기'],
  ['CONFIRMED', '예약 확정'],
  ['COMPLETED', '이용 완료'],
  ['CANCELLED', '취소'],
  ['CANCELLED_USER', '회원 취소'],
  ['CANCELLED_ADMIN', '관리자 취소'],
  ['NO_SHOW', '노쇼'],
];
const userStatusSummaryOptions = [
  ['PENDING', '대기'],
  ['CONFIRMED', '확정'],
  ['COMPLETED', '완료'],
  ['CANCELLED', '취소'],
  ['CANCELLED_USER', '회원취소'],
  ['CANCELLED_ADMIN', '관리자취소'],
  ['NO_SHOW', '노쇼'],
];
const kakaoMapTimestamp = import.meta.env.VITE_KAKAO_MAP_TIMESTAMP || '1776303319351';
const kakaoMapKey = import.meta.env.VITE_KAKAO_MAP_KEY || '2acjrw73oay7';
const kakaoMapContainerId = `daumRoughmapContainer${kakaoMapTimestamp}`;

function App() {
  const brandLogoUrl = `${import.meta.env.BASE_URL}brand-logo.png`;
  const [brandLogoVisible, setBrandLogoVisible] = useState(true);
  const [view, setView] = useState(() => resolveViewFromPath(window.location.pathname));
  const [loading, setLoading] = useState(true);
  const [csrf, setCsrf] = useState(null);
  const [notice, setNotice] = useState(null);
  const [auth, setAuth] = useState({ authenticated: false, userId: null, role: null });
  const [siteContent, setSiteContent] = useState(defaultSiteContent);
  const [courses, setCourses] = useState([]);
  const [announcements, setAnnouncements] = useState([]);
  const [staff, setStaff] = useState([]);
  const [reviews, setReviews] = useState([]);
  const [reviewSummary, setReviewSummary] = useState({ averageRating: 0, totalReviews: 0, ratingCounts: {}, recentImages: [] });
  const [myReservations, setMyReservations] = useState([]);
  const [guestReservations, setGuestReservations] = useState([]);
  const [adminUsers, setAdminUsers] = useState([]);
  const [adminReservations, setAdminReservations] = useState([]);
  const [myPage, setMyPage] = useState(myPageInit);
  const [guestSearchForm, setGuestSearchForm] = useState(guestSearchInit);
  const [loginForm, setLoginForm] = useState(loginInit);
  const [signupForm, setSignupForm] = useState(signupInit);
  const [signupChecks, setSignupChecks] = useState(signupCheckInit);
  const [showSignupPassword, setShowSignupPassword] = useState(false);
  const [showSignupPasswordConfirm, setShowSignupPasswordConfirm] = useState(false);
  const [guestForm, setGuestForm] = useState(guestInit);
  const [memberForm, setMemberForm] = useState(memberInit);
  const [reviewForm, setReviewForm] = useState(reviewInit);
  const [editingReviewId, setEditingReviewId] = useState(null);
  const [siteContentForm, setSiteContentForm] = useState(defaultSiteContent);
  const [siteHeroImageFile, setSiteHeroImageFile] = useState(null);
  const [adminCourseForm, setAdminCourseForm] = useState(courseFormInit);
  const [editingCourseId, setEditingCourseId] = useState(null);
  const [adminStaffForm, setAdminStaffForm] = useState(staffFormInit);
  const [editingStaffId, setEditingStaffId] = useState(null);
  const [adminAnnouncementForm, setAdminAnnouncementForm] = useState(announcementFormInit);
  const [editingAnnouncementId, setEditingAnnouncementId] = useState(null);
  const [adminReservationForm, setAdminReservationForm] = useState(reservationFormInit);
  const [editingReservationId, setEditingReservationId] = useState(null);
  const [adminUserForm, setAdminUserForm] = useState(userFormInit);
  const [editingUserId, setEditingUserId] = useState(null);
  const mapContainerRef = useRef(null);

  const isAdmin = auth.role === 'ROLE_ADMIN';
  const heroImage = asset(siteContent.heroImagePath) || fallbackHeroImage;
  const passwordConfirmState = !signupForm.passwordConfirm
    ? null
    : signupForm.password === signupForm.passwordConfirm
      ? { status: 'success', message: '비밀번호가 일치합니다.' }
      : { status: 'error', message: '비밀번호가 일치하지 않습니다.' };

  useEffect(() => {
    bootstrap();
  }, []);

  useEffect(() => {
    const handlePopState = () => {
      setNotice(null);
      setView(resolveViewFromPath(window.location.pathname));
    };
    window.addEventListener('popstate', handlePopState);
    return () => window.removeEventListener('popstate', handlePopState);
  }, []);

  useEffect(() => {
    if (loading || view !== 'home' || !kakaoMapTimestamp || !kakaoMapKey || !mapContainerRef.current) {
      return undefined;
    }

    let isDisposed = false;
    let frameId = null;
    let rendered = false;

    const renderMap = () => {
      const container = document.getElementById(kakaoMapContainerId);
      if (isDisposed || rendered || !window.daum?.roughmap?.Lander || !container) return;

      rendered = true;

      const mapWidth = String(Math.max(container.clientWidth || container.offsetWidth || 640, 320));
      const mapHeight = window.innerWidth <= 720 ? '320' : '420';

      container.innerHTML = '';

      new window.daum.roughmap.Lander({
        timestamp: kakaoMapTimestamp,
        key: kakaoMapKey,
        mapWidth,
        mapHeight,
      }).render();
    };

    const scheduleRender = () => {
      if (frameId) {
        window.cancelAnimationFrame(frameId);
      }

      frameId = window.requestAnimationFrame(() => {
        renderMap();
        frameId = null;
      });
    };

    const ensureRoughmapLoader = () => {
      if (window.daum?.roughmap?.Lander) {
        scheduleRender();
        return undefined;
      }

      const protocol = window.location.protocol === 'https:' ? 'https:' : 'http:';
      const cdnVersion = '16137cec';

      window.daum = window.daum || {};
      window.daum.roughmap = window.daum.roughmap || {
        cdn: cdnVersion,
        URL_KEY_DATA_LOAD_PRE: `${protocol}//t1.daumcdn.net/roughmap/`,
        url_protocal: protocol,
      };

      const existingScript = document.querySelector('script[data-kakao-roughmap-lander="true"]');
      if (existingScript) {
        if (existingScript.dataset.loaded === 'true') {
          scheduleRender();
          return undefined;
        }

        existingScript.addEventListener('load', scheduleRender);
        return () => existingScript.removeEventListener('load', scheduleRender);
      }

      const script = document.createElement('script');
      script.src = `${protocol}//t1.daumcdn.net/kakaomapweb/place/jscss/roughmap/${cdnVersion}/roughmapLander.js`;
      script.async = true;
      script.charset = 'UTF-8';
      script.dataset.kakaoRoughmapLander = 'true';
      script.addEventListener('load', () => {
        script.dataset.loaded = 'true';
        scheduleRender();
      });
      document.body.appendChild(script);

      return undefined;
    };

    const cleanupLoader = ensureRoughmapLoader();

    return () => {
      isDisposed = true;
      if (frameId) {
        window.cancelAnimationFrame(frameId);
      }
      if (typeof cleanupLoader === 'function') cleanupLoader();
      const container = document.getElementById(kakaoMapContainerId);
      if (container) {
        container.innerHTML = '';
      }
    };
  }, [kakaoMapContainerId, kakaoMapKey, kakaoMapTimestamp, loading, view]);

  async function bootstrap() {
    setLoading(true);
    try {
      const [token, me, siteContentData, courseData, announcementData, staffData, reviewData] = await Promise.all([
        fetchCsrf(),
        api('/api/auth/me'),
        api('/api/site-content'),
        api('/api/courses'),
        api('/api/public/announcements'),
        api('/api/public/staff'),
        api('/api/reviews'),
      ]);

      setCsrf(token);
      setAuth(me || { authenticated: false, userId: null, role: null });
      applySiteContent(siteContentData);
      setCourses(courseData || []);
      setAnnouncements(announcementData || []);
      setStaff(staffData || []);
      applyReviewState(reviewData);

      if (me?.authenticated) {
        const followUpTasks = [loadMyReservations(), loadMyPage()];
        if (me?.role === 'ROLE_ADMIN') {
          followUpTasks.push(loadAdminUsers(), loadAdminReservations());
        }
        await Promise.all(followUpTasks);
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

  async function loadAdminUsers() {
    setAdminUsers((await api('/api/admin/users')) || []);
  }

  async function loadAdminReservations() {
    setAdminReservations((await api('/api/admin/reservations')) || []);
  }

  async function loadCourses() {
    setCourses((await api('/api/courses')) || []);
  }

  async function loadAnnouncements() {
    setAnnouncements((await api('/api/public/announcements')) || []);
  }

  async function loadStaff() {
    setStaff((await api('/api/public/staff')) || []);
  }

  async function loadSiteContent() {
    applySiteContent(await api('/api/site-content'));
  }

  async function refreshReviews() {
    applyReviewState(await api('/api/reviews'));
  }

  function applySiteContent(data) {
    const nextContent = { ...defaultSiteContent, ...(data || {}) };
    setSiteContent(nextContent);
    setSiteContentForm(nextContent);
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

  function normalizePhoneNumber(value) {
    return (value || '').replaceAll(/[^0-9]/g, '');
  }

  function formatPhoneNumber(value) {
    const digits = normalizePhoneNumber(value).slice(0, 11);
    if (!digits) return '';

    if (digits.startsWith('02')) {
      if (digits.length <= 2) return digits;
      if (digits.length <= 5) return `${digits.slice(0, 2)}-${digits.slice(2)}`;
      if (digits.length <= 9) return `${digits.slice(0, 2)}-${digits.slice(2, digits.length - 4)}-${digits.slice(-4)}`;
      return `${digits.slice(0, 2)}-${digits.slice(2, 6)}-${digits.slice(6)}`;
    }

    if (digits.length <= 3) return digits;
    if (digits.length <= 7) return `${digits.slice(0, 3)}-${digits.slice(3)}`;
    if (digits.length <= 10) return `${digits.slice(0, 3)}-${digits.slice(3, digits.length - 4)}-${digits.slice(-4)}`;
    return `${digits.slice(0, 3)}-${digits.slice(3, 7)}-${digits.slice(7)}`;
  }

  function isValidEmail(value) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test((value || '').trim());
  }

  function isValidKoreanPhoneNumber(value) {
    return /^(01[016789]\d{7,8}|02\d{7,8}|0[3-9]\d{8,9})$/.test(value || '');
  }

  function setSignupCheck(field, status, message) {
    setSignupChecks((prev) => ({
      ...prev,
      [field]: { status, message },
    }));
  }

  function handleSignupChange({ target }) {
    const { name, type, checked } = target;
    let nextValue = type === 'checkbox' ? checked : target.value;

    if (name === 'phoneNumber') {
      nextValue = formatPhoneNumber(nextValue);
    }

    setSignupForm((prev) => ({
      ...prev,
      [name]: nextValue,
    }));

    if (name in signupCheckInit) {
      setSignupCheck(name, 'idle', '');
    }
  }

  async function requestSignupAvailability(values) {
    const params = new URLSearchParams();
    if (values.userId?.trim()) params.set('userId', values.userId.trim());
    if (values.userEmail?.trim()) params.set('userEmail', values.userEmail.trim());
    if (values.phoneNumber?.trim()) params.set('phoneNumber', values.phoneNumber.trim());
    if (!params.toString()) return null;
    return api(`/api/auth/signup/availability?${params.toString()}`);
  }

  function renderSignupHint(field, overrideState = null) {
    const state = overrideState || signupChecks[field];
    if (!state || state.status === 'idle' || !state.message) return null;
    return <small className={`field-hint ${state.status}`}>{state.message}</small>;
  }

  useEffect(() => {
    const userId = signupForm.userId.trim();
    if (!userId) {
      setSignupCheck('userId', 'idle', '');
      return undefined;
    }

    let cancelled = false;
    const timer = window.setTimeout(async () => {
      setSignupCheck('userId', 'loading', '아이디 확인 중...');
      try {
        const data = await requestSignupAvailability({ userId });
        if (cancelled) return;
        setSignupCheck('userId', data?.userIdAvailable ? 'success' : 'error', data?.userIdAvailable ? '사용 가능한 아이디입니다.' : '이미 사용 중인 아이디입니다.');
      } catch (error) {
        if (!cancelled) setSignupCheck('userId', 'error', '아이디 확인 중 오류가 발생했습니다.');
      }
    }, 350);

    return () => {
      cancelled = true;
      window.clearTimeout(timer);
    };
  }, [signupForm.userId]);

  useEffect(() => {
    const userEmail = signupForm.userEmail.trim();
    if (!userEmail) {
      setSignupCheck('userEmail', 'idle', '');
      return undefined;
    }

    if (!isValidEmail(userEmail)) {
      setSignupCheck('userEmail', 'error', '올바른 이메일 형식을 입력해 주세요.');
      return undefined;
    }

    let cancelled = false;
    const timer = window.setTimeout(async () => {
      setSignupCheck('userEmail', 'loading', '이메일 확인 중...');
      try {
        const data = await requestSignupAvailability({ userEmail });
        if (cancelled) return;
        setSignupCheck('userEmail', data?.userEmailAvailable ? 'success' : 'error', data?.userEmailAvailable ? '사용 가능한 이메일입니다.' : '이미 사용 중인 이메일입니다.');
      } catch (error) {
        if (!cancelled) setSignupCheck('userEmail', 'error', '이메일 확인 중 오류가 발생했습니다.');
      }
    }, 350);

    return () => {
      cancelled = true;
      window.clearTimeout(timer);
    };
  }, [signupForm.userEmail]);

  useEffect(() => {
    const normalizedPhoneNumber = normalizePhoneNumber(signupForm.phoneNumber);
    if (!normalizedPhoneNumber) {
      setSignupCheck('phoneNumber', 'idle', '');
      return undefined;
    }

    if (!isValidKoreanPhoneNumber(normalizedPhoneNumber)) {
      setSignupCheck('phoneNumber', 'error', '올바른 전화번호를 입력해 주세요.');
      return undefined;
    }

    let cancelled = false;
    const timer = window.setTimeout(async () => {
      setSignupCheck('phoneNumber', 'loading', '전화번호 확인 중...');
      try {
        const data = await requestSignupAvailability({ phoneNumber: normalizedPhoneNumber });
        if (cancelled) return;
        setSignupCheck('phoneNumber', data?.phoneNumberAvailable ? 'success' : 'error', data?.phoneNumberAvailable ? '사용 가능한 전화번호입니다.' : '이미 사용 중인 전화번호입니다.');
      } catch (error) {
        if (!cancelled) setSignupCheck('phoneNumber', 'error', '전화번호 확인 중 오류가 발생했습니다.');
      }
    }, 350);

    return () => {
      cancelled = true;
      window.clearTimeout(timer);
    };
  }, [signupForm.phoneNumber]);

  function jump(next, options = {}) {
    const nextPath = viewPathMap[next] || '/';
    if (options.replace) {
      window.history.replaceState({}, '', nextPath);
    } else if (window.location.pathname !== nextPath) {
      window.history.pushState({}, '', nextPath);
    }
    setNotice(null);
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

  function toDateTimeInputValue(value) {
    if (!value) return '';
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return '';
    const offsetDate = new Date(date.getTime() - date.getTimezoneOffset() * 60000);
    return offsetDate.toISOString().slice(0, 16);
  }

  function appendJsonPart(formData, name, value) {
    formData.append(name, new Blob([JSON.stringify(value)], { type: 'application/json' }));
  }

  function normalizeRole(role) {
    const normalized = String(role || 'USER').replace(/^ROLE_/, '').toUpperCase();
    return normalized === 'ADMIN' ? 'ADMIN' : 'USER';
  }

  function statusLabel(status) {
    const normalized = String(status || 'PENDING').toUpperCase();
    return reservationStatusOptions.find(([value]) => value === normalized)?.[1] || normalized;
  }

  function mergedCancelCount(counts = {}) {
    return Number(counts.CANCELLED || 0) + Number(counts.CANCELLED_USER || 0) + Number(counts.CANCELLED_ADMIN || 0);
  }

  function attachmentName(announcement, index, path) {
    return announcement?.originalAttachmentNames?.[index] || path?.split('/').pop() || `첨부파일 ${index + 1}`;
  }

  function reservationOwnerLabel(reservation) {
    return reservation.userId || reservation.name || '비회원 예약';
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
      if (!me?.authenticated) {
        throw new Error('아이디 또는 비밀번호가 올바르지 않습니다.');
      }
      setAuth(me || { authenticated: false, userId: null, role: null });
      setLoginForm(loginInit);
      const followUpTasks = [refreshReviews()];
      followUpTasks.push(loadMyReservations());
      followUpTasks.push(loadMyPage());
      if (me?.role === 'ROLE_ADMIN') {
        followUpTasks.push(loadAdminUsers(), loadAdminReservations());
      }
      setNotice({ type: 'success', text: '로그인되었습니다.' });
      jump('home');
      await Promise.allSettled(followUpTasks);
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '로그인에 실패했습니다.' });
    }
  }

  async function submitSignup(event) {
    event.preventDefault();
    const normalizedPhoneNumber = normalizePhoneNumber(signupForm.phoneNumber);

    if (signupForm.password !== signupForm.passwordConfirm) {
      setNotice({ type: 'error', text: '비밀번호 확인이 일치하지 않습니다.' });
      return;
    }

    if (!isValidEmail(signupForm.userEmail)) {
      setSignupCheck('userEmail', 'error', '올바른 이메일 형식을 입력해 주세요.');
      setNotice({ type: 'error', text: '이메일 형식을 확인해 주세요.' });
      return;
    }

    if (!isValidKoreanPhoneNumber(normalizedPhoneNumber)) {
      setSignupCheck('phoneNumber', 'error', '올바른 전화번호를 입력해 주세요.');
      setNotice({ type: 'error', text: '전화번호 형식을 확인해 주세요.' });
      return;
    }

    try {
      const availability = await requestSignupAvailability({
        userId: signupForm.userId,
        userEmail: signupForm.userEmail,
        phoneNumber: normalizedPhoneNumber,
      });

      setSignupChecks({
        userId: {
          status: availability?.userIdAvailable ? 'success' : 'error',
          message: availability?.userIdAvailable ? '사용 가능한 아이디입니다.' : '이미 사용 중인 아이디입니다.',
        },
        userEmail: {
          status: availability?.userEmailAvailable ? 'success' : 'error',
          message: availability?.userEmailAvailable ? '사용 가능한 이메일입니다.' : '이미 사용 중인 이메일입니다.',
        },
        phoneNumber: {
          status: availability?.phoneNumberAvailable ? 'success' : 'error',
          message: availability?.phoneNumberAvailable ? '사용 가능한 전화번호입니다.' : '이미 사용 중인 전화번호입니다.',
        },
      });

      if (availability?.userIdAvailable === false) {
        setNotice({ type: 'error', text: '이미 사용 중인 아이디입니다.' });
        return;
      }

      if (availability?.userEmailAvailable === false) {
        setNotice({ type: 'error', text: '이미 사용 중인 이메일입니다.' });
        return;
      }

      if (availability?.phoneNumberAvailable === false) {
        setNotice({ type: 'error', text: '이미 사용 중인 전화번호입니다.' });
        return;
      }

      const { passwordConfirm, ...signupPayload } = signupForm;
      await api('/api/auth/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...signupPayload, phoneNumber: normalizedPhoneNumber }),
      });
      setSignupForm(signupInit);
      setSignupChecks(signupCheckInit);
      setShowSignupPassword(false);
      setShowSignupPasswordConfirm(false);
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
      setGuestSearchForm({ name: guestForm.name, phoneNumber: formatPhoneNumber(guestForm.phoneNumber) });
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
      const normalizedPhoneNumber = normalizePhoneNumber(guestSearchForm.phoneNumber);
      const name = guestSearchForm.name.trim();
      if (!name) {
        setNotice({ type: 'error', text: '예약자명을 입력해 주세요.' });
        return;
      }
      if (!isValidKoreanPhoneNumber(normalizedPhoneNumber)) {
        setNotice({ type: 'error', text: '올바른 전화번호를 입력해 주세요.' });
        return;
      }

      const params = new URLSearchParams({ name, phoneNumber: normalizedPhoneNumber });
      const data = (await api(`/api/reservations/search?${params.toString()}`)) || [];
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

  async function submitSiteContent(event) {
    event.preventDefault();
    try {
      const formData = new FormData();
      appendJsonPart(formData, 'content', siteContentForm);
      if (siteHeroImageFile) formData.append('heroImage', siteHeroImageFile);

      const savedContent = await api('/api/admin/site-content', { method: 'PUT', body: formData, useCsrf: true });
      applySiteContent(savedContent);
      setSiteHeroImageFile(null);
      setNotice({ type: 'success', text: '메인 화면 사진과 문구가 저장되었습니다.' });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '메인 화면 저장에 실패했습니다.' });
    }
  }

  function resetAdminCourseForm() {
    setEditingCourseId(null);
    setAdminCourseForm(courseFormInit);
  }

  function startEditCourse(course) {
    setEditingCourseId(course.id);
    setAdminCourseForm({
      name: course.name || '',
      staffId: course.staffId ? String(course.staffId) : '',
      durationMinutes: course.durationMinutes || 60,
      memberPrice: course.memberPrice || 0,
      nonMemberPrice: course.nonMemberPrice || 0,
    });
  }

  async function submitAdminCourse(event) {
    event.preventDefault();
    try {
      const payload = {
        ...adminCourseForm,
        staffId: adminCourseForm.staffId ? Number(adminCourseForm.staffId) : null,
        durationMinutes: Number(adminCourseForm.durationMinutes || 0),
        memberPrice: Number(adminCourseForm.memberPrice || 0),
        nonMemberPrice: Number(adminCourseForm.nonMemberPrice || 0),
      };
      await api(editingCourseId ? `/api/admin/courses/${editingCourseId}` : '/api/admin/courses', {
        method: editingCourseId ? 'PUT' : 'POST',
        useCsrf: true,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });
      await loadCourses();
      resetAdminCourseForm();
      setNotice({ type: 'success', text: editingCourseId ? '코스가 수정되었습니다.' : '코스가 등록되었습니다.' });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '코스 저장에 실패했습니다.' });
    }
  }

  async function deleteAdminCourse(courseId) {
    if (!window.confirm('이 코스와 관련 예약을 함께 삭제하시겠습니까?')) return;
    try {
      await api(`/api/admin/courses/${courseId}`, { method: 'DELETE', useCsrf: true });
      await Promise.all([loadCourses(), loadAdminReservations(), loadMyReservations()]);
      if (editingCourseId === courseId) resetAdminCourseForm();
      setNotice({ type: 'success', text: '코스가 삭제되었습니다.' });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '코스 삭제에 실패했습니다.' });
    }
  }

  function resetAdminStaffForm() {
    setEditingStaffId(null);
    setAdminStaffForm(staffFormInit);
  }

  function startEditStaff(member) {
    setEditingStaffId(member.id);
    setAdminStaffForm({ name: member.name || '', profilePicture: member.profilePicture || '', profileImage: null });
  }

  async function submitAdminStaff(event) {
    event.preventDefault();
    try {
      const formData = new FormData();
      appendJsonPart(formData, 'staff', {
        id: editingStaffId,
        name: adminStaffForm.name,
        profilePicture: adminStaffForm.profilePicture,
      });
      if (adminStaffForm.profileImage) formData.append('profileImage', adminStaffForm.profileImage);

      await api(editingStaffId ? `/api/admin/staff/${editingStaffId}` : '/api/admin/staff', {
        method: editingStaffId ? 'PUT' : 'POST',
        body: formData,
        useCsrf: true,
      });
      await loadStaff();
      resetAdminStaffForm();
      setNotice({ type: 'success', text: editingStaffId ? '스태프가 수정되었습니다.' : '스태프가 등록되었습니다.' });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '스태프 저장에 실패했습니다.' });
    }
  }

  async function deleteAdminStaff(staffId) {
    if (!window.confirm('이 스태프를 삭제하고 담당 코스 연결을 해제하시겠습니까?')) return;
    try {
      await api(`/api/admin/staff/${staffId}`, { method: 'DELETE', useCsrf: true });
      await Promise.all([loadStaff(), loadCourses()]);
      if (editingStaffId === staffId) resetAdminStaffForm();
      setNotice({ type: 'success', text: '스태프가 삭제되었습니다.' });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '스태프 삭제에 실패했습니다.' });
    }
  }

  function resetAdminAnnouncementForm() {
    setEditingAnnouncementId(null);
    setAdminAnnouncementForm(announcementFormInit);
  }

  function startEditAnnouncement(announcement) {
    setEditingAnnouncementId(announcement.id);
    setAdminAnnouncementForm({
      title: announcement.title || '',
      content: announcement.content || '',
      newAttachmentFiles: [],
      deletedAttachmentPaths: [],
    });
  }

  async function submitAdminAnnouncement(event) {
    event.preventDefault();
    try {
      const formData = new FormData();
      appendJsonPart(formData, 'announcement', {
        id: editingAnnouncementId,
        title: adminAnnouncementForm.title,
        content: adminAnnouncementForm.content,
      });
      Array.from(adminAnnouncementForm.newAttachmentFiles || []).forEach((file) => formData.append('newAttachmentFiles', file));
      (adminAnnouncementForm.deletedAttachmentPaths || []).forEach((path) => formData.append('deletedAttachmentPaths', path));

      await api(editingAnnouncementId ? `/api/admin/announcements/${editingAnnouncementId}` : '/api/admin/announcements', {
        method: editingAnnouncementId ? 'PUT' : 'POST',
        body: formData,
        useCsrf: true,
      });
      await loadAnnouncements();
      resetAdminAnnouncementForm();
      setNotice({ type: 'success', text: editingAnnouncementId ? '공지사항이 수정되었습니다.' : '공지사항이 등록되었습니다.' });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '공지사항 저장에 실패했습니다.' });
    }
  }

  async function insertAnnouncementBodyImage(file) {
    if (!file) return;
    try {
      const formData = new FormData();
      formData.append('image', file);
      const uploaded = await api('/api/admin/announcements/images', { method: 'POST', body: formData, useCsrf: true });
      if (!uploaded?.url) {
        throw new Error('이미지 업로드 응답을 확인할 수 없습니다.');
      }

      const imageMarkup = `<p><img src="${uploaded.url}" alt="${file.name.replaceAll('"', '')}" /></p>`;
      setAdminAnnouncementForm((prev) => ({
        ...prev,
        content: `${prev.content || ''}\n${imageMarkup}`,
      }));
      setNotice({ type: 'success', text: '본문에 이미지가 추가되었습니다. 저장을 눌러 공지에 반영해 주세요.' });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '본문 이미지 업로드에 실패했습니다.' });
    }
  }

  async function deleteAdminAnnouncement(announcementId) {
    if (!window.confirm('이 공지사항을 삭제하시겠습니까?')) return;
    try {
      await api(`/api/admin/announcements/${announcementId}`, { method: 'DELETE', useCsrf: true });
      await loadAnnouncements();
      if (editingAnnouncementId === announcementId) resetAdminAnnouncementForm();
      setNotice({ type: 'success', text: '공지사항이 삭제되었습니다.' });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '공지사항 삭제에 실패했습니다.' });
    }
  }

  function resetAdminReservationForm() {
    setEditingReservationId(null);
    setAdminReservationForm(reservationFormInit);
  }

  function startEditReservation(reservation) {
    setEditingReservationId(reservation.id);
    setAdminReservationForm({
      id: reservation.id,
      courseId: reservation.courseId ? String(reservation.courseId) : '',
      reservationDateTime: toDateTimeInputValue(reservation.reservationDateTime),
      name: reservation.name || '',
      phoneNumber: formatPhoneNumber(reservation.phoneNumber || ''),
      status: reservation.status || 'PENDING',
    });
  }

  async function submitAdminReservation(event) {
    event.preventDefault();
    if (!editingReservationId) {
      setNotice({ type: 'error', text: '수정할 예약을 선택해 주세요.' });
      return;
    }
    try {
      await api(`/api/admin/reservations/${editingReservationId}`, {
        method: 'PUT',
        useCsrf: true,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          courseId: adminReservationForm.courseId ? Number(adminReservationForm.courseId) : null,
          reservationDateTime: adminReservationForm.reservationDateTime,
          name: adminReservationForm.name,
          phoneNumber: normalizePhoneNumber(adminReservationForm.phoneNumber),
          status: adminReservationForm.status,
        }),
      });
      await Promise.all([loadAdminReservations(), loadMyReservations()]);
      resetAdminReservationForm();
      setNotice({ type: 'success', text: '예약이 수정되었습니다.' });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '예약 수정에 실패했습니다.' });
    }
  }

  async function deleteAdminReservation(reservationId) {
    if (!window.confirm('이 예약을 삭제하시겠습니까?')) return;
    try {
      await api(`/api/admin/reservations/${reservationId}`, { method: 'DELETE', useCsrf: true });
      await Promise.all([loadAdminReservations(), loadMyReservations()]);
      if (editingReservationId === reservationId) resetAdminReservationForm();
      setNotice({ type: 'success', text: '예약이 삭제되었습니다.' });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '예약 삭제에 실패했습니다.' });
    }
  }

  function resetAdminUserForm() {
    setEditingUserId(null);
    setAdminUserForm(userFormInit);
  }

  function startEditUser(user) {
    setEditingUserId(user.userId);
    setAdminUserForm({
      userId: user.userId || '',
      name: user.name || '',
      email: user.email || '',
      phoneNumber: formatPhoneNumber(user.phoneNumber || ''),
      role: normalizeRole(user.role),
      gender: user.gender || '',
      birthdate: user.birthdate || '',
      maritalStatus: Boolean(user.maritalStatus),
      packageCount: user.packageCount || 0,
      memo: user.memo || '',
    });
  }

  async function submitAdminUser(event) {
    event.preventDefault();
    if (!editingUserId) {
      setNotice({ type: 'error', text: '수정할 회원을 선택해 주세요.' });
      return;
    }
    try {
      await api(`/api/admin/users/${encodeURIComponent(editingUserId)}`, {
        method: 'PUT',
        useCsrf: true,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          ...adminUserForm,
          phoneNumber: normalizePhoneNumber(adminUserForm.phoneNumber),
          packageCount: Number(adminUserForm.packageCount || 0),
          role: normalizeRole(adminUserForm.role),
        }),
      });
      await Promise.all([loadAdminUsers(), loadMyPage()]);
      resetAdminUserForm();
      setNotice({ type: 'success', text: '회원 정보가 수정되었습니다.' });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '회원 정보 수정에 실패했습니다.' });
    }
  }

  async function deleteAdminUser(userId) {
    if (!window.confirm(`${userId} 회원을 삭제하시겠습니까? 예약 기록은 운영 기록으로 보존됩니다.`)) return;
    try {
      await api(`/api/admin/users/${encodeURIComponent(userId)}`, { method: 'DELETE', useCsrf: true });
      await loadAdminUsers();
      if (editingUserId === userId) resetAdminUserForm();
      setNotice({ type: 'success', text: '회원이 삭제되었습니다.' });
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '회원 삭제에 실패했습니다.' });
    }
  }

  async function logout() {
    try {
      await api('/api/auth/logout', { method: 'POST', useCsrf: true });
      setAuth({ authenticated: false, userId: null, role: null });
      setMyPage(myPageInit);
      setMyReservations([]);
      setAdminUsers([]);
      setAdminReservations([]);
      setNotice({ type: 'success', text: '로그아웃되었습니다.' });
      jump('home');
      await fetchCsrf();
      await refreshReviews();
    } catch (error) {
      setNotice({ type: 'error', text: error.message || '로그아웃에 실패했습니다.' });
    }
  }

  function renderAdminToolbar() {
    return (
      <div className="admin-toolbar">
        {adminPages.map(([adminView, label]) => (
          <button
            key={adminView}
            className={`button ${view === adminView ? '' : 'secondary'}`}
            type="button"
            onClick={() => jump(adminView)}
          >
            {label}
          </button>
        ))}
      </div>
    );
  }

  function renderNotice(targetViews) {
    if (!notice) return null;
    if (targetViews && !targetViews.includes(view)) return null;
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
            <img
              className="brand-logo"
              src={brandLogoUrl}
              alt="라본 바디 테라피 로고"
              style={{ display: brandLogoVisible ? 'block' : 'none' }}
              onLoad={() => setBrandLogoVisible(true)}
              onError={(event) => {
                setBrandLogoVisible(false);
                event.currentTarget.style.display = 'none';
              }}
            />
            {!brandLogoVisible && (
              <>
                <span className="brand-kicker">CALM THERAPY</span>
                <strong>평온한 회복의 예약 라운지</strong>
              </>
            )}
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
            {isAdmin && <button className={`nav-link ${view.startsWith('admin') ? 'active' : ''}`} onClick={() => jump('admin-dashboard')}>관리자</button>}
          </nav>

          <div className="header-actions">
            {!auth.authenticated && (
              <>
                <button className="utility-button" onClick={() => jump('guest-search')}>비회원 예약조회</button>
                <div className="auth-pair" role="group" aria-label="로그인 및 회원가입">
                  <button type="button" onClick={() => jump('login')}>로그인</button>
                  <span className="auth-divider" aria-hidden="true" />
                  <button type="button" onClick={() => jump('signup')}>회원가입</button>
                </div>
                <button className="button header-cta" onClick={() => jump('guest-booking')}>예약하기</button>
              </>
            )}

            {auth.authenticated && (
              <>
                <button className="utility-button" onClick={() => jump('my-reservations')}>내 예약</button>
                <div className="auth-pair" role="group" aria-label="회원 메뉴">
                  <button type="button" onClick={() => jump('mypage')}>{myPage.userName || auth.userId} 님</button>
                  <span className="auth-divider" aria-hidden="true" />
                  <button type="button" onClick={() => jump('mypage')}>마이페이지</button>
                </div>
                <button className="button header-cta" onClick={logout}>로그아웃</button>
              </>
            )}
          </div>
        </div>

        {view === 'home' ? (
          <div className="home-hero">
            <img src={heroImage} alt="테라피 메인 이미지" />
            <div className="home-hero-copy">
              <p className="eyebrow">{siteContent.heroEyebrow}</p>
              <h1>{siteContent.heroTitle}</h1>
              <p>{siteContent.heroDescription}</p>
              <div className="hero-rating">
                <strong>{Number(reviewSummary.averageRating || 0).toFixed(1)}</strong>
                <span>고객 평점</span>
                <small>리뷰 {reviewSummary.totalReviews}개 기준</small>
              </div>
              <button className="button" onClick={() => jump(auth.authenticated ? 'member-booking' : 'guest-booking')}>지금 예약하기</button>
            </div>
          </div>
        ) : null}
      </header>

      <main className="page-stack">
        {view === 'home' && (
          <section className="panel split">
            <div>
              <p className="eyebrow">{siteContent.heroEyebrow}</p>
              <h2>{siteContent.brandName}</h2>
              <p>{siteContent.heroDescription}</p>
            </div>
            <div className="feature-grid">
              <article>
                <h3>{Number(reviewSummary.averageRating || 0).toFixed(1)}</h3>
                <p>평균 평점</p>
              </article>
              <article>
                <h3>{reviewSummary.totalReviews || 0}</h3>
                <p>누적 리뷰</p>
              </article>
              <article>
                <h3>{courses.length}</h3>
                <p>운영 프로그램</p>
              </article>
            </div>
          </section>
        )}

        {view === 'home' && (
          <section className="panel location-panel">
              <div className="location-copy">
                <p className="eyebrow">Location</p>
                <h2>오시는 길</h2>
              <p>{siteContent.locationDescription}</p>

              <div className="location-detail">
                <strong>{siteContent.storeName}</strong>
                <span>{siteContent.storeAddress}</span>
                {siteContent.storePhone ? <span>예약 및 문의 {siteContent.storePhone}</span> : null}
              </div>
            </div>

            <div className="location-map-shell">
              {kakaoMapKey && kakaoMapTimestamp ? (
                <div
                  id={kakaoMapContainerId}
                  ref={mapContainerRef}
                  className="kakao-map-canvas root_daum_roughmap root_daum_roughmap_landing"
                />
              ) : (
                <div className="map-placeholder">
                  <strong>{siteContent.storeName}</strong>
                  <p>{siteContent.storeAddress}</p>
                  <span>카카오 지도 퍼가기 키를 연결하면 실제 매장 지도가 이 영역에 표시됩니다.</span>
                </div>
              )}
            </div>
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
                  {(item.attachmentPaths || []).length > 0 && (
                    <div className="attachment-links">
                      {(item.attachmentPaths || []).map((path, index) => (
                        <a key={`${item.id}-${path}`} href={`/announcement/attachment/${item.id}/${index}`}>
                          {attachmentName(item, index, path)}
                        </a>
                      ))}
                    </div>
                  )}
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
                  <img src={asset(member.profilePicture) || fallbackHeroImage} alt={member.name} />
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
              <input
                value={guestSearchForm.name}
                onChange={(event) => setGuestSearchForm((prev) => ({ ...prev, name: event.target.value }))}
                placeholder="예약자명"
                required
              />
              <input
                value={guestSearchForm.phoneNumber}
                onChange={(event) => setGuestSearchForm((prev) => ({ ...prev, phoneNumber: formatPhoneNumber(event.target.value) }))}
                placeholder="예약 시 사용한 전화번호"
                required
              />
              <button className="button" type="submit">예약 확인</button>
            </form>
            {renderNotice(['guest-search'])}
            <div className="list-grid">
              {guestReservations.map((item, index) => (
                <article className="list-card" key={item.id || `${item.reservationDateTime}-${index}`}>
                  <div className="list-head">
                    <h3>{item.name || '비회원 예약'}</h3>
                    <span>{formatDate(item.reservationDateTime)}</span>
                  </div>
                  <p>코스 ID {item.courseId} / {statusLabel(item.status)}</p>
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
                  <p>코스 ID {item.courseId} / {statusLabel(item.status)}</p>
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
                <label>
                  <span>아이디</span>
                  <input name="userId" value={signupForm.userId} onChange={handleSignupChange} autoComplete="username" required />
                  {renderSignupHint('userId')}
                </label>
                <label>
                  <span>비밀번호</span>
                  <div className="password-field">
                    <input
                      type={showSignupPassword ? 'text' : 'password'}
                      name="password"
                      value={signupForm.password}
                      onChange={handleSignupChange}
                      autoComplete="new-password"
                      required
                    />
                    <button
                      className="password-toggle"
                      type="button"
                      aria-label={showSignupPassword ? '비밀번호 숨기기' : '비밀번호 보기'}
                      onClick={() => setShowSignupPassword((prev) => !prev)}
                    >
                      <svg viewBox="0 0 24 24" aria-hidden="true">
                        <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6Z" />
                        <circle cx="12" cy="12" r="3.2" />
                        {showSignupPassword ? null : <path d="M4 4l16 16" />}
                      </svg>
                    </button>
                  </div>
                </label>
              </div>
              <div className="two-column">
                <label>
                  <span>비밀번호 확인</span>
                  <div className="password-field">
                    <input
                      type={showSignupPasswordConfirm ? 'text' : 'password'}
                      name="passwordConfirm"
                      value={signupForm.passwordConfirm}
                      onChange={handleSignupChange}
                      autoComplete="new-password"
                      required
                    />
                    <button
                      className="password-toggle"
                      type="button"
                      aria-label={showSignupPasswordConfirm ? '비밀번호 숨기기' : '비밀번호 보기'}
                      onClick={() => setShowSignupPasswordConfirm((prev) => !prev)}
                    >
                      <svg viewBox="0 0 24 24" aria-hidden="true">
                        <path d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6Z" />
                        <circle cx="12" cy="12" r="3.2" />
                        {showSignupPasswordConfirm ? null : <path d="M4 4l16 16" />}
                      </svg>
                    </button>
                  </div>
                  {passwordConfirmState ? renderSignupHint('passwordConfirm', passwordConfirmState) : null}
                </label>
                <label><span>이름</span><input name="userName" value={signupForm.userName} onChange={handleSignupChange} required /></label>
              </div>
              <div className="two-column">
                <label>
                  <span>이메일</span>
                  <input name="userEmail" type="email" value={signupForm.userEmail} onChange={handleSignupChange} autoComplete="email" required />
                  {renderSignupHint('userEmail')}
                </label>
                <label><span>생년월일</span><input type="date" name="birthdate" value={signupForm.birthdate} onChange={handleSignupChange} required /></label>
              </div>
              <div className="two-column">
                <label>
                  <span>전화번호</span>
                  <input name="phoneNumber" value={signupForm.phoneNumber} onChange={handleSignupChange} autoComplete="tel" required />
                  {renderSignupHint('phoneNumber')}
                </label>
                <label><span>성별</span><select name="gender" value={signupForm.gender} onChange={handleSignupChange}><option value="여성">여성</option><option value="남성">남성</option></select></label>
              </div>
              <div className="two-column">
                <label><span>결혼 여부</span><select name="maritalStatus" value={signupForm.maritalStatus ? 'true' : 'false'} onChange={(event) => setSignupForm((prev) => ({ ...prev, maritalStatus: event.target.value === 'true' }))}><option value="false">미혼</option><option value="true">기혼</option></select></label>
                <div />
              </div>
              <label className="consent-row"><input type="checkbox" name="privacyConsent" checked={signupForm.privacyConsent} onChange={handleSignupChange} required /><span>개인정보 수집 및 이용에 동의합니다.</span></label>
              <button className="button" type="submit">회원가입</button>
              {renderNotice(['signup'])}
            </form>
          </section>
        )}

        {view === 'admin-dashboard' && isAdmin && (
          <section className="panel admin-panel">
            {renderAdminToolbar()}
            <div className="admin-section-head">
              <p className="eyebrow">Admin</p>
              <h2>관리자 대시보드</h2>
              <p>메인 화면, 예약, 회원, 코스, 스태프, 공지사항을 한 화면 스타일 안에서 관리합니다.</p>
              {renderNotice(['admin-dashboard'])}
            </div>
            <div className="card-grid">
              <article className="soft-card admin-stat"><span>프로그램</span><h3>{courses.length}</h3><button className="button secondary" type="button" onClick={() => jump('admin-courses')}>관리</button></article>
              <article className="soft-card admin-stat"><span>스태프</span><h3>{staff.length}</h3><button className="button secondary" type="button" onClick={() => jump('admin-staff')}>관리</button></article>
              <article className="soft-card admin-stat"><span>회원</span><h3>{adminUsers.length}</h3><button className="button secondary" type="button" onClick={() => jump('admin-users')}>관리</button></article>
              <article className="soft-card admin-stat"><span>예약</span><h3>{adminReservations.length}</h3><button className="button secondary" type="button" onClick={() => jump('admin-reservations')}>관리</button></article>
            </div>
          </section>
        )}

        {view === 'admin-content' && isAdmin && (
          <section className="panel admin-panel">
            {renderAdminToolbar()}
            <div className="admin-section-head">
              <p className="eyebrow">Main Screen</p>
              <h2>메인 화면 관리</h2>
              <p>첫 화면 사진, 제목, 소개 문구, 위치 안내 문구를 수정합니다.</p>
              {renderNotice(['admin-content'])}
            </div>
            <div className="admin-layout">
              <form className="form-card admin-form" onSubmit={submitSiteContent}>
                <label><span>브랜드명</span><input name="brandName" value={siteContentForm.brandName} onChange={update(setSiteContentForm)} required /></label>
                <label><span>메인 작은 문구</span><input name="heroEyebrow" value={siteContentForm.heroEyebrow} onChange={update(setSiteContentForm)} required /></label>
                <label><span>메인 제목</span><input name="heroTitle" value={siteContentForm.heroTitle} onChange={update(setSiteContentForm)} required /></label>
                <label><span>메인 설명</span><textarea name="heroDescription" value={siteContentForm.heroDescription} onChange={update(setSiteContentForm)} rows="4" required /></label>
                <label><span>메인 사진</span><input type="file" accept="image/*" onChange={(event) => setSiteHeroImageFile(event.target.files?.[0] || null)} /></label>
                <div className="two-column">
                  <label><span>매장명</span><input name="storeName" value={siteContentForm.storeName} onChange={update(setSiteContentForm)} required /></label>
                  <label><span>전화번호</span><input name="storePhone" value={siteContentForm.storePhone} onChange={update(setSiteContentForm)} /></label>
                </div>
                <label><span>주소 안내</span><textarea name="storeAddress" value={siteContentForm.storeAddress} onChange={update(setSiteContentForm)} rows="3" required /></label>
                <label><span>오시는 길 설명</span><textarea name="locationDescription" value={siteContentForm.locationDescription} onChange={update(setSiteContentForm)} rows="3" required /></label>
                <button className="button" type="submit">메인 화면 저장</button>
              </form>

              <article className="list-card admin-preview">
                <img src={heroImage} alt="현재 메인 사진" />
                <p className="eyebrow">{siteContentForm.heroEyebrow}</p>
                <h3>{siteContentForm.heroTitle}</h3>
                <p>{siteContentForm.heroDescription}</p>
                <div className="readonly-box">{siteContentForm.storeName} / {siteContentForm.storeAddress}</div>
              </article>
            </div>
          </section>
        )}

        {view === 'admin-users' && isAdmin && (
          <section className="panel admin-panel">
            {renderAdminToolbar()}
            <div className="admin-section-head">
              <p className="eyebrow">Members</p>
              <h2>회원 관리</h2>
              <p>회원 정보, 권한, 패키지 횟수, 관리자 메모를 수정합니다.</p>
              {renderNotice(['admin-users'])}
            </div>
            <div className="admin-layout">
              <form className="form-card admin-form" onSubmit={submitAdminUser}>
                <h3>{editingUserId ? `${editingUserId} 수정` : '회원을 선택해 주세요'}</h3>
                <label><span>이름</span><input name="name" value={adminUserForm.name} onChange={update(setAdminUserForm)} disabled={!editingUserId} required /></label>
                <label><span>이메일</span><input name="email" type="email" value={adminUserForm.email} onChange={update(setAdminUserForm)} disabled={!editingUserId} required /></label>
                <label><span>전화번호</span><input name="phoneNumber" value={adminUserForm.phoneNumber} onChange={(event) => setAdminUserForm((prev) => ({ ...prev, phoneNumber: formatPhoneNumber(event.target.value) }))} disabled={!editingUserId} required /></label>
                <div className="two-column">
                  <label><span>권한</span><select name="role" value={adminUserForm.role} onChange={update(setAdminUserForm)} disabled={!editingUserId}><option value="USER">USER</option><option value="ADMIN">ADMIN</option></select></label>
                  <label><span>패키지 횟수</span><input type="number" name="packageCount" min="0" value={adminUserForm.packageCount} onChange={update(setAdminUserForm)} disabled={!editingUserId} /></label>
                </div>
                <div className="two-column">
                  <label><span>성별</span><input name="gender" value={adminUserForm.gender} onChange={update(setAdminUserForm)} disabled={!editingUserId} /></label>
                  <label><span>생년월일</span><input type="date" name="birthdate" value={adminUserForm.birthdate || ''} onChange={update(setAdminUserForm)} disabled={!editingUserId} /></label>
                </div>
                <label className="consent-row"><input type="checkbox" name="maritalStatus" checked={adminUserForm.maritalStatus} onChange={update(setAdminUserForm)} disabled={!editingUserId} /><span>기혼</span></label>
                <label><span>관리자 메모</span><textarea name="memo" value={adminUserForm.memo} onChange={update(setAdminUserForm)} rows="5" disabled={!editingUserId} /></label>
                <div className="button-row">
                  <button className="button" type="submit" disabled={!editingUserId}>회원 저장</button>
                  {editingUserId && <button className="button secondary" type="button" onClick={resetAdminUserForm}>취소</button>}
                </div>
              </form>

              <div className="list-grid">
                {adminUsers.map((user) => (
                  <article className="list-card" key={user.userId}>
                    <div className="list-head">
                      <h3>{user.name} ({user.userId})</h3>
                      <span>{normalizeRole(user.role)}</span>
                    </div>
                    <p>{user.email} / {formatPhoneNumber(user.phoneNumber)}</p>
                    <p>패키지 {user.packageCount || 0}회</p>
                    <div className="status-counts">
                      {userStatusSummaryOptions.map(([status, label]) => (
                        <span key={`${user.userId}-${status}`}>{label} {Number(user.reservationStatusCounts?.[status] || 0)}</span>
                      ))}
                    </div>
                    {user.memo ? <p>{user.memo}</p> : null}
                    <div className="button-row">
                      <button className="button secondary" type="button" onClick={() => startEditUser(user)}>수정</button>
                      <button className="button secondary danger" type="button" onClick={() => deleteAdminUser(user.userId)}>삭제</button>
                    </div>
                  </article>
                ))}
              </div>
            </div>
          </section>
        )}

        {view === 'admin-reservations' && isAdmin && (
          <section className="panel admin-panel">
            {renderAdminToolbar()}
            <div className="admin-section-head">
              <p className="eyebrow">Reservations</p>
              <h2>예약 관리</h2>
              <p>예약 시간, 코스, 상태를 수정하고 필요 없는 예약을 삭제합니다.</p>
              {renderNotice(['admin-reservations'])}
            </div>
            <div className="admin-layout">
              <form className="form-card admin-form" onSubmit={submitAdminReservation}>
                <h3>{editingReservationId ? '예약 수정' : '예약을 선택해 주세요'}</h3>
                <label><span>코스</span><select name="courseId" value={adminReservationForm.courseId} onChange={update(setAdminReservationForm)} disabled={!editingReservationId} required><option value="">코스 선택</option>{courses.map((course) => <option key={course.id} value={course.id}>{course.name}</option>)}</select></label>
                <label><span>예약 일시</span><input type="datetime-local" name="reservationDateTime" value={adminReservationForm.reservationDateTime} onChange={update(setAdminReservationForm)} disabled={!editingReservationId} required /></label>
                <label>
                  <span>상태</span>
                  <select name="status" value={adminReservationForm.status} onChange={update(setAdminReservationForm)} disabled={!editingReservationId}>
                    {reservationStatusOptions.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
                  </select>
                </label>
                <div className="two-column">
                  <label><span>비회원 이름</span><input name="name" value={adminReservationForm.name} onChange={update(setAdminReservationForm)} disabled={!editingReservationId} /></label>
                  <label><span>비회원 전화번호</span><input name="phoneNumber" value={adminReservationForm.phoneNumber} onChange={(event) => setAdminReservationForm((prev) => ({ ...prev, phoneNumber: formatPhoneNumber(event.target.value) }))} disabled={!editingReservationId} /></label>
                </div>
                <div className="button-row">
                  <button className="button" type="submit" disabled={!editingReservationId}>예약 저장</button>
                  {editingReservationId && <button className="button secondary" type="button" onClick={resetAdminReservationForm}>취소</button>}
                </div>
              </form>

              <div className="list-grid">
                {adminReservations.map((reservation) => (
                  <article className="list-card" key={reservation.id}>
                    <div className="list-head">
                      <h3>{reservationOwnerLabel(reservation)}</h3>
                      <span>{reservation.statusLabel || statusLabel(reservation.status)}</span>
                    </div>
                    <p>{reservation.courseName || `코스 ID ${reservation.courseId || '-'}`}</p>
                    <p>{formatDate(reservation.reservationDateTime)}</p>
                    {reservation.phoneNumber ? <p>{formatPhoneNumber(reservation.phoneNumber)}</p> : null}
                    <div className="button-row">
                      <button className="button secondary" type="button" onClick={() => startEditReservation(reservation)}>수정</button>
                      <button className="button secondary danger" type="button" onClick={() => deleteAdminReservation(reservation.id)}>삭제</button>
                    </div>
                  </article>
                ))}
              </div>
            </div>
          </section>
        )}

        {view === 'admin-courses' && isAdmin && (
          <section className="panel admin-panel">
            {renderAdminToolbar()}
            <div className="admin-section-head">
              <p className="eyebrow">Programs</p>
              <h2>프로그램 관리</h2>
              <p>코스명, 담당 스태프, 시간, 가격을 등록하고 수정합니다.</p>
              {renderNotice(['admin-courses'])}
            </div>
            <div className="admin-layout">
              <form className="form-card admin-form" onSubmit={submitAdminCourse}>
                <h3>{editingCourseId ? '프로그램 수정' : '새 프로그램 등록'}</h3>
                <label><span>코스명</span><input name="name" value={adminCourseForm.name} onChange={update(setAdminCourseForm)} required /></label>
                <label><span>담당 스태프</span><select name="staffId" value={adminCourseForm.staffId} onChange={update(setAdminCourseForm)}><option value="">미지정</option>{staff.map((member) => <option key={member.id} value={member.id}>{member.name}</option>)}</select></label>
                <div className="two-column">
                  <label><span>소요 시간(분)</span><input type="number" min="1" name="durationMinutes" value={adminCourseForm.durationMinutes} onChange={update(setAdminCourseForm)} required /></label>
                  <label><span>회원가</span><input type="number" min="0" step="100" name="memberPrice" value={adminCourseForm.memberPrice} onChange={update(setAdminCourseForm)} required /></label>
                </div>
                <label><span>비회원가</span><input type="number" min="0" step="100" name="nonMemberPrice" value={adminCourseForm.nonMemberPrice} onChange={update(setAdminCourseForm)} required /></label>
                <div className="button-row">
                  <button className="button" type="submit">{editingCourseId ? '코스 저장' : '코스 등록'}</button>
                  {editingCourseId && <button className="button secondary" type="button" onClick={resetAdminCourseForm}>취소</button>}
                </div>
              </form>

              <div className="list-grid">
                {courses.map((course) => (
                  <article className="list-card" key={course.id}>
                    <div className="list-head">
                      <h3>{course.name}</h3>
                      <span>{course.durationMinutes}분</span>
                    </div>
                    <p>담당 {course.staff?.name || '미지정'}</p>
                    <p>회원가 {formatMoney(course.memberPrice)} / 비회원가 {formatMoney(course.nonMemberPrice)}</p>
                    <div className="button-row">
                      <button className="button secondary" type="button" onClick={() => startEditCourse(course)}>수정</button>
                      <button className="button secondary danger" type="button" onClick={() => deleteAdminCourse(course.id)}>삭제</button>
                    </div>
                  </article>
                ))}
              </div>
            </div>
          </section>
        )}

        {view === 'admin-staff' && isAdmin && (
          <section className="panel admin-panel">
            {renderAdminToolbar()}
            <div className="admin-section-head">
              <p className="eyebrow">Staff</p>
              <h2>스태프 관리</h2>
              <p>스태프 이름과 프로필 이미지를 등록하고 수정합니다.</p>
              {renderNotice(['admin-staff'])}
            </div>
            <div className="admin-layout">
              <form className="form-card admin-form" onSubmit={submitAdminStaff}>
                <h3>{editingStaffId ? '스태프 수정' : '스태프 등록'}</h3>
                <label><span>이름</span><input name="name" value={adminStaffForm.name} onChange={update(setAdminStaffForm)} required /></label>
                <label><span>현재 이미지 경로</span><input name="profilePicture" value={adminStaffForm.profilePicture} onChange={update(setAdminStaffForm)} /></label>
                <label><span>프로필 이미지</span><input type="file" accept="image/*" onChange={(event) => setAdminStaffForm((prev) => ({ ...prev, profileImage: event.target.files?.[0] || null }))} /></label>
                <div className="button-row">
                  <button className="button" type="submit">{editingStaffId ? '스태프 저장' : '스태프 등록'}</button>
                  {editingStaffId && <button className="button secondary" type="button" onClick={resetAdminStaffForm}>취소</button>}
                </div>
              </form>

              <div className="list-grid">
                {staff.map((member) => (
                  <article className="list-card staff-admin-card" key={member.id}>
                    <img src={asset(member.profilePicture) || fallbackHeroImage} alt={member.name} />
                    <div className="list-head"><h3>{member.name}</h3></div>
                    <div className="button-row">
                      <button className="button secondary" type="button" onClick={() => startEditStaff(member)}>수정</button>
                      <button className="button secondary danger" type="button" onClick={() => deleteAdminStaff(member.id)}>삭제</button>
                    </div>
                  </article>
                ))}
              </div>
            </div>
          </section>
        )}

        {view === 'admin-announcements' && isAdmin && (
          <section className="panel admin-panel">
            {renderAdminToolbar()}
            <div className="admin-section-head">
              <p className="eyebrow">Notice</p>
              <h2>공지 관리</h2>
              <p>공지 제목과 본문을 작성하고, 첨부파일을 추가하거나 삭제합니다.</p>
              {renderNotice(['admin-announcements'])}
            </div>
            <div className="admin-layout">
              <form className="form-card admin-form" onSubmit={submitAdminAnnouncement}>
                <h3>{editingAnnouncementId ? '공지 수정' : '공지 작성'}</h3>
                <label><span>제목</span><input name="title" value={adminAnnouncementForm.title} onChange={update(setAdminAnnouncementForm)} required /></label>
                <label><span>본문</span><textarea name="content" value={adminAnnouncementForm.content} onChange={update(setAdminAnnouncementForm)} rows="9" required /></label>
                {editingAnnouncementId && (
                  <div className="attachment-list">
                    {(announcements.find((item) => item.id === editingAnnouncementId)?.attachmentPaths || []).map((path, index) => (
                      <label className="consent-row" key={path}>
                        <input
                          type="checkbox"
                          checked={adminAnnouncementForm.deletedAttachmentPaths.includes(path)}
                          onChange={(event) => setAdminAnnouncementForm((prev) => ({
                            ...prev,
                            deletedAttachmentPaths: event.target.checked
                              ? [...prev.deletedAttachmentPaths, path]
                              : prev.deletedAttachmentPaths.filter((item) => item !== path),
                          }))}
                        />
                        <span>{announcements.find((item) => item.id === editingAnnouncementId)?.originalAttachmentNames?.[index] || path} 삭제</span>
                      </label>
                    ))}
                  </div>
                )}
                <label><span>본문 이미지 삽입</span><input type="file" accept="image/*" onChange={(event) => insertAnnouncementBodyImage(event.target.files?.[0] || null)} /></label>
                <label><span>첨부파일 추가</span><input type="file" multiple onChange={(event) => setAdminAnnouncementForm((prev) => ({ ...prev, newAttachmentFiles: event.target.files || [] }))} /></label>
                <div className="button-row">
                  <button className="button" type="submit">{editingAnnouncementId ? '공지 저장' : '공지 등록'}</button>
                  {editingAnnouncementId && <button className="button secondary" type="button" onClick={resetAdminAnnouncementForm}>취소</button>}
                </div>
              </form>

              <div className="list-grid">
                {announcements.map((announcement) => (
                  <article className="list-card" key={announcement.id}>
                    <div className="list-head">
                      <h3>{announcement.title}</h3>
                      <span>{formatDate(announcement.createdAt)}</span>
                    </div>
                    <div className="html-content" dangerouslySetInnerHTML={{ __html: announcement.content || '<p>등록된 내용이 없습니다.</p>' }} />
                    {(announcement.attachmentPaths || []).length > 0 && (
                      <div className="attachment-links">
                        {(announcement.attachmentPaths || []).map((path, index) => (
                          <a key={`${announcement.id}-${path}`} href={`/announcement/attachment/${announcement.id}/${index}`}>
                            {attachmentName(announcement, index, path)}
                          </a>
                        ))}
                      </div>
                    )}
                    <div className="button-row">
                      <button className="button secondary" type="button" onClick={() => startEditAnnouncement(announcement)}>수정</button>
                      <button className="button secondary danger" type="button" onClick={() => deleteAdminAnnouncement(announcement.id)}>삭제</button>
                    </div>
                  </article>
                ))}
              </div>
            </div>
          </section>
        )}
      </main>
    </div>
  );
}

export default App;
