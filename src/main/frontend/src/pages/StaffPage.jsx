import { useQuery } from '@tanstack/react-query'
import SectionHeading from '../components/common/SectionHeading'
import SurfaceCard from '../components/common/SurfaceCard'
import { fetchStaff } from '../api/content'

export default function StaffPage() {
  const { data: staff = [] } = useQuery({
    queryKey: ['staff'],
    queryFn: fetchStaff,
  })

  return (
    <div>
      <SectionHeading eyebrow="Staff" title="스태프 소개" />
      <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
        {staff.map((member) => (
          <SurfaceCard key={member.id}>
            <div className="mb-5 flex h-60 items-center justify-center rounded-[28px] bg-gradient-to-br from-amber-100 to-orange-100 text-6xl font-bold text-slate-900">
              {member.name?.slice(0, 1)}
            </div>
            <div className="text-2xl font-semibold text-slate-900">{member.name}</div>
            <div className="mt-1 text-sm font-medium text-amber-700">{member.role}</div>
            <div className="mt-4 text-sm leading-6 text-slate-600">{member.intro}</div>
            <div className="mt-5 grid gap-2 text-sm text-slate-600">
              <div className="flex items-center justify-between">
                <span>전문 영역</span>
                <span className="font-medium text-slate-900">{member.specialty}</span>
              </div>
              <div className="flex items-center justify-between">
                <span>경력</span>
                <span className="font-medium text-slate-900">{member.career}</span>
              </div>
            </div>
          </SurfaceCard>
        ))}
      </div>
    </div>
  )
}
