<template>
  <div>
    <img :src="'images/coastarr-' + color + '-' + background + '.png'" :width="width" :height="height"></img>
  </div>
</template>

<script>
export default {
  props: {
    width: {
      type: Number,
      default: 100,
      required: false
    },
    height: {
      type: Number,
      default: 100,
      required: false
    },
    color: {
      type: String,
      default: 'light',
      required: false
    },
    background: {
      type: String,
      default: 'trans',
      required: false
    }
  }
}
</script>
