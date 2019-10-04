# Skript Documentation

Here is the source code of [Skript website](https://skriptlang.github.io/Skript/).

What the website uses:

 * Bulma
 * FontAwesome
 * Svelte
 * Sapper

## Export Documentation

To export the documentation, run these commands in your terminal:

**Using NPM:**

```
npm install
npm run export
```

**Using YARN (recommanded):**

```
yarn
yarn export
```

It will generate the website as static website at ``__sapper__/export``.

## Contribute

### Edit documentation

 1. Replace ``/src/utils/docs.json`` file with the new one
 2. Enjoy!

### Add a contributor

 1. Go in the ``/src/utils/developers.json`` file
 2. Like others, add yours
 3. Enjoy!

### Add a tutorial

 1. Go in the ``/src/utils/tutorials.json`` file
 2. Like others, add yours
 3. Go in the ``/src/routes/tutorials`` directory
 4. Like others, write your tutorial page
 5. Enjoy!