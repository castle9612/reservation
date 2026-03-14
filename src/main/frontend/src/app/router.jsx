import { createBrowserRouter } from 'react-router-dom'
import RootLayout from '../layouts/RootLayout'
import HomePage from '../pages/HomePage'
import CoursesPage from '../pages/CoursesPage'
import CourseDetailPage from '../pages/CourseDetailPage'
import NoticesPage from '../pages/NoticesPage'
import NoticeDetailPage from '../pages/NoticeDetailPage'
import StaffPage from '../pages/StaffPage'
import LoginPage from '../pages/LoginPage'
import SignupPage from '../pages/SignupPage'
import MemberBookingPage from '../pages/MemberBookingPage'
import GuestBookingPage from '../pages/GuestBookingPage'
import MyReservationsPage from '../pages/MyReservationsPage'
import GuestLookupPage from '../pages/GuestLookupPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'courses', element: <CoursesPage /> },
      { path: 'courses/:courseId', element: <CourseDetailPage /> },
      { path: 'notices', element: <NoticesPage /> },
      { path: 'notices/:noticeId', element: <NoticeDetailPage /> },
      { path: 'staff', element: <StaffPage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'signup', element: <SignupPage /> },
      { path: 'booking/member', element: <MemberBookingPage /> },
      { path: 'booking/guest', element: <GuestBookingPage /> },
      { path: 'reservations/me', element: <MyReservationsPage /> },
      { path: 'reservations/guest-search', element: <GuestLookupPage /> },
    ],
  },
])
