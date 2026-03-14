export function currency(value) {
  if (value == null || value === '') return '-'
  return `${Number(value).toLocaleString('ko-KR')}원`
}

export function date(value) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return d.toLocaleDateString('ko-KR')
}

export function dateTime(value) {
  if (!value) return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return value
  return d.toLocaleString('ko-KR')
}
