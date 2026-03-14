export default function SurfaceCard({ children, className = '' }) {
  return (
    <div className={`rounded-[28px] border border-white/70 bg-white/85 p-6 shadow-[0_20px_80px_-35px_rgba(15,23,42,0.25)] backdrop-blur ${className}`}>
      {children}
    </div>
  )
}
