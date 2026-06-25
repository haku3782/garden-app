<template>
  <div class="modal-overlay" @click.self="$emit('cancel')">
    <div class="modal-box">
      <p class="modal-message">{{ message }}</p>
      <div class="modal-actions">
        <button type="button" class="modal-cancel" @click="$emit('cancel')">{{ cancelLabel }}</button>
        <button type="button" class="modal-confirm" :class="{ danger: variant === 'danger' }" @click="$emit('confirm')">{{ confirmLabel }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  message: { type: String, required: true },
  confirmLabel: { type: String, required: true },
  cancelLabel: { type: String, required: true },
  variant: { type: String, default: 'default' }
})
defineEmits(['confirm', 'cancel'])
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
  padding: 1rem;
}
.modal-box {
  background: #fff;
  border-radius: 10px;
  padding: 1.5rem;
  max-width: 320px;
  width: 100%;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}
.modal-message { margin: 0 0 1.25rem; color: #2d3436; font-size: 0.95rem; text-align: center; }
.modal-actions { display: flex; justify-content: center; gap: 0.5rem; }
.modal-actions button {
  padding: 0.45rem 1rem;
  font-size: 0.85rem;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.15s, transform 0.05s;
}
.modal-actions button:active { transform: scale(0.97); }
.modal-cancel {
  background: transparent;
  color: #767676;
  border: 1px solid #e3e3e3;
}
.modal-cancel:hover { background: #f7f8f7; color: #2d3436; }
.modal-confirm {
  background: #4a9d5f;
  color: #fff;
  border: none;
}
.modal-confirm:hover { background: #3d8350; }
.modal-confirm.danger { background: #e74c3c; }
.modal-confirm.danger:hover { background: #c0392b; }
</style>
