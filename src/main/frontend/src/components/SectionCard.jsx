export default function SectionCard({ title, children, full = false }) {
  return (
    <section className={full ? 'panel full' : 'panel'}>
      <h3>{title}</h3>
      {children}
    </section>
  );
}
