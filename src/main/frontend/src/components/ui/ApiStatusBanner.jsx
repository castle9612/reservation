import { ServerCrash } from 'lucide-react'

export default function ApiStatusBanner({ visible }) {
  if (!visible) return null

  return (
    <div className="mb-6 flex items-start gap-3 rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-amber-900">
      <ServerCrash className="mt-0.5 h-5 w-5 shrink-0" />
      <div>
        <p className="text-sm font-semibold">백엔드 연결이 되지 않았습니다.</p>
        <p className="mt-1 text-sm">
          화면은 표시되지만 로그인, 회원가입, 예약 저장은 서버가 켜져 있어야 동작합니다.
        </p>
      </div>
    </div>
  )
}
