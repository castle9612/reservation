import { Link } from 'react-router-dom'
import { motion } from 'framer-motion'
import { ArrowRight, CalendarHeart, Sparkles, Leaf } from 'lucide-react'
import Pill from '../ui/Pill'
import Container from '../ui/Container'

export default function HeroSection() {
  return (
    <section className="hero-grid relative overflow-hidden pt-8 pb-16 sm:pt-12 sm:pb-20">
      <Container>
        <div className="grid items-center gap-8 lg:grid-cols-[1.1fr_0.9fr]">
          <motion.div
            initial={{ opacity: 0, y: 22 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.55 }}
            className="space-y-6"
          >
            <div className="flex flex-wrap gap-2">
              <Pill>예약 중심 운영</Pill>
              <Pill>회원 / 비회원 모두 가능</Pill>
              <Pill>부드러운 케어 경험</Pill>
            </div>

            <div className="space-y-4">
              <p className="text-sm font-semibold uppercase tracking-[0.35em] text-emerald-700">
                Therapy Reservation
              </p>
              <h1 className="max-w-3xl text-5xl font-semibold leading-[1.05] tracking-tight text-slate-900 sm:text-6xl">
                몸과 마음의 리듬을
                <br />
                차분하게 되찾는 시간
              </h1>
              <p className="max-w-2xl text-lg leading-8 text-slate-600">
                편안한 분위기 안에서 코스를 고르고, 원하는 일정으로 예약하고,
                진행 상태까지 한 화면에서 확인할 수 있는 예약 서비스.
              </p>
            </div>

            <div className="flex flex-wrap gap-3">
              <Link
                to="/courses"
                className="inline-flex items-center gap-2 rounded-full bg-slate-900 px-6 py-3 text-sm font-semibold text-white transition hover:bg-slate-800"
              >
                코스 둘러보기
                <ArrowRight className="h-4 w-4" />
              </Link>
              <Link
                to="/booking/guest"
                className="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white px-6 py-3 text-sm font-semibold text-slate-800 transition hover:bg-slate-100"
              >
                비회원 예약
              </Link>
            </div>
          </motion.div>

          <motion.div
            initial={{ opacity: 0, scale: 0.96 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.6, delay: 0.1 }}
            className="relative"
          >
            <div className="glass-card soft-shadow relative overflow-hidden rounded-[36px] border border-white/70 p-8">
              <div className="absolute inset-0 bg-gradient-to-br from-emerald-100/70 via-white/20 to-teal-100/60" />
              <div className="relative space-y-5">
                <div className="grid gap-4 sm:grid-cols-2">
                  <div className="rounded-3xl bg-white/85 p-5">
                    <CalendarHeart className="h-8 w-8 text-emerald-700" />
                    <h3 className="mt-4 text-lg font-semibold text-slate-900">간단한 예약 흐름</h3>
                    <p className="mt-2 text-sm leading-6 text-slate-600">
                      코스 선택부터 예약 등록, 조회까지 한 번에 정리.
                    </p>
                  </div>
                  <div className="rounded-3xl bg-white/85 p-5">
                    <Leaf className="h-8 w-8 text-emerald-700" />
                    <h3 className="mt-4 text-lg font-semibold text-slate-900">차분한 경험</h3>
                    <p className="mt-2 text-sm leading-6 text-slate-600">
                      강한 판매 느낌보다 편안한 탐색 흐름에 집중한 화면 구성.
                    </p>
                  </div>
                </div>

                <div className="rounded-[28px] bg-slate-900 p-6 text-white">
                  <div className="flex items-center gap-2 text-emerald-300">
                    <Sparkles className="h-5 w-5" />
                    <span className="text-sm font-semibold">시그니처 케어</span>
                  </div>
                  <p className="mt-3 text-2xl font-semibold">
                    예약이 차분하고 매끄럽게 흘러가는 인터페이스
                  </p>
                  <p className="mt-3 text-sm leading-6 text-slate-300">
                    회원과 비회원 각각의 흐름을 분리해 불필요한 입력을 줄였다.
                  </p>
                </div>
              </div>
            </div>
          </motion.div>
        </div>
      </Container>
    </section>
  )
}
