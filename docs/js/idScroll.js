const links = document.querySelectorAll("div.item-warpper");
const contents = document.querySelectorAll("#content")[0];

lastActive = null;

contents.addEventListener('scroll', (e) => {
  links.forEach((ha) => {
    const rect = ha.getBoundingClientRect();
    if (rect.top > 0 && rect.top < 150) {
      const location = window.location.toString().split("#")[0];
      history.replaceState(null, null, location + "#" + ha.id);

      if (lastActive != null) {
        lastActive.classList.remove("active-item");
      }

      lastActive = document.querySelectorAll(`#nav-contents a[href="#${ha.id}"]`)[0];
      if (lastActive != null) {
        lastActive.classList.add("active-item");
      }
    }
  });
});
