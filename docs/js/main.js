// ID Scroll
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


// Active Tab
const pageLink = window.location.toString().replaceAll(/(.*)\/(.+?).html(.*)/gi, '$2');
if (pageLink === "" || pageLink == window.location.toString()) // home page - when there is no `.+?.html` pageLink will = windown.location due to current regex
  document.querySelectorAll('#global-navigation a[href="index.html"]')[0].classList.add("active-tab");
else
  document.querySelectorAll(`#global-navigation a[href="${pageLink}.html"]`)[0].classList.add("active-tab");


// No Left Panel
const noLeftPanel = document.querySelectorAll('#content.no-left-panel')[0];
if (noLeftPanel != null)
  document.querySelectorAll('#side-nav')[0].classList.add('no-left-panel');
