export default function StatusPanel({ auth, csrfToken }) {
  return (
    <div className="status-grid">
      <div className="status-pill">인증 상태: {auth.authenticated ? '로그인됨' : '비로그인'}</div>
      <div className="status-pill">사용자: {auth.userId ?? '없음'}</div>
      <div className="status-pill">권한: {auth.role ?? '없음'}</div>
      <div className="status-pill">CSRF: {csrfToken ? '수신 완료' : '미수신'}</div>
    </div>
  );
}
