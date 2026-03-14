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

export const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'courses', element: <CoursesPage /> },
      { path: 'courses/:courseId', element: <CourseDetailPage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'signup', element: <SignupPage /> },
      { path: 'booking/member', element: <MemberBookingPage /> },
      { path: 'booking/guest', element: <GuestBookingPage /> },
      { path: 'my-reservations', element: <MyReservationsPage /> },
      { path: 'guest-lookup', element: <GuestLookupPage /> },
    ],
  },
])