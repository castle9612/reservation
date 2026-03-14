export const mockCourses = [
  {
    id: 1,
    name: '시그니처 밸런스 테라피',
    description: '긴장된 몸의 흐름을 부드럽게 풀어주는 기본 시그니처 케어.',
    duration: 60,
    memberPrice: 70000,
    nonMemberPrice: 85000,
    therapist: '지은',
    image: 'Signature Therapy',
  },
  {
    id: 2,
    name: '딥 릴리즈 아로마',
    description: '등과 어깨 중심으로 깊은 압을 더해 피로를 정리하는 코스.',
    duration: 90,
    memberPrice: 98000,
    nonMemberPrice: 118000,
    therapist: '서윤',
    image: 'Deep Aroma Care',
  },
  {
    id: 3,
    name: '슬로우 스톤 리추얼',
    description: '온기감 있는 스톤 케어와 릴랙싱 스트레칭을 결합한 코스.',
    duration: 120,
    memberPrice: 136000,
    nonMemberPrice: 158000,
    therapist: '하린',
    image: 'Stone Ritual',
  },
]

export const mockNotices = [
  {
    id: 1,
    title: '3월 운영 일정 안내',
    content: '주말 예약이 빠르게 마감되고 있어 원하는 시간대가 있다면 미리 예약해 주세요.',
    createdAt: '2026-03-10',
  },
  {
    id: 2,
    title: '비회원 예약 확인 방식 변경',
    content: '이름과 전화번호로 조회 가능하며 예약 직후 확인 문구가 함께 표시됩니다.',
    createdAt: '2026-03-08',
  },
  {
    id: 3,
    title: '회원 패키지 혜택 업데이트',
    content: '패키지 회원에게는 특정 코스 예약 시 우선 시간 배정이 적용됩니다.',
    createdAt: '2026-03-02',
  },
]

export const mockStaff = [
  {
    id: 1,
    name: '지은',
    role: 'Lead Therapist',
    intro: '바디 밸런스와 긴장 완화 중심의 섬세한 케어를 담당한다.',
    specialty: '어깨/목 집중 관리',
    career: '8년',
  },
  {
    id: 2,
    name: '서윤',
    role: 'Aroma Specialist',
    intro: '부드러운 압과 아로마 케어를 결합한 릴랙싱 프로그램에 강하다.',
    specialty: '아로마 / 릴랙스',
    career: '6년',
  },
  {
    id: 3,
    name: '하린',
    role: 'Premium Care Therapist',
    intro: '장시간 피로 누적 고객을 위한 딥 릴리즈 코스를 주로 담당한다.',
    specialty: '딥 티슈 / 순환 관리',
    career: '7년',
  },
]
