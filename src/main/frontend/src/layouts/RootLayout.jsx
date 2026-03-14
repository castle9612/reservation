import { Outlet } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import Container from '../components/ui/Container'
import Header from '../components/sections/SiteHeader'
import Footer from '../components/sections/SiteFooter'
import ApiStatusBanner from '../components/ui/ApiStatusBanner'

export default function RootLayout() {
  const auth = useAuth()
  const offline = auth.isError && auth.error?.message?.includes('연결')

  return (
    <div className="min-h-screen">
      <Header />
      <Container className="pt-6">
        <ApiStatusBanner visible={offline} />
      </Container>
      <Outlet />
      <Footer />
    </div>
  )
}
