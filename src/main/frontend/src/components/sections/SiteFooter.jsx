import Container from '../ui/Container'

export default function SiteFooter() {
  return (
    <footer className="mt-20 border-t border-white/60 bg-white/70">
      <Container className="grid gap-8 py-12 md:grid-cols-3">
        <div>
          <p className="text-lg font-semibold text-slate-900">Calm Reserve</p>
          <p className="mt-3 text-sm leading-6 text-slate-600">
            편안한 테라피 예약 경험을 위한 예약 웹 서비스.
          </p>
        </div>

        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500">
            Contact
          </p>
          <ul className="mt-3 space-y-2 text-sm text-slate-600">
            <li>서울시 강남구 테라피로 21</li>
            <li>02-1234-5678</li>
            <li>hello@calmreserve.kr</li>
          </ul>
        </div>

        <div>
          <p className="text-sm font-semibold uppercase tracking-[0.2em] text-slate-500">
            Hours
          </p>
          <ul className="mt-3 space-y-2 text-sm text-slate-600">
            <li>월-금 10:00 - 21:00</li>
            <li>토 10:00 - 18:00</li>
            <li>일 / 공휴일 휴무</li>
          </ul>
        </div>
      </Container>
    </footer>
  )
}
