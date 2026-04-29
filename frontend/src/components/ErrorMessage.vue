<template>
  <div v-if="message" class="error-box">
    {{ message }}
  </div>
</template>

<script setup>
defineProps({ message: { type: String, default: '' } })
</script>

<style scoped>
.error-box {
  background: #fdf0f0;
  border: 1px solid #e0b4b4;
  color: #c0392b;
  border-radius: 4px;
  padding: 10px 14px;
  font-size: 14px;
  margin-bottom: 12px;
}
</style>
