<script>
	import Navbar from '../components/Navbar.svelte';
	import Footer from '../components/Footer.svelte';
</script>

<svelte:head>
	<title>Skript documentation</title>
</svelte:head>

<Navbar />

<section class="hero is-yellow is-fullheight">
	<div class="hero-body">
		<div class="container">
			<main>
				<slot></slot>
			</main>
		</div>
	</div>
</section>

<Footer />