import { createRouter, createWebHistory } from 'vue-router'
import InstructorRegister from '../features/instructor/InstructorRegister.vue'
import PeerEvalSectionReport from '../features/instructor/PeerEvalSectionReport.vue'
import WarTeamReport from '../features/instructor/WarTeamReport.vue'
import StudentReports from '../features/instructor/StudentReports.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    // UC-30: Instructor registration via invite link
    {
      path: '/instructor/register',
      name: 'InstructorRegister',
      component: InstructorRegister
    },
    // UC-31: Peer eval report for entire section
    {
      path: '/instructor/reports/peer-eval/section',
      name: 'PeerEvalSectionReport',
      component: PeerEvalSectionReport
    },
    // UC-32: WAR report for a team
    {
      path: '/instructor/reports/war/team',
      name: 'WarTeamReport',
      component: WarTeamReport
    },
    // UC-33 & UC-34: Reports for a specific student
    {
      path: '/instructor/reports/student',
      name: 'StudentReports',
      component: StudentReports
    }
  ],
})

export default router