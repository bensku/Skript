<script>

	import { onMount } from 'svelte';

	import Card from '../components/Card.svelte';
	import developers from '../utils/developers.json';

	let devs;

	async function setupDevs() {
		let count = 0
		devs = '<div class="small-section"><div class="columns">'
		for (let dev of Object.keys(developers)) {
			dev = developers[dev];
			if (count === 4) {
				devs += '</div></div><div class="small-section"><div class="columns">'
				count = 0;
			}
			devs += `
				<div class="column is-3">
					<div class="card">
						<div class="card-image">
							<figure class="image">
								<img src="${dev.avatar}" alt="Dev's avatar">
							</figure>
						</div>
						<div class="card-content">
							<p><strong>${dev.roles.join('\n')}</strong></p>
						</div>
					</div>
				</div>
			`
			count ++;
		}
		devs += '</div></div>'
	}

	onMount(async () => {
		setupDevs();
	})

</script>

<style>
	.special-section { padding: 0rem 2rem 0rem 2rem; }
	.main-title { font-size: 40px; justify-content: left; margin-left: 20px; }
</style>

<div class="level" style="margin-top: 50px;">
	<img class="level-item" src="favicon.png" alt="Skript icon" width="60px" style="max-width: 150px;">
	<h1 class="main-title level-item">
		<strong>Documentation</strong>
	</h1>
</div>

<div class="is-white">
	<div class="columns">
		<div class="column is-7">
			
			<div class="special-section">

				<div class="section">
					<div class="small-section is-yellow has-text-centered">
						<div class="level">
							<p class="level-item"><strong>Download latest</strong></p>
							<a href="https://github.com/SkriptLang/Skript/releases/download/2.3.7/Skript.jar" class="button is-rounded level-item">2.3.7</a>
						</div>
					</div>
				</div>
				
				<div class="small-section">
					<p>
						Skript is (surprise, surprise) a scripting plugin for Bukkit platform. It
						is easy to use for simple tasks, but yet you can create really complex things
						with it. The syntax of Skript is close to English, but it is still not magic.
						While for simple tasks you might succeed with experimentation, for anything
						more complex you will need some guidance.
					</p>
				</div>

				<div class="small-section">
					<p>
						This is the Skript documentation. You will find all supported features of Skript
						plugin here. You might also find some useful examples, in case you need more
						than what was shipped with Skript distribution. We do not currently have
						tutorials here, but you can find good ones using whatever search engine you prefer.
					</p>
				</div>

			</div>

		</div>
		<div class="column">
			<div class="small-section">

				<Card class="has-yellow-border" withoutTitle={true}>
					<div class="content">
						<p>
							Found something incorrect in this documentation? Please report
							it to the <a href="https://github.com/bensku/Skript/issues">issue tracker</a>.
						</p>
					</div>
				</Card>

			</div>

			<div class="small-section">
				<Card class="has-yellow-border">
					<h1 slot="title" class="subtitle"><strong>Looking for docs author!</strong></h1>
					<div class="content">
						<p>
							Currently, the only documentation is generated automatically. It would be
							nice to have some hand-written content such as tutorials on the docs as well. For example, currently we don't have
							a tutorial on how to use loops here; This makes it harder for newcomers to learn.
							Check <a href="https://github.com/bensku/Skript/issues/611">this issue</a> for
							more details and if you're interested in helping out.
						</p>
					</div>
				</Card>
			</div>

			<div class="small-section">
				<Card class="has-yellow-border" withoutTitle={true}>
					<div class="content">
						<span>
							<i class="fab fa-github"></i>
							<strong>View source code on <a href="https://github.com/SkriptLang/Skript">GitHub</a></strong>
						</span>
					</div>
				</Card>
			</div>

		</div>

	</div>

	<div class="section">
		<div class="small-section">
			<h1 class="title">Relevant Resources</h1>
			<ul class="is-list">
				<li><a href="https://forums.skunity.com" target="_blank">SkUnity Forums</a></li>
				<li><a href="https://dev.bukkit.org/projects/skript" target="_blank">Original Skript at BukkitDev</a> (inactive)</li>
				<li><a href="https://forums.skunity.com/resources/categories/addons.1/" target="_blank">Addon Releases @SkUnity</a></li>
				<li><a href="https://discord.gg/V4qFVnh" target="_blank">Skript Chat Discord Invite</a></li>
				<li><a href="https://skripthub.net" target="_blank">SkriptHub</a></li>
			</ul>
		</div>
	</div>

	<div class="section">
		<div class="small-section">
			<h1 class="title">Developers</h1>
			{@html devs}
		</div>
	</div>

</div>