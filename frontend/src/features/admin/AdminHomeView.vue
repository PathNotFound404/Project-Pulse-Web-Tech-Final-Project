<script setup>
const sections = [
  { to: '/admin/sections', title: 'Sections', description: 'Create and manage senior design sections, rubrics, and active weeks.' },
  { to: '/admin/teams', title: 'Teams', description: 'Create and manage teams, assign students and instructors.' },
  { to: '/admin/students', title: 'Students', description: 'Invite, search, view, and manage student accounts.' },
  { to: '/admin/instructors', title: 'Instructors', description: 'Invite, search, view, and manage instructor accounts.' },
  { to: '/admin/rubrics', title: 'Rubrics', description: 'Create and manage peer evaluation rubrics and criteria.' },
]
</script>

<template>
  <div>
    <h1>Admin Dashboard</h1>
    <p class="subtitle">Manage all aspects of Project Pulse.</p>

    <div class="card-grid">
      <RouterLink
        v-for="s in sections"
        :key="s.to"
        :to="s.to"
        class="card"
      >
        <h2>{{ s.title }}</h2>
        <p>{{ s.description }}</p>
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
h1 { font-size: 24px; margin: 0 0 6px; color: #2c3e50; }
.subtitle { color: #666; margin: 0 0 32px; font-size: 15px; }
.card-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 20px; }
.card {
  display: block; padding: 20px 22px; border: 1px solid #ddd; border-radius: 8px;
  text-decoration: none; color: inherit; background: white;
  transition: border-color 0.15s, box-shadow 0.15s;
}
.card:hover { border-color: #2c3e50; box-shadow: 0 2px 8px rgba(0,0,0,0.08); }
.card h2 { font-size: 15px; margin: 0 0 8px; color: #2c3e50; }
.card p { font-size: 13px; color: #666; margin: 0; line-height: 1.5; }
</style>
