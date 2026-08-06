<script setup lang="ts">
import { Clock3, Sparkles } from 'lucide-vue-next'

defineProps<{
  topic: string
}>()
</script>

<template>
  <section class="loading-screen" aria-live="polite" aria-labelledby="loading-title">
    <div class="loading-visual" aria-hidden="true">
      <div class="loading-badge"><Sparkles :size="42" stroke-width="1.6" /></div>
    </div>

    <div class="loading-copy">
      <span class="eyebrow">The forge is warming up</span>
      <h1 id="loading-title">Building a quiz about <em>{{ topic }}.</em></h1>
      <p>
        The question set is taking shape now. We are giving each prompt a little room to become useful, not just correct.
      </p>
      <div class="loading-meta"><Clock3 :size="15" /> This may take a moment</div>
    </div>
  </section>
</template>