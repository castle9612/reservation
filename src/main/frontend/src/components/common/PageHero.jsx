import { motion } from 'framer-motion'

export default function PageHero({ eyebrow, title, description, actions, image }) {
  return (
    <section className="grid gap-8 overflow-hidden rounded-[32px] border border-white/60 bg-white/70 p-8 shadow-[0_30px_120px_-40px_rgba(15,23,42,0.3)] backdrop-blur xl:grid-cols-[1.15fr_0.85fr]">
      <motion.div initial={{ opacity: 0, y: 18 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.45 }}>
        <p className="mb-3 text-xs font-semibold uppercase tracking-[0.35em] text-amber-700">{eyebrow}</p>
        <h1 className="text-4xl font-bold tracking-tight text-slate-900 sm:text-5xl">{title}</h1>
        <p className="mt-5 max-w-2xl text-base leading-7 text-slate-600">{description}</p>
        {actions ? <div className="mt-8 flex flex-wrap gap-3">{actions}</div> : null}
      </motion.div>

      <motion.div
        initial={{ opacity: 0, scale: 0.96 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.45, delay: 0.1 }}
        className="relative overflow-hidden rounded-[28px] bg-gradient-to-br from-stone-100 via-amber-50 to-orange-100 p-8"
      >
        <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(255,255,255,0.85),transparent_35%)]" />
        <div className="relative flex h-full min-h-[320px] flex-col justify-end rounded-[24px] border border-white/60 bg-white/40 p-6">
          <div className="mb-4 inline-flex w-fit rounded-full bg-slate-900 px-4 py-1 text-xs font-semibold tracking-[0.2em] text-white">
            THERAPY CARE
          </div>
          <div className="text-2xl font-bold text-slate-900">{image?.title ?? 'Calm Body, Clear Mind'}</div>
          <div className="mt-2 text-sm leading-6 text-slate-600">
            {image?.description ?? '섬세한 케어와 편안한 예약 경험을 하나의 흐름으로 정리했다.'}
          </div>
        </div>
      </motion.div>
    </section>
  )
}
