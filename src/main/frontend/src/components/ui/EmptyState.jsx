export default function EmptyState({ title, description }) {
  return (
    <div className="glass-card soft-shadow rounded-3xl border border-white/70 p-8 text-center">
      <h3 className="text-xl font-semibold text-slate-900">{title}</h3>
      <p className="mt-3 text-sm leading-6 text-slate-600">{description}</p>
    </div>
  )
}
