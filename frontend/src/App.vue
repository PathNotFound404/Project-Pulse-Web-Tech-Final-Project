<script setup>
import { computed } from 'vue'
import { useRoute, RouterView } from 'vue-router'
import AppLayout from './components/AppLayout.vue'

const route = useRoute()
const noLayout = computed(() => route.meta.noLayout === true)
</script>

<template>
  <AppLayout v-if="!noLayout">
    <RouterView />
  </AppLayout>
  <RouterView v-else />
</template>
