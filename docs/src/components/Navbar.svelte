<script>
	import tutorials from '../utils/tutorials.json';
	let active = false;
</script>

<style>
	.is-yellow { background-color: #d6de79 }
</style>

<div class="navbar is-yellow is-fixed-top">
	<div class="navbar-brand">
		<a href="#!" on:click|preventDefault={() => active = !active} class="navbar-burger burger {active ? "is-active": ""}">
			<span></span>
			<span></span>
			<span></span>
		</a>
	</div>
	<div class="navbar-menu {active ? "is-active" : ""}">
		<a href="/" class="navbar-item">HOME</a>		
		<div class="navbar-item has-dropdown is-hoverable">
			<a href="/documentation" on:click|preventDefault class="navbar-link">DOCUMENTATION</a>
			<div class="navbar-dropdown">
				<a href="/documentation/events" class="navbar-item">EVENTS</a>
				<a href="/documentation/conditions" class="navbar-item">CONDITIONS</a>
				<a href="/documentation/effects" class="navbar-item">EFFECTS</a>
				<a href="/documentation/expressions" class="navbar-item">EXPRESSIONS</a>
				<a href="/documentation/types" class="navbar-item">TYPES</a>
				<a href="/documentation/functions" class="navbar-item">FUNCTIONS</a>
			</div>
		</div>
		<div class="navbar-item has-dropdown is-hoverable">
			<a href="/tutorials" class="navbar-link">TUTORIALS</a>
			<div class="navbar-dropdown">
				{#each Object.keys(tutorials) as tutorial}
					<a href="/tutorials/{tutorial.toLowerCase().replace(/\s/gmui, '_')}" class="navbar-item">{tutorial.toUpperCase()}</a>
				{/each}
			</div>
		</div>
	</div>
</div>