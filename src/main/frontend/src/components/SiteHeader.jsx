export default function SiteHeader({ tab, tabs, setTab, authenticated, onLogout }) {
  const labels = {
    home: '홈',
    login: '로그인',
    signup: '회원가입'
  };

  return (
    <header className="topbar">
      <div>
        <p className="eyebrow">THERAPY RESERVATION</p>
        <h2>차분한 톤의 예약 프런트</h2>
      </div>
      <nav className="nav">
        {tabs.map((item) => (
          <button
            key={item}
            type="button"
            className={tab === item ? 'active' : ''}
            onClick={() => setTab(item)}
          >
            {labels[item]}
          </button>
        ))}
        {authenticated ? (
          <button type="button" onClick={onLogout}>로그아웃</button>
        ) : null}
      </nav>
    </header>
  );
}
