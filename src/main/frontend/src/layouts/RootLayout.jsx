import { Outlet } from 'react-router-dom'
import Header from '../components/layout/Header'

export default function RootLayout() {
  return (
    <div className="min-h-screen bg-stone-50 text-slate-800">
      <Header />
      <main className="mx-auto max-w-6xl px-4 py-8">
        <Outlet />
      </main>
    </div>
  )
}
