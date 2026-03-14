import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import SectionHeading from '../components/common/SectionHeading'
import SurfaceCard from '../components/common/SurfaceCard'
import { fetchNotices } from '../api/content'
import { date } from '../utils/format'

export default function NoticesPage() {
  const { data: notices = [] } = useQuery({
    queryKey: ['notices'],
    queryFn: fetchNotices,
  })

  return (
    <div>
      <SectionHeading eyebrow="Notices" title="공지사항" />
      <div className="space-y-4">
        {notices.map((notice) => (
          <Link key={notice.id} to={`/notices/${notice.id}`}>
            <SurfaceCard className="transition hover:-translate-y-0.5">
              <div className="text-sm text-slate-500">{date(notice.createdAt)}</div>
              <div className="mt-2 text-xl font-semibold text-slate-900">{notice.title}</div>
              <div className="mt-3 line-clamp-2 text-sm leading-6 text-slate-600">{notice.content}</div>
            </SurfaceCard>
          </Link>
        ))}
      </div>
    </div>
  )
}
