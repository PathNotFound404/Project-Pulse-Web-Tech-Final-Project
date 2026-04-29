<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth.js'

const router = useRouter()
const auth = useAuthStore()

function goHome() {
  router.push(auth.homeRoute)
}
</script>

<template>
  <div class="not-found">
    <h1>404</h1>
    <p>This page doesn't exist.</p>
    <button @click="goHome">
      {{ auth.isLoggedIn ? 'Go to Home' : 'Go to Login' }}
    </button>
  </div>
</template>

<style scoped>
.not-found {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  gap: 12px;
  text-align: center;
}

h1 {
  font-size: 80px;
  color: #2c3e50;
  margin: 0;
  line-height: 1;
}

p {
  color: #666;
  font-size: 18px;
  margin: 0;
}

button {
  margin-top: 8px;
  padding: 10px 24px;
  background: #2c3e50;
  color: white;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

button:hover {
  opacity: 0.88;
}
</style>
