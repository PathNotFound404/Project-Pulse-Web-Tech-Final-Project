<script setup>
import { useRouter, RouterLink } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const auth = useAuthStore()
const router = useRouter()

const studentLinks = [
  { to: '/home', label: 'Home' },
  { to: '/war', label: 'My WAR' },
  { to: '/peer-evaluation', label: 'Peer Evaluation' },
  { to: '/peer-evaluation-report', label: 'My Report' },
  { to: '/account', label: 'Account' },
]

const instructorLinks = [
  { to: '/instructor/home', label: 'Dashboard' },
  { to: '/instructor/reports/peer-eval/section', label: 'Section Report' },
  { to: '/instructor/reports/war/team', label: 'Team WAR' },
  { to: '/instructor/reports/student', label: 'Student Reports' },
]

const adminLinks = [
  { to: '/admin', label: 'Dashboard' },
  { to: '/admin/sections', label: 'Sections' },
  { to: '/admin/teams', label: 'Teams' },
  { to: '/admin/students', label: 'Students' },
  { to: '/admin/instructors', label: 'Instructors' },
  { to: '/admin/rubrics', label: 'Rubrics' },
]

const navLinks = {
  student: studentLinks,
  instructor: instructorLinks,
  admin: adminLinks,
}

function handleLogout() {
  auth.logout(router)
}
</script>

<template>
  <nav class="navbar">
    <span class="brand">Project Pulse</span>

    <div class="links">
      <RouterLink
        v-for="link in navLinks[auth.role] ?? []"
        :key="link.to"
        :to="link.to"
        class="nav-link"
        active-class="active"
      >
        {{ link.label }}
      </RouterLink>
    </div>

    <div class="user-area">
      <span class="username">{{ auth.fullName }}</span>
      <button class="logout-btn" @click="handleLogout">Logout</button>
    </div>
  </nav>
</template>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  gap: 0;
  padding: 0 24px;
  height: 56px;
  background: #2c3e50;
  color: white;
  position: sticky;
  top: 0;
  z-index: 100;
}

.brand {
  font-size: 17px;
  font-weight: 700;
  letter-spacing: 0.3px;
  margin-right: 32px;
  white-space: nowrap;
}

.links {
  display: flex;
  gap: 4px;
  flex: 1;
}

.nav-link {
  color: rgba(255, 255, 255, 0.78);
  text-decoration: none;
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
  transition: background 0.15s, color 0.15s;
}

.nav-link:hover,
.nav-link.active {
  background: rgba(255, 255, 255, 0.12);
  color: white;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-left: auto;
}

.username {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.75);
}

.logout-btn {
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.35);
  color: white;
  padding: 5px 14px;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.15s;
}

.logout-btn:hover {
  background: rgba(255, 255, 255, 0.12);
}
</style>
