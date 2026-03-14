import SectionCard from '../components/SectionCard';

export default function HomePage({ summary }) {
  return (
    <main className="grid">
      <SectionCard title="공지사항">
        {summary.announcements.length === 0 ? <p>등록된 공지사항이 없다.</p> : null}
        {summary.announcements.map((item) => (
          <div key={item.id} className="list-row">
            <div>
              <strong>{item.title}</strong>
              <p>{String(item.createdAt ?? '').slice(0, 10)}</p>
            </div>
            <a href={`/announcement/detail/${item.id}`}>보기</a>
          </div>
        ))}
        <a className="text-link" href="/announcement/list">전체 공지사항</a>
      </SectionCard>

      <SectionCard title="스태프">
        {summary.staff.length === 0 ? <p>등록된 스태프가 없다.</p> : null}
        {summary.staff.map((item) => (
          <div key={item.id} className="list-row">
            <div>
              <strong>{item.name}</strong>
              <p>{item.profilePicture || '프로필 사진 없음'}</p>
            </div>
            <a href="/staff">목록</a>
          </div>
        ))}
        <a className="text-link" href="/staff">전체 스태프</a>
      </SectionCard>

      <SectionCard title="점검 포인트" full>
        <ul className="plain-list">
          <li>로그인은 /login 폼 로그인으로 맞춤</li>
          <li>로그아웃은 /api/auth/logout AJAX 호출로 맞춤</li>
          <li>회원가입은 UserDTO 필드 전체를 사용</li>
          <li>공지사항, 스태프는 더미 데이터 대신 /api/public/summary에서 읽음</li>
        </ul>
      </SectionCard>
    </main>
  );
}
