import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

// Auth (no layout)
import LoginView from '../features/auth/LoginView.vue'
import RegisterView from '../features/auth/RegisterView.vue'

// Student
import HomeView from '../features/home/HomeView.vue'
import AccountView from '../features/account/AccountView.vue'
import WarView from '../features/war/WarView.vue'
import PeerEvaluationView from '../features/peerevaluation/PeerEvaluationView.vue'
import PeerEvaluationReportView from '../features/peerevaluation/PeerEvaluationReportView.vue'

// Instructor
import InstructorRegister from '../features/instructor/InstructorRegister.vue'
import InstructorHomeView from '../features/instructor/InstructorHomeView.vue'
import PeerEvalSectionReport from '../features/instructor/PeerEvalSectionReport.vue'
import WarTeamReport from '../features/instructor/WarTeamReport.vue'
import StudentReports from '../features/instructor/StudentReports.vue'

// Admin — Students
import AdminHomeView from '../features/admin/AdminHomeView.vue'
import StudentsListView from '../features/admin/students/StudentsListView.vue'
import StudentDetailView from '../features/admin/students/StudentDetailView.vue'
import InviteStudentsView from '../features/admin/students/InviteStudentsView.vue'

// Admin — Instructors
import InstructorsListView from '../features/admin/instructors/InstructorsListView.vue'
import InstructorDetailView from '../features/admin/instructors/InstructorDetailView.vue'
import InviteInstructorsView from '../features/admin/instructors/InviteInstructorsView.vue'

// Admin — Teams
import TeamsListView from '../features/admin/teams/TeamsListView.vue'
import TeamDetailView from '../features/admin/teams/TeamDetailView.vue'
import TeamFormView from '../features/admin/teams/TeamFormView.vue'

// Admin — Sections
import SectionsListView from '../features/admin/sections/SectionsListView.vue'
import SectionDetailView from '../features/admin/sections/SectionDetailView.vue'
import SectionFormView from '../features/admin/sections/SectionFormView.vue'
import ActiveWeeksView from '../features/admin/sections/ActiveWeeksView.vue'

// Admin — Rubrics
import RubricsListView from '../features/admin/rubrics/RubricsListView.vue'
import RubricFormView from '../features/admin/rubrics/RubricFormView.vue'

// 404
import NotFoundView from '../views/NotFoundView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // ── Public / Auth ──────────────────────────────────────
    { path: '/', redirect: '/login' },
    { path: '/login', component: LoginView, meta: { noLayout: true, public: true } },
    { path: '/register', component: RegisterView, meta: { noLayout: true, public: true } },
    { path: '/instructor/register', component: InstructorRegister, meta: { noLayout: true, public: true } },

    // ── Student ────────────────────────────────────────────
    { path: '/home', component: HomeView, meta: { requiresAuth: true, roles: ['student'] } },
    { path: '/account', component: AccountView, meta: { requiresAuth: true, roles: ['student'] } },
    { path: '/war', component: WarView, meta: { requiresAuth: true, roles: ['student'] } },
    { path: '/peer-evaluation', component: PeerEvaluationView, meta: { requiresAuth: true, roles: ['student'] } },
    { path: '/peer-evaluation-report', component: PeerEvaluationReportView, meta: { requiresAuth: true, roles: ['student'] } },

    // ── Instructor ─────────────────────────────────────────
    { path: '/instructor/home', component: InstructorHomeView, meta: { requiresAuth: true, roles: ['instructor'] } },
    { path: '/instructor/reports/peer-eval/section', component: PeerEvalSectionReport, meta: { requiresAuth: true, roles: ['instructor'] } },
    { path: '/instructor/reports/war/team', component: WarTeamReport, meta: { requiresAuth: true, roles: ['instructor'] } },
    { path: '/instructor/reports/student', component: StudentReports, meta: { requiresAuth: true, roles: ['instructor'] } },

    // ── Admin ──────────────────────────────────────────────
    { path: '/admin', component: AdminHomeView, meta: { requiresAuth: true, roles: ['admin'] } },

    { path: '/admin/students', component: StudentsListView, meta: { requiresAuth: true, roles: ['admin'] } },
    { path: '/admin/students/invite', component: InviteStudentsView, meta: { requiresAuth: true, roles: ['admin'] } },
    { path: '/admin/students/:id', component: StudentDetailView, meta: { requiresAuth: true, roles: ['admin'] } },

    { path: '/admin/instructors', component: InstructorsListView, meta: { requiresAuth: true, roles: ['admin'] } },
    { path: '/admin/instructors/invite', component: InviteInstructorsView, meta: { requiresAuth: true, roles: ['admin'] } },
    { path: '/admin/instructors/:id', component: InstructorDetailView, meta: { requiresAuth: true, roles: ['admin'] } },

    { path: '/admin/teams', component: TeamsListView, meta: { requiresAuth: true, roles: ['admin'] } },
    { path: '/admin/teams/new', component: TeamFormView, meta: { requiresAuth: true, roles: ['admin'] } },
    { path: '/admin/teams/:id/edit', component: TeamFormView, meta: { requiresAuth: true, roles: ['admin'] } },
    { path: '/admin/teams/:id', component: TeamDetailView, meta: { requiresAuth: true, roles: ['admin'] } },

    { path: '/admin/sections', component: SectionsListView, meta: { requiresAuth: true, roles: ['admin'] } },
    { path: '/admin/sections/new', component: SectionFormView, meta: { requiresAuth: true, roles: ['admin'] } },
    { path: '/admin/sections/:id/edit', component: SectionFormView, meta: { requiresAuth: true, roles: ['admin'] } },
    { path: '/admin/sections/:id/active-weeks', component: ActiveWeeksView, meta: { requiresAuth: true, roles: ['admin'] } },
    { path: '/admin/sections/:id', component: SectionDetailView, meta: { requiresAuth: true, roles: ['admin'] } },

    { path: '/admin/rubrics', component: RubricsListView, meta: { requiresAuth: true, roles: ['admin'] } },
    { path: '/admin/rubrics/new', component: RubricFormView, meta: { requiresAuth: true, roles: ['admin'] } },
    { path: '/admin/rubrics/:id', component: RubricFormView, meta: { requiresAuth: true, roles: ['admin'] } },

    // ── 404 ────────────────────────────────────────────────
    { path: '/:pathMatch(.*)*', component: NotFoundView, meta: { noLayout: true } },
  ],
})

const ROLE_HOME = {
  student: '/home',
  instructor: '/instructor/home',
  admin: '/admin',
}

router.beforeEach((to) => {
  const auth = useAuthStore()

  // Redirect logged-in users away from login page
  if (to.meta.public && auth.isLoggedIn) {
    return ROLE_HOME[auth.role] ?? '/home'
  }

  // Route requires auth but user is not logged in
  if (to.meta.requiresAuth && !auth.isLoggedIn) {
    return '/login'
  }

  // Route is role-restricted and user's role doesn't match
  if (to.meta.roles && auth.isLoggedIn && !to.meta.roles.includes(auth.role)) {
    return ROLE_HOME[auth.role] ?? '/home'
  }
})

export default router
