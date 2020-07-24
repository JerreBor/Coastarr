<template>
  <div id="page">
    <Navbar />
    <div id="content" class="mt-6 mx-12 lg:mt-0">
      <Nuxt />
    </div>
  </div>
</template>

<script>
export default {
  computed: {
    currentRouteName () {
      return this.$route.name
    }
  }
}
</script>

<style>
@import url("https://fonts.googleapis.com/css?family=Poppins");

* {
  padding: 0;
  margin: 0;
}

#content {
  font-family: Poppins, Georgia;
  color: white;
}

html {
  background-color: #262626
}
</style>
