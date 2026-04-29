<template>
  <div class="home-page">
    <h1>Welcome to Project Pulse</h1>
    <nav class="home-nav">
      <router-link to="/account">My Account</router-link>
      <router-link to="/war">Weekly Activity Report</router-link>
      <router-link to="/peer-evaluation">Peer Evaluation</router-link>
    </nav>
  </div>
</template>

<style scoped>
.home-page {
  max-width: 600px;
  margin: 80px auto;
  padding: 24px;
  text-align: center;
}

.home-nav {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 32px;
}

.home-nav a {
  padding: 12px;
  background-color: #2c3e50;
  color: white;
  border-radius: 4px;
  text-decoration: none;
  font-size: 15px;
}

.home-nav a:hover {
  background-color: #3d5166;
}
</style>
