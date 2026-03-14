export default function Pill({ children }) {
  return (
    <span className="inline-flex rounded-full border border-white/70 bg-white/80 px-3 py-1 text-xs font-medium text-slate-700 shadow-sm">
      {children}
    </span>
  )
}
