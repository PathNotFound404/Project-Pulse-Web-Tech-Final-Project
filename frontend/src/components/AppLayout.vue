<script setup>
import NavBar from './NavBar.vue'
</script>

<template>
  <div class="app-shell">
    <NavBar />
    <main class="page-content">
      <slot />
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.page-content {
  flex: 1;
  padding: 32px 24px;
  max-width: 1100px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}
</style>
