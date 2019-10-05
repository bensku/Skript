<script>

	import Navbar from '../components/Navbar.svelte';
	import Footer from '../components/Footer.svelte';

	$: if (process.browser) {
		if (window.location.href.match(/#.+$/gmui)) {
			const element = document.getElementById( window.location.href.replace(/^#/gmui, '') );
			if (element) element.scrollIntoView();
		}
	}

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