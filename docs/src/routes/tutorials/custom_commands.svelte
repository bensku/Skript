<script>

	import { onMount } from 'svelte';
	import { setupColors, setupScroll } from 'utils';

	onMount(async () => {
		setupColors();
		setupScroll();
	})

</script>

<div class="is-white">
	<div class="section">

		<h1 class="title">Custom commands</h1>

		<div class="section">

			<h1 class="subtitle" id="creating_custom_commands">
				<strong>Creating custom commands</strong>
				<a href="#creating_custom_commands">#</a>
			</h1>
			
			<p>
				Custom commands can be some of the trickiest things to create. They can also be some of the 
				most powerful. In this tutorial I am going to show everything about commands, and how to use 
				them. Let's start by defining what we want the user to type when they execute this command:
			</p>

			<div class="small-section">
				<pre class="skript-code">command /test:</pre>
			</div>

			<p>
				We now have set up a command called /test. But what if we want the user to enter in more information? 
				Things like a player name or a number. That is where we use arguments. Arguments are a lot like 
				variables, but only have one value that changes each time a command is executed. They are completely 
				dependent on what the player types in. In order to add them to a command you enclose the type of 
				argument with &#60; and >
			</p>

			<div class="small-section">
				<pre class="skript-code">
command /test &#60;player>:
# And for multiple arguments
command /test &#60;player> &#60;number> &#60;text>:
				</pre>
			</div>

			<p>
				You may also want to make an argument optional. That way the user doesn't have to type 
				it in if they don't want or need that argument for the command. For example, if you made 
				a command that teleported you to spawn 1, spawn 2, or spawn 3, and the user just wanted 
				to type in /spawn to get them to spawn 1. Rather than making them type /spawn 1 you can 
				just make it optional, and default to 1. Here's an example of what I'm talking about
			</p>

			<div class="small-section">
				<pre class="skript-code">command /spawn [&#60;number=1>]:</pre>
			</div>

			<p>
				The [ and ] around the argument make it optional (the user doesn't have to type it in) 
				and then we also give it a default value so that we know what to do if the user doesn't 
				type it in. Also know that if you use an optional text argument, that the default value 
				should be in "
			</p>

			<div class="small-section">
				<pre class="skript-code">command /test [&#60;text="Example">]:</pre>
			</div>

			<p>
				This first line of defining a command ends with a : Because of that (and because it 
				acts as our event) all subsequent lines need to be indented. The next few lines are all 
				about the options that our command will have. Things like a description, permission, 
				and usage message.
			</p>

			<div class="small-section">
				<pre class="skript-code">
command /test:
	description: Description of what the command does
	usage: The message that comes up if the user types the command in wrong
	permission: The permission required to use this command
	permission message: The message that appears if the user doesn't have the correct permission
	executable by: Who can use this command? Players, console, or both?
	aliases: Other names or shortcuts to this command
	trigger:
		#Effects
				</pre>
			</div>

			<p>
				If you read the tutorial on indentation, then you will know that I mentioned that the only 
				exception to the indent after colon rule was commands. Well here it is. Each of these 
				options ends with a colon, but only the trigger gets an indent. The only one of these 
				options that is required is the trigger (it holds what the command actually does) The rest 
				aren't necessary, but can be useful in controlling aspects of the command. Now let's go one 
				by one, and get into more detail on each of these options
			</p>

			<ul class="is-list">
				<li>
					<strong>Description:</strong>
					What does this command do? What is it's purpose? It is mostly for in code 
					documentation, but it's good to add anyway
				</li>
				<li>
					<strong>Usage:</strong> 
					How should the command look? This message will come up when someone 
					types a command in wrong. For example, if the command wanted a number, but the user typed in 
					"Rabbit" this message would come up. It defaults to the arguments that you defined on the 
					first line. It is used to make the command a little bit more readable. Instead of 
					"/pay &#60;player> &#60;number>" you could put "/pay &#60;player> &#60;amount>"
				</li>
				<li>
					<strong>Permission:</strong> 
					This allows you to define a permission for the command. By default there is no permission for 
					these commands, so anyone can use them.
				</li>
				<li>
					<strong>Permission message:</strong> 
					Here you define what the message should be if the user doesn't have the permission you set 
					earlier. There is a default message that will show if you don't include this option
				</li>
				<li>
					<strong>Executable by:</strong> 
					Who can use this command? The options are "players" "console" "console and players" Defaults to 
					console and players
				</li>
				<li>
					<strong>Aliases:</strong> 
					Make some shortcuts to your commands! You can add multiple aliases by separating them with commas 
					"/h, /help, /helpmeout"
				</li>
				<li>
					<strong>Trigger:</strong> 
					The most important part of any command, and the only option here that is required. Nothing else goes on this line, 
					but all the next lines need to be indented again. This block of code will define what the command should do when 
					executed. Here is a completed example:
					<div class="small-section">
						<pre class="skript-code">
command /help [&#60;number=1>]:
	description: Shows the help menus
	usage: /help [page]
	permission: help.permission
	permission message: &cYou don't have the help permission!
	executable by: players and console
	aliases: /h, /helpmerightnow
	trigger:
		message "This is a help command!"
						</pre>
					</div>
				</li>
			</ul>

		</div>

		<div class="section">
		
			<h1 class="subtitle" id="arguments">
				<strong>Arguments</strong>
				<a href="#arguments">#</a>
			</h1>

			<p>
				Now we know how to set up a command, and where to add the effects, but how do we use the arguments 
				(the data that the player entered in) in the trigger? If there is only one type of an argument 
				you can reference it by using "arg &#60;type>"

				arg player
				arg number
				arg text

				Alternatively you can use the number of the argument. This is determined by what order the 
				arguments come in the command.
			</p>

			<div class="small-section">
				<pre class="skript-code">
command /test &#60;number> &#60;player> &#60;text>:
	trigger:
       message "%arg 1%"
       message "%arg 2%"
       message "%arg 3%"
				</pre>
			</div>

			<p>
				This command will display the arguments in order 1, 2, and then 3. The number, the player, 
				and then the text. Here is a list of potential type that you can use: <a href="/documentation/types">here</a>
			</p>

			<p>
				+ means that it is a valid type, but I have not tested to make sure that it works as a command 
				argument. If anyone has tested some of these you can pm me and I'll update the list

				Post in the help forum with any questions, and on the tutorials forum if you have any improvements 
				or suggestions for this tutorial
			</p>

		</div>

	</div>
</div>