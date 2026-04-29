<script setup>
import { useAuthStore } from '../../stores/auth.js'

const auth = useAuthStore()

const reportLinks = [
  {
    to: '/instructor/reports/peer-eval/section',
    title: 'Section Peer Eval Report',
    description: 'Generate a peer evaluation report for an entire senior design section for a given week.',
  },
  {
    to: '/instructor/reports/war/team',
    title: 'Team WAR Report',
    description: 'View all weekly activities submitted by a team for a given week.',
  },
  {
    to: '/instructor/reports/student',
    title: 'Student Reports',
    description: 'Generate peer evaluation or WAR reports for an individual student over a date range.',
  },
]
</script>

<template>
  <div class="instructor-home">
    <h1>Welcome, {{ auth.firstName }}</h1>
    <p class="subtitle">Select a report to get started.</p>

    <div class="card-grid">
      <RouterLink
        v-for="link in reportLinks"
        :key="link.to"
        :to="link.to"
        class="card"
      >
        <h2>{{ link.title }}</h2>
        <p>{{ link.description }}</p>
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
.instructor-home {
  max-width: 800px;
}

h1 {
  font-size: 24px;
  margin: 0 0 6px;
  color: #2c3e50;
}

.subtitle {
  color: #666;
  margin: 0 0 32px;
  font-size: 15px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.card {
  display: block;
  padding: 20px 22px;
  border: 1px solid #ddd;
  border-radius: 8px;
  text-decoration: none;
  color: inherit;
  background: white;
  transition: border-color 0.15s, box-shadow 0.15s;
}

.card:hover {
  border-color: #2c3e50;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.card h2 {
  font-size: 15px;
  margin: 0 0 8px;
  color: #2c3e50;
}

.card p {
  font-size: 13px;
  color: #666;
  margin: 0;
  line-height: 1.5;
}
</style>
