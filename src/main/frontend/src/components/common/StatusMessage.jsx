export default function StatusMessage({ type = 'info', children }) {
  const styles = {
    info: 'bg-slate-100 text-slate-700',
    success: 'bg-emerald-50 text-emerald-700',
    error: 'bg-rose-50 text-rose-700',
  }

  return <div className={`rounded-2xl px-4 py-3 text-sm ${styles[type]}`}>{children}</div>
}
