import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import SurfaceCard from '../components/common/SurfaceCard'
import { fetchNoticeDetail } from '../api/content'
import { date } from '../utils/format'

export default function NoticeDetailPage() {
  const { noticeId } = useParams()
  const { data: notice } = useQuery({
    queryKey: ['notice', noticeId],
    queryFn: () => fetchNoticeDetail(noticeId),
  })

  if (!notice) {
    return <SurfaceCard>공지사항을 찾을 수 없습니다.</SurfaceCard>
  }

  return (
    <div className="mx-auto max-w-4xl">
      <SurfaceCard>
        <div className="text-sm text-slate-500">{date(notice.createdAt)}</div>
        <h1 className="mt-3 text-3xl font-bold text-slate-900">{notice.title}</h1>
        <div className="mt-8 whitespace-pre-wrap text-base leading-8 text-slate-700">{notice.content}</div>
        <Link to="/notices" className="mt-10 inline-flex rounded-full border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-800">
          목록으로
        </Link>
      </SurfaceCard>
    </div>
  )
}
