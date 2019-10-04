<script>
	import Card from '../../components/Card.svelte';
	import tutorials from '../../utils/tutorials.json';
</script>

<div class="is-white">
	<div class="section">
		<h1 class="title">Tutorials</h1>
		<div class="columns">

			{#each Object.keys(tutorials) as tutorial}
				<div class="column">
					<Card>
						<h1 slot="title" class="subtitle"><strong>{tutorial}</strong></h1>
						<p>{tutorials[tutorial].description}</p>
						<div slot="footer">
							<a href="/tutorials/{tutorial.toLowerCase().replace(/\s/gmui, '_')}" class="card-footer-item button is-success">Read</a>
						</div>
					</Card>
				</div>
			{/each}

		</div>
	</div>
</div>