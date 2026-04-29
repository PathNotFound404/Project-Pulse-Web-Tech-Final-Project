import { createRouter, createWebHistory } from 'vue-router'
import InstructorRegister from '../features/instructor/InstructorRegister.vue'
import PeerEvalSectionReport from '../features/instructor/PeerEvalSectionReport.vue'
import WarTeamReport from '../features/instructor/WarTeamReport.vue'
import StudentReports from '../features/instructor/StudentReports.vue'
import LoginView from '../features/auth/LoginView.vue'
import RegisterView from '../features/auth/RegisterView.vue'
import AccountView from '../features/account/AccountView.vue'
import WarView from '../features/war/WarView.vue'
import PeerEvaluationView from '../features/peerevaluation/PeerEvaluationView.vue'
import PeerEvaluationReportView from '../features/peerevaluation/PeerEvaluationReportView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/login', component: LoginView },
    { path: '/register', component: RegisterView },
    { path: '/account', component: AccountView },
    { path: '/war', component: WarView },
    {
      path: '/instructor/register',
      name: 'InstructorRegister',
      component: InstructorRegister
    },
    {
      path: '/instructor/reports/peer-eval/section',
      name: 'PeerEvalSectionReport',
      component: PeerEvalSectionReport
    },
    {
      path: '/instructor/reports/war/team',
      name: 'WarTeamReport',
      component: WarTeamReport
    },
    {
      path: '/instructor/reports/student',
      name: 'StudentReports',
      component: StudentReports
    },
    { path: '/peer-evaluation', component: PeerEvaluationView },
    { path: '/peer-evaluation-report', component: PeerEvaluationReportView },
  ],
})

export default router