(() => {
    const element = document.getElementById("auto-advance");
    if (!element) return;

    const controls = document.querySelectorAll("[data-auto-advance-control]");
    const statuses = document.querySelectorAll("[data-auto-advance-status]");
    let timer;
    let paused = false;

    function schedule() {
        timer = window.setTimeout(() => {
            window.location.assign(element.dataset.nextUrl);
        }, 2500);
    }

    function setPaused(value) {
        paused = value;
        window.clearTimeout(timer);
        controls.forEach(control => {
            control.setAttribute("aria-pressed", String(paused));
            control.textContent = paused ? "Resume auto-advance" : "Pause auto-advance";
        });
        statuses.forEach(status => {
            status.textContent = paused ? "Paused for reading" : "Continuing shortly";
        });
        if (!paused) schedule();
    }

    controls.forEach(control => {
        control.hidden = false;
        control.addEventListener("click", () => setPaused(!paused));
    });
    document.querySelectorAll("[data-event-link]").forEach(link => {
        // Keyboard focus and middle-click should be as safe as a normal click.
        ["focus", "click", "auxclick"].forEach(event => {
            link.addEventListener(event, () => setPaused(true));
        });
    });
    schedule();
})();
