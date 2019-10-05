<script>

	import { onMount } from 'svelte';
	import { setupColors, setupScroll, search, firstLetterUpperCase } from 'utils';
	import documentation from '../../utils/docs.json';
	import Card from '../Card.svelte';

	export let docType;

	let searchValue = '';
	let loadedElements = 0;

	async function setupListeners() {
		document.getElementsByClassName('search-input')[0].addEventListener('keyup', async () => {
			search(searchValue);
		})
	}

	async function loaded() {
		loadedElements ++
		if (loadedElements === Object.keys(documentation[docType]).length) {
			setupScroll();
		}
	}

	onMount(async () => {
		setupColors();
		setupListeners();
	})

</script>

<div class="section">
	<h1 class="title"><strong>{firstLetterUpperCase(docType)}</strong></h1>
</div>

<div class="has-text-centered">
	<input class="input has-text-centered search-input" bind:value={searchValue} type="text" placeholder="Find any syntax">
</div>
	
<div class="columns">

	<div class="column">
			
		{#each Object.keys(documentation[docType]) as element}
			
			<div class="small-section" id="{documentation[docType][element].id}">

				<Card on:mount={loaded}>

					<h1 slot="title" class="subtitle">
						<strong>{documentation[docType][element].name}</strong>
						<a href="#{documentation[docType][element].id}">#</a>
					</h1>

					<div slot="icon">
						{#if documentation[docType][element].since}
							<span class="tag is-large" style="background-color: rgb(97, 237, 120)">{documentation[docType][element].since}</span>
						{/if}
					</div>

					{#if documentation[docType][element].description}
						<label class="label">Description</label>
						<div class="small-section">
							<p>{documentation[docType][element].description}</p>
						</div>
					{/if}

					{#if documentation[docType][element].patterns}
						<label class="label">Patterns</label>
						<div class="small-section skript-code">
							<pre>{documentation[docType][element].patterns.join('\n')}</pre>
						</div>
					{/if}

					{#if documentation[docType][element].examples}
						<label class="label">Example</label>
						<div class="small-section skript-code">
							<pre>{documentation[docType][element].examples.join('\n')}</pre>
						</div>
					{/if}

				</Card>

			</div>

		{/each}

	</div>

</div>