export default function SectionHeading({ eyebrow, title, description, action }) {
  return (
    <div className="mb-8 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
      <div>
        {eyebrow ? (
          <p className="mb-2 text-xs font-semibold uppercase tracking-[0.3em] text-amber-700">{eyebrow}</p>
        ) : null}
        <h2 className="text-3xl font-bold tracking-tight text-slate-900">{title}</h2>
        {description ? <p className="mt-3 max-w-2xl text-slate-600">{description}</p> : null}
      </div>
      {action}
    </div>
  )
}
