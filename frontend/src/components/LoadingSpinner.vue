<template>
  <div class="spinner-wrap">
    <div class="spinner" />
    <p v-if="message" class="spinner-msg">{{ message }}</p>
  </div>
</template>

<script setup>
defineProps({ message: { type: String, default: '' } })
</script>

<style scoped>
.spinner-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
  gap: 12px;
}

.spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #ddd;
  border-top-color: #2c3e50;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.spinner-msg {
  color: #666;
  font-size: 14px;
}
</style>
