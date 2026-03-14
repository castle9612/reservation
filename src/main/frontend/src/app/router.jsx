import { createBrowserRouter } from 'react-router-dom'
import RootLayout from '../layouts/RootLayout'
import HomePage from '../pages/HomePage'
import CoursesPage from '../pages/CoursesPage'
import CourseDetailPage from '../pages/CourseDetailPage'
import LoginPage from '../pages/LoginPage'
import SignupPage from '../pages/SignupPage'
import MemberBookingPage from '../pages/MemberBookingPage'
import GuestBookingPage from '../pages/GuestBookingPage'
import MyReservationsPage from '../pages/MyReservationsPage'
import GuestLookupPage from '../pages/GuestLookupPage'
import NotFoundPage from '../pages/NotFoundPage'

export const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    errorElement: <NotFoundPage />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'courses', element: <CoursesPage /> },
      { path: 'courses/:courseId', element: <CourseDetailPage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'signup', element: <SignupPage /> },
      { path: 'reservations/member', element: <MemberBookingPage /> },
      { path: 'reservations/guest', element: <GuestBookingPage /> },
      { path: 'reservations/me', element: <MyReservationsPage /> },
      { path: 'reservations/search', element: <GuestLookupPage /> },
    ],
  },
])
